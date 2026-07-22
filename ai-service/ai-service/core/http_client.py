"""HTTP 客户端 —— 调用 Java 微服务网关（高并发改造版）。

对等 Java 后端：Druid 连接池（连接复用）+ Sentinel 熔断降级 + RabbitMQ 重试。

改造要点：
1. **连接池复用**：模块级共享 ``httpx.Client`` / ``httpx.AsyncClient``，带 ``limits``
   连接池与超时（近似 Druid max-active / max-wait），不再每次请求新建并销毁连接，
   消除高并发下的连接抖动与文件描述符耗尽。
2. **异步优先**：新增 ``AsyncGatewayClient`` / ``call_gateway_async``，在 async 路由中
   ``await`` 调用，不阻塞事件循环（原 ``GatewayClient`` 同步调用会阻塞事件循环）。
3. **重试**：同步/异步请求均带指数退避重试（默认 3 次），对齐 RabbitMQ retry。
4. **熔断**：异步路径接入 ``CircuitBreaker``，下游持续失败时快速失败，保护 Java 后端。

``GatewayClient``（同步）保留供 LangChain Tool 使用（Tool 在 LangChain 线程池内执行，
不会阻塞事件循环），但其内部已复用共享连接池。
"""

from __future__ import annotations

import logging
import time
from typing import Any, Callable

import httpx

from config import (
    GATEWAY_URL,
    HTTP_POOL_MAX_CONNECTIONS,
    HTTP_POOL_MAX_KEEPALIVE,
    HTTP_POOL_TIMEOUT,
    HTTP_RETRY_BACKOFF,
    HTTP_RETRY_MAX,
    HTTP_TIMEOUT,
)
from core.circuit_breaker import CircuitBreaker, async_retry
from core.exceptions import GatewayError, GatewayRetryableError

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# 共享连接池（模块级单例）
# ---------------------------------------------------------------------------

def _build_limits() -> httpx.Limits:
    return httpx.Limits(
        max_connections=HTTP_POOL_MAX_CONNECTIONS,
        max_keepalive_connections=HTTP_POOL_MAX_KEEPALIVE,
        keepalive_expiry=30.0,
    )


def _build_timeout() -> httpx.Timeout:
    return httpx.Timeout(HTTP_TIMEOUT, pool=HTTP_POOL_TIMEOUT)


_sync_client: httpx.Client | None = None
_async_client: httpx.AsyncClient | None = None

# 网关调用熔断器（全局共享）
_gateway_breaker = CircuitBreaker(
    name="gateway",
    failure_threshold=5,
    open_seconds=30,
    half_open_tests=1,
)


def get_sync_client() -> httpx.Client:
    """返回（懒创建）共享同步客户端，带连接池与超时。"""
    global _sync_client
    if _sync_client is None:
        _sync_client = httpx.Client(
            base_url=GATEWAY_URL,
            timeout=_build_timeout(),
            limits=_build_limits(),
        )
    return _sync_client


def get_async_client() -> httpx.AsyncClient:
    """返回（懒创建）共享异步客户端，带连接池与超时。"""
    global _async_client
    if _async_client is None:
        _async_client = httpx.AsyncClient(
            base_url=GATEWAY_URL,
            timeout=_build_timeout(),
            limits=_build_limits(),
        )
    return _async_client


async def close_clients() -> None:
    """优雅停机时关闭共享客户端。"""
    global _sync_client, _async_client
    if _sync_client is not None:
        _sync_client.close()
        _sync_client = None
    if _async_client is not None:
        await _async_client.aclose()
        _async_client = None


# ---------------------------------------------------------------------------
# 响应处理
# ---------------------------------------------------------------------------

def _build_headers(token: str) -> dict[str, str]:
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return headers


def _check_status(resp: httpx.Response) -> None:
    if resp.is_success:
        return
    if resp.status_code >= 500:
        raise GatewayRetryableError(f"网关返回 {resp.status_code}: {resp.text[:200]}")
    raise GatewayError(f"网关返回 {resp.status_code}: {resp.text[:200]}")


# ---------------------------------------------------------------------------
# 同步客户端（供 LangChain Tool 使用）
# ---------------------------------------------------------------------------

class GatewayClient:
    """通过 gateway-service 调用 Java 后端 API（同步版本）。

    复用模块级共享连接池；``close()`` 为安全空操作（不关闭共享客户端）。
    """

    def __init__(self, *, timeout: float = HTTP_TIMEOUT) -> None:
        # 超时由共享客户端统一控制，此处保留签名兼容
        self._client = get_sync_client()

    def get(self, path: str, *, token: str, params: dict | None = None) -> dict[str, Any]:
        return self._request("GET", path, token=token, params=params)

    def post(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        return self._request("POST", path, token=token, json_body=json)

    def put(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        return self._request("PUT", path, token=token, json_body=json)

    def _request(
        self,
        method: str,
        path: str,
        *,
        token: str,
        params: dict | None = None,
        json_body: dict | None = None,
    ) -> dict[str, Any]:
        client = get_sync_client()
        last_exc: Exception | None = None
        for attempt in range(1, HTTP_RETRY_MAX + 1):
            try:
                resp = client.request(
                    method, path, params=params, json=json_body,
                    headers=_build_headers(token),
                )
                _check_status(resp)
                return resp.json()
            except (httpx.TransportError, httpx.TimeoutException, GatewayRetryableError) as exc:
                last_exc = exc
                if attempt >= HTTP_RETRY_MAX:
                    break
                time.sleep(HTTP_RETRY_BACKOFF * (2 ** (attempt - 1)))
            except GatewayError:
                raise
        assert last_exc is not None
        if isinstance(last_exc, GatewayRetryableError):
            raise
        raise GatewayError(f"调用网关失败: {last_exc}")

    def close(self) -> None:
        """空操作：共享客户端由 close_clients() 统一关闭，禁止重复关闭。"""
        return None


def call_gateway_json(path: str, *, token: str, params: dict | None = None) -> str:
    """通用工具函数：调用网关并将 data 字段序列化为 JSON 字符串。

    供 LangChain Tool 使用（同步调用，运行在线程池内）。复用共享连接池 + 重试。
    """
    import json

    if not token:
        return json.dumps({"error": "未登录，无法查询"}, ensure_ascii=False)
    try:
        client = GatewayClient()
        result = client.get(path, token=token, params=params)
        data = result.get("data", {})
        records = data.get("records", data) if isinstance(data, dict) else data
        return json.dumps(records, ensure_ascii=False)
    except Exception as e:
        logger.error("调用网关失败 %s: %s", path, e)
        return json.dumps({"error": f"查询失败: {e}"}, ensure_ascii=False)


# ---------------------------------------------------------------------------
# 异步客户端（供 FastAPI async 路由使用）
# ---------------------------------------------------------------------------

class AsyncGatewayClient:
    """异步版网关客户端，复用共享连接池，自带重试 + 熔断。"""

    def __init__(self, *, timeout: float = HTTP_TIMEOUT) -> None:
        self._client = get_async_client()

    async def get(self, path: str, *, token: str, params: dict | None = None) -> dict[str, Any]:
        return await self._request("GET", path, token=token, params=params)

    async def post(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        return await self._request("POST", path, token=token, json_body=json)

    async def put(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        return await self._request("PUT", path, token=token, json_body=json)

    async def _request(
        self,
        method: str,
        path: str,
        *,
        token: str,
        params: dict | None = None,
        json_body: dict | None = None,
    ) -> dict[str, Any]:
        client = get_async_client()

        async def _do() -> dict[str, Any]:
            resp = await client.request(
                method, path, params=params, json=json_body,
                headers=_build_headers(token),
            )
            _check_status(resp)
            return resp.json()

        return await _gateway_breaker.call(
            lambda: async_retry(
                _do,
                max_attempts=HTTP_RETRY_MAX,
                backoff=HTTP_RETRY_BACKOFF,
                exceptions=(httpx.TransportError, httpx.TimeoutException, GatewayRetryableError),
                label=f"gateway:{method}:{path}",
                logger_=logger,
            )
        )


async def call_gateway_async(
    path: str,
    *,
    token: str,
    method: str = "GET",
    params: dict | None = None,
    json: dict | None = None,
) -> dict[str, Any]:
    """异步网关调用助手（带连接池 + 重试 + 熔断）。供 async 路由直接 await 使用。"""
    client = get_async_client()

    async def _do() -> dict[str, Any]:
        resp = await client.request(
            method, path, params=params, json=json,
            headers=_build_headers(token),
        )
        _check_status(resp)
        return resp.json()

    return await _gateway_breaker.call(
        lambda: async_retry(
            _do,
            max_attempts=HTTP_RETRY_MAX,
            backoff=HTTP_RETRY_BACKOFF,
            exceptions=(httpx.TransportError, httpx.TimeoutException, GatewayRetryableError),
            label=f"gateway:{method}:{path}",
            logger_=logger,
        )
    )
