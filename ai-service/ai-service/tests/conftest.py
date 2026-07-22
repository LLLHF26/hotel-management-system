"""pytest 配置与共享 fixtures。"""

from __future__ import annotations

from unittest.mock import MagicMock, patch

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from models.database import Base

TEST_PAYLOAD = {
    "userId": 1,
    "username": "admin",
    "role": "ADMIN",
    "customerId": 101,
}


@pytest.fixture
def auth_headers() -> dict[str, str]:
    return {"Authorization": "Bearer test-token"}


@pytest.fixture
def db_session():
    """内存 SQLite 数据库会话，每次测试后自动回滚。"""
    engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
    Base.metadata.create_all(bind=engine)
    Session = sessionmaker(bind=engine)
    session = Session()
    yield session
    session.rollback()
    session.close()
    Base.metadata.drop_all(bind=engine)


@pytest.fixture
def client():
    """带鉴权绕过的 TestClient（mock LLM + Embedding + 向量库 + JWT 解码）。"""
    mock_store = MagicMock()
    mock_store.similarity_search_with_score.return_value = []
    mock_store._collection.count.return_value = 0

    with (
        patch("core.llm.ChatOpenAI", autospec=True),
        patch("core.embeddings.OpenAIEmbeddings", autospec=True),
        patch("middleware.auth_middleware.decode_token", return_value=TEST_PAYLOAD),
        patch("core.vector_store.get_vector_store", return_value=mock_store),
        patch("core.vector_store.similarity_search", return_value=[]),
        patch("core.vector_store.delete_by_ids"),
        patch("core.vector_store.rebuild_from_documents"),
    ):
        from main import app
        app.middleware_stack = app.build_middleware_stack()
        with TestClient(app) as tc:
            yield tc
