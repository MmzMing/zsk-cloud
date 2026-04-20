# 管理日志 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/logs/recent | GET | SysLogController.java | 获取最近管理日志 |

## 2. 接口详情

### 2.1 获取最近管理日志

**路径**: `GET /api/system/logs/recent`

**功能描述**: 获取最近的管理日志，支持分类筛选

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | string | 否 | 分类（content/user/system） |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "logs": [
      {
        "id": "1",
        "category": "user",
        "operation": "新增用户",
        "operator": "admin",
        "target": "testuser",
        "detail": "新增用户：testuser",
        "ip": "192.168.1.100",
        "createTime": "2026-02-15 10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10
  }
}
```