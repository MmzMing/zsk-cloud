# 菜单管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/menu/list | GET | SysMenuController.java | 查询菜单列表 |
| /api/system/menu/user/{userId} | GET | SysMenuController.java | 根据用户ID查询菜单树列表 |
| /api/system/menu/{id} | GET | SysMenuController.java | 获取菜单详细信息 |
| /api/system/menu | POST | SysMenuController.java | 新增菜单 |
| /api/system/menu | PUT | SysMenuController.java | 修改菜单 |
| /api/system/menu/batch | PUT | SysMenuController.java | 批量更新菜单 |
| /api/system/menu/{ids} | DELETE | SysMenuController.java | 删除菜单（支持批量） |

## 2. 接口详情

### 2.1 查询菜单列表

**路径**: `GET /api/system/menu/list`

**功能描述**: 查询菜单列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuName | string | 否 | 菜单名称（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |
| menuType | string | 否 | 菜单类型（M目录 C菜单 F按钮） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "parentId": 0,
      "menuType": "M",
      "path": "/system",
      "perms": "system:manage",
      "status": "0",
      "orderNum": 1,
      "visible": "0"
    }
  ]
}
```

---

### 2.2 根据用户ID查询菜单树列表

**路径**: `GET /api/system/menu/user/{userId}`

**功能描述**: 根据用户ID查询有权限的菜单树列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "parentId": 0,
      "children": [
        {
          "id": 2,
          "menuName": "用户管理",
          "parentId": 1,
          "children": []
        }
      ]
    }
  ]
}
```

---

### 2.3 获取菜单详细信息

**路径**: `GET /api/system/menu/{id}`

**功能描述**: 根据菜单ID获取菜单详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 菜单ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "menuName": "系统管理",
    "parentId": 0,
    "menuType": "M",
    "path": "/system",
    "component": "Layout",
    "perms": "system:manage",
    "icon": "system",
    "status": "0",
    "orderNum": 1,
    "visible": "0",
    "isFrame": 1,
    "isCache": 0
  }
}
```

---

### 2.4 新增菜单

**路径**: `POST /api/system/menu`

**功能描述**: 新增菜单

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| menuName | string | 是 | 菜单名称 |
| parentId | Long | 是 | 父菜单ID（顶级菜单传0） |
| menuType | string | 是 | 菜单类型（M目录 C菜单 F按钮） |
| path | string | 否 | 路由路径 |
| component | string | 否 | 组件路径 |
| perms | string | 否 | 权限标识 |
| icon | string | 否 | 图标 |
| status | string | 否 | 状态，默认0（正常） |
| orderNum | Integer | 否 | 显示顺序，默认0 |
| visible | string | 否 | 是否显示，默认0（显示） |
| isFrame | Integer | 否 | 是否外链，默认1（否） |
| isCache | Integer | 否 | 是否缓存，默认0（缓存） |
| query | string | 否 | 路由参数 |

**请求示例**:

```json
{
  "menuName": "用户管理",
  "parentId": 1,
  "menuType": "C",
  "path": "/system/user",
  "component": "system/user/index",
  "perms": "system:user:list",
  "icon": "user",
  "status": "0",
  "orderNum": 1,
  "visible": "0",
  "isFrame": 1,
  "isCache": 0
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

### 2.5 修改菜单

**路径**: `PUT /api/system/menu`

**功能描述**: 修改菜单信息

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 菜单ID |
| menuName | string | 否 | 菜单名称 |
| parentId | Long | 否 | 父菜单ID |
| menuType | string | 否 | 菜单类型（M目录 C菜单 F按钮） |
| path | string | 否 | 路由路径 |
| component | string | 否 | 组件路径 |
| perms | string | 否 | 权限标识 |
| icon | string | 否 | 图标 |
| status | string | 否 | 状态（0正常 1停用） |
| orderNum | Integer | 否 | 显示顺序 |
| visible | string | 否 | 是否显示（0显示 1隐藏） |
| isFrame | Integer | 否 | 是否外链（0是 1否） |
| isCache | Integer | 否 | 是否缓存（0缓存 1不缓存） |
| query | string | 否 | 路由参数 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.6 批量更新菜单

**路径**: `PUT /api/system/menu/batch`

**功能描述**: 批量更新菜单（用于拖拽排序等场景）

**请求体**:

```json
[
  {
    "id": 1,
    "orderNum": 2,
    "parentId": 0
  },
  {
    "id": 2,
    "orderNum": 1,
    "parentId": 0
  }
]
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

### 2.7 删除菜单

**路径**: `DELETE /api/system/menu/{ids}`

**功能描述**: 删除菜单（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 菜单ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```