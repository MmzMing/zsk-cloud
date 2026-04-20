# [document] 前台文档详情 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/content/doc/detail/{id}` | GET | `DocContentController.java` | 获取文档详情 |
| `/api/document/content/doc/like/{id}` | POST | `DocContentController.java` | 切换文档点赞状态 |
| `/api/document/content/doc/favorite/{id}` | POST | `DocContentController.java` | 切换文档收藏状态 |
| `/api/document/content/doc/comments/{id}` | GET | `DocContentController.java` | 获取文档评论列表 |
| `/api/document/content/doc/comment` | POST | `DocContentController.java` | 发表文档评论 |
| `/api/document/content/doc/comment/like/{commentId}` | POST | `DocContentController.java` | 切换评论点赞状态 |
| `/api/document/content/doc/user/stats` | GET | `DocContentController.java` | 获取用户统计信息 |

---

## 1. 获取文档详情

### 接口信息
- **URL**: `GET /api/document/content/doc/detail/{id}`
- **功能**: 获取文档详情，包含作者信息、统计数据和推荐文档

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文档ID（后端使用Jackson转为string类型） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "title": "文档标题",
    "content": "文档内容",
    "category": "tech",
    "date": "2026-02-15 10:30:00",
    "coverUrl": "https://example.com/cover.jpg",
    "author": {
      "id": "1",
      "name": "作者1",
      "avatar": "",
      "fans": "0",
      "isFollowing": false
    },
    "stats": {
      "views": "100",
      "likes": 10,
      "favorites": 5,
      "isLiked": false,
      "isFavorited": false
    },
    "recommendations": [
      {
        "id": "2",
        "title": "推荐文档标题",
        "views": "50"
      }
    ]
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 文档ID |
| `title` | `string` | 文档标题 |
| `content` | `string` | 文档内容 |
| `category` | `string` | 分类编码 |
| `date` | `string` | 创建时间 |
| `coverUrl` | `string` | 封面图片URL |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称 |
| `author.avatar` | `string` | 作者头像 |
| `author.fans` | `string` | 粉丝数 |
| `author.isFollowing` | `boolean` | 是否已关注 |
| `stats.views` | `string` | 浏览量 |
| `stats.likes` | `number` | 点赞数 |
| `stats.favorites` | `number` | 收藏数 |
| `stats.isLiked` | `boolean` | 当前用户是否点赞 |
| `stats.isFavorited` | `boolean` | 当前用户是否收藏 |
| `recommendations` | `array` | 推荐文档列表 |

---

## 2. 切换文档点赞状态

### 接口信息
- **URL**: `POST /api/document/content/doc/like/{id}`
- **功能**: 切换文档点赞状态（已登录用户可用）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文档ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "status": true,
    "count": 11
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `success` | `boolean` | 操作是否成功 |
| `status` | `boolean` | 当前点赞状态（true为已点赞） |
| `count` | `number` | 点赞总数 |

### 失败响应（未登录）

```json
{
  "code": 500,
  "msg": "请先登录",
  "data": null
}
```

---

## 3. 切换文档收藏状态

### 接口信息
- **URL**: `POST /api/document/content/doc/favorite/{id}`
- **功能**: 切换文档收藏状态（已登录用户可用）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文档ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "status": true,
    "count": 6
  }
}
```

---

## 4. 获取文档评论列表

### 接口信息
- **URL**: `GET /api/document/content/doc/comments/{id}`
- **功能**: 获取文档的评论列表，支持分页和排序

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文档ID |

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |
| `sort` | `string` | 否 | new | 排序方式：hot（热门）/ new（最新） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "content": "评论内容",
        "createdAt": "2026-02-15 10:30:00",
        "likes": 5,
        "isLiked": false,
        "author": {
          "id": "1",
          "name": "用户1",
          "avatar": ""
        },
        "replies": [
          {
            "id": "2",
            "content": "回复内容",
            "createdAt": "2026-02-15 10:35:00",
            "likes": 2,
            "isLiked": false,
            "author": {
              "id": "2",
              "name": "用户2",
              "avatar": ""
            }
          }
        ]
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 5. 发表文档评论

### 接口信息
- **URL**: `POST /api/document/content/doc/comment`
- **功能**: 发表评论或回复评论（已登录用户可用）

### 请求体

```json
{
  "docId": "1",
  "content": "评论内容",
  "parentId": "1",
  "replyToId": "2"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docId` | `string` | 是 | 文档ID |
| `content` | `string` | 是 | 评论内容 |
| `parentId` | `string` | 否 | 父评论ID（回复时必填） |
| `replyToId` | `string` | 否 | 被回复用户ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "3",
    "content": "评论内容",
    "createdAt": "2026-02-15 10:40:00",
    "likes": 0,
    "isLiked": false,
    "author": {
      "id": "1",
      "name": "用户1",
      "avatar": ""
    }
  }
}
```

---

## 6. 切换评论点赞状态

### 接口信息
- **URL**: `POST /api/document/content/doc/comment/like/{commentId}`
- **功能**: 切换评论点赞状态（已登录用户可用）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `commentId` | `string` | 是 | 评论ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "status": true,
    "count": 6
  }
}
```

---

## 7. 获取用户统计信息

### 接口信息
- **URL**: `GET /api/document/content/doc/user/stats`
- **功能**: 获取当前登录用户的统计信息（点赞、关注、收藏总数）

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "userId": "1",
    "likeCount": 10,
    "fanCount": 5,
    "collectCount": 8
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `userId` | `string` | 用户ID |
| `likeCount` | `number` | 点赞总数（笔记+视频） |
| `fanCount` | `number` | 关注总数（用户+作者） |
| `collectCount` | `number` | 收藏总数（笔记+视频） |

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
