"""ai-service —— 酒店智能服务主入口。

FastAPI + LangChain 微服务，独立于 Java 后端，通过 HTTP 提供 AI 能力。
已加入高并发应对措施（对等 Java 后端微服务化改造）：

- 限流中间件（对等 Sentinel 网关限流，兜底网关未覆盖 ai-service 的场景）
- 共享 HTTP 连接池 + 异步网关调用 + 重试 + 熔断（对等 Druid 连接池 + RabbitMQ 重试 + Sentinel 降级）
- Redis/进程内缓存（对等 @Cacheable / RedisUtil）
- SQLite WAL + 连接池（对等 Druid 写并发优化）
- 分布式定时锁（对等 SchedulerLock，防止多实例重复执行后台告警）
- Uvicorn 多 worker / 并发连接限制（对等于横向多实例 + Tomcat 线程参数）
"""

from __future__ import annotations

import asyncio
import logging
from contextlib import asynccontextmanager
from pathlib import Path

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from api.alert import router as alert_router
from api.chat import router as chat_router
from api.knowledge import router as knowledge_router
from api.order_query import router as order_query_router
from api.recommend import router as recommend_router
from api.system import router as system_router
from config import (
    ALERT_LOCK_KEY,
    ALERT_LOCK_TTL,
    DEBUG,
    LOG_LEVEL,
    SERVICE_HOST,
    SERVICE_PORT,
    UVICORN_BACKLOG,
    UVICORN_LIMIT_CONCURRENCY,
    UVICORN_LIMIT_MAX_REQUESTS,
    UVICORN_TIMEOUT_KEEP_ALIVE,
    WORKERS,
)
from core.cache import close_cache
from core.distributed_lock import close_lock, distributed_lock
from core.http_client import close_clients
from middleware.auth_middleware import jwt_auth_middleware
from middleware.error_handler import global_error_handler
from middleware.rate_limit import rate_limit_middleware
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
    """后台循环：每 5 分钟扫描一次告警规则。

    通过分布式锁保证多 worker / 多实例部署下只有一个实例执行，避免重复扫描与重复告警。
    """
    from api.alert import run_alert_check

    await asyncio.sleep(60)  # 启动后延迟 1 分钟首次检查
    while True:
        try:
            async with distributed_lock(ALERT_LOCK_KEY, ALERT_LOCK_TTL) as acquired:
                if not acquired:
                    logger.debug("后台告警检查跳过（其他实例持有锁）")
                else:
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
    # 优雅关闭：释放共享客户端与 Redis 连接
    await close_clients()
    await close_cache()
    await close_lock()
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

# ---- 中间件注册（LIFO：先注册的在外层）----
# 1) 限流最外层（尽早拒绝超额请求，对等 Sentinel）
app.middleware("http")(rate_limit_middleware)
# 2) 全局异常拦截
app.middleware("http")(global_error_handler)
# 3) JWT 鉴权
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
    # reload 模式仅支持单 worker；生产多实例用 WORKERS 横向扩展
    workers = 1 if DEBUG else max(1, WORKERS)
    uvicorn.run(
        "main:app",
        host=SERVICE_HOST,
        port=SERVICE_PORT,
        reload=DEBUG,
        workers=workers,
        limit_concurrency=UVICORN_LIMIT_CONCURRENCY,
        limit_max_requests=UVICORN_LIMIT_MAX_REQUESTS,
        timeout_keep_alive=UVICORN_TIMEOUT_KEEP_ALIVE,
        backlog=UVICORN_BACKLOG,
        log_level=LOG_LEVEL.lower(),
    )
