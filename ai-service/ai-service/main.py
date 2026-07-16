"""ai-service —— 酒店智能服务主入口。

FastAPI + LangChain 微服务，独立于 Java 后端，通过 HTTP 提供 AI 能力。
"""

from __future__ import annotations

import asyncio
import logging
from contextlib import asynccontextmanager
from pathlib import Path

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from api.chat import router as chat_router
from api.knowledge import router as knowledge_router
from api.recommend import router as recommend_router
from api.order_query import router as order_query_router
from api.alert import router as alert_router
from api.system import router as system_router
from config import DEBUG, LOG_LEVEL, SERVICE_HOST, SERVICE_PORT
from middleware.auth_middleware import jwt_auth_middleware
from middleware.error_handler import global_error_handler
from models.database import init_db

from core.logging_config import setup_logging

# ---- 日志 ----
setup_logging()
logger = logging.getLogger(__name__)

# ---- 确保数据目录存在 ----
(Path(__file__).resolve().parent / "data").mkdir(exist_ok=True)
(Path(__file__).resolve().parent / "logs").mkdir(exist_ok=True)

_ALERT_CHECK_INTERVAL = 300  # 告警检查间隔（秒）


async def _alert_loop():
    """后台循环：每 5 分钟扫描一次告警规则。"""
    from api.alert import run_alert_check

    await asyncio.sleep(60)  # 启动后延迟 1 分钟首次检查
    while True:
        try:
            count = await asyncio.to_thread(run_alert_check)
            if count > 0:
                logger.info("后台告警检查完成，触发 %d 条告警", count)
        except Exception:
            logger.exception("后台告警检查异常")
        await asyncio.sleep(_ALERT_CHECK_INTERVAL)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期：启动时初始化 DB 和后台任务，关闭时清理资源。"""
    logger.info("ai-service 启动中...")
    init_db()
    logger.info("数据库初始化完成")
    alert_task = asyncio.create_task(_alert_loop())
    logger.info("告警后台任务已启动（间隔 %d 秒）", _ALERT_CHECK_INTERVAL)
    yield
    alert_task.cancel()
    logger.info("ai-service 已关闭")


# ---- FastAPI 应用 ----
app = FastAPI(
    title="ai-service",
    description="AI Hotel 智能服务 —— 智能客服 / 房型推荐 / 订单查询 / 异常告警",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs" if DEBUG else None,
    redoc_url="/redoc" if DEBUG else None,
)

# ---- 中间件注册（LIFO 顺序：先注册的在外层） ----
app.middleware("http")(global_error_handler)
app.middleware("http")(jwt_auth_middleware)

# ---- CORS（网关会通过 DedupeResponseHeader 处理重复）----
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:5174", "http://localhost:5175"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---- 路由注册 ----
app.include_router(chat_router)
app.include_router(knowledge_router)
app.include_router(recommend_router)
app.include_router(order_query_router)
app.include_router(alert_router)
app.include_router(system_router)


# ---- 直接启动 ----
if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=SERVICE_HOST,
        port=SERVICE_PORT,
        reload=DEBUG,
        log_level=LOG_LEVEL.lower(),
    )
