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
CHROMA_PERSIST_PATH = _env("CHROMA_PERSIST_PATH", str(_BASE_DIR / "data" / "chroma"))

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


@lru_cache
def get_config() -> dict[str, object]:
    """获取只读配置字典（缓存）。"""
    return {k: v for k, v in globals().items() if k.isupper() and not k.startswith("_")}
