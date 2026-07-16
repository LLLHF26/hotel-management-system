"""请求上下文 —— 通过 contextvars 在异步调用链中传递 token 和用户信息。"""

from __future__ import annotations

from contextvars import ContextVar
from dataclasses import dataclass, field


@dataclass(slots=True)
class RequestContext:
    token: str = ""
    user_id: int | None = None
    username: str | None = None
    role: str | None = None
    customer_id: int | None = None


_request_context: ContextVar[RequestContext] = ContextVar("request_context", default=RequestContext())


def get_request_context() -> RequestContext:
    return _request_context.get()


def set_request_context(ctx: RequestContext) -> None:
    _request_context.set(ctx)
