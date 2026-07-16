"""房型推荐接口。"""

from __future__ import annotations

import json
import logging
import re

from fastapi import APIRouter, Depends

from agents.recommend_agent import create_recommend_agent
from api.deps import get_current_user
from core.context import get_request_context
from core.http_client import GatewayClient
from schemas.common import Result
from schemas.recommend import RecommendItem, RecommendRequest, RecommendResponse

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai/recommend", tags=["房型推荐"])


@router.post("/room", summary="智能推荐房型", response_model=Result[RecommendResponse])
async def recommend_room(
    body: RecommendRequest,
    user: dict = Depends(get_current_user),
):
    ctx = get_request_context()

    # 1. 调用 gateway → room-service 获取可用房型和房间
    available_types: list[dict] = []
    available_rooms: list[dict] = []
    try:
        client = GatewayClient()
        types_resp = client.get("/api/room/type/list", token=ctx.token)
        available_types = _extract_records(types_resp)

        rooms_resp = client.get(
            "/api/room/list", token=ctx.token, params={"status": "空闲中"}
        )
        available_rooms = _extract_records(rooms_resp)
    except Exception:
        logger.exception("调用 room-service 获取房型/房间失败")
        return Result.ok(
            RecommendResponse(
                recommendations=[],
                query_summary=_build_summary(body),
            )
        )

    # 2. 统计各房型可用数量
    type_available_count: dict[int, int] = {}
    for room in available_rooms:
        tid = room.get("roomTypeId") or room.get("room_type_id", 0)
        type_available_count[tid] = type_available_count.get(tid, 0) + 1

    # 3. 构建包含可用数据的房型列表供 Agent 评估
    room_type_summary: list[dict] = []
    for t in available_types:
        tid = t.get("id", 0)
        avail = type_available_count.get(tid, 0)
        room_type_summary.append({
            "room_type_name": t.get("name", ""),
            "price": float(t.get("price", 0)),
            "area": t.get("area"),
            "bed_type": t.get("bedType") or t.get("bed_type"),
            "max_guests": int(t.get("maxGuests") or t.get("max_guests", 2)),
            "available_count": avail,
            "amenities": t.get("amenities", ""),
        })

    # 4. Agent 排序 + 生成推荐理由
    input_text = json.dumps({
        "check_in": body.check_in_date,
        "check_out": body.check_out_date,
        "guests": body.guests,
        "budget_min": body.budget_min,
        "budget_max": body.budget_max,
        "prefer": body.prefer or "",
        "available_room_types": room_type_summary,
    }, ensure_ascii=False)

    try:
        chain = create_recommend_agent()
        output = await chain.ainvoke({"input": input_text})
    except Exception:
        logger.exception("推荐 Agent 执行失败")
        output = ""

    # 5. 解析 Agent JSON 输出 → 结构化响应
    recommendations = _parse_agent_output(
        output, available_types, type_available_count
    )

    logger.info("推荐完成，返回 %d 条推荐结果，Agent 输出长度=%d", len(recommendations), len(output))

    return Result.ok(
        RecommendResponse(
            recommendations=recommendations,
            query_summary=_build_summary(body),
        )
    )


def _build_summary(body: RecommendRequest) -> str:
    parts = [f"{body.guests} 人入住 {body.check_in_date} 至 {body.check_out_date}"]
    if body.budget_min or body.budget_max:
        parts.append(f"预算 {body.budget_min}-{body.budget_max} 元/晚")
    if body.prefer:
        parts.append(f"偏好：{body.prefer}")
    return "，".join(parts)


def _extract_records(resp: dict) -> list[dict]:
    data = resp.get("data", {})
    if isinstance(data, list):
        return data
    return data.get("records", [])


def _parse_agent_output(
    agent_output: str,
    available_types: list[dict],
    type_available_count: dict[int, int],
) -> list[RecommendItem]:
    """解析 Agent JSON 输出，提取 score 和 reason。JSON 解析失败时回退到默认评分。"""
    try:
        json_match = re.search(r"\[.*\]", agent_output, re.DOTALL)
        if json_match:
            agent_items = json.loads(json_match.group())
            if isinstance(agent_items, list) and agent_items:
                agent_scores: dict[str, dict] = {}
                for item in agent_items:
                    name = item.get("room_type_name") or item.get("name", "")
                    agent_scores[name] = {
                        "score": max(0.0, min(1.0, float(item.get("score", 0.5)))),
                        "reason": str(item.get("reason", "")),
                    }

                scored: list[dict] = []
                for t in available_types:
                    tid = t.get("id", 0)
                    avail = type_available_count.get(tid, 0)
                    if avail == 0:
                        continue
                    name = t.get("name", "")
                    info = agent_scores.get(name, {})
                    scored.append({
                        "room_type_id": tid,
                        "room_type_name": name,
                        "price": float(t.get("price", 0)),
                        "area": t.get("area"),
                        "bed_type": t.get("bedType") or t.get("bed_type"),
                        "max_guests": int(t.get("maxGuests") or t.get("max_guests", 2)),
                        "available_count": avail,
                        "score": info.get("score", 0.5),
                        "reason": info.get("reason") or f"当前有 {avail} 间可用",
                    })

                scored.sort(key=lambda x: x["score"], reverse=True)
                return [RecommendItem(**item) for item in scored[:5]]
    except (json.JSONDecodeError, KeyError, ValueError):
        logger.warning("Agent 输出 JSON 解析失败，使用默认评分")

    # 回退：简单评分（价格越接近预算中位数越高）
    scored: list[dict] = []
    for t in available_types:
        tid = t.get("id", 0)
        avail = type_available_count.get(tid, 0)
        if avail == 0:
            continue
        scored.append({
            "room_type_id": tid,
            "room_type_name": t.get("name", ""),
            "price": float(t.get("price", 0)),
            "area": t.get("area"),
            "bed_type": t.get("bedType") or t.get("bed_type"),
            "max_guests": int(t.get("maxGuests") or t.get("max_guests", 2)),
            "available_count": avail,
            "score": 0.5,
            "reason": f"当前有 {avail} 间可用",
        })

    scored.sort(key=lambda x: x["score"], reverse=True)
    return [RecommendItem(**item) for item in scored[:5]]
