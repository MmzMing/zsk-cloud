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
| /api/system/user/{userId}/roles | GET | SysUserController.java | 查询用户关联的角色列表 |
| /api/system/user/{userId}/roles | POST | SysUserController.java | 绑定用户角色（追加角色） |
| /api/system/user/{userId}/roles | DELETE | SysUserController.java | 解绑用户角色（移除角色） |
| /api/system/user/{userId}/roles | PUT | SysUserController.java | 更新用户角色（全量替换角色） |

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
      "userType": "00",
      "email": "admin@example.com",
      "phonenumber": "13800138000",
      "sex": "0",
      "avatar": "https://example.com/avatar.jpg",
      "avatarId": "100",
      "age": 25,
      "bio": "系统管理员",
      "status": "0",
      "loginIp": "192.168.1.1",
      "loginDate": "2026-04-20 10:30:00",
      "createdAt": "2026-02-15 10:00:00",
      "updatedAt": "2026-04-20 10:30:00",
      "deleted": 0,
      "tenantId": "1"
    },
    "roles": ["admin", "user"],
    "permissions": ["user:list", "user:add"]
  }
}
```

| 参数名 | 类型 | 说明 |
|--------|------|------|
| sysUser | object | 用户基本信息 |
| sysUser.id | string | 用户ID |
| sysUser.userName | string | 用户账号 |
| sysUser.nickName | string | 用户昵称 |
| sysUser.userType | string | 用户类型（00系统用户） |
| sysUser.email | string | 用户邮箱 |
| sysUser.phonenumber | string | 手机号码 |
| sysUser.sex | string | 用户性别（0男 1女 2未知） |
| sysUser.avatar | string | 头像地址 |
| sysUser.avatarId | string | 头像图片ID |
| sysUser.age | int | 年龄 |
| sysUser.bio | string | 个人简介 |
| sysUser.status | string | 帐号状态（0正常 1停用） |
| sysUser.loginIp | string | 最后登录IP |
| sysUser.loginDate | string | 最后登录时间 |
| sysUser.createdAt | string | 创建时间 |
| sysUser.updatedAt | string | 更新时间 |
| roles | array | 用户角色标识列表 |
| permissions | array | 用户权限标识列表 |

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
    "userType": "00",
    "email": "admin@example.com",
    "phonenumber": "13800138000",
    "sex": "0",
    "avatar": "https://example.com/avatar.jpg",
    "avatarId": "100",
    "age": 25,
    "bio": "系统管理员",
    "status": "0",
    "loginIp": "192.168.1.1",
    "loginDate": "2026-04-20 10:30:00",
    "createdAt": "2026-02-15 10:00:00",
    "updatedAt": "2026-04-20 10:30:00",
    "deleted": 0,
    "tenantId": "1"
  }
}
```

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 用户ID |
| userName | string | 用户账号 |
| nickName | string | 用户昵称 |
| userType | string | 用户类型（00系统用户） |
| email | string | 用户邮箱 |
| phonenumber | string | 手机号码 |
| sex | string | 用户性别（0男 1女 2未知） |
| avatar | string | 头像地址 |
| avatarId | string | 头像图片ID |
| age | int | 年龄 |
| bio | string | 个人简介 |
| status | string | 帐号状态（0正常 1停用） |
| loginIp | string | 最后登录IP |
| loginDate | string | 最后登录时间 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |
| deleted | int | 删除标识（0未删除 1已删除） |
| tenantId | string | 租户ID |

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
        "userType": "00",
        "email": "admin@example.com",
        "phonenumber": "13800138000",
        "sex": "0",
        "avatar": "https://example.com/avatar.jpg",
        "avatarId": "100",
        "age": 25,
        "bio": "系统管理员",
        "status": "0",
        "loginIp": "192.168.1.1",
        "loginDate": "2026-04-20 10:30:00",
        "createdAt": "2026-02-15 10:00:00",
        "updatedAt": "2026-04-20 10:30:00",
        "deleted": 0,
        "tenantId": "1",
        "roleIds": ["1", "2"]
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

| 参数名 | 类型 | 说明 |
|--------|------|------|
| content | array | 用户列表 |
| content[].id | string | 用户ID |
| content[].userName | string | 用户账号 |
| content[].nickName | string | 用户昵称 |
| content[].userType | string | 用户类型 |
| content[].email | string | 用户邮箱 |
| content[].phonenumber | string | 手机号码 |
| content[].sex | string | 用户性别 |
| content[].avatar | string | 头像地址 |
| content[].avatarId | string | 头像图片ID |
| content[].age | int | 年龄 |
| content[].bio | string | 个人简介 |
| content[].status | string | 帐号状态 |
| content[].loginIp | string | 最后登录IP |
| content[].loginDate | string | 最后登录时间 |
| content[].createdAt | string | 创建时间 |
| content[].updatedAt | string | 更新时间 |
| content[].roleIds | array | 角色ID列表 |
| totalElements | int | 总记录数 |
| totalPages | int | 总页数 |
| size | int | 每页大小 |
| number | int | 当前页码（从0开始） |
| first | boolean | 是否第一页 |
| last | boolean | 是否最后一页 |

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
    "userType": "00",
    "email": "admin@example.com",
    "phonenumber": "13800138000",
    "sex": "0",
    "avatar": "https://example.com/avatar.jpg",
    "avatarId": "100",
    "age": 25,
    "bio": "系统管理员",
    "status": "0",
    "loginIp": "192.168.1.1",
    "loginDate": "2026-04-20 10:30:00",
    "createdAt": "2026-02-15 10:00:00",
    "updatedAt": "2026-04-20 10:30:00",
    "deleted": 0,
    "tenantId": "1",
    "roleIds": ["1", "2"]
  }
}
```

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | string | 用户ID |
| userName | string | 用户账号 |
| nickName | string | 用户昵称 |
| userType | string | 用户类型（00系统用户） |
| email | string | 用户邮箱 |
| phonenumber | string | 手机号码 |
| sex | string | 用户性别（0男 1女 2未知） |
| avatar | string | 头像地址 |
| avatarId | string | 头像图片ID |
| age | int | 年龄 |
| bio | string | 个人简介 |
| status | string | 帐号状态（0正常 1停用） |
| loginIp | string | 最后登录IP |
| loginDate | string | 最后登录时间 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |
| deleted | int | 删除标识（0未删除 1已删除） |
| tenantId | string | 租户ID |
| roleIds | array | 角色ID列表 |

---

### 2.5 新增用户

**路径**: `POST /api/system/user`

**功能描述**: 新增系统用户

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userName | string | 是 | 用户账号 |
| nickName | string | 是 | 用户昵称 |
| userType | string | 否 | 用户类型（00系统用户） |
| email | string | 否 | 用户邮箱 |
| phonenumber | string | 否 | 手机号码 |
| sex | string | 否 | 用户性别（0男 1女 2未知） |
| avatar | string | 否 | 头像地址 |
| age | int | 否 | 年龄 |
| bio | string | 否 | 个人简介 |
| status | string | 否 | 帐号状态（0正常 1停用），默认0 |
| roleIds | array | 否 | 角色ID列表 |

**请求示例**:

```json
{
  "userName": "newuser",
  "nickName": "新用户",
  "userType": "00",
  "email": "newuser@example.com",
  "phonenumber": "13900139000",
  "sex": "0",
  "age": 25,
  "bio": "新用户简介",
  "status": "0",
  "roleIds": ["1", "2"]
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
| nickName | string | 否 | 用户昵称 |
| userType | string | 否 | 用户类型 |
| email | string | 否 | 用户邮箱 |
| phonenumber | string | 否 | 手机号码 |
| sex | string | 否 | 用户性别（0男 1女 2未知） |
| avatar | string | 否 | 头像地址 |
| avatarId | string | 否 | 头像图片ID |
| age | int | 否 | 年龄 |
| bio | string | 否 | 个人简介 |
| status | string | 否 | 帐号状态（0正常 1停用） |
| roleIds | array | 否 | 角色ID列表 |

**请求示例**:

```json
{
  "id": "1",
  "nickName": "管理员",
  "email": "admin@example.com",
  "status": "0",
  "roleIds": ["1", "3"]
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

---

### 2.11 查询用户关联的角色列表

**路径**: `GET /api/system/user/{userId}/roles`

**功能描述**: 查询指定用户关联的角色ID列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": ["1", "2", "3"]
}
```

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | array | 角色ID列表（字符串数组） |

---

### 2.12 绑定用户角色（追加角色）

**路径**: `POST /api/system/user/{userId}/roles`

**功能描述**: 为用户追加绑定角色（不会移除已有角色）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户ID |

**请求体**:

```json
["1", "2", "3"]
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleIds | array | 是 | 角色ID列表（字符串数组） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.13 解绑用户角色（移除角色）

**路径**: `DELETE /api/system/user/{userId}/roles`

**功能描述**: 移除用户已绑定的角色

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户ID |

**请求体**:

```json
["1", "2"]
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleIds | array | 是 | 要移除的角色ID列表（字符串数组） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.14 更新用户角色（全量替换角色）

**路径**: `PUT /api/system/user/{userId}/roles`

**功能描述**: 全量替换用户的角色（先清除所有现有角色，再绑定新角色）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户ID |

**请求体**:

```json
["2", "3"]
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleIds | array | 是 | 新的角色ID列表（字符串数组） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```