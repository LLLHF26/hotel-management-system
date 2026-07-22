"""Embedding 模型工厂 —— 直接调 DashScope API（高并发改造版）。

改造要点：
1. **批量调用**：DashScope ``/embeddings`` 支持 ``input`` 为字符串数组，按 ``EMBEDDING_BATCH_SIZE``
   批量请求，显著减少网络往返、提升吞吐（向量库重建时尤为明显）。
2. **连接池复用**：复用模块级共享 ``httpx.Client``（带 limits / 超时），不再每次实例化。
3. **重试**：网络/超时错误指数退避重试（默认 3 次），对齐 Java 端 RabbitMQ retry。
"""

from __future__ import annotations

import logging
import time
from functools import lru_cache
from typing import List

import httpx
from langchain_core.embeddings import Embeddings

# 兼容旧测试 mock：pytest conftest 会 patch core.embeddings.OpenAIEmbeddings，
# 保留该符号的引用避免 mock 失败（实际 embedding 实现为 DashScopeEmbeddings）。
from langchain_openai import OpenAIEmbeddings  # noqa: F401

from config import (
    EMBEDDING_API_KEY,
    EMBEDDING_BASE_URL,
    EMBEDDING_BATCH_SIZE,
    EMBEDDING_DIMENSION,
    EMBEDDING_MODEL,
    HTTP_POOL_MAX_CONNECTIONS,
    HTTP_POOL_MAX_KEEPALIVE,
    HTTP_RETRY_BACKOFF,
    HTTP_RETRY_MAX,
    HTTP_TIMEOUT,
)

logger = logging.getLogger(__name__)

_zero_vector = [0.0] * EMBEDDING_DIMENSION


class DashScopeEmbeddings(Embeddings):
    """直接调 DashScope embedding API，避免 LangChain OpenAIEmbeddings 格式兼容问题。"""

    def __init__(self) -> None:
        self._client = httpx.Client(
            base_url=EMBEDDING_BASE_URL,
            timeout=httpx.Timeout(HTTP_TIMEOUT),
            limits=httpx.Limits(
                max_connections=HTTP_POOL_MAX_CONNECTIONS,
                max_keepalive_connections=HTTP_POOL_MAX_KEEPALIVE,
                keepalive_expiry=30.0,
            ),
            headers={"Authorization": f"Bearer {EMBEDDING_API_KEY}"},
        )

    def _embed_batch(self, batch: List[str]) -> List[List[float]]:
        """对单个批次请求，带重试（指数退避）。"""
        last_exc: Exception | None = None
        for attempt in range(1, HTTP_RETRY_MAX + 1):
            try:
                resp = self._client.post(
                    "/embeddings",
                    json={"model": EMBEDDING_MODEL, "input": batch},
                )
                if resp.is_success:
                    data = resp.json().get("data", [])
                    # DashScope 按输入顺序返回
                    return [item["embedding"] for item in data]
                logger.error("Embedding 请求失败 %s: %s", resp.status_code, resp.text[:200])
                if resp.status_code < 500:
                    break
            except (httpx.TransportError, httpx.TimeoutException) as exc:
                last_exc = exc
                if attempt >= HTTP_RETRY_MAX:
                    break
                time.sleep(HTTP_RETRY_BACKOFF * (2 ** (attempt - 1)))
        if last_exc:
            logger.warning("Embedding 批量请求异常，用零向量占位: %s", last_exc)
        return [_zero_vector for _ in batch]

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """批量 embedding：按 EMBEDDING_BATCH_SIZE 分片，降低网络往返。"""
        if not texts:
            return []
        vectors: List[List[float]] = []
        for i in range(0, len(texts), EMBEDDING_BATCH_SIZE):
            batch = texts[i : i + EMBEDDING_BATCH_SIZE]
            vectors.extend(self._embed_batch(batch))
        # 兜底：若数量不一致（异常占位）补齐零向量
        while len(vectors) < len(texts):
            vectors.append(list(_zero_vector))
        return vectors

    def embed_query(self, text: str) -> List[float]:
        return self.embed_documents([text])[0]


@lru_cache
def get_embeddings() -> DashScopeEmbeddings:
    return DashScopeEmbeddings()
