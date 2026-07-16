"""大模型客户端工厂。"""

from __future__ import annotations

from functools import lru_cache

from langchain_openai import ChatOpenAI

from config import LLM_API_KEY, LLM_BASE_URL, LLM_MAX_TOKENS, LLM_MODEL, LLM_PROVIDER, LLM_TEMPERATURE


def _make_llm(*, streaming: bool) -> ChatOpenAI:
    return ChatOpenAI(
        model=LLM_MODEL,
        api_key=LLM_API_KEY,
        base_url=LLM_BASE_URL,
        temperature=LLM_TEMPERATURE,
        max_tokens=LLM_MAX_TOKENS,
        streaming=streaming,
    )


@lru_cache
def get_llm() -> ChatOpenAI:
    """非流式 LLM，用于推荐、同步对话等场景。"""
    return _make_llm(streaming=False)


@lru_cache
def get_streaming_llm() -> ChatOpenAI:
    """流式 LLM，用于 SSE 实时对话。"""
    return _make_llm(streaming=True)


@lru_cache
def get_llm_provider() -> str:
    return LLM_PROVIDER
