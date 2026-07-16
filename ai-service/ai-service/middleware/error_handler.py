"""全局异常拦截，将未捕获异常转为统一 JSON 响应。"""

from __future__ import annotations

from fastapi import Request
from fastapi.responses import JSONResponse

from core.exceptions import AppException
from config import DEBUG


async def global_error_handler(request: Request, call_next):
    try:
        return await call_next(request)
    except AppException as exc:
        return JSONResponse(
            status_code=exc.status_code,
            content={"code": exc.code, "msg": str(exc), "data": None},
        )
    except Exception:
        # 生产环境隐藏细节，开发环境透出堆栈
        if DEBUG:
            import traceback
            detail = traceback.format_exc()
        else:
            detail = "服务器内部错误"
        return JSONResponse(
            status_code=500,
            content={"code": 500, "msg": detail, "data": None},
        )
