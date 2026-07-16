"""FastAPI 依赖注入 —— 数据库会话、当前用户信息、请求上下文。"""

from __future__ import annotations

from typing import AsyncGenerator, Generator

from fastapi import Depends, Request
from sqlalchemy.orm import Session

from core.context import RequestContext, set_request_context
from models.database import SessionLocal


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


async def get_current_user(request: Request) -> dict:
    """从 request.state 中提取鉴权中间件注入的用户信息，并写入 contextvars。
    必须是 async def —— 同步依赖在线程池执行，ContextVar 不会传回主任务。
    """
    ctx = RequestContext(
        token=getattr(request.state, "token", ""),
        user_id=getattr(request.state, "user_id", None),
        username=getattr(request.state, "username", None),
        role=getattr(request.state, "role", None),
        customer_id=getattr(request.state, "customer_id", None),
    )
    set_request_context(ctx)
    return {
        "user_id": ctx.user_id,
        "username": ctx.username,
        "role": ctx.role,
        "customer_id": ctx.customer_id,
    }
