"""订单查询 Schema。"""

from __future__ import annotations

from pydantic import BaseModel, Field


class OrderQueryRequest(BaseModel):
    query: str = Field(..., min_length=1, max_length=200, description="自然语言查询")


class OrderVO(BaseModel):
    order_no: str
    room_type_name: str
    room_number: str
    check_in_date: str
    check_out_date: str
    nights: int
    total_amount: float
    status: str


class OrderQueryResponse(BaseModel):
    parsed_intent: str
    parsed_params: dict
    orders: list[OrderVO]
    summary: str
