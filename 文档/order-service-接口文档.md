# order-service 接口文档

> 负责订单全生命周期：预订→支付→入住→退房（触发打扫）→完成。同时管理入住期间的消费挂账。

---

## 一、订单状态流转

```
                      ┌─────────┐
                      │  待支付  │
                      └────┬────┘
                           │
                ┌──────────┼──────────┐
                │ 支付成功   │          │ 取消
                ▼          ▼          ▼
           ┌─────────┐ ┌─────────┐
           │  已支付  │ │  已取消  │ ←── 释放房间（预订中→空闲中）
           └────┬────┘ └─────────┘
                │
                │ 前台确认入住
                ▼
           ┌─────────┐
           │  已入住  │
           └────┬────┘
                │
          ┌─────┼─────┐
          │ 退房  │      │ 取消（不可）
          ▼     ▼      ▼
     ┌─────────┐ ┌─────────┐
     │  已完成  │ │(退款处理)│
     └─────────┘ └─────────┘
          │
          │ 取消订单（不可）
          ▼
     (已完成订单不可取消)
```

> 关键约束：只有"待支付"可取消；"已完成"订单不可取消但可退款。

---

## 二、订单管理

### 2.1 订单列表

```
GET /api/order/list?page=1&size=10&status=&customerPhone=&roomNumber=&checkInDate=&source=&startDate=&endDate=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| status | string | 否 | 按状态筛选 |
| customerPhone | string | 否 | 按会员手机号搜索 |
| roomNumber | string | 否 | 按房间号模糊搜索 |
| checkInDate | string | 否 | 按预计入住日期筛选 |
| source | string | 否 | ONLINE / WALK_IN / FRONT_DESK |
| startDate | string | 否 | 订单创建日期范围起 |
| endDate | string | 否 | 订单创建日期范围止 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 168,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1001,
        "orderNo": "ORD202605170001",
        "customerId": 101,
        "customerName": "张三",
        "customerPhone": "13900000001",
        "roomId": 5,
        "roomNumber": "301",
        "roomTypeName": "豪华大床房",
        "checkInDate": "2026-05-18",
        "checkOutDate": "2026-05-20",
        "nights": 2,
        "roomPrice": 588.00,
        "roomTotal": 1176.00,
        "extraTotal": 0.00,
        "totalAmount": 1176.00,
        "paidAmount": 1176.00,
        "deposit": 500.00,
        "status": "已支付",
        "statusName": "已支付",
        "source": "ONLINE",
        "sourceName": "线上预订",
        "createTime": "2026-05-17T14:00:00"
      }
    ]
  }
}
```

---

### 2.2 订单详情

```
GET /api/order/{id}
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1001,
    "orderNo": "ORD202605170001",
    "customerId": 101,
    "customerName": "张三",
    "customerPhone": "13900000001",
    "roomId": 5,
    "roomNumber": "301",
    "roomTypeName": "豪华大床房",
    "checkInDate": "2026-05-18",
    "checkOutDate": "2026-05-20",
    "nights": 2,
    "actualCheckIn": null,
    "actualCheckOut": null,
    "roomPrice": 588.00,
    "roomTotal": 1176.00,
    "extraTotal": 88.00,
    "totalAmount": 1264.00,
    "paidAmount": 1176.00,
    "deposit": 500.00,
    "status": "已支付",
    "source": "ONLINE",
    "remark": "",
    "extras": [
      {
        "id": 201,
        "itemName": "迷你吧-可乐",
        "amount": 15.00,
        "quantity": 2,
        "operatorId": 2,
        "operatorName": "前台小王"
      },
      {
        "id": 202,
        "itemName": "洗衣服务",
        "amount": 58.00,
        "quantity": 1,
        "operatorId": 2,
        "operatorName": "前台小王"
      }
    ],
    "payments": [
      {
        "id": 5001,
        "paymentNo": "PAY202605170001",
        "amount": 1176.00,
        "method": "WECHAT",
        "methodName": "微信支付",
        "status": "成功",
        "paidAt": "2026-05-17T14:05:00"
      }
    ],
    "createTime": "2026-05-17T14:00:00"
  }
}
```

---

### 2.3 创建订单（用户预订）

```
POST /api/order/create
Authorization: Bearer {Token}     (会员)
Content-Type: application/json

{
  "roomId": 5,
  "customerId": 101,
  "checkInDate": "2026-05-18",
  "checkOutDate": "2026-05-20",
  "remark": "请安排高层房间"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomId | long | 是 | 房间 ID |
| customerId | long | 是 | 会员 ID（从 Token 获取） |
| checkInDate | string | 是 | 入住日期 |
| checkOutDate | string | 是 | 退房日期 |
| remark | string | 否 | 备注 |

**流程：**
1. 校验房间 status = "空闲中"，且日期无冲突
2. 计算 nights = checkOutDate - checkInDate
3. roomTotal = roomType.price × nights
4. 生成订单编号（ORD + 时间戳）
5. order.status = "待支付"
6. 调用 room-service：room.status → "预订中"

**响应：**
```json
{
  "code": 200,
  "msg": "预订成功",
  "data": {
    "id": 1001,
    "orderNo": "ORD202605170001",
    "totalAmount": 1176.00
  }
}
```

**错误码：** `400` 房间已被预订，`400` 日期不合法（入住≥退房），`404` 房间不存在

---

### 2.4 取消订单

```
PUT /api/order/{id}/cancel
Authorization: Bearer {Token}     (会员 或 ADMIN / FRONT_DESK)
Content-Type: application/json

{
  "reason": "行程变更"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 否 | 取消原因 |

**流程：**
1. 校验 order.status = "待支付"（已支付需走退款，不可直接取消）
2. order.status → "已取消"
3. 调用 room-service：room.status → "空闲中"（释放房间）

**响应：** `{ "code": 200, "msg": "订单已取消", "data": null }`

**错误码：** `400` 订单当前状态不可取消

---

### 2.5 办理入住

```
PUT /api/order/{id}/check-in
Authorization: Bearer {Token}     (ADMIN / FRONT_DESK)
Content-Type: application/json

{
  "deposit": 500.00
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deposit | decimal | 否 | 押金金额 |

**流程：**
1. 校验 order.status = "已支付"
2. actualCheckIn = now
3. order.status → "已入住"
4. 调用 room-service：room.status → "入住中"

> 若是到店散客（WALK_IN），可先创建订单（source=WALK_IN, status=待支付），然后支付+入住合并操作。

**响应：** `{ "code": 200, "msg": "入住办理成功", "data": null }`

**错误码：** `400` 订单不是"已支付"状态

---

### 2.6 办理退房（触发打扫链）

```
PUT /api/order/{id}/checkout
Authorization: Bearer {Token}     (ADMIN / FRONT_DESK)
Content-Type: application/json

{
  "refundDeposit": 500.00
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refundDeposit | decimal | 否 | 退还押金金额 |

**流程：**
1. 校验 order.status = "已入住"
2. actualCheckOut = now
3. order.status → "已完成"
4. 调用 room-service：room.status → "待清洁中"（启动打扫调度链）
5. 退还押金（如有）

**响应：** `{ "code": 200, "msg": "退房成功", "data": null }`

**错误码：** `400` 订单不是"已入住"状态

---

### 2.7 续住

```
PUT /api/order/{id}/extend
Authorization: Bearer {Token}     (ADMIN / FRONT_DESK)
Content-Type: application/json

{
  "extendDays": 2
}
```

**流程：**
1. 校验 order.status = "已入住"
2. checkOutDate 延长 extendDays 天
3. roomTotal 重新计算，totalAmount 同步更新
4. 补差价（如有）

**响应：**
```json
{
  "code": 200,
  "msg": "续住成功",
  "data": {
    "newCheckOutDate": "2026-05-22",
    "additionalAmount": 1176.00,
    "newTotalAmount": 2352.00
  }
}
```

---

### 2.8 换房

```
PUT /api/order/{id}/change-room
Authorization: Bearer {Token}     (ADMIN / FRONT_DESK)
Content-Type: application/json

{
  "newRoomId": 8
}
```

**流程：**
1. 校验 order.status = "已入住"
2. 校验新房间 status = "空闲中"
3. 旧房间 status → "空闲中"
4. 新房间 status → "入住中"
5. 更新 order.roomId / roomNumber / roomTypeName 等字段
6. 如果新房型价格不同，按剩余天数重新计算差价

**响应：**
```json
{
  "code": 200,
  "msg": "换房成功",
  "data": {
    "oldRoomNumber": "301",
    "newRoomNumber": "408",
    "priceDiff": 200.00
  }
}
```

**错误码：** `400` 新房间不可用，`400` 新旧房间相同

---

## 三、支付管理

### 3.1 支付

```
POST /api/order/{id}/pay
Authorization: Bearer {Token}
Content-Type: application/json

{
  "amount": 1176.00,
  "method": "WECHAT"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| amount | decimal | 是 | 支付金额 |
| method | string | 是 | CASH / ALIPAY / WECHAT / CARD |

**流程：**
1. 校验订单可支付（status = "待支付" 或已入住待补差价）
2. 记录 payment 流水
3. paidAmount 累加
4. 若 paidAmount >= totalAmount，status → "已支付"

**响应：**
```json
{
  "code": 200,
  "msg": "支付成功",
  "data": {
    "paymentNo": "PAY202605170001",
    "paidAmount": 1176.00,
    "remainAmount": 0.00
  }
}
```

### 3.2 退款

```
POST /api/order/{id}/refund
Authorization: Bearer {Token}     (ADMIN)
Content-Type: application/json

{
  "amount": 1176.00,
  "reason": "客户取消行程，协商退款"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| amount | decimal | 是 | 退款金额，不可超过已付金额 |
| reason | string | 是 | 退款原因 |

**流程：**
1. 校验退款金额 ≤ paidAmount
2. 写 refund_record
3. 原 payment 标记"已退款"
4. paidAmount 扣减
5. order.status → "已退款"

**响应：** `{ "code": 200, "msg": "退款成功", "data": { "refundAmount": 1176.00 } }`

**错误码：** `400` 退款金额超过已付金额

---

### 3.3 支付记录查询

```
GET /api/order/{id}/payments
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "id": 5001,
      "paymentNo": "PAY202605170001",
      "amount": 1176.00,
      "method": "WECHAT",
      "methodName": "微信支付",
      "status": "成功",
      "paidAt": "2026-05-17T14:05:00"
    }
  ]
}
```

---

## 四、消费明细（入住期间）

### 4.1 添加消费

```
POST /api/order/{id}/extra
Authorization: Bearer {Token}     (ADMIN / FRONT_DESK)
Content-Type: application/json

{
  "itemName": "迷你吧-可乐",
  "amount": 15.00,
  "quantity": 2
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| itemName | string | 是 | 项目名称 |
| amount | decimal | 是 | 单价 |
| quantity | int | 否 | 数量，默认 1 |

**流程：**
1. 写入 order_extra 表
2. extraTotal 累加，totalAmount 同步更新
3. operatorId 从 Token 获取（记录操作用户）

**响应：** `{ "code": 200, "msg": "消费已添加", "data": { "extraId": 201 } }`

**错误码：** `400` 仅"已入住"状态的订单可添加消费

---

### 4.2 查看消费明细

```
GET /api/order/{id}/extras
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "id": 201,
      "itemName": "迷你吧-可乐",
      "amount": 15.00,
      "quantity": 2,
      "subtotal": 30.00,
      "operatorName": "前台小王",
      "createTime": "2026-05-18T20:00:00"
    }
  ]
}
```

---

### 4.3 删除消费项

```
DELETE /api/order/{id}/extra/{extraId}
Authorization: Bearer {Token}     (ADMIN / FRONT_DESK)
```

> 仅"已入住"状态可删除。删除后 extraTotal 和 totalAmount 自动回扣。

**响应：** `{ "code": 200, "msg": "已删除", "data": null }`

---

## 五、我的订单（客户端）

### 5.1 我的订单列表

```
GET /api/order/my?page=1&size=10&status=
Authorization: Bearer {Token}     (会员)
```

> 从 Token 解析 customerId，仅返回当前会员的订单。

**响应：** 同订单列表，但去除了 customerId/customerPhone 等冗余筛选参数。

---

### 5.2 我的订单详情

```
GET /api/order/my/{id}
Authorization: Bearer {Token}     (会员)
```

> 仅可查看自己的订单。

**响应：** 同订单详情。

---

## 六、订单编号规则

```
ORD + YYYYMMDD + 4位流水号

示例：ORD202605170001
```

> 使用数据库自增或 Redis 自增生成流水号，保证全局唯一。

---

## 七、服务间调用

本服务通过 OpenFeign 调用以下外部接口：

| 调用方 | 被调方 | 接口 | 场景 |
|--------|--------|------|------|
| order-service | room-service | `PUT /api/room/{id}/status` | 预订锁定 / 取消释放 / 入住 / 退房 |
| order-service | user-service | `GET /api/customer/{id}` | 校验会员状态 |

调用时序（以创建订单为例）：

```
客户端 → order-service
           │
           ├── 1. GET /api/customer/{id} → user-service（校验会员）
           ├── 2. GET /api/room/{id}      → room-service（校验房间状态）
           ├── 3. 写入 orders 表
           └── 4. PUT /api/room/{id}/status → room-service（锁定房间）
```

---

## 八、接口汇总

```
订单管理
GET    /api/order/list                    订单列表（多维度筛选）
GET    /api/order/list/byTime             按时间范围查询（供 finance-service / ai-service 调用）
GET    /api/order/{id}                    订单详情（含消费+支付记录）
POST   /api/order/create                  创建订单（会员）
GET    /api/order/available-rooms         查询可用房间（输入房型+日期）
PUT    /api/order/{id}/cancel             取消订单
PUT    /api/order/{id}/check-in           办理入住（前台）
PUT    /api/order/{id}/checkout           办理退房（前台，触发打扫链）
PUT    /api/order/{id}/extend             续住（前台）
PUT    /api/order/{id}/change-room        换房（前台）

支付管理
POST   /api/order/{id}/pay                支付
POST   /api/order/{id}/refund             退款（ADMIN）
GET    /api/order/{id}/payments           支付记录
GET    /api/order/payments/time           按时间范围查询支付记录

消费明细
POST   /api/order/{id}/extra              添加消费（前台）
GET    /api/order/{id}/extras             查看消费明细
DELETE /api/order/{id}/extra/{extraId}    删除消费项

我的订单（客户端）
GET    /api/order/my                      我的订单列表
GET    /api/order/my/{id}                 我的订单详情
```

> 共 **19 个接口**。退房接口是打通打扫链路的关键节点（→ room-service 待清洁中 → 1分钟后自动打扫）。
