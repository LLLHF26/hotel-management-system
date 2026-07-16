"""系统接口冒烟测试。"""

from __future__ import annotations


def test_health(client):
    resp = client.get("/api/ai/health")
    assert resp.status_code == 200
    data = resp.json()
    assert data["code"] == 200
    assert data["data"]["status"] in ("UP", "DEGRADED")
    assert "llm_status" in data["data"]
    assert "chroma_status" in data["data"]
    assert "sqlite_status" in data["data"]


def test_info(client):
    resp = client.get("/api/ai/info")
    assert resp.status_code == 200
    data = resp.json()
    assert data["data"]["service"] == "ai-service"
    assert data["data"]["version"] == "1.0.0"
