# 字典管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/dict/type/list | GET | SysDictTypeController.java | 查询字典类型列表 |
| /api/system/dict/type/{id} | GET | SysDictTypeController.java | 获取字典类型详细信息 |
| /api/system/dict/type | POST | SysDictTypeController.java | 新增字典类型 |
| /api/system/dict/type | PUT | SysDictTypeController.java | 修改字典类型 |
| /api/system/dict/type/{ids} | DELETE | SysDictTypeController.java | 删除字典类型 |
| /api/system/dict/data/list | GET | SysDictDataController.java | 查询字典数据列表 |
| /api/system/dict/data/type/{dictType} | GET | SysDictDataController.java | 根据字典类型查询数据 |
| /api/system/dict/data/{id} | GET | SysDictDataController.java | 获取字典数据详细信息 |
| /api/system/dict/data | POST | SysDictDataController.java | 新增字典数据 |
| /api/system/dict/data | PUT | SysDictDataController.java | 修改字典数据 |
| /api/system/dict/data/{ids} | DELETE | SysDictDataController.java | 删除字典数据 |
| /api/system/dict/data/toggleStatus | PUT | SysDictDataController.java | 切换字典状态 |
| /api/system/dict/data/batchToggleStatus | PUT | SysDictDataController.java | 批量切换字典状态 |

## 2. 接口详情

### 2.1 查询字典类型列表

**路径**: `GET /api/system/dict/type/list`

**功能描述**: 查询字典类型列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictName | string | 否 | 字典名称（模糊查询） |
| dictType | string | 否 | 字典类型（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "dictName": "用户状态",
      "dictType": "user_status",
      "status": "0",
      "remark": "用户状态字典"
    }
  ]
}
```

---

### 2.2 新增字典类型

**路径**: `POST /api/system/dict/type`

**功能描述**: 新增字典类型

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictName | string | 是 | 字典名称 |
| dictType | string | 是 | 字典类型标识 |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "dictName": "用户状态",
  "dictType": "user_status",
  "status": "0",
  "remark": "用户状态字典"
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

### 2.3 根据字典类型查询字典数据

**路径**: `GET /api/system/dict/data/type/{dictType}`

**功能描述**: 根据字典类型查询字典数据列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型标识 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "dictType": "user_status",
      "dictLabel": "正常",
      "dictValue": "0",
      "dictSort": 1,
      "status": "0"
    },
    {
      "id": "2",
      "dictType": "user_status",
      "dictLabel": "停用",
      "dictValue": "1",
      "dictSort": 2,
      "status": "0"
    }
  ]
}
```

---

### 2.4 新增字典数据

**路径**: `POST /api/system/dict/data`

**功能描述**: 新增字典数据

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型标识 |
| dictLabel | string | 是 | 字典标签（显示值） |
| dictValue | string | 是 | 字典值（实际值） |
| dictSort | int | 否 | 排序，默认0 |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "dictType": "user_status",
  "dictLabel": "正常",
  "dictValue": "0",
  "dictSort": 1,
  "status": "0"
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

### 2.5 切换字典状态

**路径**: `PUT /api/system/dict/data/toggleStatus`

**功能描述**: 切换字典数据状态

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 字典ID |
| status | string | 是 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```