# [document] 前台视频详情 API 接口文档

> **版本**: 3.0  
> **更新日期**: 2026-04-27  
> **说明**: 评论相关接口已解耦到视频详情评论管理模块，本模块不再包含评论相关接口。所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取。

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docVideoHomeDetail/detail/{id}` | GET | `DocVideoHomeDetailController.java` | 获取视频详情 |
| `/api/document/docVideoHomeDetail/interaction/{id}` | GET | `DocVideoHomeDetailController.java` | 获取视频交互详情 |
| `/api/document/docVideoHomeDetail/like/{id}` | POST | `DocVideoHomeDetailController.java` | 切换视频点赞状态 |
| `/api/document/docVideoHomeDetail/favorite/{id}` | POST | `DocVideoHomeDetailController.java` | 切换视频收藏状态 |
| `/api/document/docVideoHomeDetail/follow/{authorId}` | POST | `DocVideoHomeDetailController.java` | 切换关注作者状态 |

> **注意**: 评论相关接口（获取评论列表、发表评论、评论点赞）已迁移至 `DocVideoCommentController.java`，详见 [视频详情评论管理 API 接口文档]([document]视频详情评论管理-api接口文档.md)。

---

## 1. 获取视频详情

### 接口信息
- **URL**: `GET /api/document/docVideoHomeDetail/detail/{id}`
- **功能**: 获取视频详情，包含作者信息、统计数据。所有统计数据（浏览量、点赞数、收藏数）均从 Redis 缓存获取，不再依赖主表字段。

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
      "fans": 0,
      "isFollowing": false
    },
    "stats": {
      "views": 100,
      "likes": 10,
      "favorites": 5,
      "isLiked": false,
      "isFavorited": false
    }
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 视频ID（后端使用Jackson转为string类型） |
| `title` | `string` | 视频标题 |
| `description` | `string` | 视频描述 |
| `videoUrl` | `string` | 视频播放URL |
| `coverUrl` | `string` | 封面图片URL |
| `tags` | `array` | 标签列表 |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称 |
| `author.avatar` | `string` | 作者头像 |
| `author.fans` | `number` | 粉丝数 |
| `author.isFollowing` | `boolean` | 是否已关注 |
| `stats.views` | `number` | 浏览量 |
| `stats.likes` | `number` | 点赞数 |
| `stats.favorites` | `number` | 收藏数 |
| `stats.isLiked` | `boolean` | 当前用户是否点赞 |
| `stats.isFavorited` | `boolean` | 当前用户是否收藏 |

---

## 2. 获取视频交互详情

### 接口信息
- **URL**: `GET /api/document/docVideoHomeDetail/interaction/{id}`
- **功能**: 独立查询视频的交互统计数据，包括浏览量、点赞数、收藏数以及当前用户的交互状态。

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
    "views": 100,
    "likes": 10,
    "favorites": 5,
    "isLiked": false,
    "isFavorited": false
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `views` | `number` | 浏览量 |
| `likes` | `number` | 点赞数 |
| `favorites` | `number` | 收藏数 |
| `isLiked` | `boolean` | 当前用户是否点赞 |
| `isFavorited` | `boolean` | 当前用户是否收藏 |

---

## 3. 切换视频点赞状态

### 接口信息
- **URL**: `POST /api/document/docVideoHomeDetail/like/{id}`
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

## 4. 切换视频收藏状态

### 接口信息
- **URL**: `POST /api/document/docVideoHomeDetail/favorite/{id}`
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
    "success": true,
    "status": true,
    "count": 6
  }
}
```

---

## 5. 切换关注作者状态

### 接口信息
- **URL**: `POST /api/document/docVideoHomeDetail/follow/{authorId}`
- **功能**: 切换关注视频作者状态（已登录用户可用）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `authorId` | `string` | 是 | 作者ID |

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

### 失败响应（关注自己）

```json
{
  "code": 500,
  "msg": "不能关注自己",
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
| `data` | `object/array` | 响应数据 |
