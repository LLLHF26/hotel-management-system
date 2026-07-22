"""告警 Schema。"""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, Field


class AlertVO(BaseModel):
    id: int
    type: str
    level: str
    content: str
    trigger_time: datetime
    is_read: bool

    class Config:
        from_attributes = True


class AlertStatusResponse(BaseModel):
    alerts: list[AlertVO]
    unread_count: int


class AlertBatchReadRequest(BaseModel):
    ids: list[int] = Field(..., min_length=1)


class AlertBatchDeleteRequest(BaseModel):
    ids: list[int] = Field(..., min_length=1)


class AlertRuleCreate(BaseModel):
    type: str = Field(..., min_length=1, max_length=32)
    threshold: float = Field(..., ge=0, le=1)
    is_enabled: bool = True


class AlertRuleUpdate(BaseModel):
    threshold: float | None = Field(default=None, ge=0, le=1)
    is_enabled: bool | None = None


class AlertRuleVO(BaseModel):
    id: int
    type: str
    threshold: float
    is_enabled: bool
    create_time: datetime

    class Config:
        from_attributes = True
