"""LangChain Tool —— 订单查询（调用 Java order-service）。"""

from __future__ import annotations

import json
import logging

from langchain.tools import tool

from core.context import get_request_context
from core.http_client import call_gateway_json

logger = logging.getLogger(__name__)


@tool
def query_orders(params_json: str = "{}") -> str:
    """【必须调用此工具】查询当前登录用户的酒店订单。
    当用户说"我的订单""查订单""订单记录""住宿记录""住了几次""消费多少"时，必须先调用本工具获取数据再回答。
    无需让用户提供会员ID或订单号，系统会自动识别当前用户。
    不指定条件时传空对象 {}。

    Args:
        params_json: JSON 查询条件（可选），如 {"status": "已完成"}、{"startDate": "2026-05-01"}
    """
    ctx = get_request_context()
    params = {}
    if params_json:
        try:
            params = json.loads(params_json)
        except json.JSONDecodeError:
            pass
    if ctx.customer_id:
        params.setdefault("customerId", ctx.customer_id)
    return call_gateway_json("/api/order/list", token=ctx.token or "", params=params)
