# 管理日志 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/logs/page | GET | SysLogController.java | 分页查询管理日志 |
| /api/system/logs/{ids} | DELETE | SysLogController.java | 批量删除管理日志 |

## 2. 接口详情

### 2.1 分页查询管理日志

**路径**: `GET /api/system/logs/page`

**功能描述**: 分页查询管理日志，支持多条件筛选

**请求参数**:

分页参数（PageQuery）:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | long | 否 | 页码，默认1 |
| pageSize | long | 否 | 每页大小，默认10，最大500 |
| orderByColumn | string | 否 | 排序字段 |
| isAsc | string | 否 | 排序方向（asc/desc），默认asc |

查询条件（SysLogQueryDTO）:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | string | 否 | 分类（content/user/system） |
| operator | string | 否 | 操作人，模糊匹配 |
| requestUrl | string | 否 | 请求URL，模糊匹配 |
| requestMethod | string | 否 | 请求方式（GET/POST/PUT/DELETE），精确匹配 |
| status | int | 否 | 操作状态（0正常 1异常），精确匹配 |
| title | string | 否 | 模块标题，模糊匹配 |
| businessType | int | 否 | 业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入），精确匹配 |
| beginTime | string | 否 | 操作开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | string | 否 | 操作结束时间（yyyy-MM-dd HH:mm:ss） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "category": "user",
        "operator": "admin",
        "action": "新增用户",
        "detail": "新增用户 - /api/system/user",
        "createdAt": "2026-02-15 10:00:00",
        "requestMethod": "POST",
        "requestUrl": "/api/system/user",
        "requestParam": "{\"userName\":\"testuser\"}",
        "responseResult": "{\"code\":0,\"msg\":\"success\"}",
        "status": 0,
        "costTime": 120,
        "operIp": "192.168.1.100"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | string | 日志ID |
| category | string | 分类（content/user/system） |
| operator | string | 操作人 |
| action | string | 动作名称 |
| detail | string | 详细描述 |
| createdAt | string | 创建时间 |
| requestMethod | string | 请求方式 |
| requestUrl | string | 请求URL |
| requestParam | string | 请求参数 |
| responseResult | string | 响应结果 |
| status | int | 操作状态（0正常 1异常） |
| costTime | long | 消耗时间(ms) |
| operIp | string | 操作IP |

### 2.2 批量删除管理日志

**路径**: `DELETE /api/system/logs/{ids}`

**功能描述**: 根据日志ID列表批量删除管理日志

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | string[] | 是 | 日志ID列表，多个ID以逗号分隔 |

**请求示例**: `DELETE /api/system/logs/id1,id2,id3`

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success"
}
```

**失败响应** (200):

```json
{
  "code": 500,
  "msg": "操作失败"
}
```
