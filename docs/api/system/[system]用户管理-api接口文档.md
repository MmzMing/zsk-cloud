# 用户管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/user/current | GET | SysUserController.java | 获取当前登录用户信息 |
| /api/system/user/info/{username} | GET | SysUserController.java | 通过用户名获取用户详情 |
| /api/system/user/info/email/{email} | GET | SysUserController.java | 通过邮箱获取用户详情 |
| /api/system/user/info/thirdparty/{loginType}/{thirdPartyId} | GET | SysUserController.java | 通过第三方ID获取用户详情 |
| /api/system/user/list | GET | SysUserController.java | 查询用户列表（分页） |
| /api/system/user/{id} | GET | SysUserController.java | 获取用户详细信息 |
| /api/system/user | POST | SysUserController.java | 新增用户 |
| /api/system/user | PUT | SysUserController.java | 修改用户 |
| /api/system/user/{id}/status | PUT | SysUserController.java | 切换用户状态 |
| /api/system/user/inner | PUT | SysUserController.java | 内部接口：更新用户信息 |
| /api/system/user/{ids} | DELETE | SysUserController.java | 删除用户（支持批量） |
| /api/system/user/{id}/reset-password | PUT | SysUserController.java | 重置密码 |
| /api/system/user/{ids}/reset-password | PUT | SysUserController.java | 批量重置密码 |
| /api/system/user/update/infoFile | POST | SysUserController.java | 更新用户信息（支持头像上传） |

## 2. 接口详情

### 2.1 获取当前登录用户信息

**路径**: `GET /api/system/user/current`

**功能描述**: 获取当前请求的用户信息（从安全上下文获取）

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "sysUser": {
      "id": "1",
      "userName": "admin",
      "nickName": "管理员",
      "email": "admin@example.com",
      "phonenumber": "13800138000",
      "sex": "男",
      "avatar": "https://example.com/avatar.jpg"
    },
    "roles": ["admin", "user"],
    "permissions": ["user:list", "user:add"]
  }
}
```

---

### 2.2 通过用户名获取用户详情

**路径**: `GET /api/system/user/info/{username}`

**功能描述**: 通过用户名获取用户详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| x-request-source | string | 否 | 请求来源，内部调用传 `inner` 返回完整用户信息 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "userName": "admin",
    "nickName": "管理员",
    "email": "admin@example.com",
    "phonenumber": "13800138000",
    "status": "0",
    "createTime": "2026-02-15 10:00:00"
  }
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "用户不存在",
  "data": null
}
```

---

### 2.3 查询用户列表

**路径**: `GET /api/system/user/list`

**功能描述**: 查询用户列表，支持分页和条件筛选

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userName | string | 否 | 用户名（模糊查询） |
| nickName | string | 否 | 昵称（模糊查询） |
| email | string | 否 | 邮箱（模糊查询） |
| phonenumber | string | 否 | 手机号（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页大小，默认10 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": "1",
        "userName": "admin",
        "nickName": "管理员",
        "email": "admin@example.com"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10
  }
}
```

---

### 2.4 获取用户详细信息

**路径**: `GET /api/system/user/{id}`

**功能描述**: 根据用户ID获取用户详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 用户ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "userName": "admin",
    "nickName": "管理员",
    "email": "admin@example.com",
    "phonenumber": "13800138000",
    "sex": "男",
    "avatar": "https://example.com/avatar.jpg",
    "status": "0",
    "createTime": "2026-02-15 10:00:00",
    "updateTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.5 新增用户

**路径**: `POST /api/system/user`

**功能描述**: 新增系统用户

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userName | string | 是 | 用户名 |
| nickName | string | 是 | 昵称 |
| email | string | 否 | 邮箱 |
| phonenumber | string | 否 | 手机号 |
| sex | string | 否 | 性别（男/女/未知） |
| avatar | string | 否 | 头像URL |
| status | string | 否 | 状态，默认0（正常） |

**请求示例**:

```json
{
  "userName": "newuser",
  "nickName": "新用户",
  "email": "newuser@example.com",
  "phonenumber": "13900139000",
  "sex": "男",
  "status": "0"
}
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": true
}
```

---

### 2.6 修改用户

**路径**: `PUT /api/system/user`

**功能描述**: 修改用户信息

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 用户ID |
| nickName | string | 否 | 昵称 |
| email | string | 否 | 邮箱 |
| phonenumber | string | 否 | 手机号 |
| sex | string | 否 | 性别 |
| avatar | string | 否 | 头像URL |
| status | string | 否 | 状态 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.7 切换用户状态

**路径**: `PUT /api/system/user/{id}/status`

**功能描述**: 切换用户启用/停用状态

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 用户ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | string | 是 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.8 删除用户

**路径**: `DELETE /api/system/user/{ids}`

**功能描述**: 删除用户（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 用户ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.9 重置密码

**路径**: `PUT /api/system/user/{id}/reset-password`

**功能描述**: 重置用户密码为默认值

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 用户ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.10 更新用户信息（支持头像上传）

**路径**: `POST /api/system/user/update/infoFile`

**功能描述**: 更新用户信息，支持头像文件上传

**请求体**: `multipart/form-data`

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| user | string | 是 | 用户信息JSON字符串，必须包含id字段 |
| file | MultipartFile | 否 | 头像文件，支持jpg、png、gif，大小不超过2MB |

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
  "msg": "文件大小不能超过2MB",
  "data": null
}
```