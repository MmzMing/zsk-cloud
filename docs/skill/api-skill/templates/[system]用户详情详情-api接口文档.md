# 用户模块 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/users | GET | UserController.java | 查询用户列表 |
| /api/system/users/{id} | GET | UserController.java | 查询用户详情 |
| /api/system/users | POST | UserController.java | 创建用户 |
| /api/system/users/{id} | PUT | UserController.java | 更新用户 |
| /api/system/users/{id} | DELETE | UserController.java | 删除用户 |

## 2. 接口详情

### 2.1 查询用户列表

**路径**: `GET /api/system/users`

**所属文件**: `controller/UserController.java`

**功能描述**: 查询用户列表，支持分页和条件筛选

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 页码，默认0 |
| size | int | 否 | 每页大小，默认10 |
| keyword | string | 否 | 搜索关键词（用户名/邮箱） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "zhangsan",
        "email": "zhangsan@example.com",
        "status": 1,
        "createdAt": "2024-01-01 10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

---

### 2.2 查询用户详情

**路径**: `GET /api/system/users/{id}`

**功能描述**: 根据ID查询用户详情

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 用户ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "status": 1,
    "createdAt": "2024-01-01 10:00:00",
    "updatedAt": "2024-01-02 12:00:00"
  }
}
```

**失败响应** (404):

```json
{
  "code": 404,
  "msg": "用户不存在",
  "data": null
}
```

---

### 2.3 创建用户

**路径**: `POST /api/system/users`

**功能描述**: 创建新用户

**请求体**:

```json
{
  "username": "string (必填，用户名)",
  "email": "string (必填，邮箱)",
  "password": "string (必填，密码)",
  "phone": "string (选填，手机号)",
  "status": "int (选填，状态，默认1)"
}
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "创建成功",
  "data": {
    "id": 2,
    "username": "lisi",
    "email": "lisi@example.com",
    "status": 1,
    "createdAt": "2024-01-03 15:00:00"
  }
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "用户名已存在",
  "data": null
}
```

---

### 2.4 更新用户

**路径**: `PUT /api/system/users/{id}`

**功能描述**: 更新用户信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 用户ID |

**请求体**:

```json
{
  "email": "string (选填，邮箱)",
  "phone": "string (选填，手机号)",
  "status": "int (选填，状态)"
}
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "更新成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "email": "new@example.com",
    "phone": "13900139000",
    "status": 1,
    "updatedAt": "2024-01-04 10:00:00"
  }
}
```

---

### 2.5 删除用户

**路径**: `DELETE /api/system/users/{id}`

**功能描述**: 删除用户

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 用户ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "删除成功",
  "data": null
}
```

**失败响应** (403):

```json
{
  "code": 403,
  "msg": "无法删除管理员用户",
  "data": null
}
```