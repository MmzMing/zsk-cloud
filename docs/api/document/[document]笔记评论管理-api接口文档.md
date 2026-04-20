# [document] 笔记评论管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/note/comment/list` | GET | `DocNoteCommentController.java` | 查询笔记评论列表 |
| `/api/document/note/comment/page` | GET | `DocNoteCommentController.java` | 分页查询笔记评论列表 |
| `/api/document/note/comment/{id}` | GET | `DocNoteCommentController.java` | 获取笔记评论详细信息 |
| `/api/document/note/comment` | POST | `DocNoteCommentController.java` | 新增笔记评论 |
| `/api/document/note/comment` | PUT | `DocNoteCommentController.java` | 修改笔记评论 |
| `/api/document/note/comment/{ids}` | DELETE | `DocNoteCommentController.java` | 删除笔记评论 |

---

## 1. 查询笔记评论列表

### 接口信息
- **URL**: `GET /api/document/note/comment/list`
- **功能**: 查询笔记评论列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 否 | 笔记ID |
| `commentUserId` | `string` | 否 | 评论用户ID |
| `parentCommentId` | `string` | 否 | 父评论ID |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "noteId": "10",
      "commentUserId": "1",
      "commentContent": "评论内容",
      "parentCommentId": null,
      "likeCount": 5,
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
| `noteId` | `string` | 笔记ID |
| `commentUserId` | `string` | 评论用户ID |
| `commentContent` | `string` | 评论内容 |
| `parentCommentId` | `string` | 父评论ID（顶级评论为null） |
| `likeCount` | `number` | 点赞数 |
| `deleted` | `number` | 删除标记（0未删除，1已删除） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询笔记评论列表

### 接口信息
- **URL**: `GET /api/document/note/comment/page`
- **功能**: 分页查询笔记评论列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `noteId` | `string` | 否 | - | 笔记ID |
| `commentUserId` | `string` | 否 | - | 评论用户ID |
| `parentCommentId` | `string` | 否 | - | 父评论ID |
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
        "noteId": "10",
        "commentUserId": "1",
        "commentContent": "评论内容",
        "parentCommentId": null,
        "likeCount": 5,
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
- **URL**: `GET /api/document/note/comment/{id}`
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
    "noteId": "10",
    "commentUserId": "1",
    "commentContent": "评论内容",
    "parentCommentId": null,
    "likeCount": 5,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增笔记评论

### 接口信息
- **URL**: `POST /api/document/note/comment`
- **功能**: 新增笔记评论

### 请求体

```json
{
  "noteId": "10",
  "commentUserId": "1",
  "commentContent": "新评论内容",
  "parentCommentId": null,
  "likeCount": 0,
  "deleted": 0
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 是 | 笔记ID |
| `commentUserId` | `string` | 是 | 评论用户ID |
| `commentContent` | `string` | 是 | 评论内容 |
| `parentCommentId` | `string` | 否 | 父评论ID |
| `likeCount` | `number` | 否 | 点赞数（默认0） |
| `deleted` | `number` | 否 | 删除标记（默认0） |

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
- **URL**: `PUT /api/document/note/comment`
- **功能**: 修改笔记评论

### 请求体

```json
{
  "id": "1",
  "commentContent": "修改后的评论内容"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 评论ID |
| `commentContent` | `string` | 是 | 评论内容 |

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
- **URL**: `DELETE /api/document/note/comment/{ids}`
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
| `data` | `object/array/boolean` | 响应数据 |
