"""JWT 鉴权中间件 —— 对所有非白名单接口校验 Token。"""

from __future__ import annotations

from fastapi import Request
from fastapi.responses import JSONResponse

from core.auth import decode_token
from core.exceptions import ForbiddenError, UnauthorizedError

WHITELIST = {
    # Swagger / OpenAPI
    "/docs",
    "/redoc",
    "/openapi.json",
    # 健康检查
    "/api/ai/health",
    "/api/ai/info",
}


def is_whitelisted(path: str) -> bool:
    return path in WHITELIST or path.startswith("/docs") or path.startswith("/redoc")


async def jwt_auth_middleware(request: Request, call_next):
    path = request.url.path

    if request.method == "OPTIONS" or is_whitelisted(path):
        return await call_next(request)

    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        return JSONResponse(
            status_code=401,
            content={"code": 401, "msg": "未登录或 Token 缺失", "data": None},
        )

    token = auth_header[7:].strip()
    if not token:
        return JSONResponse(
            status_code=401,
            content={"code": 401, "msg": "未登录或 Token 缺失", "data": None},
        )

    try:
        payload = decode_token(token)
        request.state.token = token
        request.state.user_id = payload.get("userId")
        request.state.username = payload.get("username")
        request.state.role = payload.get("role")
        request.state.customer_id = payload.get("customerId")
    except UnauthorizedError as e:
        return JSONResponse(
            status_code=401,
            content={"code": e.code, "msg": str(e), "data": None},
        )
    except ForbiddenError as e:
        return JSONResponse(
            status_code=403,
            content={"code": e.code, "msg": str(e), "data": None},
        )

    return await call_next(request)
