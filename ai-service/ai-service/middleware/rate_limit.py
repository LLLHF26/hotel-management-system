"""限流中间件 —— 对等 Java 网关 Sentinel 限流。

背景：Java 端 SentinelGatewayConfig 仅对 order/user/room/finance 设了限流规则，
**未覆盖 ai-service**，因此 ai-service 在网关之后仍需一层自身限流作为兜底防御，
防止突发流量（如大量并发对话请求）压垮 LLM / 向量库。

实现：基于令牌桶算法
- 全局令牌桶（控制整服务 QPS，默认 100）
- 单 IP 令牌桶（防止单用户刷接口，默认 20 QPS）
- asyncio 单线程模型下无需加锁（令牌计算在事件循环内原子完成）
命中限流返回 ``429`` 统一 JSON，与 Java 端 Sentinel 行为一致：
``{"code": 429, "msg": "请求过于频繁，请稍后重试", "data": null}``
"""

from __future__ import annotations

import time
from typing import Callable

from fastapi import Request
from fastapi.responses import JSONResponse

from config import (
    RATE_LIMIT_BURST,
    RATE_LIMIT_ENABLED,
    RATE_LIMIT_GLOBAL_QPS,
    RATE_LIMIT_PER_IP_QPS,
)

# 白名单：健康检查、文档、OPTIONS 预检不计入限流
_WHITELIST = {
    "/api/ai/health",
    "/api/ai/info",
    "/docs",
    "/redoc",
    "/openapi.json",
}


class _TokenBucket:
    """单线程安全的令牌桶。"""

    __slots__ = ("rate", "burst", "tokens", "last")

    def __init__(self, rate: float, burst: int) -> None:
        self.rate = rate
        self.burst = burst
        self.tokens = float(burst)
        self.last = time.monotonic()

    def consume(self, n: int = 1) -> bool:
        now = time.monotonic()
        self.tokens = min(self.burst, self.tokens + (now - self.last) * self.rate)
        self.last = now
        if self.tokens >= n:
            self.tokens -= n
            return True
        return False


_global_bucket = _TokenBucket(RATE_LIMIT_GLOBAL_QPS, RATE_LIMIT_BURST)
_per_ip: dict[str, _TokenBucket] = None  # 延迟初始化，避免导入期副作用


def _ensure_buckets() -> None:
    global _per_ip
    if _per_ip is None:
        _per_ip = {}


def _client_ip(request: Request) -> str:
    xff = request.headers.get("X-Forwarded-For")
    if xff:
        return xff.split(",")[0].strip()
    return request.client.host if request.client else "unknown"


def _is_whitelisted(path: str) -> bool:
    if path in _WHITELIST:
        return True
    if path.startswith("/docs") or path.startswith("/redoc"):
        return True
    return False


async def rate_limit_middleware(request: Request, call_next: Callable):
    if not RATE_LIMIT_ENABLED:
        return await call_next(request)
    if request.method == "OPTIONS" or _is_whitelisted(request.url.path):
        return await call_next(request)

    _ensure_buckets()
    ip = _client_ip(request)
    ip_bucket = _per_ip.get(ip)
    if ip_bucket is None:
        ip_bucket = _TokenBucket(
            RATE_LIMIT_PER_IP_QPS, max(RATE_LIMIT_PER_IP_QPS, RATE_LIMIT_BURST)
        )
        _per_ip[ip] = ip_bucket

    if not _global_bucket.consume(1) or not ip_bucket.consume(1):
        return JSONResponse(
            status_code=429,
            content={"code": 429, "msg": "请求过于频繁，请稍后重试", "data": None},
        )
    return await call_next(request)
