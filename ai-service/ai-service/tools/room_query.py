"""LangChain Tool —— 房型/房间查询（调用 Java room-service）。"""

from __future__ import annotations

import json
import logging

from langchain.tools import tool

from core.context import get_request_context
from core.http_client import call_gateway_json

logger = logging.getLogger(__name__)


@tool
def query_room_types(query_json: str = "") -> str:
    """查询酒店所有可用房型，返回房型名称、价格、面积、床型、最大入住人数等信息。
    在用户询问"有什么房型""推荐房间"时调用此工具。

    Args:
        query_json: 可选 JSON，如 {"keyword": "大床"} 或 {}
    """
    ctx = get_request_context()
    params = {}
    if query_json:
        try:
            params = json.loads(query_json)
        except json.JSONDecodeError:
            pass
    return call_gateway_json("/api/room/type/list", token=ctx.token or "", params=params)


@tool
def query_available_rooms(filter_json: str = "") -> str:
    """查询当前空闲中的房间列表及房间号。
    在用户询问"有空房吗""还有哪些房间可以订""行政套房有没有空房"等涉及具体房型可用性问题时调用此工具。
    返回结果包含所有空闲房间的完整列表（不分页截断）。

    Args:
        filter_json: 可选 JSON，如 {"room_type_id": 3} 或 {}
    """
    ctx = get_request_context()
    # size=200 避免后端默认分页(size=10)导致高楼层房间被截断
    params = {"status": "空闲中", "size": 200}
    if filter_json:
        try:
            extra = json.loads(filter_json)
            params.update(extra)
        except json.JSONDecodeError:
            pass
    return call_gateway_json("/api/room/list", token=ctx.token or "", params=params)


@tool
def query_room_occupancy(query_json: str = "") -> str:
    """查询酒店当前入住率，返回各房型的已占用/总房间数。
    在用户询问"还有多少空房""入住率怎么样"时调用此工具。

    Args:
        query_json: 可选 JSON，如 {} 或 {"room_type_id": 3}
    """
    ctx = get_request_context()
    params = {}
    if query_json:
        try:
            params = json.loads(query_json)
        except json.JSONDecodeError:
            pass
    return call_gateway_json("/api/room/occupancy", token=ctx.token or "", params=params)
