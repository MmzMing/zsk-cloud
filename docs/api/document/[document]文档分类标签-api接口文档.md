# [document] 文档分类标签 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docNoteCategory/list` | GET | `DocNoteCategoryController.java` | 查询文档分类列表 |
| `/api/document/docNoteCategory/page` | GET | `DocNoteCategoryController.java` | 分页查询文档分类列表 |
| `/api/document/docNoteCategory/{id}` | GET | `DocNoteCategoryController.java` | 获取文档分类详细信息 |
| `/api/document/docNoteCategory` | POST | `DocNoteCategoryController.java` | 新增文档分类 |
| `/api/document/docNoteCategory` | PUT | `DocNoteCategoryController.java` | 修改文档分类 |
| `/api/document/docNoteCategory/{ids}` | DELETE | `DocNoteCategoryController.java` | 删除文档分类 |

---

## 1. 查询文档分类列表

### 接口信息
- **URL**: `GET /api/document/docNoteCategory/list`
- **功能**: 查询文档分类列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `broadName` | `string` | 否 | 分类名称（支持模糊查询） |
| `broadCode` | `string` | 否 | 分类编码 |
| `status` | `number` | 否 | 状态（0禁用，1启用） |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "broadName": "技术",
      "broadCode": "tech",
      "sort": 1,
      "status": 1,
      "deleted": 0,
      "createTime": "2026-02-15 10:30:00",
      "updateTime": "2026-02-15 10:30:00"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 分类ID（后端使用Jackson转为string类型） |
| `broadName` | `string` | 分类名称 |
| `broadCode` | `string` | 分类编码 |
| `sort` | `number` | 排序序号 |
| `status` | `number` | 状态（0禁用，1启用） |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询文档分类列表

### 接口信息
- **URL**: `GET /api/document/docNoteCategory/page`
- **功能**: 分页查询文档分类列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `broadName` | `string` | 否 | - | 分类名称 |
| `broadCode` | `string` | 否 | - | 分类编码 |
| `status` | `number` | 否 | - | 状态 |
| `deleted` | `number` | 否 | - | 删除标记 |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "broadName": "技术",
        "broadCode": "tech",
        "sort": 1,
        "status": 1,
        "deleted": 0,
        "createTime": "2026-02-15 10:30:00",
        "updateTime": "2026-02-15 10:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 3. 获取文档分类详细信息

### 接口信息
- **URL**: `GET /api/document/docNoteCategory/{id}`
- **功能**: 获取文档分类详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 分类ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "broadName": "技术",
    "broadCode": "tech",
    "sort": 1,
    "status": 1,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增文档分类

### 接口信息
- **URL**: `POST /api/document/docNoteCategory`
- **功能**: 新增文档分类

### 请求体

```json
{
  "broadName": "生活",
  "broadCode": "life",
  "sort": 2,
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `broadName` | `string` | 是 | 分类名称 |
| `broadCode` | `string` | 是 | 分类编码 |
| `sort` | `number` | 否 | 排序序号 |
| `status` | `number` | 否 | 状态（默认1） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改文档分类

### 接口信息
- **URL**: `PUT /api/document/docNoteCategory`
- **功能**: 修改文档分类

### 请求体

```json
{
  "id": "1",
  "broadName": "修改后的分类",
  "broadCode": "new-code",
  "sort": 3,
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 分类ID |
| `broadName` | `string` | 否 | 分类名称 |
| `broadCode` | `string` | 否 | 分类编码 |
| `sort` | `number` | 否 | 排序序号 |
| `status` | `number` | 否 | 状态 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除文档分类

### 接口信息
- **URL**: `DELETE /api/document/docNoteCategory/{ids}`
- **功能**: 批量删除文档分类

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 分类ID列表，逗号分隔 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

### 失败响应

```json
{
  "code": 500,
  "msg": "错误信息",
  "data": null
}
```

### 字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `code` | `number` | 状态码（200成功，其他为失败） |
| `msg` | `string` | 响应消息 |
| `data` | `object/array/boolean/null` | 响应数据 |
