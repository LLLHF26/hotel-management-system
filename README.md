# 酒店管理系统

Spring Boot 3 微服务 + Vue 3 前端 + Python AI 智能服务 + RabbitMQ + Kafka + ELK 日志体系。

## 架构概览

```
┌─────────────────────────────────────────────────────────┐
│                      客户端层                            │
│  hotel-front-manage  │  hotel-front-desk  │  uni-app 客户端 │
│   (Vue 3 管理后台)    │  (Vue 3 前台桌面)   │  (移动端/小程序)  │
└────────────┬──────────┴────────┬───────────┴──────┬──────┘
             │                   │                  │
             └───────────────────┼──────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Spring Cloud Gateway  │
                    │        :8088             │
                    └────────────┬────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
        ▼                        ▼                        ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────────────┐
│  user-service │  │  room-service │  │    order-service      │
│    :8081      │  │    :8082      │  │       :8083           │
│  用户/客户管理 │  │  房间/房型管理 │  │    订单/支付核心       │
└───────┬───────┘  └───────┬───────┘  └───────────┬───────────┘
        │                  │                       │
        └──────────────────┼───────────────────────┘
                           │
              ┌────────────▼────────────┐
              │      RabbitMQ           │
              │   订单事件异步解耦        │
              └────────────┬────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ user-service  │  │ room-service  │  │finance-service│
│  积分/通知     │  │  清洁任务      │  │    :8084      │
└───────────────┘  └───────────────┘  │  财务/统计    │
                                       └───────────────┘

┌─────────────────────────────────────────────────────────┐
│                    日志采集层                             │
│  Java/Python ──► Kafka (logs-topic) ──► Logstash        │
│                                        ──► Elasticsearch │
│                                        ──► Kibana :5601  │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    AI 智能服务                            │
│  FastAPI :8000 + DeepSeek + LangChain + ChromaDB        │
│  智能对话 │ 订单查询 │ 客房推荐 │ 知识库 │ 告警监控       │
└─────────────────────────────────────────────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.3.6 + Spring Cloud 2023.0.4 |
| 微服务组件 | Nacos（注册中心/配置中心）、Spring Cloud Gateway |
| ORM | MyBatis-Plus 3.5.9 + Druid 连接池 |
| 数据库 | MySQL 8.0、Redis 7、SQLite（AI 服务） |
| 向量数据库 | ChromaDB |
| 消息队列 | RabbitMQ（异步业务解耦） |
| 日志采集 | Kafka 3.4 (3 broker) → Logstash → Elasticsearch → Kibana |
| 前端框架 | Vue 3 + TypeScript + Vite |
| UI 组件 | Element Plus（前台）/ 纯手写 CSS（管理后台） |
| 移动端 | uni-app（Vue 3，支持 H5/微信小程序/APP） |
| AI 服务 | FastAPI + LangChain + DeepSeek |
| 构建工具 | Maven（Java）/ Vite（前端）/ uv（Python） |
| 部署 | Docker Compose（中间件全容器化） |

## 项目结构

```
hotel/
├── hotel-backend/                 # Java 微服务
│   ├── common-service/            # 公共模块（DTO/事件/配置/日志）
│   ├── gateway-service/    :8088  # API 网关
│   ├── user-service/       :8081  # 用户服务
│   ├── room-service/       :8082  # 房间服务
│   ├── order-service/      :8083  # 订单服务
│   └── finance-service/    :8084  # 财务服务
├── hotel-front-manage/            # Vue 3 管理后台
├── hotel-front-desk/              # Vue 3 + Element Plus 前台桌面端
├── hotel-front-client/            # uni-app 移动客户端
├── ai-service/                    # Python FastAPI 智能服务
│   └── ai-service/
│       ├── agents/                # AI Agent（对话/推荐/订单查询）
│       ├── api/                   # REST API 路由
│       ├── core/                  # 核心模块（LLM/向量库/RAG/认证）
│       ├── models/                # 数据模型
│       ├── tools/                 # Agent 工具
│       └── tests/                 # 测试
├── elk/                           # ELK 配置
│   └── logstash/
│       └── logstash.conf          # Kafka → ES pipeline
└── 文档/                           # 接口文档 / 数据库设计
```

## 环境要求

- **JDK 21** + Maven 3.9+
- **Python 3.13+** + uv
- **Node.js 20+** + npm
- **Docker & Docker Compose**（中间件）
- **MySQL 8.0**（4 个库：hotel_user / hotel_room / hotel_order / hotel_finance）

## 快速启动

### 1. 启动中间件（Docker）

```bash
# 根据你的 docker-compose.yml 启动
docker compose up -d
```

需要的中间件：
- MySQL :3306（或自行安装）
- Redis :6379
- Nacos :8848
- RabbitMQ :5672（管理端 :15672）
- Kafka :9091/:9092/:9093（3 broker）
- Elasticsearch :9200
- Logstash :5044
- Kibana :5601

### 2. 启动后端微服务

```bash
cd hotel-backend

# 按顺序启动
mvn -pl common-service install
mvn -pl gateway-service spring-boot:run
mvn -pl user-service spring-boot:run
mvn -pl room-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl finance-service spring-boot:run
```

### 3. 启动 AI 服务

```bash
cd ai-service
cp .env.example .env        # 编辑填入 API Key
uv sync
uv run uvicorn ai-service.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. 启动前端

```bash
# 前台桌面端（:5174）
cd hotel-front-desk
npm install && npm run dev

# 管理后台
cd hotel-front-manage
npm install && npm run dev

# 移动客户端（H5）
cd hotel-front-client
# 用 HBuilderX 打开，运行到浏览器
```

## 核心功能

**前台桌面端**
- 工作台：今日入住/入住率/营收统计 + SVG 趋势图
- 房态看板：按楼层分组可视化，6 种状态流转
- 订单管理：创建/支付/入住/退房/续住/换房/取消/退款
- 客户管理：客户信息/消费统计/会员等级
- 订单操作弹窗：一句话调用 8 种操作（通过 defineExpose）

**管理后台**
- 工作台：统计卡片 + ECharts 数据大屏
- 房型/房间 CRUD + 状态机驱动流转
- 订单/客户/员工管理
- 财务管理：7 日营收趋势 + 入住率分析
- 告警中心 + 知识库（RAG 向量化）

**移动客户端（uni-app）**
- 客房浏览与筛选 + 智能推荐
- 在线预订与支付（倒计时自动取消）
- AI 客服对话（多意图识别）
- 订单列表与个人中心

**AI 智能服务**
- 智能客服对话（酒店设施/订单查询/推荐/政策）
- 房间推荐（预算 + 偏好 + 向量匹配）
- 自然语言订单查询
- 知识库 RAG（文档上传 → 向量化 → 检索）
- 异常检测告警（模板方法模式）

## 服务端口

| 服务 | 端口 |
|------|------|
| gateway-service | 8088 |
| user-service | 8081 |
| room-service | 8082 |
| order-service | 8083 |
| finance-service | 8084 |
| ai-service | 8000 |
| hotel-front-desk | 5174 |
| hotel-front-manage | 5173 |
| hotel-front-client | 由 HBuilderX 分配 |
| Nacos | 8848 |
| RabbitMQ | 5672 / 15672 |
| Kafka | 9091 / 9092 / 9093 |
| Elasticsearch | 9200 |
| Kibana | 5601 |
