# 字典类型 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/dict/type/list | GET | SysDictTypeController.java | 查询字典类型列表 |
| /api/system/dict/type/page | GET | SysDictTypeController.java | 分页查询字典类型列表 |
| /api/system/dict/type/{id} | GET | SysDictTypeController.java | 获取字典类型详细信息 |
| /api/system/dict/type | POST | SysDictTypeController.java | 新增字典类型 |
| /api/system/dict/type | PUT | SysDictTypeController.java | 修改字典类型 |
| /api/system/dict/type/{ids} | DELETE | SysDictTypeController.java | 删除字典类型 |

## 2. 接口详情

### 2.1 查询字典类型列表

**路径**: `GET /api/system/dict/type/list`

**功能描述**: 查询字典类型列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictName | string | 否 | 字典名称（模糊查询） |
| dictType | string | 否 | 字典类型（精确查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1001",
      "dictName": "视频分类",
      "dictType": "video_category",
      "status": "0",
      "remark": "视频内容分类"
    }
  ]
}
```

---

### 2.2 分页查询字典类型列表

**路径**: `GET /api/system/dict/type/page`

**功能描述**: 分页查询字典类型列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Long | 否 | 当前页码，默认1 |
| pageSize | Long | 否 | 每页大小，默认10，最大500 |
| orderByColumn | string | 否 | 排序字段 |
| isAsc | string | 否 | 排序方向（asc/desc），默认asc |
| dictName | string | 否 | 字典名称（模糊查询） |
| dictType | string | 否 | 字典类型（精确查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1001",
        "dictName": "视频分类",
        "dictType": "video_category",
        "status": "0",
        "remark": "视频内容分类"
      }
    ],
    "total": 10,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### 2.3 获取字典类型详细信息

**路径**: `GET /api/system/dict/type/{id}`

**功能描述**: 根据字典类型ID获取详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 字典类型ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1001",
    "dictName": "视频分类",
    "dictType": "video_category",
    "status": "0",
    "remark": "视频内容分类",
    "createTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.4 新增字典类型

**路径**: `POST /api/system/dict/type`

**功能描述**: 新增字典类型

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictName | string | 是 | 字典名称 |
| dictType | string | 是 | 字典类型（唯一标识） |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "dictName": "视频分类",
  "dictType": "video_category",
  "status": "0",
  "remark": "视频内容分类"
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

### 2.5 修改字典类型

**路径**: `PUT /api/system/dict/type`

**功能描述**: 修改字典类型

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 字典类型ID |
| dictName | string | 否 | 字典名称 |
| dictType | string | 否 | 字典类型 |
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

### 2.6 删除字典类型

**路径**: `DELETE /api/system/dict/type/{ids}`

**功能描述**: 删除字典类型（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 字典类型ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```
