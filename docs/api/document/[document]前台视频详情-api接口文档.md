# [document] 前台视频详情 API 接口文档

> **版本**: 4.0  
> **更新日期**: 2026-04-29  
> **说明**: 前台视频详情聚合接口，采用四大区域化设计：视频元信息+详情、点赞收藏+作者关注信息、评论区域、视频合集。所有交互数据（浏览量、点赞数、收藏数）均通过 Redis 缓存服务获取。评论功能已整合到本模块，不再依赖独立评论控制器。

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docHomeVideo/detail/{id}` | GET | `DocHomeVideoController.java` | 获取视频元信息+详情 |
| `/api/document/docHomeVideo/interaction/{id}` | GET | `DocHomeVideoController.java` | 获取视频交互信息（点赞收藏+作者关注） |
| `/api/document/docHomeVideo/like/{id}` | POST | `DocHomeVideoController.java` | 切换视频点赞状态 |
| `/api/document/docHomeVideo/favorite/{id}` | POST | `DocHomeVideoController.java` | 切换视频收藏状态 |
| `/api/document/docHomeVideo/follow/{authorId}` | POST | `DocHomeVideoController.java` | 切换关注作者状态 |
| `/api/document/docHomeVideo/comments/{videoId}` | GET | `DocHomeVideoController.java` | 获取视频评论列表（分页，二级结构） |
| `/api/document/docHomeVideo/comment` | POST | `DocHomeVideoController.java` | 发表/回复视频评论 |
| `/api/document/docHomeVideo/comment/like/{commentId}` | POST | `DocHomeVideoController.java` | 切换评论点赞状态 |
| `/api/document/docHomeVideo/collections/{videoId}` | GET | `DocHomeVideoController.java` | 获取视频所属公开合集列表 |

---

## 1. 获取视频元信息+详情

### 接口信息
- **URL**: `GET /api/document/docHomeVideo/detail/{id}`
- **功能**: 获取视频基本信息和内容详情，仅返回前台展示所需字段。获取元信息时优先增加浏览量（Redis）。不包含审核状态、版本号等后台管理字段。

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
    "id": "1",
    "title": "视频标题",
    "description": "视频描述",
    "videoUrl": "https://example.com/video.mp4",
    "coverUrl": "https://example.com/cover.jpg",
    "category": "tech",
    "tags": ["tag1", "tag2"]
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 视频ID |
| `title` | `string` | 视频标题 |
| `description` | `string` | 视频描述 |
| `videoUrl` | `string` | 视频播放地址 |
| `coverUrl` | `string` | 封面图URL |
| `category` | `string` | 大类分类编码 |
| `tags` | `array` | 标签列表 |

---

## 2. 获取视频交互信息（点赞收藏+作者关注）

### 接口信息
- **URL**: `GET /api/document/docHomeVideo/interaction/{id}`
- **功能**: 独立查询视频的交互统计数据和作者关注信息。浏览量、点赞数、收藏数从Redis缓存获取。当前用户的点赞、收藏、关注状态从Redis Bitmap获取。作者粉丝数从Redis缓存获取。

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
    "viewCount": 100,
    "likeCount": 10,
    "favoriteCount": 5,
    "isLiked": false,
    "isFavorited": false,
    "author": {
      "id": "1",
      "name": "作者昵称",
      "avatar": "https://example.com/avatar.jpg",
      "fans": 128,
      "isFollowing": false
    }
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `viewCount` | `number` | 浏览量（从Redis缓存获取） |
| `likeCount` | `number` | 点赞数（从Redis缓存获取） |
| `favoriteCount` | `number` | 收藏数（从Redis缓存获取） |
| `isLiked` | `boolean` | 当前用户是否已点赞 |
| `isFavorited` | `boolean` | 当前用户是否已收藏 |
| `author` | `object` | 作者信息 |
| `author.id` | `string` | 作者用户ID |
| `author.name` | `string` | 作者昵称 |
| `author.avatar` | `string` | 作者头像URL |
| `author.fans` | `number` | 作者粉丝数（从Redis缓存获取） |
| `author.isFollowing` | `boolean` | 当前用户是否已关注该作者 |

---

## 3. 切换视频点赞状态

### 接口信息
- **URL**: `POST /api/document/docHomeVideo/like/{id}`
- **功能**: 切换视频点赞状态（已登录用户可用），通过Redis缓存服务操作

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
| `status` | `boolean` | 操作后的状态（true为已点赞，false为已取消） |
| `count` | `number` | 最新点赞数 |

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
- **URL**: `POST /api/document/docHomeVideo/favorite/{id}`
- **功能**: 切换视频收藏状态（已登录用户可用），通过Redis缓存服务操作

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

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `success` | `boolean` | 操作是否成功 |
| `status` | `boolean` | 操作后的状态（true为已收藏，false为已取消） |
| `count` | `number` | 最新收藏数 |

---

## 5. 切换关注作者状态

### 接口信息
- **URL**: `POST /api/document/docHomeVideo/follow/{authorId}`
- **功能**: 切换关注视频作者状态（已登录用户可用），通过Redis缓存服务操作

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

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `success` | `boolean` | 操作是否成功 |
| `status` | `boolean` | 操作后的状态（true为已关注，false为已取消） |
| `count` | `number` | 最新粉丝数 |

### 失败响应（关注自己）

```json
{
  "code": 500,
  "msg": "不能关注自己",
  "data": null
}
```

---

## 6. 获取视频评论列表（分页，二级结构）

### 接口信息
- **URL**: `GET /api/document/docHomeVideo/comments/{videoId}`
- **功能**: 查询视频的根评论列表，每个根评论包含其下的所有回复。采用B站式二级评论结构：所有回复统一挂在根评论下，不存在层级嵌套。支持热门/最新排序。评论点赞数从Redis缓存获取。

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `pageNum` | `number` | 否 | 当前页码，默认1 |
| `pageSize` | `number` | 否 | 每页大小，默认10，最大500 |
| `sort` | `string` | 否 | 排序方式（hot-按点赞数降序，new-按创建时间降序） |

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
        "author": {
          "id": "1",
          "name": "评论者昵称",
          "avatar": "https://example.com/avatar.jpg",
          "fans": 10,
          "isFollowing": false
        },
        "createdAt": "2026-04-29 10:00:00",
        "likes": 5,
        "isLiked": false,
        "replies": [
          {
            "id": "2",
            "content": "回复内容",
            "author": {
              "id": "2",
              "name": "回复者昵称",
              "avatar": "https://example.com/avatar2.jpg",
              "fans": 5,
              "isFollowing": true
            },
            "createdAt": "2026-04-29 10:05:00",
            "likes": 1,
            "isLiked": false,
            "replies": [],
            "replyTo": {
              "id": "1",
              "name": "评论者昵称",
              "avatar": "https://example.com/avatar.jpg",
              "fans": 10,
              "isFollowing": false
            }
          }
        ],
        "replyTo": null
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `list` | `array` | 评论列表 |
| `list[].id` | `string` | 评论ID |
| `list[].content` | `string` | 评论内容 |
| `list[].author` | `object` | 评论作者信息 |
| `list[].author.id` | `string` | 作者用户ID |
| `list[].author.name` | `string` | 作者昵称 |
| `list[].author.avatar` | `string` | 作者头像URL |
| `list[].author.fans` | `number` | 作者粉丝数 |
| `list[].author.isFollowing` | `boolean` | 当前用户是否已关注该作者 |
| `list[].createdAt` | `string` | 评论创建时间 |
| `list[].likes` | `number` | 评论点赞数（从Redis缓存获取） |
| `list[].isLiked` | `boolean` | 当前用户是否已点赞该评论 |
| `list[].replies` | `array` | 回复列表（二级结构，结构与根评论相同） |
| `list[].replyTo` | `object` | 回复目标用户信息（根评论为null） |
| `list[].replyTo.id` | `string` | 被回复用户ID |
| `list[].replyTo.name` | `string` | 被回复用户昵称 |
| `list[].replyTo.avatar` | `string` | 被回复用户头像URL |
| `list[].replyTo.fans` | `number` | 被回复用户粉丝数 |
| `list[].replyTo.isFollowing` | `boolean` | 当前用户是否已关注被回复用户 |
| `total` | `number` | 总记录数 |
| `pageNum` | `number` | 当前页码 |
| `pageSize` | `number` | 每页大小 |

---

## 7. 发表/回复视频评论

### 接口信息
- **URL**: `POST /api/document/docHomeVideo/comment`
- **功能**: 发表根评论和回复评论使用同一个接口。采用B站式评论结构：parentId记录根评论ID，replyToId记录被回复用户ID。

### 请求体

```json
{
  "videoId": "1",
  "content": "评论内容",
  "parentId": null,
  "replyToId": null
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |
| `content` | `string` | 是 | 评论内容 |
| `parentId` | `string` | 否 | 父评论ID（根评论时为空，回复时传根评论ID） |
| `replyToId` | `string` | 否 | 回复目标用户ID（直接回复根评论时为空，回复某条评论时传该评论的用户ID） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "100",
    "content": "评论内容",
    "author": {
      "id": "1",
      "name": "当前用户昵称",
      "avatar": "https://example.com/avatar.jpg",
      "fans": 10,
      "isFollowing": false
    },
    "createdAt": "2026-04-29 10:30:00",
    "likes": 0,
    "isLiked": false,
    "replies": [],
    "replyTo": null
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 评论ID |
| `content` | `string` | 评论内容 |
| `author` | `object` | 评论作者信息（同评论列表中的author结构） |
| `createdAt` | `string` | 评论创建时间 |
| `likes` | `number` | 评论点赞数 |
| `isLiked` | `boolean` | 当前用户是否已点赞该评论 |
| `replies` | `array` | 回复列表（新发表的评论为空数组） |
| `replyTo` | `object` | 回复目标用户信息（根评论为null） |

### 失败响应（未登录）

```json
{
  "code": 500,
  "msg": "请先登录",
  "data": null
}
```

---

## 8. 切换评论点赞状态

### 接口信息
- **URL**: `POST /api/document/docHomeVideo/comment/like/{commentId}`
- **功能**: 切换视频评论点赞状态（已登录用户可用），通过Redis缓存服务操作

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

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `success` | `boolean` | 操作是否成功 |
| `status` | `boolean` | 操作后的状态（true为已点赞，false为已取消） |
| `count` | `number` | 最新点赞数 |

---

## 9. 获取视频所属公开合集列表

### 接口信息
- **URL**: `GET /api/document/docHomeVideo/collections/{videoId}`
- **功能**: 查询包含该视频的所有公开合集，每个合集包含其视频列表。仅返回公开状态（status=1）的合集，私密合集不对外展示。

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "collectionName": "合集名称",
      "description": "合集描述",
      "coverUrl": "https://example.com/collection-cover.jpg",
      "videoCount": 5,
      "videos": [
        {
          "id": "1",
          "title": "视频标题",
          "videoUrl": "https://example.com/video.mp4",
          "coverUrl": "https://example.com/cover.jpg",
          "description": "视频描述",
          "viewCount": 100
        }
      ]
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `data` | `array` | 合集列表 |
| `data[].id` | `string` | 合集ID |
| `data[].collectionName` | `string` | 合集名称 |
| `data[].description` | `string` | 合集描述 |
| `data[].coverUrl` | `string` | 合集封面图URL |
| `data[].videoCount` | `number` | 视频数量 |
| `data[].videos` | `array` | 合集中的视频列表 |
| `data[].videos[].id` | `string` | 视频ID |
| `data[].videos[].title` | `string` | 视频标题 |
| `data[].videos[].videoUrl` | `string` | 视频播放地址 |
| `data[].videos[].coverUrl` | `string` | 封面图URL |
| `data[].videos[].description` | `string` | 视频描述 |
| `data[].videos[].viewCount` | `number` | 浏览量（从Redis缓存获取） |

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
