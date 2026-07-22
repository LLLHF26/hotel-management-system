"""统一异常定义。"""

from __future__ import annotations


class AppException(Exception):
    """业务异常基类，携带 HTTP 状态码与错误码。"""

    def __init__(self, message: str, *, code: int = 500, status_code: int = 400) -> None:
        super().__init__(message)
        self.code = code
        self.status_code = status_code


class NotFoundError(AppException):
    def __init__(self, message: str = "资源不存在") -> None:
        super().__init__(message, code=404, status_code=404)


class UnauthorizedError(AppException):
    def __init__(self, message: str = "未登录或 Token 已过期") -> None:
        super().__init__(message, code=401, status_code=401)


class ForbiddenError(AppException):
    def __init__(self, message: str = "权限不足") -> None:
        super().__init__(message, code=403, status_code=403)


class ConflictError(AppException):
    def __init__(self, message: str = "数据冲突") -> None:
        super().__init__(message, code=409, status_code=409)


class LLMError(AppException):
    def __init__(self, message: str = "大模型调用失败") -> None:
        super().__init__(message, code=500, status_code=500)


class GatewayError(AppException):
    def __init__(self, message: str = "Java 服务调用失败") -> None:
        super().__init__(message, code=500, status_code=500)


class GatewayRetryableError(GatewayError):
    """网关 5xx / 网络层错误，可触发重试与熔断。"""
