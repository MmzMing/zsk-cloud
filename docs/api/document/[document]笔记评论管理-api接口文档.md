# [document] 笔记评论管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docNoteComment/list` | GET | `DocNoteCommentController.java` | 查询笔记评论列表 |
| `/api/document/docNoteComment/page` | GET | `DocNoteCommentController.java` | 分页查询笔记评论列表 |
| `/api/document/docNoteComment/{id}` | GET | `DocNoteCommentController.java` | 获取笔记评论详细信息 |
| `/api/document/docNoteComment` | POST | `DocNoteCommentController.java` | 新增笔记评论 |
| `/api/document/docNoteComment` | PUT | `DocNoteCommentController.java` | 修改笔记评论 |
| `/api/document/docNoteComment/{ids}` | DELETE | `DocNoteCommentController.java` | 删除笔记评论 |

---

## 1. 查询笔记评论列表

### 接口信息
- **URL**: `GET /api/document/docNoteComment/list`
- **功能**: 查询笔记评论列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docId` | `string` | 否 | 笔记ID |
| `userId` | `string` | 否 | 用户ID |
| `content` | `string` | 否 | 评论内容（支持模糊查询） |
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
      "userId": "1",
      "content": "评论内容",
      "parentId": "0",
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
| `id` | `string` | 评论ID（后端使用Jackson转为string类型） |
| `docId` | `string` | 笔记ID |
| `userId` | `string` | 用户ID |
| `content` | `string` | 评论内容 |
| `parentId` | `string` | 父评论ID（0表示顶级评论） |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询笔记评论列表

### 接口信息
- **URL**: `GET /api/document/docNoteComment/page`
- **功能**: 分页查询笔记评论列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `docId` | `string` | 否 | - | 笔记ID |
| `userId` | `string` | 否 | - | 用户ID |
| `content` | `string` | 否 | - | 评论内容 |
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
        "userId": "1",
        "content": "评论内容",
        "parentId": "0",
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

## 3. 获取笔记评论详细信息

### 接口信息
- **URL**: `GET /api/document/docNoteComment/{id}`
- **功能**: 获取笔记评论详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 评论ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "docId": "1",
    "userId": "1",
    "content": "评论内容",
    "parentId": "0",
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增笔记评论

### 接口信息
- **URL**: `POST /api/document/docNoteComment`
- **功能**: 新增笔记评论

### 请求体

```json
{
  "docId": "1",
  "userId": "1",
  "content": "评论内容",
  "parentId": "0"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docId` | `string` | 是 | 笔记ID |
| `userId` | `string` | 是 | 用户ID |
| `content` | `string` | 是 | 评论内容 |
| `parentId` | `string` | 否 | 父评论ID（默认0） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改笔记评论

### 接口信息
- **URL**: `PUT /api/document/docNoteComment`
- **功能**: 修改笔记评论

### 请求体

```json
{
  "id": "1",
  "content": "修改后的评论内容"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 评论ID |
| `content` | `string` | 否 | 评论内容 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除笔记评论

### 接口信息
- **URL**: `DELETE /api/document/docNoteComment/{ids}`
- **功能**: 批量删除笔记评论

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 评论ID列表，逗号分隔 |

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
