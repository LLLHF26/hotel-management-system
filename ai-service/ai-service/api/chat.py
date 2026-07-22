"""智能客服对话接口。"""

from __future__ import annotations

import json
import logging
import re
import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, Request
from langchain_core.messages import AIMessage, HumanMessage
from sqlalchemy import func
from sqlalchemy.orm import Session
from sse_starlette.sse import EventSourceResponse

from api.deps import get_current_user, get_db
from agents.chat_agent import create_chat_agent
from core.context import RequestContext, set_request_context
from core.vector_store import async_similarity_search
from models.chat_log import ChatLog
from schemas.chat import (
    ChatFeedbackRequest,
    ChatRequest,
    ChatResponse,
    MessageRecord,
    SessionHistory,
    SessionSummary,
)
from schemas.common import PageResult, Result

logger = logging.getLogger(__name__)

# ---- 意图分类关键词 ----
_INTENT_PATTERNS: list[tuple[str, re.Pattern]] = [
    ("查订单", re.compile(r"订单|住宿记录|消费记录|花了多少|多少钱|账单")),
    ("推荐房型", re.compile(r"推荐|房型|房间.*推荐|住.*什么|住.*哪种|订.*房|预订|有.*房|大床|双床|套房|怎么.*订")),
    ("客服问答", re.compile(r"怎么|如何|有没有|可以|能|什么|哪里|几点|多少钱|设施|政策|游泳池|wifi|停车|早餐|退房|入住|取消|押金|行李|接送|宠物|抽烟|加床|儿童|健身房|餐厅|泳池|桑拿|SPA|商务|会议室")),
    ("闲聊", re.compile(r"你好|谢谢|再见|天气|今天.*怎么样|你是谁|帮我|介绍.*自己")),
]


def _classify_intent(message: str) -> str:
    """基于关键词规则分类用户意图。"""
    for intent, pattern in _INTENT_PATTERNS:
        if pattern.search(message):
            return intent
    return "客服问答"


def _estimate_tokens(text: str) -> int:
    """粗略估算 token 数：中文字符约 1.5 token/字，英文约 0.3 token/字。"""
    chinese_chars = sum(1 for c in text if "一" <= c <= "鿿")
    other_chars = len(text) - chinese_chars
    return max(1, int(chinese_chars * 1.5 + other_chars * 0.3))


def _build_history(history: list) -> list:
    """将 ChatMessage 列表转换为 LangChain 消息格式。"""
    messages: list = []
    for msg in history:
        if msg.role == "user":
            messages.append(HumanMessage(content=msg.content))
        elif msg.role in ("assistant", "ai"):
            messages.append(AIMessage(content=msg.content))
    return messages

router = APIRouter(prefix="/api/ai/chat", tags=["智能客服"])


@router.post("", summary="智能对话（流式 SSE）")
async def chat_stream(request: Request, body: ChatRequest):
    """流式 SSE 接口：逐步返回 LLM 生成的 token，最后发送 done 事件。"""
    user: dict = await _resolve_user(request)

    async def event_generator():
        set_request_context(RequestContext(
            token=user.get("token", ""),
            user_id=user.get("user_id"),
            username=user.get("username"),
            role=user.get("role"),
            customer_id=user.get("customer_id"),
        ))
        session_id = body.session_id or f"sess_{uuid.uuid4().hex[:12]}"
        try:
            knowledge_results = await async_similarity_search(body.message, k=3)
            knowledge_context = "\n".join(
                item.get("content", "") for item in knowledge_results
            )

            agent = create_chat_agent()
            intent = _classify_intent(body.message)
            reply_parts: list[str] = []
            chat_history = _build_history(body.history)

            async for event in agent.astream_events(
                {
                    "input": body.message,
                    "knowledge_context": knowledge_context,
                    "chat_history": chat_history,
                },
                version="v2",
            ):
                kind = event.get("event", "")
                if kind == "on_chat_model_stream":
                    chunk = event["data"]["chunk"]
                    if chunk.content:
                        reply_parts.append(chunk.content)
                        yield {"event": "token", "data": chunk.content}
                elif kind == "on_tool_start":
                    yield {"event": "tool", "data": event.get("name", "")}

            reply = "".join(reply_parts)
            # 将同步 DB 写入移出事件循环（避免阻塞其他请求）
            await asyncio.to_thread(
                _save_chat_log_sync,
                user.get("customer_id"), session_id, "user", body.message, intent,
            )
            await asyncio.to_thread(
                _save_chat_log_sync,
                user.get("customer_id"), session_id, "assistant", reply, intent,
            )
            yield {"event": "done", "data": json.dumps({
                "session_id": session_id,
                "intent": intent,
                "tokens": _estimate_tokens(reply),
            }, ensure_ascii=False)}
        except Exception:
            logger.exception("流式对话异常")
            yield {"event": "error", "data": "大模型调用失败，请稍后重试"}

    return EventSourceResponse(event_generator())


@router.post("/sync", summary="智能对话（同步）", response_model=Result[ChatResponse])
async def chat_sync(
    body: ChatRequest,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    session_id = body.session_id or f"sess_{uuid.uuid4().hex[:12]}"

    knowledge_results = await async_similarity_search(body.message, k=3)
    knowledge_context = "\n".join(
        item.get("content", "") for item in knowledge_results
    )

    intent = _classify_intent(body.message)
    chat_history = _build_history(body.history)

    try:
        agent = create_chat_agent()
        result = await agent.ainvoke(
            {
                "input": body.message,
                "knowledge_context": knowledge_context,
                "chat_history": chat_history,
            }
        )
        reply = result.get("output", "")
    except Exception:
        logger.exception("同步对话异常")
        return Result.fail("大模型调用失败，请稍后重试")

    _save_chat_log(
        db, user.get("customer_id"), session_id, "user", body.message, intent
    )
    _save_chat_log(
        db, user.get("customer_id"), session_id, "assistant", reply, intent
    )

    return Result.ok(
        ChatResponse(
            session_id=session_id,
            reply=reply,
            intent=intent,
            tokens=_estimate_tokens(reply),
        )
    )


@router.get("/history/{session_id}", summary="获取会话历史", response_model=Result[SessionHistory])
async def get_history(session_id: str, db: Session = Depends(get_db)):
    logs = (
        db.query(ChatLog)
        .filter(ChatLog.session_id == session_id)
        .order_by(ChatLog.create_time.asc())
        .all()
    )
    messages = [
        MessageRecord(
            role=log.role,
            content=log.content,
            intent=log.intent,
            time=log.create_time,
        )
        for log in logs
    ]
    return Result.ok(SessionHistory(session_id=session_id, messages=messages))


@router.get("/sessions", summary="获取用户会话列表", response_model=Result[PageResult[SessionSummary]])
async def get_sessions(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
    page: int = 1, size: int = 10,
):
    customer_id = user.get("customer_id")

    # 聚合统计：每个 session 的时间范围 + 消息数
    stats = (
        db.query(
            ChatLog.session_id,
            func.min(ChatLog.create_time).label("first_time"),
            func.max(ChatLog.create_time).label("last_time"),
            func.count().label("msg_count"),
        )
        .filter(ChatLog.customer_id == customer_id)
        .group_by(ChatLog.session_id)
        .subquery()
    )

    # 最新消息 ID
    latest_id = (
        db.query(func.max(ChatLog.id).label("max_id"))
        .filter(ChatLog.customer_id == customer_id)
        .group_by(ChatLog.session_id)
        .subquery()
    )

    q = (
        db.query(
            stats.c.session_id,
            stats.c.first_time,
            stats.c.last_time,
            stats.c.msg_count,
            ChatLog.content.label("last_message"),
        )
        .join(latest_id, ChatLog.id == latest_id.c.max_id)
        .join(stats, ChatLog.session_id == stats.c.session_id)
        .order_by(stats.c.last_time.desc())
    )

    total = q.count()
    rows = q.offset((page - 1) * size).limit(size).all()

    records = [
        SessionSummary(
            session_id=row.session_id,
            first_message=(row.last_message or "")[:30],
            message_count=row.msg_count,
            create_time=row.first_time,
            last_time=row.last_time,
        )
        for row in rows
    ]
    return Result.ok(PageResult(total=total, records=records))


@router.post("/feedback", summary="对话反馈", response_model=Result[None])
async def submit_feedback(body: ChatFeedbackRequest, db: Session = Depends(get_db)):
    logs = (
        db.query(ChatLog)
        .filter(ChatLog.session_id == body.session_id)
        .order_by(ChatLog.create_time.asc())
        .offset(body.message_index)
        .limit(1)
        .all()
    )
    if logs:
        logs[0].feedback = body.feedback
        db.commit()
    return Result.ok(msg="反馈成功")


def _save_chat_log(
    db: Session,
    customer_id: int | None,
    session_id: str,
    role: str,
    content: str,
    intent: str = "",
) -> None:
    log = ChatLog(
        customer_id=customer_id,
        session_id=session_id,
        role=role,
        content=content,
        intent=intent if role == "assistant" else "",
    )
    db.add(log)
    db.commit()


def _save_chat_log_sync(
    customer_id: int | None,
    session_id: str,
    role: str,
    content: str,
    intent: str = "",
) -> None:
    """同步保存对话日志（用于流式接口，因为流式生成器中不能使用 Depends）。"""
    from models.database import SessionLocal
    db = SessionLocal()
    try:
        log = ChatLog(
            customer_id=customer_id,
            session_id=session_id,
            role=role,
            content=content,
            intent=intent if role == "assistant" else "",
        )
        db.add(log)
        db.commit()
    except Exception:
        logger.exception("保存对话日志失败")
    finally:
        db.close()


async def _resolve_user(request: Request) -> dict:
    """从 request.state 提取用户信息（流式接口不经过 Depends）。"""
    return {
        "user_id": getattr(request.state, "user_id", None),
        "username": getattr(request.state, "username", None),
        "role": getattr(request.state, "role", None),
        "customer_id": getattr(request.state, "customer_id", None),
        "token": getattr(request.state, "token", ""),
    }
