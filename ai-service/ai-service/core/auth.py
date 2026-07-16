"""JWT 解析与权限校验。"""

from __future__ import annotations

import jwt
from jwt.exceptions import ExpiredSignatureError, InvalidTokenError

from config import JWT_ALGORITHM, JWT_SECRET
from core.exceptions import ForbiddenError, UnauthorizedError

# 角色常量
ROLE_ADMIN = "ADMIN"
ROLE_FRONT_DESK = "FRONT_DESK"
ROLE_CLEANER = "CLEANER"


def decode_token(token: str) -> dict:
    """解析 JWT，返回 payload；Token 无效则抛 UnauthorizedError。"""
    try:
        return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except ExpiredSignatureError:
        raise UnauthorizedError("Token 已过期")
    except InvalidTokenError:
        raise UnauthorizedError("Token 无效")


def require_role(token: str, *roles: str) -> dict:
    """校验 Token 并检查角色。同时返回 payload。"""
    payload = decode_token(token)
    user_role = payload.get("role", "")
    if roles and user_role not in roles:
        raise ForbiddenError(f"需要 {', '.join(roles)} 角色权限，当前角色: {user_role}")
    return payload
