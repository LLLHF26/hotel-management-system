"""订单智能查询接口。"""

from __future__ import annotations

import json
import logging
import re

from fastapi import APIRouter, Depends

from agents.order_agent import create_order_agent
from api.deps import get_current_user
from core.context import get_request_context
from core.http_client import call_gateway_async
from schemas.common import Result
from schemas.order_query import OrderQueryRequest, OrderQueryResponse, OrderVO

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai/order", tags=["订单智能查询"])


@router.post("/query", summary="自然语言查订单", response_model=Result[OrderQueryResponse])
async def query_orders(
    body: OrderQueryRequest,
    user: dict = Depends(get_current_user),
):
    ctx = get_request_context()

    # 1. LangChain 解析自然语言 → 提取意图 + 参数
    parsed_intent = ""
    parsed_params: dict = {}
    try:
        chain = create_order_agent()
        output = await chain.ainvoke({"input": body.query})
        intent, params = _parse_agent_json(output)
        parsed_intent = intent
        parsed_params = params
    except Exception:
        logger.exception("订单查询 Agent 失败")
        return Result.ok(OrderQueryResponse(
            parsed_intent="", parsed_params={}, orders=[], summary="意图解析失败，请重试"
        ))

    # 2. 调用 gateway → order-service 获取订单
    gateway_params: dict = {}
    if ctx.customer_id:
        gateway_params["customerId"] = ctx.customer_id

    # 映射 Agent 提取的参数到 gateway 参数
    if parsed_params.get("start_date"):
        gateway_params["startDate"] = parsed_params["start_date"]
    if parsed_params.get("end_date"):
        gateway_params["endDate"] = parsed_params["end_date"]
    if parsed_params.get("status"):
        gateway_params["status"] = parsed_params["status"]

    orders: list[OrderVO] = []
    try:
        resp = await call_gateway_async(
            "/api/order/list", token=ctx.token, params=gateway_params
        )
        data = resp.get("data", {})
        order_list = data.get("records", data) if isinstance(data, dict) else data
        if isinstance(order_list, list):
            for o in order_list:
                orders.append(OrderVO(
                    order_no=o.get("orderNo", ""),
                    room_type_name=o.get("roomTypeName", ""),
                    room_number=o.get("roomNumber", ""),
                    check_in_date=str(o.get("checkInDate", "")),
                    check_out_date=str(o.get("checkOutDate", "")),
                    nights=int(o.get("nights", 0)),
                    total_amount=float(o.get("totalAmount", 0)),
                    status=o.get("status", ""),
                ))
    except Exception:
        logger.exception("调用 order-service 失败")

    # 3. 生成摘要
    total_amount = sum(o.total_amount for o in orders)
    if orders:
        summary = f"共查询到 {len(orders)} 笔订单"
        if total_amount > 0:
            summary += f"，合计 {total_amount:.2f} 元"
    else:
        summary = "未查询到匹配的订单"

    return Result.ok(OrderQueryResponse(
        parsed_intent=parsed_intent or "查询订单",
        parsed_params=parsed_params,
        orders=orders,
        summary=summary,
    ))


def _parse_agent_json(output: str) -> tuple[str, dict]:
    """从 Agent 输出中提取 JSON 格式的意图和参数。"""
    try:
        json_match = re.search(r"\{.*\}", output, re.DOTALL)
        if json_match:
            data = json.loads(json_match.group())
            return str(data.get("parsed_intent", "")), dict(data.get("parsed_params", {}))
    except (json.JSONDecodeError, KeyError):
        pass
    return "", {}
