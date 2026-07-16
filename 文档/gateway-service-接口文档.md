# gateway-service 接口文档

> 网关是前端三端访问后端的唯一入口。不含业务逻辑，负责路由转发、JWT 鉴权、限流。

---

## 一、网关自身端点

网关自身暴露少量端点（非代理），由 Spring Boot Actuator + 自定义控制器提供。

### 1.1 健康检查

```
GET /actuator/health
```

**无需认证。**

**响应：**
```json
{
  "status": "UP"
}
```

---

### 1.2 用户登录（白名单放行）

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

> 网关将此请求路由至 user-service 处理，但鉴权过滤器对此路径放行。

**响应：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "role": "ADMIN",
    "roleName": "管理员"
  }
}
```

---

### 1.3 Token 刷新

```
POST /api/auth/refresh
Authorization: Bearer {旧Token}

无 Body
```

**响应：**
```json
{
  "code": 200,
  "msg": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expireAt": "2026-05-18T12:00:00"
  }
}
```

---

### 1.4 用户登出

```
POST /api/auth/logout
Authorization: Bearer {Token}
```

**响应：**
```json
{
  "code": 200,
  "msg": "登出成功",
  "data": null
}
```

> 网关将 Token 加入 Redis 黑名单，后续该 Token 请求一律拦截。

---

## 二、鉴权说明

### 2.1 请求头

所有需要认证的接口必须在请求头中携带 Token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 2.2 白名单路径

以下路径**无需 Token**：

```
POST   /api/auth/login
POST   /api/auth/refresh
GET    /actuator/health
GET    /api/room/type/list        （房型浏览，供客户端免登录查看）
GET    /api/room/list             （房间浏览，供客户端免登录查看）
```

### 2.3 鉴权失败响应

```json
{
  "code": 401,
  "msg": "未登录或 Token 已过期",
  "data": null
}
```

### 2.4 权限不足响应

```json
{
  "code": 403,
  "msg": "权限不足",
  "data": null
}
```

---

## 三、路由规则表

网关根据 URL 前缀将请求转发至对应的微服务。

| 路由前缀 | 目标服务 | 说明 |
|----------|---------|------|
| `/api/auth/**` | user-service (8081) | 登录、刷新、登出 |
| `/api/user/**` | user-service (8081) | 员工/会员管理 |
| `/api/customer/**` | user-service (8081) | 会员注册、查询 |
| `/api/room/**` | room-service (8082) | 房间、房型、房态、打扫 |
| `/api/order/**` | order-service (8083) | 订单、支付 |
| `/api/finance/**` | finance-service (8084) | 财务、报表 |
| `/api/ai/**` | ai-service (8000) | Python 智能服务 |

---

## 四、限流规则（Sentinel）

| 接口路径 | 限制 | 说明 |
|---------|------|------|
| `/api/auth/login` | 10次/分钟/IP | 防暴力破解 |
| `/api/room/list` | 200次/秒 | 高频查询，需适当放宽 |
| `/api/order/**` | 50次/秒 | 订单操作 |
| 其他全部 | 100次/秒 | 默认 |

**限流触发响应：**
```json
{
  "code": 429,
  "msg": "请求过于频繁，请稍后重试",
  "data": null
}
```

---

## 五、统一响应格式

所有经过网关的请求，下游服务已按统一格式返回，网关直接透传：

```json
{
  "code": 200,
  "msg": "成功",
  "data": { }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 被限流 |
| 500 | 服务器内部错误 |

---

## 六、跨域配置（CORS）

网关统一处理跨域，后端微服务无需各自配置。

```
Access-Control-Allow-Origin: *（开发阶段）
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type
```

> 生产环境应将 `Allow-Origin` 替换为具体域名。

---

## 七、网关调用示例

### 示例 1：客户端查看房型列表

```bash
curl -X GET http://localhost:8080/api/room/type/list
```

网关路由：`/api/room/**` → `room-service`，无需鉴权，直接拿到房型列表。

---

### 示例 2：管理员创建员工

```bash
curl -X POST http://localhost:8080/api/user/create \
  -H "Authorization: Bearer {Token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cleaner01",
    "password": "123456",
    "realName": "张阿姨",
    "role": "CLEANER",
    "phone": "13800001111"
  }'
```

网关校验 Token → 解析出角色为 ADMIN → 路由至 user-service → 创建员工。

---

### 示例 3：前台办理退房（触发打扫链）

```bash
curl -X PUT http://localhost:8080/api/order/1001/checkout \
  -H "Authorization: Bearer {Token}"
```

网关校验 Token → 路由至 order-service → order-service 内部调用 room-service 置房态为"待清洁中"。

---

## 八、网关过滤器链

```
请求 → [1.CORS过滤器] → [2.白名单检查] → [3.JWT解析]
    → [4.权限校验] → [5.Sentinel限流] → [6.路由转发] → 下游服务
```

1. **CORS 过滤器** — 处理 OPTIONS 预检请求，添加跨域头
2. **白名单检查** — 匹配白名单路径则直接放行，跳过步骤 3、4
3. **JWT 解析** — 从 Authorization 头提取 Token，校验签名与有效期
4. **权限校验** — 从 Token 中提取 role，校验是否有权限访问当前路径
5. **Sentinel 限流** — QPS 超限则直接返回 429
6. **路由转发** — 匹配路由规则，转发至对应微服务
