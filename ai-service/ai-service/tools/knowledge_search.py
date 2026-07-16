"""LangChain Tool —— 知识库语义检索。"""

from __future__ import annotations

from langchain.tools import tool

from core.vector_store import similarity_search


@tool
def search_knowledge(query: str) -> str:
    """在酒店知识库中语义检索相关信息。适用于：酒店政策、设施、周边、入住规则等问题。

    Args:
        query: 用户问题的语义搜索查询词
    """
    results = similarity_search(query, k=5)
    if not results:
        return "未找到相关知识。"
    lines = []
    for i, item in enumerate(results, 1):
        lines.append(f"[{i}] {item.get('content', '')} （相关度: {item.get('score', 0)}）")
    return "\n".join(lines)
