# [document] 文档审核 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docNoteReview/list` | GET | `DocNoteReviewController.java` | 查询文档审核列表 |
| `/api/document/docNoteReview/page` | GET | `DocNoteReviewController.java` | 分页查询文档审核列表 |
| `/api/document/docNoteReview/{id}` | GET | `DocNoteReviewController.java` | 获取文档审核详细信息 |
| `/api/document/docNoteReview` | POST | `DocNoteReviewController.java` | 新增文档审核 |
| `/api/document/docNoteReview` | PUT | `DocNoteReviewController.java` | 修改文档审核 |
| `/api/document/docNoteReview/{ids}` | DELETE | `DocNoteReviewController.java` | 删除文档审核 |
| `/api/document/docNoteReview/batch` | PUT | `DocNoteReviewController.java` | 批量审核 |

---

## 1. 查询文档审核列表

### 接口信息
- **URL**: `GET /api/document/docNoteReview/list`
- **功能**: 查询文档审核列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docId` | `string` | 否 | 文档ID |
| `status` | `number` | 否 | 审核状态（0待审核，1已通过，2已拒绝） |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "docId": "1",
      "status": 1,
      "reviewComment": "审核通过",
      "reviewerId": "1",
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
| `id` | `string` | 审核ID（后端使用Jackson转为string类型） |
| `docId` | `string` | 文档ID |
| `status` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `reviewComment` | `string` | 审核意见 |
| `reviewerId` | `string` | 审核人ID |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询文档审核列表

### 接口信息
- **URL**: `GET /api/document/docNoteReview/page`
- **功能**: 分页查询文档审核列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `docId` | `string` | 否 | - | 文档ID |
| `status` | `number` | 否 | - | 审核状态 |
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
        "docId": "1",
        "status": 1,
        "reviewComment": "审核通过",
        "reviewerId": "1",
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

## 3. 获取文档审核详细信息

### 接口信息
- **URL**: `GET /api/document/docNoteReview/{id}`
- **功能**: 获取文档审核详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 审核ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "docId": "1",
    "status": 1,
    "reviewComment": "审核通过",
    "reviewerId": "1",
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增文档审核

### 接口信息
- **URL**: `POST /api/document/docNoteReview`
- **功能**: 新增文档审核

### 请求体

```json
{
  "docId": "1",
  "status": 1,
  "reviewComment": "审核通过",
  "reviewerId": "1"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docId` | `string` | 是 | 文档ID |
| `status` | `number` | 是 | 审核状态 |
| `reviewComment` | `string` | 否 | 审核意见 |
| `reviewerId` | `string` | 是 | 审核人ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改文档审核

### 接口信息
- **URL**: `PUT /api/document/docNoteReview`
- **功能**: 修改文档审核

### 请求体

```json
{
  "id": "1",
  "status": 2,
  "reviewComment": "审核不通过"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 审核ID |
| `status` | `number` | 否 | 审核状态 |
| `reviewComment` | `string` | 否 | 审核意见 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除文档审核

### 接口信息
- **URL**: `DELETE /api/document/docNoteReview/{ids}`
- **功能**: 批量删除文档审核

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 审核ID列表，逗号分隔 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 7. 批量审核

### 接口信息
- **URL**: `PUT /api/document/docNoteReview/batch`
- **功能**: 批量审核文档

### 请求体

```json
{
  "ids": ["1", "2", "3"],
  "status": 1,
  "reviewComment": "批量审核通过"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `array` | 是 | 审核ID列表 |
| `status` | `number` | 是 | 审核状态 |
| `reviewComment` | `string` | 否 | 审核意见 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
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
