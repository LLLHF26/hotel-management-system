"""SQLite 数据库引擎与会话工厂（高并发改造版）。

对等 Java 后端 Druid 连接池的写并发优化：
1. **WAL 日志模式**：读写并发能力提升（读不阻塞写，多读可并行），写串行但有 busy_timeout 兜底。
2. **busy_timeout**：高并发写竞争时等待而非立即报错（SQLITE_BUSY），对齐 Druid 的等待策略。
3. **synchronous=NORMAL**：WAL 下仍保证崩溃安全，但写入更快。
4. **连接池（QueuePool）**：pool_size + max_overflow 复用连接，避免每次请求新建连接
   （近似 Druid max-active / min-idle）。``check_same_thread=False`` + 跨线程借用连接。
"""

from __future__ import annotations

from sqlalchemy import create_engine, event
from sqlalchemy.orm import DeclarativeBase, sessionmaker
from sqlalchemy.pool import QueuePool

from config import DEBUG, SQLITE_PATH

engine = create_engine(
    f"sqlite:///{SQLITE_PATH}",
    echo=DEBUG,
    connect_args={"check_same_thread": False, "timeout": 30},
    poolclass=QueuePool,
    pool_size=10,          # 常驻连接数（近似 Druid min-idle）
    max_overflow=20,       # 峰值可额外借出连接（近似 Druid max-active - min-idle）
    pool_pre_ping=True,    # 取出连接前探活，避免拿到失效连接
    pool_recycle=1800,     # 30 分钟回收，避开 SQLite 文件锁长期占用
)


@event.listens_for(engine, "connect")
def _set_sqlite_pragma(dbapi_conn, conn_record) -> None:  # noqa: ANN001
    """每个新连接建立时开启 WAL / busy_timeout / synchronous=NORMAL。"""
    cur = dbapi_conn.cursor()
    try:
        cur.execute("PRAGMA journal_mode=WAL;")
        cur.execute("PRAGMA busy_timeout=30000;")
        cur.execute("PRAGMA synchronous=NORMAL;")
        cur.execute("PRAGMA foreign_keys=ON;")
    finally:
        cur.close()


SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass


def init_db() -> None:
    """创建所有表（生产环境建议用 Alembic 迁移）。"""
    import models.knowledge  # noqa: F401  ensure table registered
    import models.chat_log   # noqa: F401
    import models.alert      # noqa: F401
    Base.metadata.create_all(bind=engine)
