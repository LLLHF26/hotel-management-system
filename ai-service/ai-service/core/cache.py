"""缓存层 —— 对等 Java 后端 RedisTemplate / @Cacheable。

设计原则：
- 优先使用 Redis（配置 ``REDIS_URL`` 时），实现跨多 worker / 多实例共享的热点缓存。
- 未配置 Redis 时，自动降级为**进程内 TTL 缓存**（单 worker 内有效），保证功能不依赖外部组件。
- 读取/写入 Redis 失败时同样降级到内存缓存，避免缓存组件故障拖垮主流程。

主要用于：RAG 语义检索结果缓存、热点问答/查询结果缓存，显著降低对 LLM 与向量库的重复压力。
"""

from __future__ import annotations

import json
import logging
import time
from typing import Any, Callable, Optional

from config import CACHE_DEFAULT_TTL, REDIS_URL

logger = logging.getLogger(__name__)


class _MemoryCache:
    """极简进程内 TTL 缓存（asyncio 单线程安全，无需锁）。"""

    __slots__ = ("_store", "_exp")

    def __init__(self) -> None:
        self._store: dict[str, str] = {}
        self._exp: dict[str, float] = {}

    def get(self, key: str) -> Optional[str]:
        now = time.monotonic()
        exp = self._exp.get(key)
        if exp is None:
            return None
        if exp > now:
            return self._store.get(key)
        self._store.pop(key, None)
        self._exp.pop(key, None)
        return None

    def set(self, key: str, value: str, ttl: int) -> None:
        self._store[key] = value
        self._exp[key] = time.monotonic() + ttl

    def delete(self, key: str) -> None:
        self._store.pop(key, None)
        self._exp.pop(key, None)

    def clear(self) -> None:
        self._store.clear()
        self._exp.clear()


_memory = _MemoryCache()
_use_redis = bool(REDIS_URL)
_redis = None


async def _get_redis():
    global _redis
    if _redis is None:
        import redis.asyncio as aioredis

        _redis = aioredis.from_url(REDIS_URL, decode_responses=True, socket_timeout=2.0)
    return _redis


def cache_key(prefix: str, *parts: Any) -> str:
    """拼装缓存键，例如 cache_key('rag', 'k', 'abc') -> 'rag:k:abc'。"""
    return ":".join([prefix, *(str(p) for p in parts)])


async def cache_get(key: str) -> Optional[str]:
    if _use_redis:
        try:
            return await (await _get_redis()).get(key)
        except Exception:
            logger.warning("Redis 读取失败，降级内存缓存: %s", key)
    return _memory.get(key)


async def cache_set(key: str, value: str, ttl: int = CACHE_DEFAULT_TTL) -> None:
    if _use_redis:
        try:
            await (await _get_redis()).set(key, value, ex=ttl)
            return
        except Exception:
            logger.warning("Redis 写入失败，降级内存缓存: %s", key)
    _memory.set(key, value, ttl)


async def cache_delete(key: str) -> None:
    if _use_redis:
        try:
            await (await _get_redis()).delete(key)
        except Exception:
            logger.warning("Redis 删除失败，忽略: %s", key)
    _memory.delete(key)


async def cached_call(key: str, ttl: int, producer: Callable[[], Any]) -> Any:
    """先查缓存，命中直接返回；未命中则执行 ``producer()``（可返回任意 JSON 可序列化对象）并写回。"""
    cached = await cache_get(key)
    if cached is not None:
        try:
            return json.loads(cached)
        except json.JSONDecodeError:
            pass
    value = await producer() if _is_coroutine(producer) else producer()
    try:
        await cache_set(key, json.dumps(value, ensure_ascii=False), ttl)
    except (TypeError, ValueError):
        # 不可序列化则跳过缓存
        pass
    return value


def _is_coroutine(fn: Callable) -> bool:
    import asyncio

    return asyncio.iscoroutinefunction(fn)


async def close_cache() -> None:
    """关闭 Redis 连接（优雅停机时调用）。"""
    global _redis
    if _redis is not None:
        try:
            await _redis.aclose()
        except Exception:
            pass
        _redis = None
