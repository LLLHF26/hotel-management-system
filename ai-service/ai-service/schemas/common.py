"""通用 Schema —— 统一返回格式、分页。"""

from __future__ import annotations

from typing import Generic, TypeVar

from pydantic import BaseModel, Field

T = TypeVar("T")


class Result(BaseModel, Generic[T]):
    code: int = 200
    msg: str = "成功"
    data: T | None = None

    @classmethod
    def ok(cls, data: T | None = None, msg: str = "成功") -> "Result[T]":
        return cls(code=200, msg=msg, data=data)

    @classmethod
    def fail(cls, msg: str, code: int = 500) -> "Result[None]":
        return cls(code=code, msg=msg, data=None)


class PageResult(BaseModel, Generic[T]):
    total: int
    records: list[T]


class PageQuery(BaseModel):
    page: int = Field(default=1, ge=1)
    size: int = Field(default=10, ge=1, le=100)
