# 操作日志 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/operLog/list | GET | SysOperLogController.java | 查询操作日志列表 |
| /api/system/operLog/{id} | GET | SysOperLogController.java | 获取操作日志详细信息 |
| /api/system/operLog/{ids} | DELETE | SysOperLogController.java | 批量删除操作日志 |
| /api/system/operLog/clear | DELETE | SysOperLogController.java | 清空操作日志 |

## 2. 接口详情

### 2.1 查询操作日志列表

**路径**: `GET /api/system/operLog/list`

**功能描述**: 查询操作日志列表，支持分页

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | 否 | 模块标题（模糊查询） |
| operName | string | 否 | 操作人员（模糊查询） |
| status | Integer | 否 | 操作状态（0成功 1失败） |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页大小，默认10 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "rows": [
      {
        "id": "1",
        "title": "用户管理",
        "businessType": 1,
        "method": "POST",
        "requestUrl": "/api/system/user",
        "operName": "admin",
        "status": 0,
        "errorMsg": null,
        "operTime": "2026-02-15 10:00:00"
      }
    ],
    "total": 100
  }
}
```

---

### 2.2 获取操作日志详细信息

**路径**: `GET /api/system/operLog/{id}`

**功能描述**: 根据日志ID获取操作日志详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 日志ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "title": "用户管理",
    "businessType": 1,
    "method": "POST",
    "requestUrl": "/api/system/user",
    "requestMethod": "POST",
    "operatorType": 1,
    "operName": "admin",
    "deptName": "系统管理部",
    "operUrl": "/api/system/user",
    "operIp": "127.0.0.1",
    "operLocation": "本地",
    "operParam": "{\"userName\":\"test\",\"nickName\":\"测试用户\"}",
    "jsonResult": "{\"code\":0,\"msg\":\"success\",\"data\":true}",
    "status": 0,
    "errorMsg": null,
    "operTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.3 批量删除操作日志

**路径**: `DELETE /api/system/operLog/{ids}`

**功能描述**: 批量删除操作日志

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 日志ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.4 清空操作日志

**路径**: `DELETE /api/system/operLog/clear`

**功能描述**: 清空所有操作日志

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```