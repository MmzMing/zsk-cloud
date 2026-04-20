# 登录管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/login/online/list | GET | SysLoginManageController.java | 查询在线用户列表 |
| /api/system/login/online/forceLogout | POST | SysLoginManageController.java | 强制下线用户 |
| /api/system/login/online/refresh/{sessionId} | PUT | SysLoginManageController.java | 刷新会话过期时间 |

## 2. 接口详情

### 2.1 查询在线用户列表

**路径**: `GET /api/system/login/online/list`

**功能描述**: 查询当前在线用户列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userName | string | 否 | 用户名（模糊查询） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "sessionId": "abc123",
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "ipAddress": "192.168.1.100",
      "loginTime": "2026-02-15 10:00:00",
      "expireTime": "2026-02-15 12:00:00"
    }
  ]
}
```

---

### 2.2 强制下线用户

**路径**: `POST /api/system/login/online/forceLogout`

**功能描述**: 强制指定用户下线

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | string | 否 | 会话ID（与userId二选一） |
| userId | string | 否 | 用户ID（与sessionId二选一） |
| all | Boolean | 否 | 是否强制所有会话下线，默认false |

**请求示例**:

```json
{
  "userId": 1,
  "all": true
}
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "强制下线失败",
  "data": null
}
```

---

### 2.3 刷新会话过期时间

**路径**: `PUT /api/system/login/online/refresh/{sessionId}`

**功能描述**: 刷新指定会话的过期时间

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | string | 是 | 会话ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```