# [document] 用户统计 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docAllContent/user/stats` | GET | `DocAllContentController.java` | 获取用户统计信息 |
| `/api/document/docAllContent/content/stats` | GET | `DocAllContentController.java` | 获取内容统计信息 |

---

## 1. 获取用户统计信息

### 接口信息
- **URL**: `GET /api/document/docAllContent/user/stats`
- **功能**: 获取当前登录用户的统计信息（点赞、关注、收藏、评论总数）

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "userId": "1",
    "likeCount": 10,
    "fanCount": 5,
    "collectCount": 8,
    "commentCount": 3
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `userId` | `string` | 用户ID（后端使用Jackson转为string类型） |
| `likeCount` | `number` | 点赞总数（笔记+视频） |
| `fanCount` | `number` | 关注总数（用户+笔记作者+视频作者） |
| `collectCount` | `number` | 收藏总数（笔记+视频） |
| `commentCount` | `number` | 评论总数 |

### 失败响应（未登录）

```json
{
  "code": 500,
  "msg": "请先登录",
  "data": null
}
```

---

## 2. 获取内容统计信息

### 接口信息
- **URL**: `GET /api/document/docAllContent/content/stats`
- **功能**: 获取内容统计信息（文章总数、视频总数、评论总数、上周新增数据）

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "docCount": 100,
    "videoCount": 50,
    "commentCount": 30,
    "lastWeekDocCount": 10,
    "lastWeekVideoCount": 5,
    "lastWeekCommentCount": 8
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `docCount` | `number` | 文章总数 |
| `videoCount` | `number` | 视频总数 |
| `commentCount` | `number` | 评论总数 |
| `lastWeekDocCount` | `number` | 上周新增文章数 |
| `lastWeekVideoCount` | `number` | 上周新增视频数 |
| `lastWeekCommentCount` | `number` | 上周新增评论数 |

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
| `data` | `object/array` | 响应数据 |
