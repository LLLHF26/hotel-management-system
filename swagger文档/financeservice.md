# finance-service


**简介**:finance-service


**HOST**:http://localhost:8084


**联系人**:hotel


**Version**:1.0.0


**接口路径**:/v3/api-docs


[TOC]






# 数据分析


## 入住率趋势分析


**接口地址**:`/api/finance/analysis/occupancy`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||
|type|统计粒度（day/week/month，默认day）|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultOccupancyTrendVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||OccupancyTrendVO|OccupancyTrendVO|
|&emsp;&emsp;dates|日期列表|array|string|
|&emsp;&emsp;occupancyRates|入住率列表|array|string|
|&emsp;&emsp;availableRooms|可用房间数|integer(int32)||
|&emsp;&emsp;avgOccupancyRate|平均入住率|string||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"dates": [],
		"occupancyRates": [],
		"availableRooms": 0,
		"avgOccupancyRate": ""
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 各房型入住率统计


**接口地址**:`/api/finance/analysis/occupancy-by-type`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListOccupancyByTypeVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|OccupancyByTypeVO|
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;totalRooms|总房间数|integer(int32)||
|&emsp;&emsp;occupiedNights|已入住间夜数|integer(int32)||
|&emsp;&emsp;totalNights|总间夜数|integer(int32)||
|&emsp;&emsp;occupancyRate|入住率|string||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"roomTypeName": "",
			"totalRooms": 0,
			"occupiedNights": 0,
			"totalNights": 0,
			"occupancyRate": ""
		}
	]
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


# 退款管理


## 新增退款记录


**接口地址**:`/api/finance/refund/add`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "orderId": 0,
  "orderNo": "",
  "paymentNo": "",
  "refundAmount": 0,
  "reason": "",
  "status": "",
  "operatorName": "",
  "createTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|refundRecordVO|退款记录信息|body|true|RefundRecordVO|RefundRecordVO|
|&emsp;&emsp;id|退款记录ID||false|integer(int64)||
|&emsp;&emsp;orderId|订单ID||false|integer(int64)||
|&emsp;&emsp;orderNo|订单编号||false|string||
|&emsp;&emsp;paymentNo|支付编号||false|string||
|&emsp;&emsp;refundAmount|退款金额||false|number||
|&emsp;&emsp;reason|退款原因||false|string||
|&emsp;&emsp;status|退款状态||false|string||
|&emsp;&emsp;operatorName|操作员姓名||false|string||
|&emsp;&emsp;createTime|创建时间||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 退款记录列表


**接口地址**:`/api/finance/refunds`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码（默认1）|query|false|integer(int32)||
|size|每页条数（默认10）|query|false|integer(int32)||
|startDate|开始日期|query|false|string||
|endDate|结束日期|query|false|string||
|status|退款状态|query|false|string||
|keyword|搜索关键字|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultRefundRecordVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultRefundRecordVO|PageResultRefundRecordVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|RefundRecordVO|
|&emsp;&emsp;&emsp;&emsp;id|退款记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;orderId|订单ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;refundAmount|退款金额|number||
|&emsp;&emsp;&emsp;&emsp;reason|退款原因|string||
|&emsp;&emsp;&emsp;&emsp;status|退款状态|string||
|&emsp;&emsp;&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"total": 0,
		"page": 0,
		"size": 0,
		"records": [
			{
				"id": 0,
				"orderId": 0,
				"orderNo": "",
				"paymentNo": "",
				"refundAmount": 0,
				"reason": "",
				"status": "",
				"operatorName": "",
				"createTime": ""
			}
		]
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 退款详情


**接口地址**:`/api/finance/refund/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|退款记录ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultRefundDetailVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||RefundDetailVO|RefundDetailVO|
|&emsp;&emsp;id|退款记录ID|integer(int64)||
|&emsp;&emsp;orderId|订单ID|integer(int64)||
|&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;originalAmount|原支付金额|number||
|&emsp;&emsp;originalMethod|原支付方式|string||
|&emsp;&emsp;refundAmount|退款金额|number||
|&emsp;&emsp;reason|退款原因|string||
|&emsp;&emsp;status|退款状态|string||
|&emsp;&emsp;operatorName|操作人|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"orderId": 0,
		"orderNo": "",
		"paymentNo": "",
		"originalAmount": 0,
		"originalMethod": "",
		"refundAmount": 0,
		"reason": "",
		"status": "",
		"operatorName": "",
		"createTime": ""
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


# 报表管理


## 导出财务报表


**接口地址**:`/api/finance/report/export`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|type|报表类型|query|true|string||
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||
|format|导出格式（默认xlsx）|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


# 营收管理


## 营收总览


**接口地址**:`/api/finance/revenue/summary`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultRevenueSummaryVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||RevenueSummaryVO|RevenueSummaryVO|
|&emsp;&emsp;today||TodaySummary|TodaySummary|
|&emsp;&emsp;&emsp;&emsp;date|日期|string||
|&emsp;&emsp;&emsp;&emsp;roomRevenue|房费收入|number||
|&emsp;&emsp;&emsp;&emsp;extraRevenue|额外消费收入|number||
|&emsp;&emsp;&emsp;&emsp;totalRevenue|总收入|number||
|&emsp;&emsp;&emsp;&emsp;orderCount|订单数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;checkInCount|入住数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;checkOutCount|退房数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;occupancyRate|入住率|string||
|&emsp;&emsp;thisMonth||MonthSummary|MonthSummary|
|&emsp;&emsp;&emsp;&emsp;month|月份|string||
|&emsp;&emsp;&emsp;&emsp;roomRevenue|房费收入|number||
|&emsp;&emsp;&emsp;&emsp;extraRevenue|额外消费收入|number||
|&emsp;&emsp;&emsp;&emsp;totalRevenue|总收入|number||
|&emsp;&emsp;&emsp;&emsp;orderCount|订单数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;avgDailyRevenue|日均收入|number||
|&emsp;&emsp;thisYear||YearSummary|YearSummary|
|&emsp;&emsp;&emsp;&emsp;year|年份|string||
|&emsp;&emsp;&emsp;&emsp;roomRevenue|房费收入|number||
|&emsp;&emsp;&emsp;&emsp;extraRevenue|额外消费收入|number||
|&emsp;&emsp;&emsp;&emsp;totalRevenue|总收入|number||
|&emsp;&emsp;&emsp;&emsp;orderCount|订单数|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"today": {
			"date": "",
			"roomRevenue": 0,
			"extraRevenue": 0,
			"totalRevenue": 0,
			"orderCount": 0,
			"checkInCount": 0,
			"checkOutCount": 0,
			"occupancyRate": ""
		},
		"thisMonth": {
			"month": "",
			"roomRevenue": 0,
			"extraRevenue": 0,
			"totalRevenue": 0,
			"orderCount": 0,
			"avgDailyRevenue": 0
		},
		"thisYear": {
			"year": "",
			"roomRevenue": 0,
			"extraRevenue": 0,
			"totalRevenue": 0,
			"orderCount": 0
		}
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 时间段营收概览


**接口地址**:`/api/finance/revenue/range`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultRevenueRangeVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||RevenueRangeVO|RevenueRangeVO|
|&emsp;&emsp;startDate|开始日期|string||
|&emsp;&emsp;endDate|结束日期|string||
|&emsp;&emsp;roomRevenue|房费收入|number||
|&emsp;&emsp;extraRevenue|额外消费收入|number||
|&emsp;&emsp;totalRevenue|总收入|number||
|&emsp;&emsp;orderCount|订单数|integer(int32)||
|&emsp;&emsp;checkInCount|入住数|integer(int32)||
|&emsp;&emsp;checkOutCount|退房数|integer(int32)||
|&emsp;&emsp;avgOccupancyRate|平均入住率|string||
|&emsp;&emsp;paymentBreakdown|支付方式分项|object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"startDate": "",
		"endDate": "",
		"roomRevenue": 0,
		"extraRevenue": 0,
		"totalRevenue": 0,
		"orderCount": 0,
		"checkInCount": 0,
		"checkOutCount": 0,
		"avgOccupancyRate": "",
		"paymentBreakdown": {}
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 月度营收统计


**接口地址**:`/api/finance/revenue/monthly`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|year|统计年份（默认当前年份）|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListMonthlyRevenueItemVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|MonthlyRevenueItemVO|
|&emsp;&emsp;month|月份|string||
|&emsp;&emsp;roomRevenue|房费收入|number||
|&emsp;&emsp;extraRevenue|额外消费收入|number||
|&emsp;&emsp;totalRevenue|总收入|number||
|&emsp;&emsp;orderCount|订单数|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"month": "",
			"roomRevenue": 0,
			"extraRevenue": 0,
			"totalRevenue": 0,
			"orderCount": 0
		}
	]
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 营收明细列表


**接口地址**:`/api/finance/revenue/detail`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码（默认1）|query|false|integer(int32)||
|size|每页条数（默认20）|query|false|integer(int32)||
|startDate|开始日期|query|false|string||
|endDate|结束日期|query|false|string||
|paymentMethod|支付方式|query|false|string||
|orderNo|订单号|query|false|string||
|roomNumber|房间号|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultRevenueDetailVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultRevenueDetailVO|PageResultRevenueDetailVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|RevenueDetailVO|
|&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;customerName|客户姓名|string||
|&emsp;&emsp;&emsp;&emsp;amount|金额|number||
|&emsp;&emsp;&emsp;&emsp;method|支付方式代码|string||
|&emsp;&emsp;&emsp;&emsp;methodName|支付方式名称|string||
|&emsp;&emsp;&emsp;&emsp;type|类型（收入或退款）|string||
|&emsp;&emsp;&emsp;&emsp;paidAt|支付时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"total": 0,
		"page": 0,
		"size": 0,
		"records": [
			{
				"paymentNo": "",
				"orderNo": "",
				"roomNumber": "",
				"roomTypeName": "",
				"customerName": "",
				"amount": 0,
				"method": "",
				"methodName": "",
				"type": "",
				"paidAt": ""
			}
		]
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 每日营收趋势


**接口地址**:`/api/finance/revenue/daily`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultDailyTrendVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||DailyTrendVO|DailyTrendVO|
|&emsp;&emsp;dates|日期列表|array|string|
|&emsp;&emsp;roomRevenue|房费收入列表|array|number|
|&emsp;&emsp;extraRevenue|额外消费收入列表|array|number|
|&emsp;&emsp;totalRevenue|总收入列表|array|number|
|&emsp;&emsp;orderCount|订单数列表|array|integer(int32)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"dates": [],
		"roomRevenue": [],
		"extraRevenue": [],
		"totalRevenue": [],
		"orderCount": []
	}
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 按房型统计营收


**接口地址**:`/api/finance/revenue/by-room-type`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListRoomTypeRevenueVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|RoomTypeRevenueVO|
|&emsp;&emsp;roomTypeId|房型ID|integer(int64)||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;orderCount|订单数量|integer(int32)||
|&emsp;&emsp;revenue|总营收|number||
|&emsp;&emsp;roomCount|房间数量|integer(int32)||
|&emsp;&emsp;occupancyRate|入住率|string||
|&emsp;&emsp;rank|排名|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"roomTypeId": 0,
			"roomTypeName": "",
			"orderCount": 0,
			"revenue": 0,
			"roomCount": 0,
			"occupancyRate": "",
			"rank": 0
		}
	]
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


## 按支付方式统计营收


**接口地址**:`/api/finance/revenue/by-payment-method`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|true|string||
|endDate|结束日期|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListPaymentMethodStatVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|PaymentMethodStatVO|
|&emsp;&emsp;method|支付方式代码|string||
|&emsp;&emsp;methodName|支付方式名称|string||
|&emsp;&emsp;amount|金额|number||
|&emsp;&emsp;count|笔数|integer(int32)||
|&emsp;&emsp;rate|占比|string||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"method": "",
			"methodName": "",
			"amount": 0,
			"count": 0,
			"rate": ""
		}
	]
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {}
}
```