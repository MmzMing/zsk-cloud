# [document] 文档审核 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/note/review/queue` | GET | `DocNoteReviewController.java` | 获取审核队列 |
| `/api/document/note/review/submit` | POST | `DocNoteReviewController.java` | 提交审核结果 |

---

## 1. 获取审核队列

### 接口信息
- **URL**: `GET /api/document/note/review/queue`
- **功能**: 获取待审核的笔记列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
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
        "title": "待审核笔记标题",
        "category": "tech",
        "status": "pending",
        "createdAt": "2026-02-15 10:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 笔记ID（后端使用Jackson转为string类型） |
| `title` | `string` | 笔记标题 |
| `category` | `string` | 分类编码 |
| `status` | `string` | 审核状态（pending待审核，approved已通过） |
| `createdAt` | `string` | 创建时间 |

---

## 2. 提交审核结果

### 接口信息
- **URL**: `POST /api/document/note/review/submit`
- **功能**: 提交笔记审核结果

### 请求体

```json
{
  "id": "1",
  "result": "approved",
  "reason": "内容合规，审核通过"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |
| `result` | `string` | 是 | 审核结果：approved（通过），rejected（拒绝） |
| `reason` | `string` | 否 | 审核备注/拒绝原因 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 失败响应

```json
{
  "code": 500,
  "msg": "审核失败",
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
| `data` | `object/array/null` | 响应数据 |
