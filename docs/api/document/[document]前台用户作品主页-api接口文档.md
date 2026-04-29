# [document] 前台用户作品主页 API 接口文档

> **版本**: 1.0  
> **更新日期**: 2026-04-30  
> **说明**: 前台用户作品主页聚合接口，提供用户已发布作品（笔记+视频）的列表查询和交互统计汇总。所有交互数据（点赞数、浏览数、收藏数）均通过 Redis 缓存服务获取，缓存未命中时自动回源数据库。仅展示已审核通过且正常状态的作品。

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docHomeUser/{userId}/works` | GET | `DocHomeUserController.java` | 获取用户作品列表（分页，支持按类型筛选） |
| `/api/document/docHomeUser/{userId}/stats` | GET | `DocHomeUserController.java` | 获取用户作品统计（总点赞数、总浏览数、总收藏数） |

---

## 1. 获取用户作品列表

### 接口信息
- **URL**: `GET /api/document/docHomeUser/{userId}/works`
- **功能**: 根据用户ID分页查询其已发布的笔记和视频作品，支持按类型筛选。每条作品包含基础信息（标题、描述、封面等）和交互数据（点赞数、浏览数、收藏数）。交互数据通过 Cache 服务的批量接口从 Redis 获取，避免 N+1 查询问题。

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `number` | 是 | 目标用户ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `type` | `string` | 否 | 作品类型筛选（`note`-仅笔记 `video`-仅视频 不传-全部），默认查询全部 |
| `pageNum` | `number` | 否 | 当前页码，默认1 |
| `pageSize` | `number` | 否 | 每页大小，默认10，最大500 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "title": "Spring Boot 3.0 实战指南",
        "type": "note",
        "description": "一篇关于Spring Boot 3.0的深度实践笔记",
        "coverUrl": "https://example.com/cover1.jpg",
        "category": "tech",
        "tags": "Java,Spring",
        "viewCount": 1024,
        "likeCount": 56,
        "favoriteCount": 23,
        "createTime": "2026-04-28 14:30:00"
      },
      {
        "id": "2",
        "title": "Redis 高可用架构详解",
        "type": "video",
        "description": "深入讲解Redis哨兵模式和集群模式",
        "coverUrl": "https://example.com/cover2.jpg",
        "category": "tech",
        "tags": "Redis,架构",
        "viewCount": 2048,
        "likeCount": 128,
        "favoriteCount": 67,
        "createTime": "2026-04-27 10:00:00"
      }
    ],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 2,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `list` | `array` | 作品列表 |
| `list[].id` | `string` | 作品ID |
| `list[].title` | `string` | 作品标题 |
| `list[].type` | `string` | 作品类型（`note`-笔记 `video`-视频） |
| `list[].description` | `string` | 作品描述/简介 |
| `list[].coverUrl` | `string` | 封面图访问地址（从 document_files 表解析） |
| `list[].category` | `string` | 大类分类编码 |
| `list[].tags` | `string` | 标签（多个用英文逗号分隔） |
| `list[].viewCount` | `number` | 浏览量（从Redis缓存获取） |
| `list[].likeCount` | `number` | 点赞数（从Redis缓存获取） |
| `list[].favoriteCount` | `number` | 收藏数（从Redis缓存获取） |
| `list[].createTime` | `string` | 创建时间（格式：yyyy-MM-dd HH:mm:ss） |
| `total` | `number` | 总记录数 |
| `pageNum` | `number` | 当前页码 |
| `pageSize` | `number` | 每页大小 |
| `totalPages` | `number` | 总页数 |
| `hasNext` | `boolean` | 是否有下一页 |
| `hasPrevious` | `boolean` | 是否有上一页 |

### 查询策略说明

| type 参数 | 查询策略 | 分页方式 | 性能 |
| :--- | :--- | :--- | :--- |
| `note` | 仅查询笔记表 | 数据库分页 | ⭐⭐⭐ 最优 |
| `video` | 仅查询视频表 | 数据库分页 | ⭐⭐⭐ 最优 |
| 不传/其他 | 查询笔记+视频，合并排序 | 内存分页 | ⭐⭐ 适用于作品量不大的场景 |

---

## 2. 获取用户作品统计

### 接口信息
- **URL**: `GET /api/document/docHomeUser/{userId}/stats`
- **功能**: 汇总用户所有已发布作品的交互数据，包括总获赞数、总浏览数、总收藏数，以及笔记和视频的数量。交互数据均通过 Redis 缓存服务获取。

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `number` | 是 | 目标用户ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalLikeCount": 184,
    "totalViewCount": 3072,
    "totalFavoriteCount": 90,
    "noteCount": 8,
    "videoCount": 7
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `totalLikeCount` | `number` | 总获赞数（所有笔记点赞数 + 所有视频点赞数，从Redis缓存获取） |
| `totalViewCount` | `number` | 总浏览数（所有笔记浏览数 + 所有视频浏览数，从Redis缓存获取） |
| `totalFavoriteCount` | `number` | 总收藏数（所有笔记收藏数 + 所有视频收藏数，从Redis缓存获取） |
| `noteCount` | `number` | 已发布且审核通过的笔记数量 |
| `videoCount` | `number` | 已发布且审核通过的视频数量 |

### 性能说明

> 统计接口需要遍历用户所有作品逐个查询交互数据。如果用户作品量非常大，此接口可能较慢。后续可考虑增加汇总缓存优化。

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

---

## 数据筛选规则

两个接口均遵循以下筛选规则，仅展示公开展示的作品：

| 筛选条件 | 值 | 说明 |
| :--- | :--- | :--- |
| `status` | `1` | 正常状态（排除下架=2、草稿=3） |
| `auditStatus` | `1` | 审核通过（排除待审核=0、驳回=2） |
| `deleted` | `0` | 未删除 |

---

## 相关文件

| 文件 | 说明 |
| :--- | :--- |
| `DocHomeUserController.java` | 控制器 — 接收HTTP请求，返回 `R<T>` |
| `IDocHomeUserService.java` | 服务接口 — 定义作品列表查询和统计方法 |
| `DocHomeUserServiceImpl.java` | 服务实现 — 聚合 Mapper + Cache 服务，组装数据 |
| `DocHomeUserWorksVo.java` | 作品项 VO — 单条作品的基础信息+交互数据 |
| `DocHomeUserStatsVo.java` | 作品统计 VO — 用户维度的交互数据汇总 |
