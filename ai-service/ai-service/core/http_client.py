"""HTTP 客户端 —— 调用 Java 微服务网关。"""

from __future__ import annotations

import json
import logging
from typing import Any

import httpx

from config import GATEWAY_URL

logger = logging.getLogger(__name__)


class GatewayClient:
    """通过 gateway-service 调用 Java 后端 API（同步版本，供 LangChain Tool 使用）。"""

    def __init__(self, *, timeout: float = 30.0) -> None:
        self._client = httpx.Client(
            base_url=GATEWAY_URL,
            timeout=httpx.Timeout(timeout),
        )

    def get(self, path: str, *, token: str, params: dict | None = None) -> dict[str, Any]:
        resp = self._client.get(
            path,
            params=params,
            headers=_build_headers(token),
        )
        return _handle_response(resp)

    def post(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        resp = self._client.post(
            path,
            json=json,
            headers=_build_headers(token),
        )
        return _handle_response(resp)

    def put(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        resp = self._client.put(
            path,
            json=json,
            headers=_build_headers(token),
        )
        return _handle_response(resp)

    def close(self) -> None:
        self._client.close()


class AsyncGatewayClient:
    """异步版网关客户端，供 FastAPI async handler 使用。"""

    def __init__(self, *, timeout: float = 30.0) -> None:
        self._client = httpx.AsyncClient(
            base_url=GATEWAY_URL,
            timeout=httpx.Timeout(timeout),
        )

    async def get(self, path: str, *, token: str, params: dict | None = None) -> dict[str, Any]:
        resp = await self._client.get(
            path,
            params=params,
            headers=_build_headers(token),
        )
        return _handle_response(resp)

    async def post(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        resp = await self._client.post(
            path,
            json=json,
            headers=_build_headers(token),
        )
        return _handle_response(resp)

    async def put(self, path: str, *, token: str, json: dict | None = None) -> dict[str, Any]:
        resp = await self._client.put(
            path,
            json=json,
            headers=_build_headers(token),
        )
        return _handle_response(resp)

    async def close(self) -> None:
        await self._client.aclose()


def call_gateway_json(path: str, *, token: str, params: dict | None = None) -> str:
    """通用工具函数：调用网关并将 data 字段序列化为 JSON 字符串。
    供 LangChain Tool 使用（同步调用）。
    """
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


def _build_headers(token: str) -> dict[str, str]:
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return headers


def _handle_response(resp: httpx.Response) -> dict[str, Any]:
    """统一处理网关响应，非 2xx 抛 GatewayError。"""
    from core.exceptions import GatewayError

    if resp.is_success:
        return resp.json()
    raise GatewayError(f"网关返回 {resp.status_code}: {resp.text[:200]}")
