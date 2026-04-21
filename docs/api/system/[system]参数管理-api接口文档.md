# 参数管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/config/list | GET | SysConfigController.java | 查询参数列表 |
| /api/system/config/page | GET | SysConfigController.java | 分页查询参数列表 |
| /api/system/config/{id} | GET | SysConfigController.java | 获取参数详细信息 |
| /api/system/config | POST | SysConfigController.java | 新增参数 |
| /api/system/config | PUT | SysConfigController.java | 修改参数 |
| /api/system/config/{ids} | DELETE | SysConfigController.java | 删除参数 |

## 2. 接口详情

### 2.1 查询参数列表

**路径**: `GET /api/system/config/list`

**功能描述**: 查询系统参数列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configName | string | 否 | 参数名称（模糊查询） |
| configKey | string | 否 | 参数键名（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "configName": "网站标题",
      "configKey": "site.title",
      "configValue": "ZSK系统",
      "status": "0",
      "remark": "网站首页标题"
    }
  ]
}
```

---

### 2.2 分页查询参数列表

**路径**: `GET /api/system/config/page`

**功能描述**: 分页查询系统参数列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Long | 否 | 当前页码，默认1 |
| pageSize | Long | 否 | 每页大小，默认10，最大500 |
| orderByColumn | string | 否 | 排序字段 |
| isAsc | string | 否 | 排序方向（asc/desc），默认asc |
| configName | string | 否 | 参数名称（模糊查询） |
| configKey | string | 否 | 参数键名（精确查询） |
| configType | Integer | 否 | 参数类型（0否 1是） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "configName": "网站标题",
        "configKey": "site.title",
        "configValue": "ZSK系统",
        "configType": 0,
        "status": "0",
        "remark": "网站首页标题"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 2.3 获取参数详细信息

**路径**: `GET /api/system/config/{id}`

**功能描述**: 根据参数ID获取参数详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 参数ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "configName": "网站标题",
    "configKey": "site.title",
    "configValue": "ZSK系统",
    "status": "0",
    "remark": "网站首页标题",
    "createTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.4 新增参数

**路径**: `POST /api/system/config`

**功能描述**: 新增系统参数

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configName | string | 是 | 参数名称 |
| configKey | string | 是 | 参数键名（唯一标识） |
| configValue | string | 是 | 参数值 |
| configType | Integer | 否 | 参数类型，默认0（0否 1是） |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "configName": "网站标题",
  "configKey": "site.title",
  "configValue": "ZSK系统",
  "configType": 0,
  "status": "0",
  "remark": "网站首页标题"
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

### 2.5 修改参数

**路径**: `PUT /api/system/config`

**功能描述**: 修改系统参数

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 参数ID |
| configName | string | 否 | 参数名称 |
| configValue | string | 否 | 参数值 |
| configType | Integer | 否 | 参数类型 |
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

### 2.6 删除参数

**路径**: `DELETE /api/system/config/{ids}`

**功能描述**: 删除参数（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 参数ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```
