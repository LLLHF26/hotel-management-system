"""智能对话 Schema。"""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, Field


class ChatMessage(BaseModel):
    role: str = Field(pattern=r"^(user|assistant|ai)$")
    content: str


class ChatRequest(BaseModel):
    session_id: str | None = Field(default=None, description="会话 ID，不传则自动创建")
    message: str = Field(..., min_length=1, max_length=500, description="用户输入")
    history: list[ChatMessage] = Field(default_factory=list)


class ChatResponse(BaseModel):
    session_id: str
    reply: str
    intent: str | None = None
    tokens: int = 0


class ChatFeedbackRequest(BaseModel):
    session_id: str
    message_index: int = Field(ge=0)
    feedback: int = Field(..., ge=0, le=1, description="1=有用 0=无用")


class SessionSummary(BaseModel):
    session_id: str
    first_message: str
    message_count: int
    create_time: datetime
    last_time: datetime

    class Config:
        from_attributes = True


class MessageRecord(BaseModel):
    role: str
    content: str
    intent: str | None = None
    time: datetime

    class Config:
        from_attributes = True


class SessionHistory(BaseModel):
    session_id: str
    messages: list[MessageRecord]
