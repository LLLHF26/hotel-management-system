"""房型推荐 Schema。"""

from __future__ import annotations

from pydantic import BaseModel, Field


class RecommendRequest(BaseModel):
    check_in_date: str = Field(..., pattern=r"^\d{4}-\d{2}-\d{2}$")
    check_out_date: str = Field(..., pattern=r"^\d{4}-\d{2}-\d{2}$")
    guests: int = Field(..., ge=1)
    budget_min: float | None = None
    budget_max: float | None = None
    prefer: str | None = Field(default=None, max_length=200)


class RecommendItem(BaseModel):
    room_type_id: int
    room_type_name: str
    price: float
    area: int | None = None
    bed_type: str | None = None
    max_guests: int
    available_count: int
    score: float
    reason: str


class RecommendResponse(BaseModel):
    recommendations: list[RecommendItem]
    query_summary: str
