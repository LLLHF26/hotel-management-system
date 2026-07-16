# user-service 接口文档

> 负责员工管理、会员管理、登录认证、保洁员查询。所有接口（除登录）需携带 JWT Token。

---

## 一、认证接口

### 1.1 登录

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**响应：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "ADMIN"
  }
}
```

**错误码：** `400` 用户名或密码错误，`400` 账号已被禁用

---

### 1.2 刷新 Token

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

### 1.3 登出

```
POST /api/auth/logout
Authorization: Bearer {Token}
```

**响应：** `{ "code": 200, "msg": "登出成功", "data": null }`

> Token 加入 Redis 黑名单，有效期至原 Token 过期为止。

---

## 二、员工管理（需 ADMIN 角色）

### 2.1 员工列表

```
GET /api/user/list?page=1&size=10&keyword=&role=&status=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| keyword | string | 否 | 按用户名或姓名模糊搜索 |
| role | string | 否 | 按角色筛选：ADMIN / FRONT_DESK / CLEANER |
| status | int | 否 | 1=正常 0=禁用，不传则全部 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 25,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800000001",
        "role": "ADMIN",
        "status": 1,
        "createTime": "2026-01-15T10:00:00"
      }
    ]
  }
}
```

---

### 2.2 员工详情

```
GET /api/user/{id}
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800000001",
    "email": "admin@hotel-backend.com",
    "role": "ADMIN",
    "avatar": "https://xxx.com/avatar.jpg",
    "status": 1,
    "createTime": "2026-01-15T10:00:00",
    "updateTime": "2026-05-01T08:30:00"
  }
}
```

---

### 2.3 创建员工

```
POST /api/user/create
Content-Type: application/json

{
  "username": "cleaner03",
  "password": "123456",
  "realName": "保洁王阿姨",
  "phone": "13800001113",
  "email": "cleaner03@hotel.com",
  "role": "CLEANER"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 登录账号，4-32位 |
| password | string | 是 | 密码，6-32位 |
| realName | string | 是 | 真实姓名 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| role | string | 是 | ADMIN / FRONT_DESK / CLEANER |

**响应：** `{ "code": 200, "msg": "创建成功", "data": { "id": 4 } }`

**错误码：** `400` 用户名已存在

---

### 2.4 修改员工

```
PUT /api/user/{id}
Content-Type: application/json

{
  "realName": "保洁王大姐",
  "phone": "13800001114",
  "email": "cleaner03_new@hotel.com",
  "role": "CLEANER"
}
```

> 不可修改 username；password 通过修改密码接口单独改。

**响应：** `{ "code": 200, "msg": "修改成功", "data": null }`

---

### 2.5 删除员工

```
DELETE /api/user/{id}
```

> 逻辑删除，`is_deleted = 1`。**不可删除自己。**

**响应：** `{ "code": 200, "msg": "删除成功", "data": null }`

**错误码：** `400` 不能删除自己，`404` 员工不存在

---

### 2.6 启用 / 禁用员工

```
PUT /api/user/{id}/status
Content-Type: application/json

{
  "status": 0
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 1=启用 0=禁用 |

**响应：** `{ "code": 200, "msg": "已禁用", "data": null }`

> 禁用的员工无法登录。

---

### 2.7 修改员工角色

```
PUT /api/user/{id}/role
Content-Type: application/json

{
  "role": "CLEANER"
}
```

**响应：** `{ "code": 200, "msg": "角色修改成功", "data": null }`

---

## 三、保洁员管理

### 3.1 保洁员列表

> 供 room-service 内网调用，用于打扫任务分配。也支持前端管理端查看。

```
GET /api/user/cleaner/list
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "id": 3,
      "realName": "保洁张阿姨",
      "phone": "13800001111",
      "currentTaskCount": 2
    },
    {
      "id": 4,
      "realName": "保洁李阿姨",
      "phone": "13800001112",
      "currentTaskCount": 0
    }
  ]
}
```

> `currentTaskCount` 为该保洁员当前打扫中的任务数，用于分配策略（优先分配给任务数最少的保洁员）。

---

### 3.2 保洁员打扫记录

```
GET /api/user/cleaner/{id}/tasks?page=1&size=10
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 156,
    "page": 1,
    "size": 10,
    "records": [
      {
        "taskId": 1001,
        "roomId": 5,
        "roomNumber": "305",
        "status": "已完成",
        "startTime": "2026-05-17T09:00:00",
        "endTime": "2026-05-17T09:05:00"
      }
    ]
  }
}
```

> 数据来源：跨服务查询 room-service 的 `cleaning_task` 表（通过 OpenFeign 或数据库直读，按业务需求决定）。

---

## 四、会员管理

### 4.1 会员列表

```
GET /api/customer/list?page=1&size=10&keyword=&memberLevel=&status=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| keyword | string | 否 | 按姓名或手机号模糊搜索 |
| memberLevel | string | 否 | 按等级筛选：NORMAL / SILVER / GOLD / DIAMOND |
| status | int | 否 | 1=正常 0=冻结 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 580,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 101,
        "realName": "张三",
        "idCard": "320xxx19900101xxxx",
        "phone": "13900000001",
        "gender": 0,
        "points": 3600,
        "totalConsumed": 18000.00,
        "memberLevel": "GOLD",
        "status": 1,
        "createTime": "2025-06-01T12:00:00"
      }
    ]
  }
}
```

---

### 4.2 会员详情

```
GET /api/customer/{id}
```

**响应：**（字段同列表，增加积分记录）

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 101,
    "realName": "张三",
    "idCard": "320xxx19900101xxxx",
    "phone": "13900000001",
    "gender": 0,
    "points": 3600,
    "totalConsumed": 18000.00,
    "memberLevel": "GOLD",
    "status": 1
  }
}
```

---

### 4.3 会员注册（客户端）

```
POST /api/customer/register
Content-Type: application/json

{
  "realName": "张三",
  "idCard": "320xxx19900101xxxx",
  "phone": "13900000001",
  "gender": 0,
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | string | 是 | 真实姓名 |
| phone | string | 是 | 手机号（登录凭证） |
| password | string | 否 | 密码，不传则首次快捷登录后补设 |
| idCard | string | 否 | 身份证号 |
| gender | int | 否 | 0=男 1=女 |

**响应：** `{ "code": 200, "msg": "注册成功", "data": { "id": 101 } }`

**错误码：** `400` 手机号已注册

---

### 4.4 修改会员信息

```
PUT /api/customer/{id}
Content-Type: application/json

{
  "realName": "张三（更新）",
  "idCard": "320xxx19900101xxxx",
  "gender": 0
}
```

> phone 不可随意修改（需短信验证）。memberLevel 和 points 由系统自动计算，不可手动改。

**响应：** `{ "code": 200, "msg": "修改成功", "data": null }`

---

### 4.5 冻结 / 解冻会员

```
PUT /api/customer/{id}/status
Content-Type: application/json

{
  "status": 0
}
```

**响应：** `{ "code": 200, "msg": "已冻结", "data": null }`

> 冻结后该会员无法登录客户端。

---

### 4.6 积分记录查询

```
GET /api/customer/{id}/points?page=1&size=20
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 45,
    "records": [
      {
        "id": 2001,
        "type": "EARN",
        "typeName": "获得",
        "points": 200,
        "balanceAfter": 3600,
        "reason": "订单 ORD202605160001 消费奖励",
        "createTime": "2026-05-16T14:00:00"
      },
      {
        "id": 2002,
        "type": "CONSUME",
        "typeName": "消费",
        "points": -500,
        "balanceAfter": 3100,
        "reason": "兑换免费早餐券",
        "createTime": "2026-05-17T08:00:00"
      }
    ]
  }
}
```

---

### 4.7 增加积分

```
POST /api/customer/{id}/points/add?points=200&reason=订单消费奖励
Authorization: Bearer {JWT}     (需 ADMIN)
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| points | int | 是 | 积分变化量（正数增加、负数减少） |
| reason | string | 是 | 变动原因 |

**响应：** `{ "code": 200, "msg": "积分更新成功", "data": null }`

---

### 4.8 增加消费额

```
POST /api/customer/{id}/consumed/add?amount=588.00
Authorization: Bearer {JWT}     (需 ADMIN)
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| amount | decimal | 是 | 消费金额 |

> 消费额累计影响会员等级（NORMAL → SILVER → GOLD → DIAMOND）。

**响应：** `{ "code": 200, "msg": "消费额更新成功", "data": null }`

---

## 五、文件上传（OSS）

### 5.1 上传头像

```
POST /api/user/upload/avatar
Authorization: Bearer {JWT}
Content-Type: multipart/form-data

Form 参数:
  file: (binary) 图片文件（支持 jpg/png/webp/gif）
```

**响应：**

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": { "url": "https://gulimall-hello-lhf.oss-cn-beijing.aliyuncs.com/hotel/avatars/a1b2c3d4.jpg" }
}
```

> 图片存储于阿里云 OSS。上传后获得 HTTPS URL，填入 `avatar` 字段。

---

## 六、个人中心（登录用户操作自己）

### 5.1 查看个人信息

```
GET /api/user/profile
Authorization: Bearer {Token}
```

> 从 Token 中解析 userId，无需传参。

**响应：** 同 `GET /api/user/{id}`

---

### 5.2 修改个人信息

```
PUT /api/user/profile
Content-Type: application/json

{
  "realName": "前台小刘",
  "phone": "13900002222",
  "email": "front02@hotel.com",
  "avatar": "https://xxx.com/avatar2.jpg"
}
```

**响应：** `{ "code": 200, "msg": "修改成功", "data": null }`

---

### 5.3 修改密码

```
PUT /api/user/password
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**响应：** `{ "code": 200, "msg": "密码修改成功，请重新登录", "data": null }`

**错误码：** `400` 旧密码错误

---

## 七、会员个人中心（客户端）

### 7.1 会员查看自己信息

```
GET /api/customer/profile
Authorization: Bearer {Token}
```

> 从 Token 解析 customerId，返回自己的资料+积分。

---

### 7.2 会员修改自己信息

```
PUT /api/customer/profile
Content-Type: application/json

{
  "realName": "张三",
  "idCard": "320xxx19900101xxxx",
  "gender": 0
}
```

---

### 7.3 会员修改密码

```
PUT /api/customer/password
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

## 八、接口汇总

```
认证
POST   /api/auth/login              登录（白名单）
POST   /api/auth/refresh            刷新 Token（白名单）
POST   /api/auth/logout             登出

员工管理（ADMIN）
GET    /api/user/list               员工列表
GET    /api/user/{id}               员工详情
POST   /api/user/create             创建员工
PUT    /api/user/{id}               修改员工
DELETE /api/user/{id}               删除员工
PUT    /api/user/{id}/status        启用/禁用
PUT    /api/user/{id}/role          修改角色

文件上传
POST   /api/user/upload/avatar      上传头像

保洁员（ADMIN / FRONT_DESK / 内网）
GET    /api/user/cleaner/list       保洁员列表
GET    /api/user/cleaner/{id}/tasks 保洁员打扫记录

会员管理（ADMIN / FRONT_DESK）
GET    /api/customer/list           会员列表
GET    /api/customer/{id}           会员详情
POST   /api/customer/register       会员注册（客户端免登录）
PUT    /api/customer/{id}           修改会员信息
PUT    /api/customer/{id}/status    冻结/解冻会员
GET    /api/customer/{id}/points    积分记录
POST   /api/customer/{id}/points/add   增加积分
POST   /api/customer/{id}/consumed/add 增加消费额

个人中心（登录用户）
GET    /api/user/profile            查看自己信息
PUT    /api/user/profile            修改自己信息
PUT    /api/user/password           修改密码

会员个人中心（登录会员）
GET    /api/customer/profile        查看自己信息
PUT    /api/customer/profile        修改自己信息
PUT    /api/customer/password       修改密码
```

> 共 **27 个接口**。保洁员打扫记录接口需跨服务调 room-service 查 `cleaning_task` 表。
