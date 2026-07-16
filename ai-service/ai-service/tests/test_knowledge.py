"""知识库接口测试 —— 文件上传模式。"""

from __future__ import annotations

import io
from pathlib import Path


def _make_txt_file(content: str, filename: str = "test_hotel_info.txt") -> tuple:
    """创建测试用 TXT 文件。"""
    return ("files", (filename, io.BytesIO(content.encode("utf-8")), "text/plain"))


def test_upload_and_list(client, auth_headers):
    content = (
        "酒店游泳池位于B1层，开放时间06:00-22:00，住客免费使用。"
        "酒店提供免费WiFi，覆盖所有客房和公共区域。"
        "退房时间为中午12:00，延迟退房需加收半天房费。"
        "酒店提供免费停车场，位于地下B2层。"
        "自助早餐位于2层餐厅，开放时间07:00-10:00，每位68元。"
    )
    files = [_make_txt_file(content)]
    resp = client.post(
        "/api/ai/knowledge/upload",
        files=files,
        params={"category": "设施咨询"},
        headers=auth_headers,
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["code"] == 200
    assert len(data["data"]) >= 1
    upload_result = data["data"][0]
    assert upload_result["filename"] == "test_hotel_info.txt"
    assert upload_result["file_type"] == "txt"
    assert upload_result["chunk_count"] > 0

    # list should include it
    resp = client.get("/api/ai/knowledge/list", headers=auth_headers)
    assert resp.status_code == 200
    list_data = resp.json()
    assert list_data["data"]["total"] >= 1

    # cleanup
    doc_id = upload_result["id"]
    client.delete(f"/api/ai/knowledge/{doc_id}", headers=auth_headers)


def test_upload_duplicate_rejected(client, auth_headers):
    content = "酒店前台24小时营业。"
    files = [_make_txt_file(content, "test_dup.txt")]

    resp1 = client.post(
        "/api/ai/knowledge/upload",
        files=files,
        params={"category": "政策咨询"},
        headers=auth_headers,
    )
    assert resp1.status_code == 200
    doc_id = resp1.json()["data"][0]["id"]

    # upload again with same filename
    resp2 = client.post(
        "/api/ai/knowledge/upload",
        files=files,
        params={"category": "政策咨询"},
        headers=auth_headers,
    )
    assert resp2.status_code == 200
    # second upload should have id=0 (skipped)
    assert resp2.json()["data"][0]["id"] == 0

    client.delete(f"/api/ai/knowledge/{doc_id}", headers=auth_headers)


def test_update_category(client, auth_headers):
    content = "测试更新分类的内容。"
    files = [_make_txt_file(content, "test_update_cat.txt")]
    resp = client.post("/api/ai/knowledge/upload", files=files, params={"category": "通用"}, headers=auth_headers)
    assert resp.status_code == 200
    doc_id = resp.json()["data"][0]["id"]

    resp = client.put(
        f"/api/ai/knowledge/{doc_id}",
        json={"category": "政策咨询"},
        headers=auth_headers,
    )
    assert resp.status_code == 200

    # verify
    resp = client.get("/api/ai/knowledge/list", params={"category": "政策咨询"}, headers=auth_headers)
    records = resp.json()["data"]["records"]
    assert any(r["id"] == doc_id for r in records)

    client.delete(f"/api/ai/knowledge/{doc_id}", headers=auth_headers)


def test_toggle_status(client, auth_headers):
    content = "测试启用禁用切换的文本内容。"
    files = [_make_txt_file(content, "test_toggle.txt")]
    resp = client.post("/api/ai/knowledge/upload", files=files, params={"category": "通用"}, headers=auth_headers)
    assert resp.status_code == 200
    doc_id = resp.json()["data"][0]["id"]

    # disable
    resp = client.put(f"/api/ai/knowledge/{doc_id}/status", json={"is_enabled": False}, headers=auth_headers)
    assert resp.status_code == 200
    assert "已禁用" in resp.json()["msg"]

    # enable
    resp = client.put(f"/api/ai/knowledge/{doc_id}/status", json={"is_enabled": True}, headers=auth_headers)
    assert resp.status_code == 200
    assert "已启用" in resp.json()["msg"]

    client.delete(f"/api/ai/knowledge/{doc_id}", headers=auth_headers)


def test_delete(client, auth_headers):
    content = "待删除的测试文档内容。"
    files = [_make_txt_file(content, "test_delete.txt")]
    resp = client.post("/api/ai/knowledge/upload", files=files, params={"category": "通用"}, headers=auth_headers)
    assert resp.status_code == 200
    doc_id = resp.json()["data"][0]["id"]

    resp = client.delete(f"/api/ai/knowledge/{doc_id}", headers=auth_headers)
    assert resp.status_code == 200

    # verify gone
    resp = client.get("/api/ai/knowledge/list", headers=auth_headers)
    records = resp.json()["data"]["records"]
    assert not any(r["id"] == doc_id for r in records)


def test_filter(client, auth_headers):
    resp = client.get("/api/ai/knowledge/list", params={"file_type": "txt"}, headers=auth_headers)
    assert resp.status_code == 200


def test_rebuild_vector(client, auth_headers):
    resp = client.post("/api/ai/knowledge/rebuild-vector", headers=auth_headers)
    assert resp.status_code == 200
    assert resp.json()["code"] == 200


def test_not_found(client, auth_headers):
    resp = client.put("/api/ai/knowledge/99999", json={"category": "x"}, headers=auth_headers)
    assert resp.status_code == 404

    resp = client.delete("/api/ai/knowledge/99999", headers=auth_headers)
    assert resp.status_code == 404

    resp = client.put("/api/ai/knowledge/99999/status", json={"is_enabled": True}, headers=auth_headers)
    assert resp.status_code == 404
