# [document] 全局搜索 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/search/all` | GET | `SearchController.java` | 全站搜索 |

---

## 1. 全站搜索

### 接口信息
- **URL**: `GET /api/document/search/all`
- **功能**: 全站搜索，支持搜索视频和笔记。搜索结果中的统计数据（浏览量、点赞数）均通过 Redis 缓存服务获取，不再依赖主表统计字段。仅返回审核通过且状态正常的公开内容。

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `keyword` | `string` | 否 | - | 搜索关键字（支持标题、内容模糊匹配） |
| `type` | `string` | 否 | `all` | 搜索类型：`all`（全部）、`video`（视频）、`document`（笔记） |
| `sort` | `string` | 否 | - | 排序方式：`hot`（热门）、`latest`（最新） |
| `category` | `string` | 否 | - | 分类编码筛选 |
| `pageNum` | `number` | 否 | `1` | 页码 |
| `pageSize` | `number` | 否 | `10` | 每页数量 |

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
        "duration": "10:30",
        "playCount": 100,
        "readCount": null,
        "likeCount": 25,
        "authorId": "1",
        "author": "作者1",
        "tags": ["tag1", "tag2"]
      },
      {
        "id": "2",
        "type": "document",
        "title": "笔记标题",
        "description": "笔记内容摘要",
        "category": "life",
        "thumbnail": "https://example.com/doc-cover.jpg",
        "duration": null,
        "playCount": null,
        "readCount": 50,
        "likeCount": 10,
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
| `id` | `string` | 资源ID（雪花算法生成，后端转为 string 类型） |
| `type` | `string` | 资源类型：`video`（视频）、`document`（笔记） |
| `title` | `string` | 标题 |
| `description` | `string` | 描述/内容摘要 |
| `category` | `string` | 分类编码 |
| `thumbnail` | `string` | 缩略图/封面 URL |
| `duration` | `string` | 视频时长（仅视频类型有值，如 `10:30`） |
| `playCount` | `number` | 播放量（仅视频类型有值） |
| `readCount` | `number` | 阅读量（仅笔记类型有值） |
| `likeCount` | `number` | 点赞数（通过 Redis 缓存获取） |
| `authorId` | `string` | 作者ID |
| `author` | `string` | 作者名称（优先显示昵称，无昵称则显示用户名） |
| `tags` | `array<string>` | 标签列表（逗号分隔解析为数组） |

### 搜索类型说明

| 类型编码 | 说明 |
| :--- | :--- |
| `all` | 搜索视频和笔记 |
| `video` | 只搜索视频 |
| `document` | 只搜索笔记 |

### 排序方式说明

| 排序方式 | 说明 |
| :--- | :--- |
| `hot` | 按浏览量降序排序（视频按 `playCount`，笔记按 `readCount`） |
| `latest` | 按创建时间降序排序（按雪花ID趋势排序） |
| 默认/其他 | 不排序，保持原始顺序 |

### 业务约束

1. **内容过滤**：仅返回 `deleted = 0`（未删除）、`status = 1`（状态正常）、`audit_status = 1`（审核通过）的内容。
2. **统计数据来源**：浏览量、点赞数均通过 Redis 缓存服务实时获取，不依赖数据库主表字段。
3. **作者信息**：通过远程用户服务批量查询，优先显示昵称，无昵称则显示用户名。
4. **封面图片**：通过文件服务批量查询封面文件 URL。
5. **内存分页**：搜索完成后在内存中进行分页，适用于结果集可控的场景。

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
| `code` | `number` | 状态码（200 成功，其他为失败） |
| `msg` | `string` | 响应消息 |
| `data` | `object/array` | 响应数据 |
