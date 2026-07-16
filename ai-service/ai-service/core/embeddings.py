"""Embedding 模型工厂 —— 直接调 DashScope API，绕过 LangChain 兼容问题。"""

from __future__ import annotations

import logging
from functools import lru_cache
from typing import List

import httpx
from langchain_core.embeddings import Embeddings

from config import EMBEDDING_API_KEY, EMBEDDING_BASE_URL, EMBEDDING_DIMENSION, EMBEDDING_MODEL

logger = logging.getLogger(__name__)


class DashScopeEmbeddings(Embeddings):
    """直接调 DashScope embedding API，避免 LangChain OpenAIEmbeddings 格式兼容问题。"""

    def __init__(self) -> None:
        self._client = httpx.Client(
            base_url=EMBEDDING_BASE_URL,
            timeout=30.0,
            headers={"Authorization": f"Bearer {EMBEDDING_API_KEY}"},
        )

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        vectors: List[List[float]] = []
        for text in texts:
            try:
                resp = self._client.post(
                    "/embeddings",
                    json={"model": EMBEDDING_MODEL, "input": text},
                )
                data = resp.json()
                if resp.is_success:
                    vectors.append(data["data"][0]["embedding"])
                else:
                    logger.error("Embedding 请求失败: %s", data)
                    vectors.append([0.0] * EMBEDDING_DIMENSION)
            except Exception:
                logger.exception("Embedding 请求异常，用零向量占位")
                vectors.append([0.0] * EMBEDDING_DIMENSION)
        return vectors

    def embed_query(self, text: str) -> List[float]:
        return self.embed_documents([text])[0]


@lru_cache
def get_embeddings() -> DashScopeEmbeddings:
    return DashScopeEmbeddings()
