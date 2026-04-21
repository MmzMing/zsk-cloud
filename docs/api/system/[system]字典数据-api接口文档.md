# 字典数据 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/dict/data/list | GET | SysDictDataController.java | 查询字典数据列表 |
| /api/system/dict/data/page | GET | SysDictDataController.java | 分页查询字典数据列表 |
| /api/system/dict/data/type/{dictType} | GET | SysDictDataController.java | 根据字典类型查询字典数据 |
| /api/system/dict/data/{id} | GET | SysDictDataController.java | 获取字典数据详细信息 |
| /api/system/dict/data | POST | SysDictDataController.java | 新增字典数据 |
| /api/system/dict/data | PUT | SysDictDataController.java | 修改字典数据 |
| /api/system/dict/data/{ids} | DELETE | SysDictDataController.java | 删除字典数据 |
| /api/system/dict/data/toggleStatus | PUT | SysDictDataController.java | 切换字典状态 |
| /api/system/dict/data/batchToggleStatus | PUT | SysDictDataController.java | 批量切换字典状态 |

## 2. 接口详情

### 2.1 查询字典数据列表

**路径**: `GET /api/system/dict/data/list`

**功能描述**: 查询字典数据列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 否 | 字典类型（精确查询） |
| dictLabel | string | 否 | 字典标签（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "10001",
      "dictLabel": "前端开发",
      "dictValue": "1",
      "dictType": "video_category",
      "dictSort": 1,
      "status": "0"
    }
  ]
}
```

---

### 2.2 分页查询字典数据列表

**路径**: `GET /api/system/dict/data/page`

**功能描述**: 分页查询字典数据列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Long | 否 | 当前页码，默认1 |
| pageSize | Long | 否 | 每页大小，默认10，最大500 |
| orderByColumn | string | 否 | 排序字段 |
| isAsc | string | 否 | 排序方向（asc/desc），默认asc |
| dictType | string | 否 | 字典类型（精确查询） |
| dictLabel | string | 否 | 字典标签（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "10001",
        "dictLabel": "前端开发",
        "dictValue": "1",
        "dictType": "video_category",
        "dictSort": 1,
        "status": "0"
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

---

### 2.3 根据字典类型查询字典数据

**路径**: `GET /api/system/dict/data/type/{dictType}`

**功能描述**: 根据字典类型查询字典数据列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "10001",
      "dictLabel": "前端开发",
      "dictValue": "1",
      "dictType": "video_category",
      "dictSort": 1,
      "status": "0"
    },
    {
      "id": "10002",
      "dictLabel": "后端开发",
      "dictValue": "2",
      "dictType": "video_category",
      "dictSort": 2,
      "status": "0"
    }
  ]
}
```

---

### 2.4 获取字典数据详细信息

**路径**: `GET /api/system/dict/data/{id}`

**功能描述**: 根据字典ID获取字典数据详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 字典数据ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "10001",
    "dictLabel": "前端开发",
    "dictValue": "1",
    "dictType": "video_category",
    "dictSort": 1,
    "cssClass": "primary",
    "listClass": "primary",
    "isDefault": 1,
    "status": "0",
    "remark": "前端开发分类",
    "createTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.5 新增字典数据

**路径**: `POST /api/system/dict/data`

**功能描述**: 新增字典数据

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictLabel | string | 是 | 字典标签 |
| dictValue | string | 是 | 字典键值 |
| dictType | string | 是 | 字典类型 |
| dictSort | Integer | 否 | 排序号，默认0 |
| cssClass | string | 否 | 样式属性 |
| listClass | string | 否 | 表格回显样式 |
| isDefault | Integer | 否 | 是否默认（0否 1是），默认0 |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "dictLabel": "前端开发",
  "dictValue": "1",
  "dictType": "video_category",
  "dictSort": 1,
  "cssClass": "primary",
  "listClass": "primary",
  "isDefault": 1,
  "status": "0",
  "remark": "前端开发分类"
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

### 2.6 修改字典数据

**路径**: `PUT /api/system/dict/data`

**功能描述**: 修改字典数据

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 字典数据ID |
| dictLabel | string | 否 | 字典标签 |
| dictValue | string | 否 | 字典键值 |
| dictType | string | 否 | 字典类型 |
| dictSort | Integer | 否 | 排序号 |
| cssClass | string | 否 | 样式属性 |
| listClass | string | 否 | 表格回显样式 |
| isDefault | Integer | 否 | 是否默认 |
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

### 2.7 删除字典数据

**路径**: `DELETE /api/system/dict/data/{ids}`

**功能描述**: 删除字典数据（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 字典数据ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.8 切换字典状态

**路径**: `PUT /api/system/dict/data/toggleStatus`

**功能描述**: 切换字典数据状态

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 字典数据ID |
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

### 2.9 批量切换字典状态

**路径**: `PUT /api/system/dict/data/batchToggleStatus`

**功能描述**: 批量切换字典数据状态

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 字典数据ID列表（请求体） |
| status | string | 是 | 状态（0正常 1停用）（请求参数） |

**请求示例**:

```json
[10001, 10002, 10003]
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```
