"""SQLite 数据库引擎与会话工厂。"""

from __future__ import annotations

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from config import DEBUG, SQLITE_PATH

engine = create_engine(
    f"sqlite:///{SQLITE_PATH}",
    echo=DEBUG,
    connect_args={"check_same_thread": False},
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass


def init_db() -> None:
    """创建所有表（生产环境建议用 Alembic 迁移）。"""
    import models.knowledge  # noqa: F401  ensure table registered
    import models.chat_log   # noqa: F401
    import models.alert      # noqa: F401
    Base.metadata.create_all(bind=engine)
