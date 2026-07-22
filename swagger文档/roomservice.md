# room-service


**简介**:room-service


**HOST**:http://localhost:8082


**联系人**:hotel


**Version**:1.0.0


**接口路径**:/v3/api-docs


[TOC]






# 房间图片上传


## 上传房间图片


**接口地址**:`/api/room/upload/image`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file|图片文件|query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringString|
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


## 上传房型封面图


**接口地址**:`/api/room/upload/cover`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file|图片文件|query|true|file||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultMapStringString|
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


# 打扫管理


## 打扫任务列表


**接口地址**:`/api/room/cleaning/tasks`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页数量|query|false|integer(int32)||
|status|任务状态|query|false|string||
|cleanerId|保洁员ID|query|false|integer(int64)||
|date|日期|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultCleaningTaskVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultCleaningTaskVO|PageResultCleaningTaskVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|CleaningTaskVO|
|&emsp;&emsp;&emsp;&emsp;id|任务ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;cleanerId|保洁员ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;status|任务状态|string||
|&emsp;&emsp;&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;endTime|结束时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;durationMinutes|打扫耗时（分钟）|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;elapsedMinutes|已耗时（分钟）|integer(int32)||


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
				"roomId": 0,
				"roomNumber": "",
				"roomTypeName": "",
				"cleanerId": 0,
				"cleanerName": "",
				"status": "",
				"startTime": "",
				"endTime": "",
				"durationMinutes": 0,
				"elapsedMinutes": 0
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


## 保洁员当前任务


**接口地址**:`/api/room/cleaning/tasks/cleaner/{cleanerId}/active`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|cleanerId|保洁员ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListCleaningTaskVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|CleaningTaskVO|
|&emsp;&emsp;id|任务ID|integer(int64)||
|&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;cleanerId|保洁员ID|integer(int64)||
|&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;status|任务状态|string||
|&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;endTime|结束时间|string(date-time)||
|&emsp;&emsp;durationMinutes|打扫耗时（分钟）|integer(int32)||
|&emsp;&emsp;elapsedMinutes|已耗时（分钟）|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"roomId": 0,
			"roomNumber": "",
			"roomTypeName": "",
			"cleanerId": 0,
			"cleanerName": "",
			"status": "",
			"startTime": "",
			"endTime": "",
			"durationMinutes": 0,
			"elapsedMinutes": 0
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


## 打扫任务详情


**接口地址**:`/api/room/cleaning/task/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|任务ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultCleaningTaskVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||CleaningTaskVO|CleaningTaskVO|
|&emsp;&emsp;id|任务ID|integer(int64)||
|&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;cleanerId|保洁员ID|integer(int64)||
|&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;status|任务状态|string||
|&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;endTime|结束时间|string(date-time)||
|&emsp;&emsp;durationMinutes|打扫耗时（分钟）|integer(int32)||
|&emsp;&emsp;elapsedMinutes|已耗时（分钟）|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"roomId": 0,
		"roomNumber": "",
		"roomTypeName": "",
		"cleanerId": 0,
		"cleanerName": "",
		"status": "",
		"startTime": "",
		"endTime": "",
		"durationMinutes": 0,
		"elapsedMinutes": 0
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


# 房间管理


## 房间详情（含打扫-维修进度）


**接口地址**:`/api/room/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房间ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultRoomVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||RoomVO|RoomVO|
|&emsp;&emsp;id|房间ID|integer(int64)||
|&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;roomTypeId|房型ID|integer(int64)||
|&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;floor|楼层|integer(int32)||
|&emsp;&emsp;status|状态|string||
|&emsp;&emsp;price|价格|number||
|&emsp;&emsp;description|描述|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;updateTime|更新时间|string(date-time)||
|&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;taskStartTime|任务开始时间|string(date-time)||
|&emsp;&emsp;currentTask||TaskInfo|TaskInfo|
|&emsp;&emsp;&emsp;&emsp;taskId|任务ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;type|任务类型|string||
|&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;reason|维修原因|string||
|&emsp;&emsp;&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;remainMinutes|预计剩余分钟数|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"roomNumber": "",
		"roomTypeId": 0,
		"roomTypeName": "",
		"floor": 0,
		"status": "",
		"price": 0,
		"description": "",
		"createTime": "",
		"updateTime": "",
		"cleanerName": "",
		"taskStartTime": "",
		"currentTask": {
			"taskId": 0,
			"type": "",
			"cleanerName": "",
			"reason": "",
			"startTime": "",
			"remainMinutes": 0
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


## 修改房间


**接口地址**:`/api/room/{id}`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "roomNumber": "",
  "roomTypeId": 0,
  "floor": 0,
  "description": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房间ID|path|true|integer(int64)||
|roomSaveDTO|房间信息|body|true|RoomSaveDTO|RoomSaveDTO|
|&emsp;&emsp;roomNumber|房间编号||true|string||
|&emsp;&emsp;roomTypeId|房型ID||true|integer(int64)||
|&emsp;&emsp;floor|楼层||false|integer(int32)||
|&emsp;&emsp;description|描述||false|string||


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


## 删除房间


**接口地址**:`/api/room/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房间ID|path|true|integer(int64)||


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


## 手动变更房态


**接口地址**:`/api/room/{id}/status`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "status": "",
  "reason": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房间ID|path|true|integer(int64)||
|roomStatusChangeDTO|房态变更信息|body|true|RoomStatusChangeDTO|RoomStatusChangeDTO|
|&emsp;&emsp;status|||true|string||
|&emsp;&emsp;reason|||false|string||


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


## 手动分配保洁


**接口地址**:`/api/room/{id}/cleaning/assign`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "cleanerId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房间ID|path|true|integer(int64)||
|cleaningAssignDTO|保洁分配信息|body|true|CleaningAssignDTO|CleaningAssignDTO|
|&emsp;&emsp;cleanerId|保洁员ID||true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultObject|
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


## 创建房间


**接口地址**:`/api/room/create`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "roomNumber": "",
  "roomTypeId": 0,
  "floor": 0,
  "description": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|roomSaveDTO|房间信息|body|true|RoomSaveDTO|RoomSaveDTO|
|&emsp;&emsp;roomNumber|房间编号||true|string||
|&emsp;&emsp;roomTypeId|房型ID||true|integer(int64)||
|&emsp;&emsp;floor|楼层||false|integer(int32)||
|&emsp;&emsp;description|描述||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultObject|
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


## 按状态筛选房间


**接口地址**:`/api/room/status/{status}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|status|房间状态|path|true|string||
|page|页码|query|false|integer(int32)||
|size|每页数量|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultRoomVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultRoomVO|PageResultRoomVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|RoomVO|
|&emsp;&emsp;&emsp;&emsp;id|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeId|房型ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;floor|楼层|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;status|状态|string||
|&emsp;&emsp;&emsp;&emsp;price|价格|number||
|&emsp;&emsp;&emsp;&emsp;description|描述|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;updateTime|更新时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;taskStartTime|任务开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;currentTask||TaskInfo|TaskInfo|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;taskId|任务ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type|任务类型|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;reason|维修原因|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;remainMinutes|预计剩余分钟数|integer(int32)||


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
				"roomNumber": "",
				"roomTypeId": 0,
				"roomTypeName": "",
				"floor": 0,
				"status": "",
				"price": 0,
				"description": "",
				"createTime": "",
				"updateTime": "",
				"cleanerName": "",
				"taskStartTime": "",
				"currentTask": {
					"taskId": 0,
					"type": "",
					"cleanerName": "",
					"reason": "",
					"startTime": "",
					"remainMinutes": 0
				}
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


## 房间列表


**接口地址**:`/api/room/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页数量|query|false|integer(int32)||
|roomTypeId|房型ID|query|false|integer(int64)||
|status|房间状态|query|false|string||
|floor|楼层|query|false|integer(int32)||
|keyword|搜索关键词|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultRoomVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultRoomVO|PageResultRoomVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|RoomVO|
|&emsp;&emsp;&emsp;&emsp;id|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeId|房型ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;floor|楼层|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;status|状态|string||
|&emsp;&emsp;&emsp;&emsp;price|价格|number||
|&emsp;&emsp;&emsp;&emsp;description|描述|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;updateTime|更新时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;taskStartTime|任务开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;currentTask||TaskInfo|TaskInfo|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;taskId|任务ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type|任务类型|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;reason|维修原因|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;remainMinutes|预计剩余分钟数|integer(int32)||


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
				"roomNumber": "",
				"roomTypeId": 0,
				"roomTypeName": "",
				"floor": 0,
				"status": "",
				"price": 0,
				"description": "",
				"createTime": "",
				"updateTime": "",
				"cleanerName": "",
				"taskStartTime": "",
				"currentTask": {
					"taskId": 0,
					"type": "",
					"cleanerName": "",
					"reason": "",
					"startTime": "",
					"remainMinutes": 0
				}
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


## 房态看板


**接口地址**:`/api/room/dashboard`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|floor|楼层|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultDashboardVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||DashboardVO|DashboardVO|
|&emsp;&emsp;total|房间总数|integer(int32)||
|&emsp;&emsp;summary|各状态数量统计|object||
|&emsp;&emsp;rooms|房间列表|array|RoomVO|
|&emsp;&emsp;&emsp;&emsp;id|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;&emsp;&emsp;roomTypeId|房型ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomTypeName|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;floor|楼层|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;status|状态|string||
|&emsp;&emsp;&emsp;&emsp;price|价格|number||
|&emsp;&emsp;&emsp;&emsp;description|描述|string||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;updateTime|更新时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;taskStartTime|任务开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;currentTask||TaskInfo|TaskInfo|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;taskId|任务ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type|任务类型|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;cleanerName|保洁员姓名|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;reason|维修原因|string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;remainMinutes|预计剩余分钟数|integer(int32)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"total": 0,
		"summary": {},
		"rooms": [
			{
				"id": 0,
				"roomNumber": "",
				"roomTypeId": 0,
				"roomTypeName": "",
				"floor": 0,
				"status": "",
				"price": 0,
				"description": "",
				"createTime": "",
				"updateTime": "",
				"cleanerName": "",
				"taskStartTime": "",
				"currentTask": {
					"taskId": 0,
					"type": "",
					"cleanerName": "",
					"reason": "",
					"startTime": "",
					"remainMinutes": 0
				}
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


# 房型管理


## 房型详情


**接口地址**:`/api/room/type/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房型ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultRoomTypeVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||RoomTypeVO|RoomTypeVO|
|&emsp;&emsp;id|房型ID|integer(int64)||
|&emsp;&emsp;name|房型名称|string||
|&emsp;&emsp;description|描述|string||
|&emsp;&emsp;area|面积|integer(int32)||
|&emsp;&emsp;bedType|床型|string||
|&emsp;&emsp;maxGuests|最大入住人数|integer(int32)||
|&emsp;&emsp;price|价格|number||
|&emsp;&emsp;coverImage|封面图片|string||
|&emsp;&emsp;images|图片列表|array|string|
|&emsp;&emsp;amenities|设施|string||
|&emsp;&emsp;sortOrder|排序顺序|integer(int32)||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;updateTime|更新时间|string(date-time)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"name": "",
		"description": "",
		"area": 0,
		"bedType": "",
		"maxGuests": 0,
		"price": 0,
		"coverImage": "",
		"images": [],
		"amenities": "",
		"sortOrder": 0,
		"createTime": "",
		"updateTime": ""
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


## 修改房型


**接口地址**:`/api/room/type/{id}`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "name": "",
  "price": 0,
  "description": "",
  "area": 0,
  "bedType": "",
  "maxGuests": 0,
  "coverImage": "",
  "images": [],
  "amenities": "",
  "sortOrder": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房型ID|path|true|integer(int64)||
|roomTypeSaveDTO|房型信息|body|true|RoomTypeSaveDTO|RoomTypeSaveDTO|
|&emsp;&emsp;name|房型名称||true|string||
|&emsp;&emsp;price|价格||true|number||
|&emsp;&emsp;description|描述||false|string||
|&emsp;&emsp;area|面积||false|integer(int32)||
|&emsp;&emsp;bedType|床型||false|string||
|&emsp;&emsp;maxGuests|最大入住人数||false|integer(int32)||
|&emsp;&emsp;coverImage|封面图片||false|string||
|&emsp;&emsp;images|图片列表||false|array|string|
|&emsp;&emsp;amenities|设施||false|string||
|&emsp;&emsp;sortOrder|排序顺序||false|integer(int32)||


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


## 删除房型


**接口地址**:`/api/room/type/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房型ID|path|true|integer(int64)||


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


## 创建房型


**接口地址**:`/api/room/type/create`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "name": "",
  "price": 0,
  "description": "",
  "area": 0,
  "bedType": "",
  "maxGuests": 0,
  "coverImage": "",
  "images": [],
  "amenities": "",
  "sortOrder": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|roomTypeSaveDTO|房型信息|body|true|RoomTypeSaveDTO|RoomTypeSaveDTO|
|&emsp;&emsp;name|房型名称||true|string||
|&emsp;&emsp;price|价格||true|number||
|&emsp;&emsp;description|描述||false|string||
|&emsp;&emsp;area|面积||false|integer(int32)||
|&emsp;&emsp;bedType|床型||false|string||
|&emsp;&emsp;maxGuests|最大入住人数||false|integer(int32)||
|&emsp;&emsp;coverImage|封面图片||false|string||
|&emsp;&emsp;images|图片列表||false|array|string|
|&emsp;&emsp;amenities|设施||false|string||
|&emsp;&emsp;sortOrder|排序顺序||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultObject|
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


## 房型列表


**接口地址**:`/api/room/type/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页数量|query|false|integer(int32)||
|keyword|搜索关键词|query|false|string||
|bedType|床型|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultRoomTypeVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultRoomTypeVO|PageResultRoomTypeVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|RoomTypeVO|
|&emsp;&emsp;&emsp;&emsp;id|房型ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;name|房型名称|string||
|&emsp;&emsp;&emsp;&emsp;description|描述|string||
|&emsp;&emsp;&emsp;&emsp;area|面积|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;bedType|床型|string||
|&emsp;&emsp;&emsp;&emsp;maxGuests|最大入住人数|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;price|价格|number||
|&emsp;&emsp;&emsp;&emsp;coverImage|封面图片|string||
|&emsp;&emsp;&emsp;&emsp;images|图片列表|array|string|
|&emsp;&emsp;&emsp;&emsp;amenities|设施|string||
|&emsp;&emsp;&emsp;&emsp;sortOrder|排序顺序|integer(int32)||
|&emsp;&emsp;&emsp;&emsp;createTime|创建时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;updateTime|更新时间|string(date-time)||


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
				"name": "",
				"description": "",
				"area": 0,
				"bedType": "",
				"maxGuests": 0,
				"price": 0,
				"coverImage": "",
				"images": [],
				"amenities": "",
				"sortOrder": 0,
				"createTime": "",
				"updateTime": ""
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


## 热门房型


**接口地址**:`/api/room/type/hot`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListHotRoomTypeVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|HotRoomTypeVO|
|&emsp;&emsp;id|房型ID|integer(int64)||
|&emsp;&emsp;name|房型名称|string||
|&emsp;&emsp;bedType|床型|string||
|&emsp;&emsp;maxGuests|最大入住人数|integer(int32)||
|&emsp;&emsp;price|价格|number||
|&emsp;&emsp;coverImage|封面图片|string||
|&emsp;&emsp;description|描述|string||
|&emsp;&emsp;orderCount|订单数量|integer(int64)||


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"name": "",
			"bedType": "",
			"maxGuests": 0,
			"price": 0,
			"coverImage": "",
			"description": "",
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


# 维修管理


## 设为维修中


**接口地址**:`/api/room/{id}/maintenance/start`


**请求方式**:`POST`


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
|id|房间ID|path|true|integer(int64)||
|maintenanceStartDTO|维修开始信息|body|true|MaintenanceStartDTO|MaintenanceStartDTO|
|&emsp;&emsp;reason|维修原因||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultObject|
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


## 维修完成


**接口地址**:`/api/room/{id}/maintenance/complete`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "cost": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|房间ID|path|true|integer(int64)||
|maintenanceCompleteDTO|维修完成信息|body|true|MaintenanceCompleteDTO|MaintenanceCompleteDTO|
|&emsp;&emsp;cost|维修费用||false|number||


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


## 维修记录列表


**接口地址**:`/api/room/maintenance/records`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页数量|query|false|integer(int32)||
|roomId|房间ID|query|false|integer(int64)||
|status|维修状态|query|false|string||
|startDate|开始日期|query|false|string||
|endDate|结束日期|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultPageResultMaintenanceRecordVO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||PageResultMaintenanceRecordVO|PageResultMaintenanceRecordVO|
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;page||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;records||array|MaintenanceRecordVO|
|&emsp;&emsp;&emsp;&emsp;id|记录ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomId|房间ID|integer(int64)||
|&emsp;&emsp;&emsp;&emsp;roomNumber|房间编号|string||
|&emsp;&emsp;&emsp;&emsp;reason|维修原因|string||
|&emsp;&emsp;&emsp;&emsp;status|维修状态|string||
|&emsp;&emsp;&emsp;&emsp;startTime|开始时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;endTime|结束时间|string(date-time)||
|&emsp;&emsp;&emsp;&emsp;cost|维修费用|number||


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
				"roomId": 0,
				"roomNumber": "",
				"reason": "",
				"status": "",
				"startTime": "",
				"endTime": "",
				"cost": 0
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