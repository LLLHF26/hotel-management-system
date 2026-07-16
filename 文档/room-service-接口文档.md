# room-service 接口文档

> 负责房型管理、房间管理、六状态房态维护、打扫调度、维修记录。是系统中数据最密集的服务。

---

## 一、房型管理

### 1.1 房型列表

```
GET /api/room/type/list?page=1&size=10&keyword=&bedType=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| keyword | string | 否 | 按房型名称模糊搜索 |
| bedType | string | 否 | 按床型筛选：大床 / 双床 / 套房 |

> 客户端免登录也可调用，用于酒店浏览页展示房型。

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 6,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "name": "豪华大床房",
        "description": "35㎡，独立卫浴，城市景观",
        "area": 35,
        "bedType": "大床",
        "maxGuests": 2,
        "price": 588.00,
        "coverImage": "https://img.hotel.com/room/deluxe-king.jpg",
        "amenities": "WiFi,空调,浴缸,迷你吧",
        "sortOrder": 1
      }
    ]
  }
}
```

---

### 1.2 房型详情

```
GET /api/room/type/{id}
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1,
    "name": "豪华大床房",
    "description": "35㎡，独立卫浴，城市景观",
    "area": 35,
    "bedType": "大床",
    "maxGuests": 2,
    "price": 588.00,
    "coverImage": "https://img.hotel.com/room/deluxe-king.jpg",
    "images": [
      "https://img.hotel-backend.com/room/deluxe-king-1.jpg",
      "https://img.hotel-backend.com/room/deluxe-king-2.jpg"
    ],
    "amenities": "WiFi,空调,浴缸,迷你吧",
    "sortOrder": 1,
    "createTime": "2026-01-01T00:00:00",
    "updateTime": "2026-03-15T10:00:00"
  }
}
```

---

### 1.3 创建房型

```
POST /api/room/type/create
Authorization: Bearer {Token}     (需 ADMIN)
Content-Type: application/json

{
  "name": "豪华大床房",
  "description": "35㎡，独立卫浴，城市景观",
  "area": 35,
  "bedType": "大床",
  "maxGuests": 2,
  "price": 588.00,
  "coverImage": "https://img.hotel.com/room/deluxe-king.jpg",
  "images": [
    "https://img.hotel.com/room/deluxe-king-1.jpg",
    "https://img.hotel.com/room/deluxe-king-2.jpg"
  ],
  "amenities": "WiFi,空调,浴缸,迷你吧",
  "sortOrder": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 房型名称 |
| price | decimal | 是 | 标准价格（元/晚） |
| description | string | 否 | 简要描述 |
| area | int | 否 | 面积（㎡） |
| bedType | string | 否 | 大床 / 双床 / 套房 |
| maxGuests | int | 否 | 最大入住人数，默认 2 |
| coverImage | string | 否 | 封面图 URL |
| images | array | 否 | 图片列表 |
| amenities | string | 否 | 设施（逗号分隔） |
| sortOrder | int | 否 | 排序权重，默认 0 |

**响应：** `{ "code": 200, "msg": "创建成功", "data": { "id": 2 } }`

---

### 1.4 修改房型

```
PUT /api/room/type/{id}
Authorization: Bearer {Token}     (需 ADMIN)
Content-Type: application/json

{
  "name": "豪华大床房（升级版）",
  "price": 688.00,
  "description": "...",
  "area": 38,
  "bedType": "大床",
  "maxGuests": 2,
  "coverImage": "...",
  "images": [...],
  "amenities": "WiFi,空调,浴缸,迷你吧,智能音箱",
  "sortOrder": 1
}
```

> 所有字段均可选填，仅更新传入的字段。

**响应：** `{ "code": 200, "msg": "修改成功", "data": null }`

---

### 1.5 删除房型

```
DELETE /api/room/type/{id}
Authorization: Bearer {Token}     (需 ADMIN)
```

> 逻辑删除。如果该房型下仍有房间在用，返回错误。

**响应：** `{ "code": 200, "msg": "删除成功", "data": null }`

**错误码：** `400` 该房型下还有房间，无法删除

---

## 二、房间管理

### 2.1 房间列表

```
GET /api/room/list?page=1&size=10&roomTypeId=&status=&floor=&keyword=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| roomTypeId | long | 否 | 按房型筛选 |
| status | string | 否 | 按状态筛选（六状态之一） |
| floor | int | 否 | 按楼层筛选 |
| keyword | string | 否 | 按房间编号模糊搜索 |

> 客户端免登录也可调用，仅返回"空闲中"房间供预订浏览。

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 80,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 5,
        "roomNumber": "301",
        "roomTypeId": 1,
        "roomTypeName": "豪华大床房",
        "floor": 3,
        "status": "空闲中",
        "price": 588.00,
        "description": "朝南，阳光充足",
        "createTime": "2026-01-01T00:00:00",
        "updateTime": "2026-05-17T08:00:00"
      }
    ]
  }
}
```

---

### 2.2 房间详情

```
GET /api/room/{id}
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 5,
    "roomNumber": "301",
    "roomTypeId": 1,
    "roomTypeName": "豪华大床房",
    "floor": 3,
    "status": "打扫中",
    "price": 588.00,
    "description": "朝南，阳光充足",
    "currentTask": {
      "taskId": 1001,
      "type": "cleaning",
      "cleanerName": "保洁张阿姨",
      "startTime": "2026-05-17T09:00:00",
      "remainMinutes": 3
    },
    "createTime": "2026-01-01T00:00:00",
    "updateTime": "2026-05-17T08:00:00"
  }
}
```

> 当房间状态为"打扫中"时，`currentTask` 返回当前打扫信息；状态为"维修中"时返回维修信息；其余状态该字段为 null。

---

### 2.3 创建房间

```
POST /api/room/create
Authorization: Bearer {Token}     (需 ADMIN)
Content-Type: application/json

{
  "roomNumber": "401",
  "roomTypeId": 2,
  "floor": 4,
  "description": "新装修，朝南"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomNumber | string | 是 | 房间编号，全局唯一 |
| roomTypeId | long | 是 | 房型 ID |
| floor | int | 否 | 楼层 |
| description | string | 否 | 备注 |

**响应：** `{ "code": 200, "msg": "创建成功", "data": { "id": 81 } }`

**错误码：** `400` 房间编号已存在

---

### 2.4 修改房间

```
PUT /api/room/{id}
Authorization: Bearer {Token}     (需 ADMIN)
Content-Type: application/json

{
  "roomNumber": "401",
  "roomTypeId": 2,
  "floor": 5,
  "description": "调至五楼"
}
```

**响应：** `{ "code": 200, "msg": "修改成功", "data": null }`

---

### 2.5 删除房间

```
DELETE /api/room/{id}
Authorization: Bearer {Token}     (需 ADMIN)
```

> 逻辑删除。仅"空闲中"和"维修中"状态的房间可删除。

**响应：** `{ "code": 200, "msg": "删除成功", "data": null }`

**错误码：** `400` 房间当前状态不允许删除

---

## 三、房态管理

### 3.1 房态看板

> 前台管理端核心接口，按状态分组展示所有房间。

```
GET /api/room/dashboard?floor=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| floor | int | 否 | 按楼层筛选，不传则全部 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "summary": {
      "total": 80,
      "空闲中": 20,
      "预订中": 15,
      "入住中": 30,
      "待清洁中": 8,
      "打扫中": 5,
      "维修中": 2
    },
    "rooms": [
      {
        "id": 5,
        "roomNumber": "301",
        "roomTypeName": "豪华大床房",
        "floor": 3,
        "status": "打扫中",
        "price": 588.00,
        "cleanerName": "保洁张阿姨",
        "taskStartTime": "2026-05-17T09:00:00"
      }
    ]
  }
}
```

> `rooms` 为全部房间列表，前端按 `status` 分组渲染卡片。打扫中的房间额外展示保洁员姓名和开始时间。

---

### 3.2 按状态筛选房间

```
GET /api/room/status/{status}?page=1&size=20
```

| status 可选值 | 说明 |
|--------------|------|
| 空闲中 | 可售 |
| 预订中 | 已预订未入住 |
| 入住中 | 有客 |
| 待清洁中 | 退房后等待打扫 |
| 打扫中 | 保洁打扫中 |
| 维修中 | 维修 |

**响应：** 同房间列表格式。

---

### 3.3 手动变更房态

```
PUT /api/room/{id}/status
Authorization: Bearer {Token}     (需 ADMIN 或 FRONT_DESK)
Content-Type: application/json

{
  "status": "维修中",
  "reason": "空调故障需要维修"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 是 | 目标状态 |
| reason | string | 否 | 操作原因（设为"维修中"时必填） |

> 后台校验状态跳转合法性。例如：不可从"打扫中"直接跳到"维修中"。

**合法跳转表：**

| 当前状态 | 可变为 |
|---------|--------|
| 空闲中 | 预订中、维修中 |
| 预订中 | 入住中、空闲中 |
| 入住中 | 待清洁中 |
| 待清洁中 | 打扫中 |
| 打扫中 | 空闲中 |
| 维修中 | 空闲中 |

**响应：** `{ "code": 200, "msg": "房态已变更为 维修中", "data": null }`

**错误码：** `400` 非法状态跳转（如"打扫中 → 维修中"）

---

## 四、打扫管理

### 4.1 打扫任务列表

```
GET /api/room/cleaning/tasks?page=1&size=10&status=&cleanerId=&date=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| status | string | 否 | 打扫中 / 已完成，不传则全部 |
| cleanerId | long | 否 | 按保洁员筛选 |
| date | string | 否 | 按日期筛选（格式：2026-05-17） |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 320,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1001,
        "roomId": 5,
        "roomNumber": "301",
        "cleanerId": 3,
        "cleanerName": "保洁张阿姨",
        "status": "打扫中",
        "startTime": "2026-05-17T09:00:00",
        "endTime": null
      },
      {
        "id": 1000,
        "roomId": 3,
        "roomNumber": "205",
        "cleanerId": 4,
        "cleanerName": "保洁李阿姨",
        "status": "已完成",
        "startTime": "2026-05-17T08:30:00",
        "endTime": "2026-05-17T08:35:00"
      }
    ]
  }
}
```

---

### 4.2 打扫任务详情

```
GET /api/room/cleaning/task/{id}
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1001,
    "roomId": 5,
    "roomNumber": "301",
    "roomTypeName": "豪华大床房",
    "cleanerId": 3,
    "cleanerName": "保洁张阿姨",
    "status": "已完成",
    "startTime": "2026-05-17T09:00:00",
    "endTime": "2026-05-17T09:05:00",
    "durationMinutes": 5
  }
}
```

---

### 4.3 手动分配保洁（管理员）

```
POST /api/room/{id}/cleaning/assign
Authorization: Bearer {Token}     (需 ADMIN 或 FRONT_DESK)
Content-Type: application/json

{
  "cleanerId": 3
}
```

> 覆盖定时任务的自动分配。房间必须处于"待清洁中"状态。

**前置条件：** 房间 status = 待清洁中

**响应：** `{ "code": 200, "msg": "已分配保洁张阿姨", "data": { "taskId": 1001 } }`

**错误码：** `400` 房间不是待清洁中状态

---

### 4.4 保洁员当前任务

```
GET /api/room/cleaning/tasks/cleaner/{cleanerId}/active
```

> 查询某保洁员当前正在进行中的打扫任务。

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "taskId": 1001,
      "roomId": 5,
      "roomNumber": "301",
      "status": "打扫中",
      "startTime": "2026-05-17T09:00:00",
      "elapsedMinutes": 2
    }
  ]
}
```

---

## 五、维修管理

### 5.1 设为维修中

```
POST /api/room/{id}/maintenance/start
Authorization: Bearer {Token}     (需 ADMIN)
Content-Type: application/json

{
  "reason": "空调制冷故障，需要维修"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 是 | 维修原因 |

> 内部操作：① room.status → "维修中"，② 写入 maintenance_record。

**前置条件：** 房间 status = 空闲中

**响应：** `{ "code": 200, "msg": "已设为维修中", "data": { "recordId": 10 } }`

**错误码：** `400` 仅空闲中的房间可设为维修中

---

### 5.2 维修完成

```
POST /api/room/{id}/maintenance/complete
Authorization: Bearer {Token}     (需 ADMIN)
Content-Type: application/json

{
  "cost": 200.00
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cost | decimal | 否 | 维修费用 |

> 内部操作：① room.status → "空闲中"，② maintenance_record.status → "已完成" + end_time。

**响应：** `{ "code": 200, "msg": "维修完成，房间已恢复空闲", "data": null }`

---

### 5.3 维修记录列表

```
GET /api/room/maintenance/records?page=1&size=10&roomId=&status=&startDate=&endDate=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| roomId | long | 否 | 按房间筛选 |
| status | string | 否 | 维修中 / 已完成 |
| startDate | string | 否 | 开始日期范围起 |
| endDate | string | 否 | 开始日期范围止 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 15,
    "records": [
      {
        "id": 10,
        "roomId": 5,
        "roomNumber": "301",
        "reason": "空调制冷故障，需要维修",
        "status": "已完成",
        "startTime": "2026-05-16T10:00:00",
        "endTime": "2026-05-16T15:00:00",
        "cost": 200.00
      }
    ]
  }
}
```

---

## 六、内部定时任务说明

> 以下逻辑由 `@Scheduled` 在 room-service 内部自动执行，不暴露为 HTTP 接口。

### 打扫调度（每 10 秒扫描）

```
扫描 room 表
  ├── status = '待清洁中' 且 update_time 距现在 > 1 分钟
  │      → 从 user-service 获取保洁员列表
  │      → 选 currentTaskCount 最小的
  │      → room.status → '打扫中'
  │      → 写入 cleaning_task（start_time = now）
  │
  └── status = '打扫中' 且 cleaning_task.start_time 距现在 > 5 分钟
         → room.status → '空闲中'
         → cleaning_task.status → '已完成'（end_time = now）
```

---

## 七、供外部调用的 Feign 接口

> 以下接口由 common-service 定义 Feign 声明，供 order-service 等调用。

### 7.1 退房后置为待清洁

```
PUT /api/room/{id}/status
（order-service 通过 OpenFeign 调用，同 3.3 手动变更接口）
```

### 7.2 预订时锁定房间

```
PUT /api/room/{id}/status
{ "status": "预订中" }
```

> 由 order-service 创建订单成功后调用。

---

## 八、文件上传（OSS）

> 图片存储于阿里云 OSS，上传后返回 HTTPS URL，将 URL 填入房型创建/修改接口的 `coverImage` 或 `images` 字段。

### 8.1 上传封面图

```
POST /api/room/upload/cover
Content-Type: multipart/form-data

Form 参数:
  file: (binary) 图片文件（支持 jpg/png/webp/gif）
```

**响应：**
```json
{
  "code": 200,
  "msg": "上传成功",
  "data": { "url": "https://gulimall-hello-lhf.oss-cn-beijing.aliyuncs.com/hotel/room-images/a1b2c3d4.jpg" }
}
```

### 8.2 上传轮播图

```
POST /api/room/upload/image
Content-Type: multipart/form-data

Form 参数:
  file: (binary) 图片文件（支持 jpg/png/webp/gif）
```

**响应：** 同 8.1

> **使用方式：** 先调用上传接口获取 URL，再将 URL 作为 `coverImage`（字符串）或 `images`（字符串数组）传入 `POST /api/room/type/create` 或 `PUT /api/room/type/{id}`。

---

## 九、接口汇总

```
房型管理
GET    /api/room/type/list                列表（客户端免登录）
GET    /api/room/type/{id}                详情
POST   /api/room/type/create              创建（ADMIN）
PUT    /api/room/type/{id}                修改（ADMIN）
DELETE /api/room/type/{id}                删除（ADMIN）

文件上传
POST   /api/room/upload/cover             上传封面图
POST   /api/room/upload/image             上传轮播图

房间管理
GET    /api/room/list                     列表（客户端免登录仅见空闲中）
GET    /api/room/{id}                     详情（含当前打扫/维修信息）
POST   /api/room/create                   创建（ADMIN）
PUT    /api/room/{id}                     修改（ADMIN）
DELETE /api/room/{id}                     删除（ADMIN）

房态管理
GET    /api/room/dashboard                房态看板（前台核心接口）
GET    /api/room/status/{status}          按状态查房间
PUT    /api/room/{id}/status              手动变更房态（含合法跳转校验）

打扫管理
GET    /api/room/cleaning/tasks           打扫任务列表
GET    /api/room/cleaning/task/{id}       打扫任务详情
POST   /api/room/{id}/cleaning/assign     手动分配保洁
GET    /api/room/cleaning/tasks/cleaner/{cleanerId}/active   保洁员当前任务

维修管理
POST   /api/room/{id}/maintenance/start   设为维修中（ADMIN）
POST   /api/room/{id}/maintenance/complete 维修完成（ADMIN）
GET    /api/room/maintenance/records      维修记录列表
```

> 共 **23 个接口**。内部定时任务（打扫 1 分钟延迟 + 5 分钟完成）由 @Scheduled 自动执行，不走 HTTP。
