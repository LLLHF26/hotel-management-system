"""文件解析与文本分块 —— 支持 PDF / DOCX / TXT 上传。"""

from __future__ import annotations

import io
import logging
from pathlib import Path

from langchain.text_splitter import RecursiveCharacterTextSplitter

logger = logging.getLogger(__name__)

ALLOWED_EXTENSIONS = {".pdf", ".docx", ".txt", ".md"}

_splitter = RecursiveCharacterTextSplitter(
    chunk_size=500,
    chunk_overlap=50,
    separators=["\n\n", "\n", "。", "！", "？", "；", ".", "!", "?", ";", " "],
)


def parse_file(file_path: str | Path, filename: str) -> list[str]:
    """解析上传文件并返回文本分块列表。"""
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise ValueError(f"不支持的文件类型: {ext}，仅支持 pdf / docx / txt")

    raw_text = _extract_text(file_path, ext)
    if not raw_text.strip():
        raise ValueError("文件内容为空，无法提取文本")

    chunks = _splitter.split_text(raw_text)
    return [c.strip() for c in chunks if c.strip()]


def _extract_text(file_path: str | Path, ext: str) -> str:
    if ext in (".txt", ".md"):
        return _parse_txt(file_path)
    if ext == ".pdf":
        return _parse_pdf(file_path)
    if ext == ".docx":
        return _parse_docx(file_path)
    return ""


def _parse_txt(file_path: str | Path) -> str:
    with open(file_path, encoding="utf-8", errors="replace") as f:
        return f.read()


def _parse_pdf(file_path: str | Path) -> str:
    from pypdf import PdfReader

    reader = PdfReader(str(file_path))
    parts: list[str] = []
    for page in reader.pages:
        text = page.extract_text()
        if text:
            parts.append(text)
    return "\n".join(parts)


def _parse_docx(file_path: str | Path) -> str:
    from docx import Document

    doc = Document(str(file_path))
    parts: list[str] = []
    for para in doc.paragraphs:
        if para.text.strip():
            parts.append(para.text)
    return "\n".join(parts)
