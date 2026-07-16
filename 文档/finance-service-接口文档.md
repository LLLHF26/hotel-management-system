# finance-service 接口文档

> 负责营收统计、收入明细、退款记录、报表导出。数据来源为 order-service 的订单和支付数据，finance-service 自身做汇总与查询。

---

## 一、营收统计

### 1.1 营收概览

```
GET /api/finance/revenue/summary
```

> 返回今日、本月、本年的营收汇总，供管理端首页仪表盘使用。

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "today": {
      "date": "2026-05-17",
      "roomRevenue": 8232.00,
      "extraRevenue": 456.00,
      "totalRevenue": 8688.00,
      "orderCount": 14,
      "checkInCount": 12,
      "checkOutCount": 10,
      "occupancyRate": "75.0%"
    },
    "thisMonth": {
      "month": "2026-05",
      "roomRevenue": 246960.00,
      "extraRevenue": 13680.00,
      "totalRevenue": 260640.00,
      "orderCount": 420,
      "avgDailyRevenue": 15331.76
    },
    "thisYear": {
      "year": "2026",
      "roomRevenue": 1234800.00,
      "extraRevenue": 68400.00,
      "totalRevenue": 1303200.00,
      "orderCount": 2100
    }
  }
}
```

---

### 1.2 日营收趋势（给 ECharts 折线图）

```
GET /api/finance/revenue/daily?startDate=2026-05-01&endDate=2026-05-17
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | string | 是 | 起始日期 |
| endDate | string | 是 | 截止日期 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "dates": ["2026-05-01", "2026-05-02", "...", "2026-05-17"],
    "roomRevenue": [15200, 14800, "...", 8232],
    "extraRevenue": [800, 750, "...", 456],
    "totalRevenue": [16000, 15550, "...", 8688],
    "orderCount": [25, 24, "...", 14]
  }
}
```

> 四个数组按日期一一对应，前端直接填入 ECharts 的 series。

---

### 1.3 月营收统计

```
GET /api/finance/revenue/monthly?year=2026
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 年份，默认当前年 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "month": "01",
      "roomRevenue": 235600.00,
      "extraRevenue": 14200.00,
      "totalRevenue": 249800.00,
      "orderCount": 398
    },
    {
      "month": "05",
      "roomRevenue": 246960.00,
      "extraRevenue": 13680.00,
      "totalRevenue": 260640.00,
      "orderCount": 420
    }
  ]
}
```

---

### 1.4 自定义范围营收汇总

```
GET /api/finance/revenue/range?startDate=2026-05-01&endDate=2026-05-17
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | string | 是 | 起始日期 |
| endDate | string | 是 | 截止日期 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "startDate": "2026-05-01",
    "endDate": "2026-05-17",
    "roomRevenue": 139944.00,
    "extraRevenue": 7752.00,
    "totalRevenue": 147696.00,
    "orderCount": 238,
    "checkInCount": 210,
    "checkOutCount": 198,
    "avgOccupancyRate": "72.3%",
    "paymentBreakdown": {
      "cash": 22000.00,
      "alipay": 35680.00,
      "wechat": 78016.00,
      "card": 12000.00
    }
  }
}
```

---

## 二、收入明细

### 2.1 收入明细列表

```
GET /api/finance/revenue/detail?page=1&size=20&startDate=&endDate=&paymentMethod=&orderNo=&roomNumber=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| startDate | string | 否 | 支付时间起始 |
| endDate | string | 否 | 支付时间截止 |
| paymentMethod | string | 否 | CASH / ALIPAY / WECHAT / CARD |
| orderNo | string | 否 | 按订单编号搜索 |
| roomNumber | string | 否 | 按房间号搜索 |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "total": 560,
    "page": 1,
    "size": 20,
    "records": [
      {
        "paymentNo": "PAY202605170001",
        "orderNo": "ORD202605170001",
        "roomNumber": "301",
        "roomTypeName": "豪华大床房",
        "customerName": "张三",
        "amount": 1176.00,
        "method": "WECHAT",
        "methodName": "微信支付",
        "type": "房费",
        "paidAt": "2026-05-17T14:05:00"
      }
    ]
  }
}
```

---

### 2.2 按支付方式统计

```
GET /api/finance/revenue/by-payment-method?startDate=2026-05-01&endDate=2026-05-17
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    { "method": "WECHAT", "methodName": "微信支付", "amount": 78016.00, "count": 145, "rate": "52.8%" },
    { "method": "ALIPAY", "methodName": "支付宝",   "amount": 35680.00, "count": 68,  "rate": "24.2%" },
    { "method": "CASH",   "methodName": "现金",     "amount": 22000.00, "count": 42,  "rate": "14.9%" },
    { "method": "CARD",   "methodName": "银行卡",   "amount": 12000.00, "count": 23,  "rate": "8.1%" }
  ]
}
```

> `rate` 为该支付方式金额占总收入的百分比。

---

### 2.3 房型收入排名

```
GET /api/finance/revenue/by-room-type?startDate=2026-05-01&endDate=2026-05-17
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "roomTypeId": 1,
      "roomTypeName": "豪华大床房",
      "roomCount": 20,
      "revenue": 58800.00,
      "orderCount": 100,
      "occupancyRate": "78.2%",
      "rank": 1
    },
    {
      "roomTypeId": 2,
      "roomTypeName": "商务双床房",
      "roomCount": 15,
      "revenue": 42000.00,
      "orderCount": 80,
      "occupancyRate": "71.5%",
      "rank": 2
    }
  ]
}
```

---

## 三、入住率分析

### 3.1 入住率趋势（给 ECharts）

```
GET /api/finance/analysis/occupancy?startDate=2026-05-01&endDate=2026-05-17&type=day
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | string | 是 | 起始日期 |
| endDate | string | 是 | 截止日期 |
| type | string | 否 | day=按日 / month=按月，默认 day |

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "dates": ["2026-05-01", "...", "2026-05-17"],
    "occupancyRates": ["80.0%", "...", "75.0%"],
    "availableRooms": 80,
    "avgOccupancyRate": "72.3%"
  }
}
```

### 3.2 房型入住率对比

```
GET /api/finance/analysis/occupancy-by-type?startDate=2026-05-01&endDate=2026-05-17
```

**响应：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "roomTypeName": "豪华大床房",
      "totalRooms": 20,
      "occupiedNights": 266,
      "totalNights": 340,
      "occupancyRate": "78.2%"
    }
  ]
}
```

---

## 四、退款记录

### 4.1 退款记录列表

```
GET /api/finance/refunds?page=1&size=10&startDate=&endDate=&status=&keyword=
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| startDate | string | 否 | 退款时间起始 |
| endDate | string | 否 | 退款时间截止 |
| status | string | 否 | 审核中 / 成功 / 驳回 |
| keyword | string | 否 | 按订单号搜索 |

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
        "id": 10,
        "orderId": 1050,
        "orderNo": "ORD202605150032",
        "paymentNo": "PAY202605150032",
        "refundAmount": 1176.00,
        "reason": "行程变更",
        "status": "成功",
        "operatorName": "系统管理员",
        "createTime": "2026-05-16T10:30:00"
      }
    ]
  }
}
```

---

### 4.2 退款记录详情

```
GET /api/finance/refund/{id}
```

**响应：** 同列表字段，增加原支付信息：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 10,
    "orderId": 1050,
    "orderNo": "ORD202605150032",
    "paymentNo": "PAY202605150032",
    "originalAmount": 1176.00,
    "originalMethod": "WECHAT",
    "refundAmount": 1176.00,
    "reason": "行程变更",
    "status": "成功",
    "operatorName": "系统管理员",
    "createTime": "2026-05-16T10:30:00"
  }
}
```

---

## 五、报表导出

### 5.1 导出营收报表

```
GET /api/finance/report/export?type=revenue&startDate=2026-05-01&endDate=2026-05-17&format=xlsx
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | revenue=营收报表 / order=订单明细 / refund=退款汇总 |
| startDate | string | 是 | 起始日期 |
| endDate | string | 是 | 截止日期 |
| format | string | 否 | xlsx（Excel）/ pdf，默认 xlsx |

**响应：** 文件流（`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`）

> 报表包含：日期、房费收入、其他收入、总收入、订单数、入住率、各支付方式金额等列。

---

### 5.2 导出订单明细

```
GET /api/finance/report/export?type=order&startDate=2026-05-01&endDate=2026-05-17&format=xlsx
```

> 导出指定日期范围内的全部订单明细（订单号、客户、房型、房号、入住/退房日期、金额、支付方式、状态等）。

---

### 5.3 导出退款汇总

```
GET /api/finance/report/export?type=refund&startDate=2026-05-01&endDate=2026-05-17&format=xlsx
```

> 导出指定日期范围内的退款记录汇总。

---

## 六、数据来源说明

> finance-service 自身不存储订单和支付的原始数据，通过以下方式获取：

| 数据 | 来源 | 方式 |
|------|------|------|
| 日营收汇总 | 自身 `daily_revenue` 表 | 每日凌晨定时任务从 order-service + payment 聚合写入 |
| 收入明细 | order-service | OpenFeign 查询订单 + 支付记录 |
| 退款记录 | 自身 `refund_record` 表 | order-service 退款时通过 Feign 同步写入 |
| 入住率 | order-service + room-service | 综合计算：入住房间数 ÷ 总可售房间数 |

### 日结定时任务

```
每日 00:05 执行：
1. 查询昨日已完成/已入住的订单（来自 order-service）
2. 查询昨日支付成功的 payment
3. 汇总写入 daily_revenue 表
4. 确保日结数据落库，后续查询不再依赖跨服务调用
```

---

## 七、接口汇总

```
营收统计
GET    /api/finance/revenue/summary         营收概览（今日/本月/本年）
GET    /api/finance/revenue/daily           日营收趋势（ECharts 数组格式）
GET    /api/finance/revenue/monthly         月营收统计
GET    /api/finance/revenue/range           自定义范围营收汇总

收入明细
GET    /api/finance/revenue/detail          收入明细列表
GET    /api/finance/revenue/by-payment-method 按支付方式统计（含占比）
GET    /api/finance/revenue/by-room-type    房型收入排名

入住率分析
GET    /api/finance/analysis/occupancy      入住率趋势（ECharts）
GET    /api/finance/analysis/occupancy-by-type 各房型入住率对比

退款记录
GET    /api/finance/refunds                 退款记录列表
GET    /api/finance/refund/{id}             退款详情
POST   /api/finance/refund/add              同步退款记录（Feign 回调，由 order-service 调用）

报表导出
GET    /api/finance/report/export           导出 Excel/PDF 报表
```

> 共 **13 个接口**。收入明细类接口实时查 order-service，日结汇总通过凌晨定时任务写入 daily_revenue 表。
