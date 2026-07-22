"""Chroma 向量库操作封装（本地持久化，无需启动外部服务）。

使用 ``chromadb.PersistentClient``，向量数据直接落盘到 ``CHROMA_PERSIST_PATH``，
进程内读写，**不依赖任何外部向量库服务**（如 Milvus）。对外暴露与原 Milvus 版
完全一致的接口，下游调用方（api/knowledge.py、api/chat.py、tools/knowledge_search.py、
api/system.py）无需任何改动即可无缝切换：

- ``add_texts(texts, metadatas, ids)``      写入文本块
- ``delete(ids=...)`` / ``delete(expr=...)`` 按主键删除
- ``get()``                                  -> ``{"ids": [...]}``（重建时用，分页避免上限）
- ``similarity_search_with_score(query, k)`` -> ``[(Document, score), ...]``
- ``count()``                                集合实体数

高并发改造延续：

- ``async_similarity_search`` 通过 ``asyncio.to_thread`` 将 Chroma 同步 IO 移出事件循环，
  避免阻塞其它请求；
- 检索结果写入缓存（``CACHE_RAG_TTL``），相同问题命中缓存，显著降低重复计算压力。
"""

from __future__ import annotations

import asyncio
import hashlib
import logging
import uuid
from functools import lru_cache
from typing import List

from langchain_core.documents import Document
import chromadb

from core import cache
from core.embeddings import get_embeddings
from config import (
    CACHE_RAG_TTL,
    CHROMA_COLLECTION,
    CHROMA_PERSIST_PATH,
    EMBEDDING_DIMENSION,
)

logger = logging.getLogger(__name__)


class ChromaVectorStore:
    """基于 ``chromadb.PersistentClient`` 的本地向量库封装（无需外部服务）。"""

    def _recreate_collection(self) -> None:
        """删除并重建集合（维度变更 / 数据损坏时使用）。"""
        try:
            self._client.delete_collection(CHROMA_COLLECTION)
        except Exception:
            logger.debug("Chroma 删除集合失败或不存在，忽略")
        self._collection = self._client.get_or_create_collection(
            name=CHROMA_COLLECTION,
            metadata={"hnsw:space": "cosine"},  # 文本嵌入用余弦相似度
        )

    def _get_or_create_collection(self):
        """获取集合，若已存在向量的维度与当前配置不符（如 embedding 模型更换），自动重建。

        通过一次零向量探测查询判断：若集合已有向量但维度与当前 ``EMBEDDING_DIMENSION``
        不一致，Chroma 会抛出维度不匹配错误，此时删除并重建集合。
        """
        col = self._client.get_or_create_collection(
            name=CHROMA_COLLECTION,
            metadata={"hnsw:space": "cosine"},
        )
        self._collection = col
        try:
            if col.count() > 0:
                col.query(query_embeddings=[[0.0] * EMBEDDING_DIMENSION], n_results=1)
        except Exception as e:
            if "dimension" in str(e).lower():
                logger.warning(
                    "已有向量维度与当前 %d 不一致，重建集合以适配新模型",
                    EMBEDDING_DIMENSION,
                )
                self._recreate_collection()
            else:
                logger.debug("集合维度守卫探测跳过: %s", e)
        return self._collection

    def __init__(self) -> None:
        self._ef = get_embeddings()
        self._client = chromadb.PersistentClient(path=CHROMA_PERSIST_PATH)
        self._collection = self._get_or_create_collection()

    # ---- 写入 ----
    def add_texts(
        self,
        texts: List[str],
        metadatas: List[dict] | None = None,
        ids: List[str] | None = None,
    ) -> List[str]:
        if not texts:
            return []
        metadatas = metadatas or [{} for _ in texts]
        ids = ids or [str(uuid.uuid4()) for _ in texts]

        vectors = self._ef.embed_documents(texts)
        # 兜底：embedding 异常时数量可能不一致，补齐零向量
        while len(vectors) < len(texts):
            vectors.append([0.0] * EMBEDDING_DIMENSION)

        try:
            self._collection.add(
                ids=ids,
                documents=texts,
                embeddings=vectors,
                metadatas=metadatas,
            )
        except chromadb.errors.InvalidArgumentError as e:
            # 维度不匹配（如更换 embedding 模型）：重建集合后重试一次
            if "dimension" in str(e).lower():
                logger.warning("向量维度不匹配，重建集合后重试: %s", e)
                self._recreate_collection()
                self._collection.add(
                    ids=ids,
                    documents=texts,
                    embeddings=vectors,
                    metadatas=metadatas,
                )
            else:
                raise
        return ids

    # ---- 删除 ----
    def delete(self, ids: List[str] | None = None, expr: str | None = None) -> None:
        if ids is not None:
            try:
                self._collection.delete(ids=ids)
            except Exception:
                logger.debug("Chroma 删除部分 id 可能不存在，已忽略: %s", ids)
        elif expr is not None:
            # Chroma 不支持裸表达式删除，按 where 透传（当前上游未使用）
            try:
                self._collection.delete(where=expr)
            except Exception:
                logger.debug("Chroma 表达式删除失败，已忽略: %s", expr)

    # ---- 获取全部主键（重建用，分页避免单次上限） ----
    def get(self, limit: int = 10000) -> dict:
        ids: List[str] = []
        offset = 0
        while True:
            res = self._collection.get(limit=limit, offset=offset)
            batch = res.get("ids") or []
            if not batch:
                break
            ids.extend(batch)
            if len(batch) < limit:
                break
            offset += len(batch)
        return {"ids": ids}

    # ---- 检索 ----
    def similarity_search_with_score(self, query: str, k: int = 5):
        qv = self._ef.embed_query(query)
        try:
            res = self._collection.query(query_embeddings=[qv], n_results=k)
        except chromadb.errors.InvalidArgumentError as e:
            # 维度不匹配：重建空集合后返回空结果（聊天不崩溃，待重建向量库补全）
            if "dimension" in str(e).lower():
                logger.warning("检索维度不匹配，重建集合: %s", e)
                self._recreate_collection()
                return []
            logger.exception("Chroma 检索失败")
            return []
        except Exception:
            logger.exception("Chroma 检索失败")
            return []

        batch_texts = (res.get("documents") or [[]])[0]
        batch_metas = (res.get("metadatas") or [[]])[0]
        batch_dist = (res.get("distances") or [[]])[0]

        results = []
        for text, meta, dist in zip(batch_texts, batch_metas, batch_dist):
            doc = Document(page_content=text, metadata=dict(meta or {}))
            results.append((doc, float(dist)))
        return results

    # ---- 计数 ----
    def count(self) -> int:
        try:
            return self._collection.count()
        except Exception:
            return 0


# =============================================================================
# 模块级函数（保持与原 Milvus 版完全相同的签名，下游调用方无需改动）
# =============================================================================

@lru_cache
def get_vector_store() -> ChromaVectorStore:
    return ChromaVectorStore()


def delete_by_ids(ids: list[str]) -> None:
    store = get_vector_store()
    store.delete(ids=ids)


def similarity_search(query: str, k: int = 5) -> list[dict]:
    """语义相似度检索，返回 Top-K 结果（同步版，供 LangChain Tool 使用）。"""
    store = get_vector_store()
    results = store.similarity_search_with_score(query, k=k)
    return [
        {**doc.metadata, "content": doc.page_content, "score": round(score, 4)}
        for doc, score in results
    ]


def rebuild_from_documents(docs: list[tuple[str, dict]]) -> None:
    """用 (text, metadata) 列表全量重建向量库。

    先删除并重建集合，再批量写入新文档。
    """
    store = get_vector_store()
    try:
        store._client.delete_collection(store._collection.name)
    except Exception:
        logger.debug("Chroma 删除集合失败或不存在，忽略")

    store._collection = store._client.get_or_create_collection(
        name=CHROMA_COLLECTION,
        metadata={"hnsw:space": "cosine"},
    )

    if not docs:
        return

    texts, metadatas = [], []
    for text, meta in docs:
        texts.append(text)
        metadatas.append(meta)

    store.add_texts(texts=texts, metadatas=metadatas)


def _search_key(query: str, k: int) -> str:
    digest = hashlib.sha256(f"{query}|k={k}".encode("utf-8")).hexdigest()[:16]
    return cache.cache_key("rag", digest)


async def async_similarity_search(query: str, k: int = 5) -> list[dict]:
    """异步语义检索（高并发友好）。

    - 通过 ``asyncio.to_thread`` 将 Chroma 同步 IO 移出事件循环，避免阻塞其它请求；
    - 检索结果写入缓存（``CACHE_RAG_TTL``），相同问题命中缓存，显著降低重复计算压力。
    """
    key = _search_key(query, k)

    async def _produce() -> list[dict]:
        return await asyncio.to_thread(similarity_search, query, k)

    return await cache.cached_call(key, CACHE_RAG_TTL, _produce)
