"""熔断与重试 —— 对等 Java 后端 Sentinel 熔断降级 + RabbitMQ 重试。

- ``CircuitBreaker``：纯标准库实现的异步熔断器（CLOSED / OPEN / HALF_OPEN 三态）。
  连续失败达到阈值后"熔断"一段时间，期间直接拒绝请求（快速失败、保护下游），
  到点后进入半开状态试探，成功则恢复、失败则重新熔断。
- ``async_retry``：指数退避重试，默认重试 3 次（max_attempts=3，backoff=0.5 → 0.5/1/2s），
  对齐 Java 端 RabbitMQ listener retry（initial-interval=1000ms, max-attempts=3, multiplier=2.0）。

所有组件均为单线程事件循环安全（asyncio 内无需额外加锁计算）。
"""

from __future__ import annotations

import asyncio
import logging
import time
from typing import Awaitable, Callable

logger = logging.getLogger(__name__)


class CircuitBreakerOpen(Exception):
    """熔断器处于 OPEN 状态，请求被拒绝。"""


class CircuitBreaker:
    """异步熔断器（CLOSED / OPEN / HALF_OPEN）。"""

    CLOSED = "closed"
    OPEN = "open"
    HALF_OPEN = "half_open"

    def __init__(
        self,
        name: str = "default",
        *,
        failure_threshold: int = 5,
        open_seconds: int = 30,
        half_open_tests: int = 1,
    ) -> None:
        self.name = name
        self.failure_threshold = failure_threshold
        self.open_seconds = open_seconds
        self.half_open_tests = half_open_tests

        self._state = self.CLOSED
        self._failures = 0
        self._opened_at = 0.0
        self._half_open_inflight = 0
        self._lock = asyncio.Lock()

    # ---- 内部状态机 ----
    def _now(self) -> float:
        return time.monotonic()

    async def _allow(self) -> bool:
        async with self._lock:
            now = self._now()
            if self._state == self.OPEN:
                if now - self._opened_at >= self.open_seconds:
                    self._state = self.HALF_OPEN
                    self._half_open_inflight = 0
                else:
                    return False
            if self._state == self.HALF_OPEN:
                if self._half_open_inflight >= self.half_open_tests:
                    return False
                self._half_open_inflight += 1
                return True
            return True

    async def _on_result(self, success: bool) -> None:
        async with self._lock:
            if self._state == self.HALF_OPEN:
                if success:
                    self._state = self.CLOSED
                    self._failures = 0
                else:
                    self._state = self.OPEN
                    self._opened_at = self._now()
                self._half_open_inflight -= 1
                return
            # CLOSED
            if success:
                self._failures = 0
            else:
                self._failures += 1
                if self._failures >= self.failure_threshold:
                    self._state = self.OPEN
                    self._opened_at = self._now()
                    logger.warning(
                        "熔断器开启 [%s]，%ds 后进入半开试探", self.name, self.open_seconds
                    )

    @property
    def state(self) -> str:
        return self._state

    async def call(self, coro_fn: Callable[..., Awaitable], *args, **kwargs):
        """在熔断保护下执行协程函数。被拒绝时抛出 ``CircuitBreakerOpen``。"""
        allowed = await self._allow()
        if not allowed:
            raise CircuitBreakerOpen(f"熔断器开启，拒绝请求: {self.name}")
        try:
            result = await coro_fn(*args, **kwargs)
        except Exception:
            await self._on_result(False)
            raise
        await self._on_result(True)
        return result

    def reset(self) -> None:
        """手动复位（运维/测试用）。"""
        self._state = self.CLOSED
        self._failures = 0
        self._opened_at = 0.0
        self._half_open_inflight = 0


async def async_retry(
    coro_fn: Callable[..., Awaitable],
    *,
    max_attempts: int = 3,
    backoff: float = 0.5,
    exceptions: tuple[type[BaseException], ...] = (Exception,),
    label: str = "operation",
    logger_: logging.Logger | None = None,
    **kwargs,
):
    """指数退避重试（对齐 RabbitMQ retry：3 次，退避 0.5/1/2s）。

    ``coro_fn`` 为返回协程的工厂（每次重试都重新构造协程）；``**kwargs`` 透传给它。
    """
    log = logger_ or logger
    last_exc: BaseException | None = None
    for attempt in range(1, max_attempts + 1):
        try:
            return await coro_fn(**kwargs)
        except exceptions as exc:  # type: ignore[misc]
            last_exc = exc
            if attempt >= max_attempts:
                break
            wait = backoff * (2 ** (attempt - 1))
            if log:
                log.warning(
                    "%s 第 %d 次失败，%0.2fs 后重试: %s",
                    label, attempt, wait, exc,
                )
            await asyncio.sleep(wait)
    assert last_exc is not None
    raise last_exc
