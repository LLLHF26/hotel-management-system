"""系统管理接口（健康检查、服务信息）。"""

from __future__ import annotations

import logging
import sys
import time

import httpx
from fastapi import APIRouter
from sqlalchemy import text

from config import DEBUG, GATEWAY_URL, LLM_MODEL, LLM_PROVIDER
from core.llm import get_llm
from models.database import SessionLocal
from schemas.common import Result

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai", tags=["系统管理"])

_START_TIME = time.time()


def _format_uptime() -> str:
    seconds = int(time.time() - _START_TIME)
    days, rem = divmod(seconds, 86400)
    hours, rem = divmod(rem, 3600)
    minutes, _ = divmod(rem, 60)
    return f"{days}d {hours}h {minutes}m"


def _check_sqlite() -> tuple[str, bool]:
    try:
        db = SessionLocal()
        db.execute(text("SELECT 1"))
        db.close()
        return "connected", True
    except Exception:
        return "disconnected", False


def _check_llm() -> tuple[str, bool]:
    try:
        get_llm()
        return "connected", True
    except Exception:
        return "disconnected", False


def _check_vector_store() -> tuple[str, bool]:
    try:
        from core.vector_store import get_vector_store
        store = get_vector_store()
        store.count()
        return "connected", True
    except Exception:
        return "disconnected", False


def _check_gateway() -> tuple[str, bool]:
    try:
        with httpx.Client(timeout=httpx.Timeout(5.0)) as client:
            resp = client.get(f"{GATEWAY_URL}/actuator/health")
            if resp.is_success:
                return "connected", True
            return "error", False
    except Exception:
        return "disconnected", False


@router.get("/health", summary="服务健康检查")
async def health():
    llm_status, _ = _check_llm()
    sqlite_status, _ = _check_sqlite()
    vector_status, _ = _check_vector_store()
    gateway_status, _ = _check_gateway()

    all_up = all(
        s == "connected"
        for s in [llm_status, sqlite_status, vector_status]
    )

    return Result.ok(
        {
            "status": "UP" if all_up else "DEGRADED",
            "llm_provider": LLM_MODEL,
            "llm_status": llm_status,
            "vector_status": vector_status,
            "sqlite_status": sqlite_status,
            "gateway_status": gateway_status,
            "uptime": _format_uptime(),
        },
        msg="服务正常" if all_up else "部分组件异常",
    )


@router.get("/info", summary="获取服务信息")
async def info():
    knowledge_count = 0
    vector_count = 0
    try:
        db = SessionLocal()
        from models.knowledge import KnowledgeDocument
        knowledge_count = (
            db.query(KnowledgeDocument)
            .filter(KnowledgeDocument.is_enabled == True)
            .count()
        )
        db.close()
    except Exception:
        pass

    try:
        from core.vector_store import get_vector_store
        store = get_vector_store()
        vector_count = store.count()
    except Exception:
        pass

    python_version = f"{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}"

    return Result.ok(
        {
            "service": "ai-service",
            "version": "1.0.0",
            "framework": "FastAPI 0.115.6",
            "langchain": "0.3.13",
            "python": python_version,
            "knowledge_count": knowledge_count,
            "vector_count": vector_count,
        }
    )
