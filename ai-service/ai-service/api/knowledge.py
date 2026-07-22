"""知识库管理接口 —— 文件上传模式（PDF / DOCX / TXT）。"""

from __future__ import annotations

import logging
import mimetypes
import shutil
from pathlib import Path

from fastapi import APIRouter, Depends, UploadFile
from fastapi.responses import FileResponse
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
    """上传一个或多个 PDF / DOCX / TXT 文件，解析入库并写入向量库。

    向量库（本地 Chroma）初始化失败时降级为「仅入库、未向量化」，避免整接口 500；
    待向量库恢复后通过「重建向量库」补全向量。
    """
    _require_admin(user)
    _ensure_files_dir()

    # 初始化向量库；本地 Chroma 一般可用，失败时降级为仅入库
    try:
        store = get_vector_store()
    except Exception:
        logger.exception("向量库不可用，上传降级为仅入库（待向量库恢复后重建）")
        store = None

    results: list[KnowledgeUploadResult] = []
    vector_failed = 0

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

        # 覆盖上传 — 先删除旧记录（向量库可用时一并删除旧向量）
        existing = db.query(KnowledgeDocument).filter(
            KnowledgeDocument.filename == file.filename
        ).first()
        if existing:
            if store is not None:
                try:
                    store.delete(ids=_chunk_ids(existing.id, existing.chunk_count))
                except Exception:
                    logger.exception("删除旧向量失败 doc_id=%s", existing.id)
            db.delete(existing)
            db.flush()

        # 先入库（chunk_count 暂置 0，向量写入成功后再更新）
        doc = KnowledgeDocument(
            filename=file.filename,
            file_type=file_type,
            file_size=file_size,
            category=category,
            chunk_count=0,
        )
        db.add(doc)
        db.flush()

        if store is not None:
            try:
                ids = _chunk_ids(doc.id, len(chunks))
                metadatas = [
                    {"doc_id": doc.id, "filename": file.filename, "chunk_index": i}
                    for i in range(len(chunks))
                ]
                store.add_texts(texts=chunks, metadatas=metadatas, ids=ids)
                doc.chunk_count = len(chunks)
                db.commit()
                db.refresh(doc)
                results.append(KnowledgeUploadResult(
                    id=doc.id,
                    filename=doc.filename,
                    file_type=doc.file_type,
                    file_size=doc.file_size,
                    chunk_count=doc.chunk_count,
                ))
            except Exception:
                logger.exception("向量库写入失败 doc_id=%s", doc.id)
                db.rollback()
                if saved_path.exists():
                    saved_path.unlink()
                vector_failed += 1
                continue
        else:
            # 向量库不可用：文件已落盘，标记为未向量化入库
            db.commit()
            db.refresh(doc)
            results.append(KnowledgeUploadResult(
                id=doc.id,
                filename=doc.filename,
                file_type=doc.file_type,
                file_size=doc.file_size,
                chunk_count=0,
            ))

    if results:
        msg = f"成功上传 {len(results)} 个文件"
        if vector_failed:
            msg += f"，{vector_failed} 个因向量库暂不可用未向量化（恢复后请点「重建向量库」）"
        return Result.ok(results, msg=msg)

    return Result.fail("未处理任何文件：请确认格式为 PDF/DOCX/TXT/MD 且内容非空")


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
# Preview / download
# ---------------------------------------------------------------------------

# 已知扩展名与 MIME 类型映射（mimetypes 兜底可能不够准，显式修正）
_MIME_TYPES = {
    ".pdf": "application/pdf",
    ".txt": "text/plain; charset=utf-8",
    ".md": "text/plain; charset=utf-8",
    ".docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ".doc": "application/msword",
}


@router.get("/{doc_id}/preview", summary="预览/下载文档")
async def preview_document(
    doc_id: int,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    doc = _get_or_404(db, doc_id)

    file_path = _FILES_DIR / doc.filename
    if not file_path.exists():
        raise NotFoundError("文件已丢失")

    ext = Path(doc.filename).suffix.lower()
    media_type = _MIME_TYPES.get(ext) or (mimetypes.guess_type(str(file_path))[0] or "application/octet-stream")

    return FileResponse(
        str(file_path),
        media_type=media_type,
        filename=doc.filename,
        content_disposition_type="inline",
    )

def _read_text_with_encoding_fallback(path: Path) -> str:
    """以 UTF-8 优先读取，失败时回退 GBK/GB2312，最后 latin-1 兜底。"""
    raw = path.read_bytes()
    for enc in ("utf-8", "utf-8-sig", "gbk", "gb2312"):
        try:
            return raw.decode(enc)
        except UnicodeDecodeError:
            continue
    return raw.decode("latin-1", errors="replace")


@router.get("/{doc_id}/content", summary="获取文档文本内容（UTF-8）", response_model=Result[str])
async def get_document_content(
    doc_id: int,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    """返回文档 UTF-8 文本，用于前端 Markdown 渲染。自动处理 GBK 等旧编码。"""
    _require_admin(user)
    doc = _get_or_404(db, doc_id)

    file_path = _FILES_DIR / doc.filename
    if not file_path.exists():
        raise NotFoundError("文件已丢失")

    ext = Path(doc.filename).suffix.lower()
    if ext not in {".md", ".txt", ".markdown"}:
        raise ConflictError("仅支持文本类文档（MD/TXT）内容读取")

    try:
        text = _read_text_with_encoding_fallback(file_path)
    except Exception as e:
        logger.exception("读取文档内容失败 doc_id=%s", doc_id)
        raise ConflictError(f"读取文件失败: {e}")

    return Result.ok(text)


def _get_or_404(db: Session, doc_id: int) -> KnowledgeDocument:
    doc = db.get(KnowledgeDocument, doc_id)
    if not doc:
        raise NotFoundError(f"知识库文档 {doc_id} 不存在")
    return doc
