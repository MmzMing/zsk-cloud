# [document] 前台视频详情 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/content/video/detail/{id}` | GET | `DocVideoContentController.java` | 获取视频详情 |
| `/api/document/content/video/like/{id}` | POST | `DocVideoContentController.java` | 切换视频点赞状态 |
| `/api/document/content/video/favorite/{id}` | POST | `DocVideoContentController.java` | 切换视频收藏状态 |
| `/api/document/content/video/comments/{id}` | GET | `DocVideoContentController.java` | 获取视频评论列表 |
| `/api/document/content/video/comment` | POST | `DocVideoContentController.java` | 发表视频评论 |
| `/api/document/content/video/comment/like/{commentId}` | POST | `DocVideoContentController.java` | 切换评论点赞状态 |

---

## 1. 获取视频详情

### 接口信息
- **URL**: `GET /api/document/content/video/detail/{id}`
- **功能**: 获取视频详情，包含作者信息、统计数据和推荐视频

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID（后端使用Jackson转为string类型） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "title": "视频标题",
    "description": "视频描述",
    "videoUrl": "https://example.com/video.mp4",
    "coverUrl": "https://example.com/cover.jpg",
    "tags": ["tag1", "tag2"],
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
      "date": "2026-02-15 10:30:00",
      "isLiked": false,
      "isFavorited": false
    },
    "recommendations": [
      {
        "id": "2",
        "title": "推荐视频标题",
        "coverUrl": "https://example.com/cover2.jpg",
        "views": "50",
        "description": "推荐视频描述",
        "date": "2026-02-14 10:00:00"
      }
    ],
    "episodes": []
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 视频ID |
| `title` | `string` | 视频标题 |
| `description` | `string` | 视频描述 |
| `videoUrl` | `string` | 视频播放URL |
| `coverUrl` | `string` | 封面图片URL |
| `tags` | `array` | 标签列表 |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称 |
| `author.avatar` | `string` | 作者头像 |
| `author.fans` | `string` | 粉丝数 |
| `author.isFollowing` | `boolean` | 是否已关注 |
| `stats.views` | `string` | 播放量 |
| `stats.likes` | `number` | 点赞数 |
| `stats.favorites` | `number` | 收藏数 |
| `stats.date` | `string` | 创建时间 |
| `stats.isLiked` | `boolean` | 当前用户是否点赞 |
| `stats.isFavorited` | `boolean` | 当前用户是否收藏 |
| `recommendations` | `array` | 推荐视频列表 |
| `episodes` | `array` | 剧集列表 |

---

## 2. 切换视频点赞状态

### 接口信息
- **URL**: `POST /api/document/content/video/like/{id}`
- **功能**: 切换视频点赞状态（已登录用户可用）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "isLiked": true,
    "count": 11
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `isLiked` | `boolean` | 当前点赞状态（true为已点赞） |
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

## 3. 切换视频收藏状态

### 接口信息
- **URL**: `POST /api/document/content/video/favorite/{id}`
- **功能**: 切换视频收藏状态（已登录用户可用）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "isFavorited": true,
    "count": 6
  }
}
```

---

## 4. 获取视频评论列表

### 接口信息
- **URL**: `GET /api/document/content/video/comments/{id}`
- **功能**: 获取视频的评论列表，支持分页和排序

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `page` | `number` | 否 | 1 | 页码 |
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
        "replies": []
      }
    ],
    "total": 1
  }
}
```

---

## 5. 发表视频评论

### 接口信息
- **URL**: `POST /api/document/content/video/comment`
- **功能**: 发表评论或回复评论（已登录用户可用）

### 请求体

```json
{
  "videoId": "1",
  "content": "评论内容",
  "parentId": "1"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |
| `content` | `string` | 是 | 评论内容 |
| `parentId` | `string` | 否 | 父评论ID（回复时必填） |

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
- **URL**: `POST /api/document/content/video/comment/like/{commentId}`
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
    "isLiked": true,
    "likes": 6
  }
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
| `data` | `object/array` | 响应数据 |
