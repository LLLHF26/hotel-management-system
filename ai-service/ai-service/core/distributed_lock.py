"""分布式定时任务锁 —— 对等 Java 后端 SchedulerLock。

用途：ai-service 以多 worker / 多实例部署时，后台告警检查循环只允许一个实例执行，
避免重复扫描与重复告警。基于 Redis ``SET key value NX EX`` 实现（带 owner + TTL + 安全释放）。

- 配置了 ``REDIS_URL``：使用 Redis 分布式锁，跨进程/跨主机生效。
- 未配置 Redis：降级为**进程内锁**（单进程内防重入，多进程场景不保证唯一，仅兜底）。

提供异步上下文管理器 ``distributed_lock(key, ttl)``，用法：

    async with distributed_lock(ALERT_LOCK_KEY, ALERT_LOCK_TTL) as acquired:
        if not acquired:
            return  # 其他实例持有锁
        ... do work ...
"""

from __future__ import annotations

import logging
import time
import uuid
from typing import Optional

from config import ALERT_LOCK_KEY, ALERT_LOCK_TTL, REDIS_URL

logger = logging.getLogger(__name__)

_use_redis = bool(REDIS_URL)
_redis = None

# 进程内兜底锁状态（asyncio 单线程安全）
_local_owner: Optional[str] = None
_local_exp: float = 0.0


async def _get_redis():
    global _redis
    if _redis is None:
        import redis.asyncio as aioredis

        _redis = aioredis.from_url(REDIS_URL, decode_responses=True, socket_timeout=2.0)
    return _redis


class _DistributedLock:
    """异步上下文管理器：进入时尝试获取锁，退出时释放。"""

    def __init__(self, key: str, ttl: int) -> None:
        self.key = key
        self.ttl = ttl
        self.owner = uuid.uuid4().hex
        self.acquired = False

    async def _acquire(self) -> bool:
        if _use_redis:
            try:
                r = await _get_redis()
                ok = await r.set(self.key, self.owner, ex=self.ttl, nx=True)
                return ok is True
            except Exception:
                logger.warning("Redis 锁获取失败，降级本地锁: %s", self.key)
                return self._acquire_local()
        return self._acquire_local()

    def _acquire_local(self) -> bool:
        global _local_owner, _local_exp
        now = time.monotonic()
        if _local_owner is None or now >= _local_exp:
            _local_owner = self.owner
            _local_exp = now + self.ttl
            return True
        return False

    async def _release(self) -> None:
        if _use_redis and _redis is not None:
            try:
                # 仅当持有者为自己时才删除，避免误删他人锁
                r = await _get_redis()
                script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
                await r.eval(script, 1, self.key, self.owner)
                return
            except Exception:
                logger.warning("Redis 锁释放失败: %s", self.key)
                return
        self._release_local()

    def _release_local(self) -> None:
        global _local_owner
        if _local_owner == self.owner:
            _local_owner = None

    async def __aenter__(self) -> bool:
        self.acquired = await self._acquire()
        return self.acquired

    async def __aexit__(self, exc_type, exc, tb) -> None:
        if self.acquired:
            await self._release()


def distributed_lock(key: str = ALERT_LOCK_KEY, ttl: int = ALERT_LOCK_TTL) -> _DistributedLock:
    """获取一个分布式锁上下文管理器。进入返回 ``True`` 表示抢到锁。"""
    return _DistributedLock(key, ttl)


async def close_lock() -> None:
    """关闭 Redis 连接（优雅停机时调用）。"""
    global _redis
    if _redis is not None:
        try:
            await _redis.aclose()
        except Exception:
            pass
        _redis = None
