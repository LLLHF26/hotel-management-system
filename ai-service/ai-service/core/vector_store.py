"""Chroma 向量库操作封装。"""

from __future__ import annotations

from functools import lru_cache

from langchain_chroma import Chroma

from core.embeddings import get_embeddings
from config import CHROMA_PERSIST_PATH

_COLLECTION_NAME = "ai_knowledge"


@lru_cache
def get_vector_store() -> Chroma:
    return Chroma(
        collection_name=_COLLECTION_NAME,
        embedding_function=get_embeddings(),
        persist_directory=CHROMA_PERSIST_PATH,
    )


def rebuild_from_documents(docs: list[tuple[str, dict]]) -> None:
    """用 (text, metadata) 列表全量重建向量库。

    先清空集合中所有文档，再批量写入新文档。
    """
    store = get_vector_store()
    try:
        existing = store.get()
        if existing.get("ids"):
            store.delete(ids=existing["ids"])
    except Exception:
        pass

    if not docs:
        return

    texts, metadatas = [], []
    for text, meta in docs:
        texts.append(text)
        metadatas.append(meta)

    store.add_texts(texts=texts, metadatas=metadatas)


def delete_by_ids(ids: list[str]) -> None:
    store = get_vector_store()
    store.delete(ids=ids)


def similarity_search(query: str, k: int = 5) -> list[dict]:
    """语义相似度检索，返回 Top-K 结果。"""
    store = get_vector_store()
    results = store.similarity_search_with_score(query, k=k)
    return [
        {**doc.metadata, "content": doc.page_content, "score": round(score, 4)}
        for doc, score in results
    ]
