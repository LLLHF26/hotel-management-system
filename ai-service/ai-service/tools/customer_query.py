"""LangChain Tool —— 会员信息查询（调用 Java user-service）。"""

from __future__ import annotations

import json
import logging

from langchain.tools import tool

from core.context import get_request_context
from core.http_client import GatewayClient

logger = logging.getLogger(__name__)


@tool
def query_customer(customer_id: str = "") -> str:
    """【必须调用此工具】查询会员信息（等级、积分、消费等）。
    当用户说"我的会员""我的积分""我的等级""会员信息""个人信息"时，必须先调用本工具获取数据再回答。
    无需让用户提供会员ID，不传参数时自动查询当前登录用户。

    Args:
        customer_id: 会员 ID（可选，留空则查当前用户），如 "1"
    """
    ctx = get_request_context()
    if not ctx.token:
        return json.dumps({"error": "未登录，无法查询会员信息"}, ensure_ascii=False)
    cid = customer_id.strip() if customer_id and customer_id.strip() else str(ctx.customer_id or "")
    if not cid:
        return json.dumps({"error": "无法确定会员 ID，请先登录"}, ensure_ascii=False)
    try:
        client = GatewayClient()
        result = client.get(f"/api/customer/{cid}", token=ctx.token)
        data = result.get("data", {})
        return json.dumps(data, ensure_ascii=False)
    except Exception as e:
        logger.error("调用 user-service 失败: %s", e)
        return json.dumps({"error": f"查询失败: {e}"}, ensure_ascii=False)
