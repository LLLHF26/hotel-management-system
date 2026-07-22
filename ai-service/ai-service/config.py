"""全局配置 —— 读取 .env 并暴露为模块级常量。"""

from __future__ import annotations

import os
from pathlib import Path
from functools import lru_cache

from dotenv import load_dotenv

_BASE_DIR = Path(__file__).resolve().parent
load_dotenv(_BASE_DIR.parent / ".env")


class _Missing(str):  # marker for required env vars
    def __bool__(self) -> bool:
        return False

    def __repr__(self) -> str:
        return "<REQUIRED>"


def _env(key: str, default: str = _Missing()) -> str:
    val = os.getenv(key, default)
    if isinstance(val, _Missing):
        raise RuntimeError(f"缺少必需的环境变量: {key}")
    return val


def _env_bool(key: str, default: bool = False) -> bool:
    return os.getenv(key, str(default)).lower() in ("1", "true", "yes")


def _env_int(key: str, default: int) -> int:
    return int(os.getenv(key, str(default)))


# ---- 大模型 ----
LLM_PROVIDER = _env("LLM_PROVIDER", "qwen-plus")
LLM_API_KEY = _env("LLM_API_KEY")
LLM_BASE_URL = _env("LLM_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1")
LLM_MODEL = _env("LLM_MODEL", "qwen-plus")  # 勿用 qwen3.5-plus（思考模型，content 为空）
LLM_TEMPERATURE = float(os.getenv("LLM_TEMPERATURE", "0.7"))
LLM_MAX_TOKENS = _env_int("LLM_MAX_TOKENS", 2048)

# Embedding（独立配置，可用不同于 LLM 的服务商）
EMBEDDING_MODEL = _env("EMBEDDING_MODEL", "text-embedding-v3")
EMBEDDING_DIMENSION = _env_int("EMBEDDING_DIMENSION", 1024)
EMBEDDING_API_KEY = _env("EMBEDDING_API_KEY", LLM_API_KEY)
EMBEDDING_BASE_URL = _env("EMBEDDING_BASE_URL", LLM_BASE_URL)

# ---- 服务 ----
SERVICE_HOST = _env("SERVICE_HOST", "0.0.0.0")
SERVICE_PORT = _env_int("SERVICE_PORT", 8000)
DEBUG = _env_bool("DEBUG", False)

# ---- 数据库 ----
SQLITE_PATH = _env("SQLITE_PATH", str(_BASE_DIR / "data" / "ai_hotel.db"))

# ---- 向量数据库（Chroma 本地持久化，无需启动外部服务，替代 Milvus）----
CHROMA_PERSIST_PATH = _env("CHROMA_PERSIST_PATH", str(_BASE_DIR / "data" / "chroma"))  # 本地落盘目录
CHROMA_COLLECTION = _env("CHROMA_COLLECTION", "ai_knowledge")  # 集合名

# ---- Java 网关 ----
GATEWAY_URL = _env("GATEWAY_URL", "http://localhost:8080").rstrip("/")

# ---- JWT ----
JWT_SECRET = _env("JWT_SECRET", "dev-secret-change-in-production")
JWT_ALGORITHM = _env("JWT_ALGORITHM", "HS256")

# ---- 日志 ----
LOG_LEVEL = _env("LOG_LEVEL", "INFO")
LOG_FILE = _env("LOG_FILE", str(_BASE_DIR / "logs" / "ai-service.log"))

# ---- Kafka 日志 ----
KAFKA_BOOTSTRAP_SERVERS = _env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9091,localhost:9092,localhost:9093")
KAFKA_LOG_TOPIC = _env("KAFKA_LOG_TOPIC", "logs-topic")

# =============================================================================
# 高并发场景配置（对等 Java 后端：Druid 连接池 / Redis / Sentinel / SchedulerLock）
# =============================================================================

# ---- HTTP 客户端连接池（对等 Druid 连接池 + httpx limits）----
HTTP_POOL_MAX_CONNECTIONS = _env_int("HTTP_POOL_MAX_CONNECTIONS", 100)      # 最大连接数（近似 Druid max-active）
HTTP_POOL_MAX_KEEPALIVE = _env_int("HTTP_POOL_MAX_KEEPALIVE", 20)           # 最大保活连接数
HTTP_TIMEOUT = float(os.getenv("HTTP_TIMEOUT", "30"))                       # 单次请求超时（秒）
HTTP_POOL_TIMEOUT = float(os.getenv("HTTP_POOL_TIMEOUT", "5"))              # 从连接池获取连接超时（秒）

# ---- 网关调用重试（对等 RabbitMQ retry：3 次 + 指数退避）----
HTTP_RETRY_MAX = _env_int("HTTP_RETRY_MAX", 3)
HTTP_RETRY_BACKOFF = float(os.getenv("HTTP_RETRY_BACKOFF", "0.5"))          # 基础退避（秒），实际 = backoff * 2^(n-1)

# ---- 熔断（对等 Sentinel 熔断降级）----
CIRCUIT_BREAKER_FAILURE_THRESHOLD = _env_int("CIRCUIT_BREAKER_FAILURE_THRESHOLD", 5)  # 连续失败达到阈值后熔断
CIRCUIT_BREAKER_OPEN_SECONDS = _env_int("CIRCUIT_BREAKER_OPEN_SECONDS", 30)            # 熔断开启时长（秒）
CIRCUIT_BREAKER_HALFOPEN_TESTS = _env_int("CIRCUIT_BREAKER_HALFOPEN_TESTS", 1)         # 半开状态试探次数

# ---- Redis（可选；不配置则缓存/锁降级为进程内实现，对等 Java 端 RedisTemplate）----
REDIS_URL = _env("REDIS_URL", "")  # 例如 redis://localhost:6379/0 ；留空=禁用 Redis

# ---- 缓存（对等 @Cacheable / RedisUtil）----
CACHE_DEFAULT_TTL = _env_int("CACHE_DEFAULT_TTL", 300)                      # 热点缓存默认 TTL（秒）
CACHE_RAG_TTL = _env_int("CACHE_RAG_TTL", 600)                             # RAG 检索结果缓存 TTL（秒）

# ---- 限流（对等 Sentinel 网关限流；作为网关未覆盖 ai-service 的兜底防御）----
RATE_LIMIT_ENABLED = _env_bool("RATE_LIMIT_ENABLED", True)
RATE_LIMIT_GLOBAL_QPS = _env_int("RATE_LIMIT_GLOBAL_QPS", 100)             # 全局令牌桶速率（对等其它服务 200，AI 服务按 LLM 时延取 100）
RATE_LIMIT_PER_IP_QPS = _env_int("RATE_LIMIT_PER_IP_QPS", 20)              # 单 IP 令牌桶速率
RATE_LIMIT_BURST = _env_int("RATE_LIMIT_BURST", 50)                        # 令牌桶容量（突发）

# ---- 分布式定时锁（对等 SchedulerLock，防止多实例重复执行后台任务）----
ALERT_LOCK_KEY = _env("ALERT_LOCK_KEY", "ai-service:lock:alert-check")
ALERT_LOCK_TTL = _env_int("ALERT_LOCK_TTL", 120)                           # 锁 TTL（秒）须大于单轮检查耗时

# ---- Embedding 批量（提升吞吐，减少网络往返）----
EMBEDDING_BATCH_SIZE = _env_int("EMBEDDING_BATCH_SIZE", 16)

# ---- Uvicorn 并发参数（对等于多实例部署 + Tomcat 线程参数）----
WORKERS = _env_int("WORKERS", 1)                                          # 工作进程数（多实例横向扩展）
UVICORN_LIMIT_CONCURRENCY = _env_int("UVICORN_LIMIT_CONCURRENCY", 256)    # 单进程最大并发连接
UVICORN_TIMEOUT_KEEP_ALIVE = _env_int("UVICORN_TIMEOUT_KEEP_ALIVE", 5)    # keep-alive 超时（秒）
UVICORN_BACKLOG = _env_int("UVICORN_BACKLOG", 2048)                       # 等待队列长度
UVICORN_LIMIT_MAX_REQUESTS = _env_int("UVICORN_LIMIT_MAX_REQUESTS", 10000)  # 处理 N 请求后重启，缓解内存泄漏


@lru_cache
def get_config() -> dict[str, object]:
    """获取只读配置字典（缓存）。"""
    return {k: v for k, v in globals().items() if k.isupper() and not k.startswith("_")}
