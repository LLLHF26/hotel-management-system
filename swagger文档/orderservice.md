# order-service


**简介**:order-service


**HOST**:http://localhost:8083


**联系人**:hotel


**Version**:1.0.0


**接口路径**:/v3/api-docs


[TOC]






# 额外消费管理


## 添加额外消费（如餐饮、洗衣等）


**接口地址**:`/api/order/{id}/extra`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "itemName": "",
  "amount": 0,
  "quantity": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|extraDTO|额外消费信息|body|true|ExtraDTO|ExtraDTO|
|&emsp;&emsp;itemName|项目名称||true|string||
|&emsp;&emsp;amount|金额||true|number||
|&emsp;&emsp;quantity|数量||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


## 订单额外消费列表


**接口地址**:`/api/order/{id}/extras`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListExtraVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|ExtraVO|
|&emsp;&emsp;id|消费明细ID|integer(int64)||
|&emsp;&emsp;itemName|项目名称|string||
|&emsp;&emsp;amount|单价|number||
|&emsp;&emsp;quantity|数量|integer(int32)||
|&emsp;&emsp;subtotal|小计|number||
|&emsp;&emsp;operatorId|操作员ID|integer(int64)||
|&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"itemName": "",
			"amount": 0,
			"quantity": 0,
			"subtotal": 0,
			"operatorId": 0,
			"operatorName": "",
			"createTime": ""
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


## 删除额外消费记录


**接口地址**:`/api/order/{id}/extra/{extraId}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|extraId|额外消费记录ID|path|true|integer(int64)||


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


# 客户端-我的订单


## 我的订单列表（客户端）


**接口地址**:`/api/order/my`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页条数|query|false|integer(int32)||
|status|订单状态|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultOrderVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultOrderVO|PageResultOrderVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|OrderVO|
|&emsp;&emsp;&emsp;&emsp;id|订单ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;&emsp;&emsp;customerId|客户ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;customerName|客户姓名|string||
|&emsp;&emsp;&emsp;&emsp;customerPhone|客户电话|string||
|&emsp;&emsp;&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;checkInDate|入住日期|string(date)||
|&emsp;&emsp;&emsp;&emsp;checkOutDate|离店日期|string(date)||
|&emsp;&emsp;&emsp;&emsp;nights|入住晚数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;actualCheckIn|实际入住时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;actualCheckOut|实际离店时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;roomPrice|房间单价|number||
|&emsp;&emsp;&emsp;&emsp;roomTotal|房费合计|number||
|&emsp;&emsp;&emsp;&emsp;extraTotal|额外消费合计|number||
|&emsp;&emsp;&emsp;&emsp;totalAmount|订单总价|number||
|&emsp;&emsp;&emsp;&emsp;paidAmount|已支付金额|number||
|&emsp;&emsp;&emsp;&emsp;deposit|押金|number||
|&emsp;&emsp;&emsp;&emsp;status|状态编码|string||
|&emsp;&emsp;&emsp;&emsp;statusName|订单状态|string||
|&emsp;&emsp;&emsp;&emsp;source|来源编码|string||
|&emsp;&emsp;&emsp;&emsp;sourceName|来源名称|string||
|&emsp;&emsp;&emsp;&emsp;remark|备注|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;extras|消费明细|array|ExtraVO|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id|消费明细ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;itemName|项目名称|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;amount|单价|number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;quantity|数量|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;subtotal|小计|number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;operatorId|操作员ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;payments|支付记录|array|PaymentVO|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;paidAt|支付时间|string(date-time)||


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
				"orderNo": "",
				"customerId": 0,
				"customerName": "",
				"customerPhone": "",
				"roomId": 0,
				"roomNumber": "",
				"roomTypeName": "",
				"checkInDate": "",
				"checkOutDate": "",
				"nights": 0,
				"actualCheckIn": "",
				"actualCheckOut": "",
				"roomPrice": 0,
				"roomTotal": 0,
				"extraTotal": 0,
				"totalAmount": 0,
				"paidAmount": 0,
				"deposit": 0,
				"status": "",
				"statusName": "",
				"source": "",
				"sourceName": "",
				"remark": "",
				"createTime": "",
				"extras": [
					{
						"id": 0,
						"itemName": "",
						"amount": 0,
						"quantity": 0,
						"subtotal": 0,
						"operatorId": 0,
						"operatorName": "",
						"createTime": ""
					}
				],
				"payments": [
					{
						"id": 0,
						"paymentNo": "",
						"amount": 0,
						"method": "",
						"methodName": "",
						"status": "",
						"paidAt": ""
					}
				]
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


## 我的订单详情（客户端）


**接口地址**:`/api/order/my/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultOrderVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||OrderVO|OrderVO|
|&emsp;&emsp;id|订单ID|integer(int64)||
|&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;customerId|客户ID|integer(int64)||
|&emsp;&emsp;customerName|客户姓名|string||
|&emsp;&emsp;customerPhone|客户电话|string||
|&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;roomNumber|房间号|string||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;checkInDate|入住日期|string(date)||
|&emsp;&emsp;checkOutDate|离店日期|string(date)||
|&emsp;&emsp;nights|入住晚数|integer(int32)||
|&emsp;&emsp;actualCheckIn|实际入住时间|string(date-time)||
|&emsp;&emsp;actualCheckOut|实际离店时间|string(date-time)||
|&emsp;&emsp;roomPrice|房间单价|number||
|&emsp;&emsp;roomTotal|房费合计|number||
|&emsp;&emsp;extraTotal|额外消费合计|number||
|&emsp;&emsp;totalAmount|订单总价|number||
|&emsp;&emsp;paidAmount|已支付金额|number||
|&emsp;&emsp;deposit|押金|number||
|&emsp;&emsp;status|状态编码|string||
|&emsp;&emsp;statusName|订单状态|string||
|&emsp;&emsp;source|来源编码|string||
|&emsp;&emsp;sourceName|来源名称|string||
|&emsp;&emsp;remark|备注|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;extras|消费明细|array|ExtraVO|
|&emsp;&emsp;&emsp;&emsp;id|消费明细ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;itemName|项目名称|string||
|&emsp;&emsp;&emsp;&emsp;amount|单价|number||
|&emsp;&emsp;&emsp;&emsp;quantity|数量|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;subtotal|小计|number||
|&emsp;&emsp;&emsp;&emsp;operatorId|操作员ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;payments|支付记录|array|PaymentVO|
|&emsp;&emsp;&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;&emsp;&emsp;paidAt|支付时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"orderNo": "",
		"customerId": 0,
		"customerName": "",
		"customerPhone": "",
		"roomId": 0,
		"roomNumber": "",
		"roomTypeName": "",
		"checkInDate": "",
		"checkOutDate": "",
		"nights": 0,
		"actualCheckIn": "",
		"actualCheckOut": "",
		"roomPrice": 0,
		"roomTotal": 0,
		"extraTotal": 0,
		"totalAmount": 0,
		"paidAmount": 0,
		"deposit": 0,
		"status": "",
		"statusName": "",
		"source": "",
		"sourceName": "",
		"remark": "",
		"createTime": "",
		"extras": [
			{
				"id": 0,
				"itemName": "",
				"amount": 0,
				"quantity": 0,
				"subtotal": 0,
				"operatorId": 0,
				"operatorName": "",
				"createTime": ""
			}
		],
		"payments": [
			{
				"id": 0,
				"paymentNo": "",
				"amount": 0,
				"method": "",
				"methodName": "",
				"status": "",
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


# 订单管理


## 续住


**接口地址**:`/api/order/{id}/extend`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "extendDays": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|extendDTO|续住信息|body|true|ExtendDTO|ExtendDTO|
|&emsp;&emsp;extendDays|续住天数||true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


## 办理退房


**接口地址**:`/api/order/{id}/checkout`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "refundDeposit": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|checkOutDTO|退房信息|body|true|CheckOutDTO|CheckOutDTO|
|&emsp;&emsp;refundDeposit|退还押金||false|number||


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


## 办理入住


**接口地址**:`/api/order/{id}/check-in`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "deposit": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|checkInDTO|入住信息|body|true|CheckInDTO|CheckInDTO|
|&emsp;&emsp;deposit|押金||false|number||


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


## 换房


**接口地址**:`/api/order/{id}/change-room`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "newRoomId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|changeRoomDTO|换房信息|body|true|ChangeRoomDTO|ChangeRoomDTO|
|&emsp;&emsp;newRoomId|新房间ID||true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


## 取消订单


**接口地址**:`/api/order/{id}/cancel`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "reason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|orderCancelDTO|取消信息|body|true|OrderCancelDTO|OrderCancelDTO|
|&emsp;&emsp;reason|取消原因||false|string||


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


## 创建订单（支持幂等性键防重复提交）


**接口地址**:`/api/order/create`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "roomTypeId": 0,
  "customerId": 0,
  "checkInDate": "",
  "checkOutDate": "",
  "roomCount": 0,
  "guestName": "",
  "guestPhone": "",
  "remark": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|X-Idempotency-Key|幂等性键，用于防重复提交|header|true|string||
|orderCreateDTO|订单创建信息|body|true|OrderCreateDTO|OrderCreateDTO|
|&emsp;&emsp;roomTypeId|房型ID||true|integer(int64)||
|&emsp;&emsp;customerId|客户ID||false|integer(int64)||
|&emsp;&emsp;checkInDate|入住日期||true|string||
|&emsp;&emsp;checkOutDate|退房日期||true|string||
|&emsp;&emsp;roomCount|房间数量||false|integer(int32)||
|&emsp;&emsp;guestName|住客姓名||false|string||
|&emsp;&emsp;guestPhone|住客电话||false|string||
|&emsp;&emsp;remark|备注||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


## 订单详情


**接口地址**:`/api/order/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultOrderVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||OrderVO|OrderVO|
|&emsp;&emsp;id|订单ID|integer(int64)||
|&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;customerId|客户ID|integer(int64)||
|&emsp;&emsp;customerName|客户姓名|string||
|&emsp;&emsp;customerPhone|客户电话|string||
|&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;roomNumber|房间号|string||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;checkInDate|入住日期|string(date)||
|&emsp;&emsp;checkOutDate|离店日期|string(date)||
|&emsp;&emsp;nights|入住晚数|integer(int32)||
|&emsp;&emsp;actualCheckIn|实际入住时间|string(date-time)||
|&emsp;&emsp;actualCheckOut|实际离店时间|string(date-time)||
|&emsp;&emsp;roomPrice|房间单价|number||
|&emsp;&emsp;roomTotal|房费合计|number||
|&emsp;&emsp;extraTotal|额外消费合计|number||
|&emsp;&emsp;totalAmount|订单总价|number||
|&emsp;&emsp;paidAmount|已支付金额|number||
|&emsp;&emsp;deposit|押金|number||
|&emsp;&emsp;status|状态编码|string||
|&emsp;&emsp;statusName|订单状态|string||
|&emsp;&emsp;source|来源编码|string||
|&emsp;&emsp;sourceName|来源名称|string||
|&emsp;&emsp;remark|备注|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;extras|消费明细|array|ExtraVO|
|&emsp;&emsp;&emsp;&emsp;id|消费明细ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;itemName|项目名称|string||
|&emsp;&emsp;&emsp;&emsp;amount|单价|number||
|&emsp;&emsp;&emsp;&emsp;quantity|数量|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;subtotal|小计|number||
|&emsp;&emsp;&emsp;&emsp;operatorId|操作员ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;payments|支付记录|array|PaymentVO|
|&emsp;&emsp;&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;&emsp;&emsp;paidAt|支付时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"orderNo": "",
		"customerId": 0,
		"customerName": "",
		"customerPhone": "",
		"roomId": 0,
		"roomNumber": "",
		"roomTypeName": "",
		"checkInDate": "",
		"checkOutDate": "",
		"nights": 0,
		"actualCheckIn": "",
		"actualCheckOut": "",
		"roomPrice": 0,
		"roomTotal": 0,
		"extraTotal": 0,
		"totalAmount": 0,
		"paidAmount": 0,
		"deposit": 0,
		"status": "",
		"statusName": "",
		"source": "",
		"sourceName": "",
		"remark": "",
		"createTime": "",
		"extras": [
			{
				"id": 0,
				"itemName": "",
				"amount": 0,
				"quantity": 0,
				"subtotal": 0,
				"operatorId": 0,
				"operatorName": "",
				"createTime": ""
			}
		],
		"payments": [
			{
				"id": 0,
				"paymentNo": "",
				"amount": 0,
				"method": "",
				"methodName": "",
				"status": "",
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


## 热门房型统计（按订单量排名）


**接口地址**:`/api/order/stats/hot-room-types`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|topN|排名数量，默认前6名|query|false|integer(int32)||
|days|统计最近天数，默认30天|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListHotRoomTypeCountVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|HotRoomTypeCountVO|
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;orderCount|订单数量|integer(int64)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"roomTypeName": "",
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


## 订单列表（多条件筛选）


**接口地址**:`/api/order/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页条数|query|false|integer(int32)||
|status|订单状态|query|false|string||
|customerPhone|客户手机号|query|false|string||
|roomNumber|房间号|query|false|string||
|checkInDate|入住日期|query|false|string||
|source|订单来源|query|false|string||
|startDate|开始日期|query|false|string||
|endDate|结束日期|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultOrderVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultOrderVO|PageResultOrderVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|OrderVO|
|&emsp;&emsp;&emsp;&emsp;id|订单ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;&emsp;&emsp;customerId|客户ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;customerName|客户姓名|string||
|&emsp;&emsp;&emsp;&emsp;customerPhone|客户电话|string||
|&emsp;&emsp;&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;checkInDate|入住日期|string(date)||
|&emsp;&emsp;&emsp;&emsp;checkOutDate|离店日期|string(date)||
|&emsp;&emsp;&emsp;&emsp;nights|入住晚数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;actualCheckIn|实际入住时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;actualCheckOut|实际离店时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;roomPrice|房间单价|number||
|&emsp;&emsp;&emsp;&emsp;roomTotal|房费合计|number||
|&emsp;&emsp;&emsp;&emsp;extraTotal|额外消费合计|number||
|&emsp;&emsp;&emsp;&emsp;totalAmount|订单总价|number||
|&emsp;&emsp;&emsp;&emsp;paidAmount|已支付金额|number||
|&emsp;&emsp;&emsp;&emsp;deposit|押金|number||
|&emsp;&emsp;&emsp;&emsp;status|状态编码|string||
|&emsp;&emsp;&emsp;&emsp;statusName|订单状态|string||
|&emsp;&emsp;&emsp;&emsp;source|来源编码|string||
|&emsp;&emsp;&emsp;&emsp;sourceName|来源名称|string||
|&emsp;&emsp;&emsp;&emsp;remark|备注|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;extras|消费明细|array|ExtraVO|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id|消费明细ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;itemName|项目名称|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;amount|单价|number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;quantity|数量|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;subtotal|小计|number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;operatorId|操作员ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;payments|支付记录|array|PaymentVO|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;paidAt|支付时间|string(date-time)||


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
				"orderNo": "",
				"customerId": 0,
				"customerName": "",
				"customerPhone": "",
				"roomId": 0,
				"roomNumber": "",
				"roomTypeName": "",
				"checkInDate": "",
				"checkOutDate": "",
				"nights": 0,
				"actualCheckIn": "",
				"actualCheckOut": "",
				"roomPrice": 0,
				"roomTotal": 0,
				"extraTotal": 0,
				"totalAmount": 0,
				"paidAmount": 0,
				"deposit": 0,
				"status": "",
				"statusName": "",
				"source": "",
				"sourceName": "",
				"remark": "",
				"createTime": "",
				"extras": [
					{
						"id": 0,
						"itemName": "",
						"amount": 0,
						"quantity": 0,
						"subtotal": 0,
						"operatorId": 0,
						"operatorName": "",
						"createTime": ""
					}
				],
				"payments": [
					{
						"id": 0,
						"paymentNo": "",
						"amount": 0,
						"method": "",
						"methodName": "",
						"status": "",
						"paidAt": ""
					}
				]
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


## 按时间段查询订单列表


**接口地址**:`/api/order/list/byTime`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startDate|开始日期|query|false|string||
|endDate|结束日期|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListOrderVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|OrderVO|
|&emsp;&emsp;id|订单ID|integer(int64)||
|&emsp;&emsp;orderNo|订单编号|string||
|&emsp;&emsp;customerId|客户ID|integer(int64)||
|&emsp;&emsp;customerName|客户姓名|string||
|&emsp;&emsp;customerPhone|客户电话|string||
|&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;roomNumber|房间号|string||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;checkInDate|入住日期|string(date)||
|&emsp;&emsp;checkOutDate|离店日期|string(date)||
|&emsp;&emsp;nights|入住晚数|integer(int32)||
|&emsp;&emsp;actualCheckIn|实际入住时间|string(date-time)||
|&emsp;&emsp;actualCheckOut|实际离店时间|string(date-time)||
|&emsp;&emsp;roomPrice|房间单价|number||
|&emsp;&emsp;roomTotal|房费合计|number||
|&emsp;&emsp;extraTotal|额外消费合计|number||
|&emsp;&emsp;totalAmount|订单总价|number||
|&emsp;&emsp;paidAmount|已支付金额|number||
|&emsp;&emsp;deposit|押金|number||
|&emsp;&emsp;status|状态编码|string||
|&emsp;&emsp;statusName|订单状态|string||
|&emsp;&emsp;source|来源编码|string||
|&emsp;&emsp;sourceName|来源名称|string||
|&emsp;&emsp;remark|备注|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;extras|消费明细|array|ExtraVO|
|&emsp;&emsp;&emsp;&emsp;id|消费明细ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;itemName|项目名称|string||
|&emsp;&emsp;&emsp;&emsp;amount|单价|number||
|&emsp;&emsp;&emsp;&emsp;quantity|数量|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;subtotal|小计|number||
|&emsp;&emsp;&emsp;&emsp;operatorId|操作员ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;operatorName|操作员姓名|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;payments|支付记录|array|PaymentVO|
|&emsp;&emsp;&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;&emsp;&emsp;paidAt|支付时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"orderNo": "",
			"customerId": 0,
			"customerName": "",
			"customerPhone": "",
			"roomId": 0,
			"roomNumber": "",
			"roomTypeName": "",
			"checkInDate": "",
			"checkOutDate": "",
			"nights": 0,
			"actualCheckIn": "",
			"actualCheckOut": "",
			"roomPrice": 0,
			"roomTotal": 0,
			"extraTotal": 0,
			"totalAmount": 0,
			"paidAmount": 0,
			"deposit": 0,
			"status": "",
			"statusName": "",
			"source": "",
			"sourceName": "",
			"remark": "",
			"createTime": "",
			"extras": [
				{
					"id": 0,
					"itemName": "",
					"amount": 0,
					"quantity": 0,
					"subtotal": 0,
					"operatorId": 0,
					"operatorName": "",
					"createTime": ""
				}
			],
			"payments": [
				{
					"id": 0,
					"paymentNo": "",
					"amount": 0,
					"method": "",
					"methodName": "",
					"status": "",
					"paidAt": ""
				}
			]
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


## Nacos配置中心演示（动态刷新）


**接口地址**:`/api/order/config-demo`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultString|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||string||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": ""
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


## 查询指定房型在时间段内的可用房间


**接口地址**:`/api/order/available-rooms`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|roomTypeId|房型ID|query|true|integer(int64)||
|checkIn|入住日期|query|true|string||
|checkOut|退房日期|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


# 支付管理


## 订单退款


**接口地址**:`/api/order/{id}/refund`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "amount": 0,
  "reason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|refundDTO|退款信息|body|true|RefundDTO|RefundDTO|
|&emsp;&emsp;amount|退款金额||true|number||
|&emsp;&emsp;reason|退款原因||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


## 订单支付


**接口地址**:`/api/order/{id}/pay`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "amount": 0,
  "method": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||
|payDTO|支付信息|body|true|PayDTO|PayDTO|
|&emsp;&emsp;amount|支付金额||true|number||
|&emsp;&emsp;method|支付方式||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringObject|
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


## 订单支付记录


**接口地址**:`/api/order/{id}/payments`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|订单ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListPaymentVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|PaymentVO|
|&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;paidAt|支付时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"paymentNo": "",
			"amount": 0,
			"method": "",
			"methodName": "",
			"status": "",
			"paidAt": ""
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


## 按时间段查询支付记录


**接口地址**:`/api/order/payments/time`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|startTime|开始时间|query|true|string||
|endTime|结束时间|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListPaymentVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|PaymentVO|
|&emsp;&emsp;id|支付记录ID|integer(int64)||
|&emsp;&emsp;paymentNo|支付编号|string||
|&emsp;&emsp;amount|支付金额|number||
|&emsp;&emsp;method|支付方式编码|string||
|&emsp;&emsp;methodName|支付方式|string||
|&emsp;&emsp;status|支付状态|string||
|&emsp;&emsp;paidAt|支付时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"paymentNo": "",
			"amount": 0,
			"method": "",
			"methodName": "",
			"status": "",
			"paidAt": ""
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