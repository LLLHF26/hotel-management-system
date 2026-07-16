"""知识库 Schema —— 文档上传模式。"""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, Field


class KnowledgeDocumentVO(BaseModel):
    id: int
    filename: str
    file_type: str
    file_size: int
    category: str
    chunk_count: int
    is_enabled: bool
    create_time: datetime
    update_time: datetime

    class Config:
        from_attributes = True


class KnowledgeDocumentUpdate(BaseModel):
    category: str | None = Field(default=None, min_length=1, max_length=32)


class KnowledgeStatusUpdate(BaseModel):
    is_enabled: bool


class KnowledgeUploadResult(BaseModel):
    id: int
    filename: str
    file_type: str
    file_size: int
    chunk_count: int
