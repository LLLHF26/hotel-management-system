# ai-service 接口文档

> Python FastAPI + LangChain 智能服务，独立于 Java 微服务体系，通过 HTTP REST 提供 AI 能力。调用 Java 后端接口时通过 `httpx` 走 gateway-service（或特定接口直连微服务）。

---

## 一、服务说明

| 项目 | 说明 |
|------|------|
| 框架 | FastAPI 0.115.6 + LangChain 0.3.13 + LangGraph 0.2.58 |
| 语言 | Python 3.13+ |
| 包管理 | uv |
| 端口 | `8000` |
| 存储 | SQLite（知识库元数据 + 对话日志 + 告警）+ Chroma（向量检索） |
| 大模型 | 通义千问（qwen-plus），通过 OpenAI 兼容 API 接入；Embedding 使用 text-embedding-v3 |
| 鉴权 | 自定义 ASGI 中间件解析 `Authorization: Bearer {JWT}`，白名单路径（/api/ai/health、/api/ai/info、/docs、/openapi.json）免鉴权 |
| 调用 Java 服务 | 同步工具（LangChain Tool）使用 `GatewayClient`→gateway-service；告警检查使用 `httpx` 直连微服务端口 |

### 统一返回格式

```json
{
  "code": 200,
  "msg": "成功",
  "data": {}
}
```

### 统一错误码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突（重名等） |
| 500 | 服务器内部错误 / 大模型调用失败 |

---

## 二、智能客服（核心接口）

### 2.1 智能对话（流式 SSE）

```
POST /api/ai/chat
Authorization: Bearer {JWT}
Content-Type: application/json
Accept: text/event-stream
```

**请求体：**

```json
{
  "session_id": "sess_abc123",
  "message": "你们酒店有游泳池吗",
  "history": [
    {"role": "user", "content": "你好"},
    {"role": "assistant", "content": "您好！有什么可以帮您的？"}
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| session_id | string | 否 | 会话 ID，首次不传则自动创建 |
| message | string | 是 | 用户输入的问题，1-500 字 |
| history | array | 否 | 历史对话，每条含 role(user/assistant) 和 content |

**响应（SSE 流式）：**

```
event: token
data: {"token": "我们"}

event: token
data: {"token": "酒店"}

...

event: done
data: {"session_id": "sess_abc123", "intent": "客服问答", "tokens": 45}
```

> 流式输出结束后，后端自动将完整对话写入 `chat_log` 表。意图分类基于关键词正则规则，支持四种意图：客服问答、推荐房型、查订单、闲聊。

**错误码：** `400` 消息为空或超长，`500` 大模型调用失败

---

### 2.2 智能对话（同步）

```
POST /api/ai/chat/sync
Authorization: Bearer {JWT}
Content-Type: application/json
```

**请求体：** 同 2.1

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "session_id": "sess_abc123",
    "reply": "您好！我们酒店配有室内恒温游泳池，位于酒店三楼，开放时间为每天 6:00-22:00，住店客人可免费使用。",
    "intent": "客服问答",
    "tokens": 38
  }
}
```

---

### 2.3 获取会话历史

```
GET /api/ai/chat/history/{session_id}
Authorization: Bearer {JWT}
```

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "session_id": "sess_abc123",
    "messages": [
      {"role": "user", "content": "你们酒店有游泳池吗", "time": "2026-05-18T09:00:00"},
      {"role": "assistant", "content": "您好！我们酒店配有室内恒温游泳池...", "intent": "客服问答", "time": "2026-05-18T09:00:05"}
    ]
  }
}
```

---

### 2.4 获取用户会话列表

```
GET /api/ai/chat/sessions?page=1&size=10
Authorization: Bearer {JWT}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 12,
    "records": [
      {
        "session_id": "sess_abc123",
        "first_message": "你好",
        "message_count": 8,
        "create_time": "2026-05-18T09:00:00",
        "last_time": "2026-05-18T09:15:00"
      }
    ]
  }
}
```

---

### 2.5 对话反馈

```
POST /api/ai/chat/feedback
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "session_id": "sess_abc123",
  "message_index": 2,
  "feedback": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| session_id | string | 是 | 会话 ID |
| message_index | int | 是 | 对话消息序号（从 0 开始） |
| feedback | int | 是 | 1=有用 0=无用 |

**响应：** `{ "code": 200, "msg": "反馈成功", "data": null }`

---

## 三、知识库管理（需 ADMIN 角色）

> 知识库采用**文件上传**模式：管理员上传 PDF / DOCX / TXT / Markdown 文件，后端自动解析文本、RecursiveCharacterTextSplitter 分块（chunk_size=500, overlap=50），逐块写入 Chroma 向量数据库。每个文件的 chunks 关联到 `knowledge_document` 表中的记录，支持启用/禁用和覆盖上传。

### 3.1 上传文件

```
POST /api/ai/knowledge/upload?category=通用
Authorization: Bearer {JWT}
Content-Type: multipart/form-data

Form 参数:
  files: (多个文件) PDF / DOCX / TXT / MD 文件
  category: (可选) 分类名称，默认"通用"
```

**响应：**

```json
{
  "code": 200,
  "msg": "成功上传 2 个文件",
  "data": [
    {
      "id": 1,
      "filename": "酒店设施介绍.pdf",
      "file_type": "pdf",
      "file_size": 245760,
      "chunk_count": 12
    },
    {
      "id": 2,
      "filename": "入住政策说明.docx",
      "file_type": "docx",
      "file_size": 102400,
      "chunk_count": 5
    }
  ]
}
```

> 如果上传的文件名已存在，则**覆盖**旧文件（删除旧的向量和 DB 记录，重新写入）。

**错误码：** `403` 非管理员操作

---

### 3.2 文档列表

```
GET /api/ai/knowledge/list?page=1&size=10&category=&keyword=&file_type=&is_enabled=
Authorization: Bearer {JWT}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| category | string | 否 | 按分类筛选 |
| keyword | string | 否 | 按文件名模糊搜索 |
| file_type | string | 否 | 按文件类型筛选：pdf / docx / txt / md |
| is_enabled | bool | 否 | true=启用 false=禁用，不传则全部 |

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 45,
    "records": [
      {
        "id": 1,
        "filename": "酒店设施介绍.pdf",
        "file_type": "pdf",
        "file_size": 245760,
        "category": "设施",
        "chunk_count": 12,
        "is_enabled": true,
        "create_time": "2026-05-01T10:00:00",
        "update_time": "2026-05-10T14:00:00"
      }
    ]
  }
}
```

---

### 3.3 修改文档分类

```
PUT /api/ai/knowledge/{doc_id}
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "category": "政策"
}
```

**响应：** `{ "code": 200, "msg": "修改成功", "data": null }`

**错误码：** `404` 文档不存在

---

### 3.4 启用 / 禁用文档

```
PUT /api/ai/knowledge/{doc_id}/status
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "is_enabled": false
}
```

> **启用**：重新解析存储的文件并写入 Chroma 向量库。**禁用**：从 Chroma 中删除该文档的全部向量，但保留 DB 记录和源文件。

**响应：** `{ "code": 200, "msg": "已禁用", "data": null }`

---

### 3.5 删除文档

```
DELETE /api/ai/knowledge/{doc_id}
Authorization: Bearer {JWT}
```

> 同时删除：Chrom a向量、存储文件、DB 记录。

**响应：** `{ "code": 200, "msg": "删除成功", "data": null }`

---

### 3.6 向量库重建

```
POST /api/ai/knowledge/rebuild-vector
Authorization: Bearer {JWT}
```

> 全量清空 Chroma 向量库，重新解析所有 `is_enabled=true` 的存储文件并写入向量。在知识库文件大量变更或向量库损坏时使用。

**响应：** `{ "code": 200, "msg": "向量库重建完成", "data": { "count": 120 } }`

---

### 3.7 获取所有分类

```
GET /api/ai/knowledge/categories
Authorization: Bearer {JWT}
```

**响应：** `{ "code": 200, "msg": "成功", "data": ["设施", "政策", "周边", "规则"] }`

---

## 四、房型推荐

### 4.1 智能推荐房型

```
POST /api/ai/recommend/room
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "check_in_date": "2026-05-20",
  "check_out_date": "2026-05-22",
  "guests": 2,
  "budget_min": 200,
  "budget_max": 500,
  "prefer": "高楼层、安静"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| check_in_date | string | 是 | 入住日期（YYYY-MM-DD） |
| check_out_date | string | 是 | 退房日期（YYYY-MM-DD） |
| guests | int | 是 | 入住人数 |
| budget_min | float | 否 | 最低预算（元/晚） |
| budget_max | float | 否 | 最高预算（元/晚） |
| prefer | string | 否 | 偏好描述（自然语言） |

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "recommendations": [
      {
        "room_type_id": 3,
        "room_type_name": "豪华大床房",
        "price": 388.00,
        "area": 35,
        "bed_type": "大床",
        "max_guests": 2,
        "available_count": 5,
        "score": 0.92,
        "reason": "价格在预算范围内，大床适合 2 人入住，当前有 5 间空房"
      }
    ],
    "query_summary": "2 人入住 2 晚，预算 200-500 元/晚，偏好高楼层安静"
  }
}
```

> 后端流程：接收请求 → 调用 room-service 查询可用房型列表 → LangChain Prompt → LLM 结合入住人数、日期、预算、偏好进行智能排序 → 返回推荐结果及理由。

**错误码：** `400` 日期不合法，`400` 入住人数 ≤ 0，`500` Java 服务调用失败

---

## 五、订单智能查询

### 5.1 自然语言查订单

```
POST /api/ai/order/query
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "query": "帮我查一下上个月的住宿记录"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 自然语言查询，1-200 字 |

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "parsed_intent": "查询历史订单",
    "parsed_params": {
      "start_date": "2026-04-01",
      "end_date": "2026-04-30",
      "status": "已完成"
    },
    "orders": [...],
    "summary": "您 4 月份共有 2 笔住宿记录，总消费 1234.00 元"
  }
}
```

> 后端流程：接收自然语言查询 → `order_agent`（LangChain Prompt → LLM → 参数提取为 JSON） → 调用 order-service 获取订单数据 → 格式化返回。

**支持的查询场景：** "我的订单"、"上个月的住宿记录"、"有没有即将入住的订单"、"最近一笔订单多少钱"

**错误码：** `400` 查询语句为空，`500` Java 服务调用失败

---

## 六、异常监测与告警

> 告警模块由两部分组成：**定时任务**（由 uvicorn `@app.on_event("startup")` 中 `asyncio.create_task` 启动的后台循环，每 5 分钟自动检查）和 **HTTP 接口**（手动触发 + 规则管理）。

### 6.1 当前告警状态

```
GET /api/ai/alert/status
Authorization: Bearer {JWT}
```

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "alerts": [
      {
        "id": 1,
        "type": "满房预警",
        "level": "WARNING",
        "content": "豪华大床房 入住率 95%，超过阈值 90%",
        "trigger_time": "2026-05-18T08:00:00",
        "is_read": false
      }
    ],
    "unread_count": 2
  }
}
```

---

### 6.2 标记告警已读

```
PUT /api/ai/alert/{alert_id}/read
Authorization: Bearer {JWT}
```

**响应：** `{ "code": 200, "msg": "已标记为已读", "data": null }`

---

### 6.3 标记全部已读

```
PUT /api/ai/alert/read-all
Authorization: Bearer {JWT}
```

**响应：** `{ "code": 200, "msg": "已全部标记为已读", "data": null }`

---

### 6.4 配置告警规则

```
POST /api/ai/alert/rule
Authorization: Bearer {JWT}     (需 ADMIN)
Content-Type: application/json

{
  "type": "满房预警",
  "threshold": 0.9,
  "is_enabled": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 告警类型：满房预警 / 价格异常 / 异常退单 |
| threshold | float | 是 | 触发阈值（如 0.9 表示入住率 ≥ 90% 时告警） |
| is_enabled | bool | 是 | 是否启用 |

**响应：** `{ "code": 200, "msg": "规则已保存", "data": { "id": 3 } }`

---

### 6.5 告警规则列表

```
GET /api/ai/alert/rules
Authorization: Bearer {JWT}     (需 ADMIN)
```

**响应：** 返回规则数组，包含 id、type、threshold、is_enabled、create_time。

---

### 6.6 更新告警规则

```
PUT /api/ai/alert/rule/{rule_id}
Authorization: Bearer {JWT}     (需 ADMIN)
Content-Type: application/json

{
  "threshold": 0.85,
  "is_enabled": true
}
```

**响应：** `{ "code": 200, "msg": "规则已更新", "data": null }`

---

### 6.7 删除告警规则

```
DELETE /api/ai/alert/rule/{rule_id}
Authorization: Bearer {JWT}     (需 ADMIN)
```

**响应：** `{ "code": 200, "msg": "规则已删除", "data": null }`

---

### 6.8 手动触发告警检查

```
POST /api/ai/alert/check
Authorization: Bearer {JWT}     (需 ADMIN)
```

> 立即扫描所有启用的告警规则，评估并生成告警记录。正常情况由后台定时任务（每 5 分钟）自动执行。

**响应：** `{ "code": 200, "msg": "触发 1 条告警", "data": { "triggered": 1 } }`

---

### 告警类型说明

| 告警类型 | 检测逻辑 | 依赖服务 |
|---------|---------|---------|
| 满房预警 | 调用 finance-service `/api/finance/analysis/occupancy-by-type`，任一房型入住率 ≥ 阈值则触发 | finance-service |
| 价格异常 | 调用 finance-service `/api/finance/revenue/daily`，近 3 天日均营收较前 3 天变化幅度 ≥ 阈值 | finance-service |
| 异常退单 | 调用 order-service `/api/order/list/byTime`，近 7 天退款率 ≥ 阈值则触发 | order-service |

> 告警检查直连各微服务端口（非网关），无 Token 时仍可执行（后台定时任务场景）。

---

## 七、系统管理

### 7.1 服务健康检查

```
GET /api/ai/health
```

> 白名单接口，无需鉴权。

**响应：**

```json
{
  "code": 200,
  "msg": "服务正常",
  "data": {
    "status": "UP",
    "llm_provider": "qwen-plus",
    "llm_status": "connected",
    "chroma_status": "connected",
    "sqlite_status": "connected",
    "gateway_status": "connected",
    "uptime": "3d 12h 45m"
  }
}
```

---

### 7.2 获取服务信息

```
GET /api/ai/info
```

> 白名单接口，无需鉴权。

**响应：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "service": "ai-service",
    "version": "1.0.0",
    "framework": "FastAPI 0.115.6",
    "langchain": "0.3.13",
    "python": "3.13",
    "knowledge_count": 45,
    "vector_count": 120
  }
}
```

---

## 八、接口汇总

```
智能客服
POST   /api/ai/chat                    智能对话（流式 SSE）
POST   /api/ai/chat/sync               智能对话（同步）
GET    /api/ai/chat/history/{id}       获取会话历史
GET    /api/ai/chat/sessions           获取用户会话列表
POST   /api/ai/chat/feedback           对话反馈

知识库管理（ADMIN）
GET    /api/ai/knowledge/list          文档列表
POST   /api/ai/knowledge/upload        上传文件（PDF/DOCX/TXT/MD）
PUT    /api/ai/knowledge/{doc_id}      修改文档分类
DELETE /api/ai/knowledge/{doc_id}      删除文档
PUT    /api/ai/knowledge/{doc_id}/status  启用/禁用文档
POST   /api/ai/knowledge/rebuild-vector  向量库重建
GET    /api/ai/knowledge/categories    获取所有分类

房型推荐
POST   /api/ai/recommend/room          智能推荐房型

订单智能查询
POST   /api/ai/order/query             自然语言查订单

异常监测与告警
GET    /api/ai/alert/status            当前告警状态
PUT    /api/ai/alert/{id}/read         标记告警已读
PUT    /api/ai/alert/read-all          标记全部已读
POST   /api/ai/alert/check             手动触发告警检查
POST   /api/ai/alert/rule              配置告警规则
GET    /api/ai/alert/rules             告警规则列表
PUT    /api/ai/alert/rule/{id}         更新告警规则
DELETE /api/ai/alert/rule/{id}         删除告警规则

系统管理
GET    /api/ai/health                  服务健康检查（白名单）
GET    /api/ai/info                    服务信息（白名单）
```

> 共 **21 个接口**。除标注"白名单"的 2 个接口和 CORS 预检 OPTIONS 请求外，其他所有接口均需 JWT 鉴权。

---



## 九、LangChain 工具链定义

> 以下为 ai-service 内部定义的 LangChain `@tool`，供对话 Agent 调用：

| Tool 名称 | 文件 | 功能 | 调用的外部接口 |
|-----------|------|------|--------------|
| `search_knowledge` | `tools/knowledge_search.py` | 语义检索知识库 | Chroma 向量库（本地） |
| `query_room_types` | `tools/room_query.py` | 查询房型列表 | `call_gateway_json("/api/room/type/list")` |
| `query_available_rooms` | `tools/room_query.py` | 查询空闲房间 | `call_gateway_json("/api/room/list", params={"status":"空闲中"})` |
| `query_orders` | `tools/order_query_tool.py` | 查询用户订单 | `GatewayClient().get("/api/order/my")` |
| `query_customer` | `tools/customer_query.py` | 查询会员信息 | `GatewayClient().get("/api/customer/{id}")` |

> `query_customer` 和 `query_orders` 不传参数时自动使用当前登录用户的身份信息。

