# 字典类型 API接口文档

## 1. 接口列表

### 1.1 基础管理接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/dict/type/list | GET | SysDictTypeController.java | 查询字典类型列表 |
| /api/system/dict/type/page | GET | SysDictTypeController.java | 分页查询字典类型列表 |
| /api/system/dict/type/{id} | GET | SysDictTypeController.java | 获取字典类型详细信息 |
| /api/system/dict/type | POST | SysDictTypeController.java | 新增字典类型 |
| /api/system/dict/type | PUT | SysDictTypeController.java | 修改字典类型 |
| /api/system/dict/type/{ids} | DELETE | SysDictTypeController.java | 删除字典类型 |

### 1.2 缓存管理接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/dict/type/cache/warmUp | POST | SysDictTypeController.java | 手动触发缓存预热 |
| /api/system/dict/type/cache/tags | GET | SysDictTypeController.java | 获取所有缓存标签集合 |
| /api/system/dict/type/cache/tag/{tag} | GET | SysDictTypeController.java | 根据标签获取缓存数据 |
| /api/system/dict/type/cache/all | GET | SysDictTypeController.java | 获取全部缓存数据（按标签分组） |
| /api/system/dict/type/cache/refresh/{dictType} | POST | SysDictTypeController.java | 刷新指定字典类型的缓存 |
| /api/system/dict/type/cache/{dictType} | DELETE | SysDictTypeController.java | 删除指定字典类型的缓存 |
| /api/system/dict/type/cache/all | DELETE | SysDictTypeController.java | 清空所有字典缓存 |

---

## 2. 缓存设计说明

### 2.1 缓存 Key 命名规则

| 缓存 Key | 类型 | 存储内容 | 示例 |
|---------|------|---------|------|
| `dict:tags` | Set | 所有已缓存的字典类型标签集合 | 成员: `sys_common_status`, `sys_yes_no`, `doc_audit_status` |
| `dict:data:{dictType}` | List | 某个字典类型下的数据列表（按 dictSort 排序） | `dict:data:sys_common_status` → `[{正常,0}, {停用,1}]` |

### 2.2 缓存标签区分方式

**标签 = dictType 值**，例如：

- `sys_common_status` — 通用状态
- `sys_yes_no` — 是否开关  
- `doc_audit_status` — 审核状态

所有标签统一存储在 `dict:tags` 这个 Redis Set 中，通过它可以快速查询当前系统缓存了哪些字典类型。

### 2.3 缓存过期策略

- **过期时间**: 24 小时
- **自动预热**: 应用启动时自动执行缓存预热
- **手动刷新**: 字典数据变更时调用刷新接口

---

## 3. 基础接口详情

### 3.1 查询字典类型列表

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

### 3.2 分页查询字典类型列表

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

### 3.3 获取字典类型详细信息

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

### 3.4 新增字典类型

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

### 3.5 修改字典类型

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

### 3.6 删除字典类型

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

---

## 4. 缓存管理接口详情

### 4.1 手动触发缓存预热

**路径**: `POST /api/system/dict/type/cache/warmUp`

**功能描述**: 手动触发缓存预热，加载所有正常状态的字典类型及其数据到Redis

**请求参数**: 无

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 4.2 获取所有缓存标签集合

**路径**: `GET /api/system/dict/type/cache/tags`

**功能描述**: 获取所有已缓存的字典类型标签（dictType）集合

**请求参数**: 无

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    "sys_common_status",
    "sys_yes_no",
    "sys_user_type",
    "sys_user_sex",
    "sys_data_scope",
    "doc_audit_status",
    "doc_note_status",
    "video_category",
    "video_tag",
    "document_category",
    "document_tag"
  ]
}
```

---

### 4.3 根据标签获取缓存数据

**路径**: `GET /api/system/dict/type/cache/tag/{tag}`

**功能描述**: 根据字典类型标签获取缓存的字典数据列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tag | string | 是 | 字典类型标签（dictType值） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 20021,
      "dictSort": 1,
      "dictLabel": "正常",
      "dictValue": "0",
      "dictType": "sys_common_status",
      "listClass": "success",
      "isDefault": 1,
      "status": "0"
    },
    {
      "id": 20022,
      "dictSort": 2,
      "dictLabel": "停用",
      "dictValue": "1",
      "dictType": "sys_common_status",
      "listClass": "danger",
      "isDefault": 0,
      "status": "0"
    }
  ]
}
```

---

### 4.4 获取全部缓存数据（按标签分组）

**路径**: `GET /api/system/dict/type/cache/all`

**功能描述**: 获取所有已缓存的字典数据，按字典类型分组

**请求参数**: 无

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "sys_common_status": [
      {"id": 20021, "dictLabel": "正常", "dictValue": "0", "dictSort": 1, ...},
      {"id": 20022, "dictLabel": "停用", "dictValue": "1", "dictSort": 2, ...}
    ],
    "sys_yes_no": [
      {"id": 20041, "dictLabel": "否", "dictValue": "0", "dictSort": 1, ...},
      {"id": 20042, "dictLabel": "是", "dictValue": "1", "dictSort": 2, ...}
    ],
    "video_category": [
      {"id": 10001, "dictLabel": "前端开发", "dictValue": "1", "dictSort": 1, ...},
      {"id": 10002, "dictLabel": "后端开发", "dictValue": "2", "dictSort": 2, ...}
    ]
  }
}
```

---

### 4.5 刷新指定字典类型的缓存

**路径**: `POST /api/system/dict/type/cache/refresh/{dictType}`

**功能描述**: 刷新指定字典类型的缓存，先删除旧缓存再重新从数据库加载

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型（如 sys_common_status） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 4.6 删除指定字典类型的缓存

**路径**: `DELETE /api/system/dict/type/cache/{dictType}`

**功能描述**: 删除指定字典类型的缓存及对应标签

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型（如 sys_common_status） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 4.7 清空所有字典缓存

**路径**: `DELETE /api/system/dict/type/cache/all`

**功能描述**: 清空所有字典类型的缓存

**请求参数**: 无

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```
