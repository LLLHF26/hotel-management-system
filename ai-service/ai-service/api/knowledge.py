"""知识库管理接口 —— 文件上传模式（PDF / DOCX / TXT）。"""

from __future__ import annotations

import logging
import shutil
from pathlib import Path

from fastapi import APIRouter, Depends, UploadFile
from sqlalchemy.orm import Session

from api.deps import get_current_user, get_db
from core.auth import ROLE_ADMIN, ForbiddenError
from core.exceptions import ConflictError, NotFoundError
from core.file_parser import parse_file
from core.vector_store import get_vector_store
from models.knowledge import KnowledgeDocument
from schemas.common import PageResult, Result
from schemas.knowledge import (
    KnowledgeDocumentUpdate,
    KnowledgeDocumentVO,
    KnowledgeStatusUpdate,
    KnowledgeUploadResult,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai/knowledge", tags=["知识库管理"])

_FILES_DIR = Path(__file__).resolve().parent.parent / "data" / "knowledge_files"


def _ensure_files_dir() -> None:
    _FILES_DIR.mkdir(parents=True, exist_ok=True)


def _require_admin(user: dict) -> None:
    if user.get("role") != ROLE_ADMIN:
        raise ForbiddenError("仅管理员可操作知识库")


def _chunk_ids(doc_id: int, chunk_count: int) -> list[str]:
    return [f"doc_{doc_id}_chunk_{i}" for i in range(chunk_count)]


# ---------------------------------------------------------------------------
# Upload
# ---------------------------------------------------------------------------

@router.post("/upload", summary="上传知识库文件", response_model=Result[list[KnowledgeUploadResult]])
async def upload_files(
    files: list[UploadFile],
    category: str = "通用",
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    """上传一个或多个 PDF / DOCX / TXT 文件，解析入库并写入向量库。"""
    _require_admin(user)
    _ensure_files_dir()

    store = get_vector_store()
    results: list[KnowledgeUploadResult] = []

    for file in files:
        if not file.filename:
            continue

        ext = Path(file.filename).suffix.lower()
        if ext not in {".pdf", ".docx", ".txt", ".md"}:
            continue

        # 解析文件
        tmp_path = _FILES_DIR / f"_tmp_{file.filename}"
        try:
            content = await file.read()
            tmp_path.write_bytes(content)

            chunks = parse_file(str(tmp_path), file.filename)
            if not chunks:
                continue

            file_size = len(content)
            file_type = ext.lstrip(".")

            saved_path = _FILES_DIR / file.filename
            shutil.move(str(tmp_path), str(saved_path))
        except ValueError as e:
            logger.warning("文件解析失败 %s: %s", file.filename, e)
            if tmp_path.exists():
                tmp_path.unlink()
            continue
        except Exception:
            logger.exception("文件处理异常 %s", file.filename)
            if tmp_path.exists():
                tmp_path.unlink()
            continue

        # 检查重名 — 存在则先删除旧的向量和 DB 记录，实现覆盖上传
        existing = db.query(KnowledgeDocument).filter(
            KnowledgeDocument.filename == file.filename
        ).first()
        if existing:
            try:
                store.delete(ids=_chunk_ids(existing.id, existing.chunk_count))
            except Exception:
                logger.exception("删除旧向量失败 doc_id=%s", existing.id)
            db.delete(existing)
            db.flush()

        # 写入 DB
        doc = KnowledgeDocument(
            filename=file.filename,
            file_type=file_type,
            file_size=file_size,
            category=category,
            chunk_count=len(chunks),
        )
        db.add(doc)
        db.flush()

        # 写入 Chroma
        try:
            ids = _chunk_ids(doc.id, len(chunks))
            metadatas = [
                {"doc_id": doc.id, "filename": file.filename, "chunk_index": i}
                for i in range(len(chunks))
            ]
            store.add_texts(texts=chunks, metadatas=metadatas, ids=ids)
        except Exception:
            logger.exception("Chroma 写入失败 doc_id=%s", doc.id)
            db.rollback()
            if saved_path.exists():
                saved_path.unlink()
            continue

        db.commit()
        db.refresh(doc)

        results.append(KnowledgeUploadResult(
            id=doc.id,
            filename=doc.filename,
            file_type=doc.file_type,
            file_size=doc.file_size,
            chunk_count=doc.chunk_count,
        ))

    return Result.ok(results, msg=f"成功上传 {len(results)} 个文件")


# ---------------------------------------------------------------------------
# List
# ---------------------------------------------------------------------------

@router.get("/list", summary="知识库文档列表", response_model=Result[PageResult[KnowledgeDocumentVO]])
async def list_documents(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
    page: int = 1,
    size: int = 10,
    category: str | None = None,
    keyword: str | None = None,
    file_type: str | None = None,
    is_enabled: bool | None = None,
):
    _require_admin(user)
    q = db.query(KnowledgeDocument)
    if category:
        q = q.filter(KnowledgeDocument.category == category)
    if keyword:
        q = q.filter(KnowledgeDocument.filename.contains(keyword))
    if file_type:
        q = q.filter(KnowledgeDocument.file_type == file_type)
    if is_enabled is not None:
        q = q.filter(KnowledgeDocument.is_enabled == is_enabled)

    total = q.count()
    records = q.order_by(KnowledgeDocument.create_time.desc()).offset(
        (page - 1) * size
    ).limit(size).all()

    return Result.ok(PageResult(
        total=total,
        records=[KnowledgeDocumentVO.model_validate(r) for r in records],
    ))


# ---------------------------------------------------------------------------
# Update category
# ---------------------------------------------------------------------------

@router.put("/{doc_id}", summary="修改文档分类", response_model=Result[None])
async def update_document(
    doc_id: int,
    body: KnowledgeDocumentUpdate,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    doc = _get_or_404(db, doc_id)
    if body.category is not None:
        doc.category = body.category
    db.commit()
    return Result.ok(msg="修改成功")


# ---------------------------------------------------------------------------
# Delete
# ---------------------------------------------------------------------------

@router.delete("/{doc_id}", summary="删除知识库文档", response_model=Result[None])
async def delete_document(
    doc_id: int,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    doc = _get_or_404(db, doc_id)

    # 删除向量
    try:
        store = get_vector_store()
        store.delete(ids=_chunk_ids(doc.id, doc.chunk_count))
        
    except Exception:
        logger.exception("删除 Chroma 向量失败 doc_id=%s", doc_id)

    # 删除存储文件
    file_path = _FILES_DIR / doc.filename
    if file_path.exists():
        file_path.unlink()

    # 删除 DB 记录
    db.delete(doc)
    db.commit()
    return Result.ok(msg="删除成功")


# ---------------------------------------------------------------------------
# Enable / Disable
# ---------------------------------------------------------------------------

@router.put("/{doc_id}/status", summary="启用/禁用文档", response_model=Result[None])
async def toggle_document(
    doc_id: int,
    body: KnowledgeStatusUpdate,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    doc = _get_or_404(db, doc_id)
    if doc.is_enabled == body.is_enabled:
        return Result.ok(msg="状态未变更")

    store = get_vector_store()
    if body.is_enabled:
        # 重新解析文件并写入向量
        file_path = _FILES_DIR / doc.filename
        if not file_path.exists():
            raise NotFoundError("文件已丢失，无法启用")
        try:
            chunks = parse_file(str(file_path), doc.filename)
            doc.chunk_count = len(chunks)
            store.add_texts(
                texts=chunks,
                metadatas=[
                    {"doc_id": doc.id, "filename": doc.filename, "chunk_index": i}
                    for i in range(len(chunks))
                ],
                ids=_chunk_ids(doc.id, len(chunks)),
            )
            
        except Exception:
            logger.exception("重新启用文档失败 doc_id=%s", doc_id)
            raise
    else:
        try:
            store.delete(ids=_chunk_ids(doc.id, doc.chunk_count))
            
        except Exception:
            logger.exception("禁用删除向量失败 doc_id=%s", doc_id)

    doc.is_enabled = body.is_enabled
    db.commit()
    return Result.ok(msg="已启用" if body.is_enabled else "已禁用")


# ---------------------------------------------------------------------------
# Rebuild vector store
# ---------------------------------------------------------------------------

@router.post("/rebuild-vector", summary="向量库重建", response_model=Result[dict])
async def rebuild_vector(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    _ensure_files_dir()

    docs = db.query(KnowledgeDocument).filter(KnowledgeDocument.is_enabled == True).all()

    store = get_vector_store()
    try:
        existing = store.get()
        if existing.get("ids"):
            store.delete(ids=existing["ids"])
    except Exception:
        logger.exception("清空向量库失败")

    total_chunks = 0
    for doc in docs:
        file_path = _FILES_DIR / doc.filename
        if not file_path.exists():
            logger.warning("文件丢失，跳过 doc_id=%s filename=%s", doc.id, doc.filename)
            continue
        try:
            chunks = parse_file(str(file_path), doc.filename)
            doc.chunk_count = len(chunks)
            store.add_texts(
                texts=chunks,
                metadatas=[
                    {"doc_id": doc.id, "filename": doc.filename, "chunk_index": i}
                    for i in range(len(chunks))
                ],
                ids=_chunk_ids(doc.id, len(chunks)),
            )
            total_chunks += len(chunks)
        except Exception:
            logger.exception("重建失败 doc_id=%s", doc.id)

    
    db.commit()
    return Result.ok({"count": total_chunks}, msg=f"向量库重建完成，共同步 {total_chunks} 个文本块")


# ---------------------------------------------------------------------------
# Categories
# ---------------------------------------------------------------------------

@router.get("/categories", summary="获取所有分类", response_model=Result[list[str]])
async def get_categories(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    cats = (
        db.query(KnowledgeDocument.category)
        .distinct()
        .order_by(KnowledgeDocument.category)
        .all()
    )
    return Result.ok([c[0] for c in cats if c[0]])


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _get_or_404(db: Session, doc_id: int) -> KnowledgeDocument:
    doc = db.get(KnowledgeDocument, doc_id)
    if not doc:
        raise NotFoundError(f"知识库文档 {doc_id} 不存在")
    return doc
