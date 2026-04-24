# [document] 全局搜索 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/search/all` | GET | `SearchController.java` | 全站搜索 |

---

## 1. 全站搜索

### 接口信息
- **URL**: `GET /api/document/search/all`
- **功能**: 全站搜索，支持搜索视频和笔记。搜索结果中的统计数据（浏览量、点赞数、收藏数、评论数）均通过 Redis 缓存服务获取。

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `keyword` | `string` | 否 | - | 搜索关键字 |
| `type` | `string` | 否 | all | 搜索类型：all（全部）、video（视频）、document（文档） |
| `sort` | `string` | 否 | - | 排序方式：hot（热门）、like（收藏） |
| `category` | `string` | 否 | - | 分类筛选 |
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
        "type": "video",
        "title": "视频标题",
        "description": "视频描述内容",
        "category": "tech",
        "thumbnail": "https://example.com/cover.jpg",
        "playCount": 100,
        "readCount": null,
        "commentCount": 5,
        "favoriteCount": 8,
        "authorId": "1",
        "author": "作者1",
        "tags": ["tag1", "tag2"]
      },
      {
        "id": "2",
        "type": "document",
        "title": "文档标题",
        "description": "文档内容摘要",
        "category": "life",
        "thumbnail": "https://example.com/doc-cover.jpg",
        "playCount": null,
        "readCount": 50,
        "commentCount": 3,
        "favoriteCount": 0,
        "authorId": "2",
        "author": "作者2",
        "tags": []
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 内容ID（后端使用Jackson转为string类型） |
| `type` | `string` | 内容类型：video（视频）、document（文档） |
| `title` | `string` | 标题 |
| `description` | `string` | 描述/内容摘要 |
| `category` | `string` | 分类编码 |
| `thumbnail` | `string` | 缩略图URL |
| `playCount` | `number` | 播放量（视频） |
| `readCount` | `number` | 阅读量（文档） |
| `commentCount` | `number` | 评论数 |
| `favoriteCount` | `number` | 收藏数 |
| `authorId` | `string` | 作者ID |
| `author` | `string` | 作者名称 |
| `tags` | `array` | 标签列表 |

### 搜索类型说明

| 类型 | 说明 |
| :--- | :--- |
| `all` | 搜索视频和文档 |
| `video` | 只搜索视频 |
| `document` | 只搜索文档 |

### 排序方式说明

| 排序方式 | 说明 |
| :--- | :--- |
| `hot` | 按热度排序（播放量+阅读量） |
| `like` | 按收藏数排序 |
| 默认 | 不排序，按原始顺序返回 |

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
