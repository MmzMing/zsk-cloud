# 角色管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/role/list | GET | SysRoleController.java | 查询角色列表 |
| /api/system/role/{id} | GET | SysRoleController.java | 获取角色详细信息 |
| /api/system/role | POST | SysRoleController.java | 新增角色 |
| /api/system/role | PUT | SysRoleController.java | 修改角色 |
| /api/system/role/{ids} | DELETE | SysRoleController.java | 删除角色（支持批量） |
| /api/system/role/copy | POST | SysRoleController.java | 批量复制角色 |
| /api/system/role/{roleId}/menus | GET | SysRoleController.java | 查看角色权限 |
| /api/system/role/{roleId}/menus | POST | SysRoleController.java | 绑定角色权限 |
| /api/system/role/{roleId}/menus | DELETE | SysRoleController.java | 解绑角色权限 |
| /api/system/role/{roleId}/menus | PUT | SysRoleController.java | 更新角色权限 |
| /api/system/role/{roleId}/users | GET | SysRoleController.java | 查看角色用户 |
| /api/system/role/{roleId}/users | POST | SysRoleController.java | 绑定角色用户 |
| /api/system/role/{roleId}/users | DELETE | SysRoleController.java | 解绑角色用户 |
| /api/system/role/{roleId}/users | PUT | SysRoleController.java | 更新角色用户 |

## 2. 接口详情

### 2.1 查询角色列表

**路径**: `GET /api/system/role/list`

**功能描述**: 查询角色列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleName | string | 否 | 角色名称（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "roleName": "管理员",
      "roleKey": "admin",
      "status": "0",
      "remark": "系统管理员"
    }
  ]
}
```

---

### 2.2 获取角色详细信息

**路径**: `GET /api/system/role/{id}`

**功能描述**: 根据角色ID获取角色详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 角色ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "roleName": "管理员",
    "roleKey": "admin",
    "roleSort": 1,
    "status": "0",
    "remark": "系统管理员",
    "createTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.3 新增角色

**路径**: `POST /api/system/role`

**功能描述**: 新增角色

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleName | string | 是 | 角色名称 |
| roleKey | string | 是 | 角色权限字符串 |
| roleSort | int | 否 | 角色排序 |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "roleName": "普通用户",
  "roleKey": "user",
  "roleSort": 2,
  "status": "0",
  "remark": "普通用户角色"
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

### 2.4 修改角色

**路径**: `PUT /api/system/role`

**功能描述**: 修改角色信息

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 角色ID |
| roleName | string | 否 | 角色名称 |
| roleKey | string | 否 | 角色权限字符串 |
| roleSort | int | 否 | 角色排序 |
| status | string | 否 | 状态 |
| remark | string | 否 | 备注 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.5 删除角色

**路径**: `DELETE /api/system/role/{ids}`

**功能描述**: 删除角色（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 角色ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.6 批量复制角色

**路径**: `POST /api/system/role/copy`

**功能描述**: 批量复制角色

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 要复制的角色ID列表 |

**请求示例**:

```json
{
  "ids": [1, 2, 3]
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

### 2.7 查看角色权限

**路径**: `GET /api/system/role/{roleId}/menus`

**功能描述**: 查询角色关联的菜单ID列表（即角色拥有的权限）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [1, 2, 3, 100, 101]
}
```

---

### 2.8 绑定角色权限

**路径**: `POST /api/system/role/{roleId}/menus`

**功能描述**: 为角色追加权限（菜单），已存在的菜单不会重复绑定

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuIds | List<Long> | 是 | 要绑定的菜单ID列表 |

**请求示例**:

```json
{
  "menuIds": [1, 2, 3, 100]
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

### 2.9 解绑角色权限

**路径**: `DELETE /api/system/role/{roleId}/menus`

**功能描述**: 移除角色的指定权限（菜单）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuIds | List<Long> | 是 | 要解绑的菜单ID列表 |

**请求示例**:

```json
{
  "menuIds": [1, 2]
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

### 2.10 更新角色权限

**路径**: `PUT /api/system/role/{roleId}/menus`

**功能描述**: 全量替换角色的权限（菜单），先删除原有权限再绑定新权限

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuIds | List<Long> | 是 | 新的菜单ID列表（全量） |

**请求示例**:

```json
{
  "menuIds": [1, 2, 3, 100, 101, 200]
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

### 2.11 查看角色用户

**路径**: `GET /api/system/role/{roleId}/users`

**功能描述**: 查询角色关联的用户ID列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [1, 5, 10]
}
```

---

### 2.12 绑定角色用户

**路径**: `POST /api/system/role/{roleId}/users`

**功能描述**: 为角色追加用户，已存在的用户不会重复绑定

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userIds | List<Long> | 是 | 要绑定的用户ID列表 |

**请求示例**:

```json
{
  "userIds": [1, 5, 10]
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

### 2.13 解绑角色用户

**路径**: `DELETE /api/system/role/{roleId}/users`

**功能描述**: 移除角色的指定用户

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userIds | List<Long> | 是 | 要解绑的用户ID列表 |

**请求示例**:

```json
{
  "userIds": [5, 10]
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

### 2.14 更新角色用户

**路径**: `PUT /api/system/role/{roleId}/users`

**功能描述**: 全量替换角色的用户，先删除原有用户再绑定新用户

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleId | Long | 是 | 角色ID |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userIds | List<Long> | 是 | 新的用户ID列表（全量） |

**请求示例**:

```json
{
  "userIds": [1, 5, 10, 20]
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