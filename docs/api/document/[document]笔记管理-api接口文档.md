# [document] 笔记管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docNote/list` | GET | `DocNoteController.java` | 查询笔记列表 |
| `/api/document/docNote/page` | GET | `DocNoteController.java` | 分页查询笔记列表 |
| `/api/document/docNote/{id}` | GET | `DocNoteController.java` | 获取笔记详细信息 |
| `/api/document/docNote/{id}/interaction` | GET | `DocNoteController.java` | 获取笔记交互数据 |
| `/api/document/docNote` | POST | `DocNoteController.java` | 新增笔记 |
| `/api/document/docNote` | PUT | `DocNoteController.java` | 修改笔记 |
| `/api/document/docNote/{ids}` | DELETE | `DocNoteController.java` | 删除笔记 |
| `/api/document/docNote/draft/list` | GET | `DocNoteController.java` | 获取草稿列表 |
| `/api/document/docNote/status/batch` | PUT | `DocNoteController.java` | 批量更新状态 |
| `/api/document/docNote/category/batch` | PUT | `DocNoteController.java` | 批量迁移分类 |
| `/api/document/docNote/{id}/pinned` | PUT | `DocNoteController.java` | 切换置顶状态 |
| `/api/document/docNote/{id}/recommended` | PUT | `DocNoteController.java` | 切换推荐状态 |

---

## 1. 查询笔记列表

### 接口信息
- **URL**: `GET /api/document/docNote/list`
- **功能**: 查询笔记列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | 用户ID |
| `noteName` | `string` | 否 | 笔记名称（支持模糊查询） |
| `status` | `number` | 否 | 状态（1发布，2下架，3草稿） |
| `auditStatus` | `number` | 否 | 审核状态（0待审核，1已通过，2已拒绝） |
| `broadCode` | `string` | 否 | 分类编码 |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "userId": "1",
      "author": {
        "id": "1",
        "name": "作者昵称",
        "avatar": "https://example.com/avatar.jpg"
      },
      "noteName": "笔记标题",
      "noteTags": "tag1,tag2",
      "content": "笔记内容",
      "description": "笔记简介",
      "coverFile": {
        "fileId": "file-abc123",
        "fileUrl": "https://example.com/cover.jpg"
      },
      "broadCode": "tech",
      "narrowCode": "java",
      "noteGrade": 1,
      "noteMode": 1,
      "suitableUsers": "初学者",
      "auditStatus": 1,
      "status": 1,
      "stats": {
        "views": 1234,
        "likes": 56,
        "favorites": 32,
        "isLiked": false,
        "isFavorited": false
      },
      "publishTime": "2026-02-15 10:30:00",
      "isPinned": 0,
      "isRecommended": 1,
      "seoTitle": "SEO标题",
      "seoDescription": "SEO描述",
      "seoKeywords": "keyword1,keyword2",
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
| `id` | `string` | 笔记ID（Long类型自动转为string） |
| `userId` | `string` | 用户ID（Long类型自动转为string） |
| `author` | `object` | 作者信息 |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称（优先使用昵称，若无则使用用户名） |
| `author.avatar` | `string` | 作者头像URL |
| `noteName` | `string` | 笔记名称 |
| `noteTags` | `string` | 笔记标签（逗号分隔） |
| `content` | `string` | 文档内容 |
| `description` | `string` | 笔记简介/描述 |
| `coverFile` | `object` | 封面文件信息 |
| `coverFile.fileId` | `string` | 封面图片文件ID |
| `coverFile.fileUrl` | `string` | 封面图片文件URL |
| `broadCode` | `string` | 大类编码 |
| `narrowCode` | `string` | 小类编码 |
| `noteGrade` | `number` | 笔记等级 |
| `noteMode` | `number` | 笔记模式 |
| `suitableUsers` | `string` | 适合人群 |
| `auditStatus` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `status` | `number` | 笔记状态（1发布，2下架，3草稿） |
| `stats` | `object` | 统计信息（从Redis缓存获取） |
| `stats.views` | `number` | 浏览量 |
| `stats.likes` | `number` | 点赞数 |
| `stats.favorites` | `number` | 收藏数 |
| `stats.isLiked` | `boolean` | 是否已点赞 |
| `stats.isFavorited` | `boolean` | 是否已收藏 |
| `publishTime` | `string` | 笔记发布时间 |
| `isPinned` | `number` | 是否置顶（0否，1是） |
| `isRecommended` | `number` | 是否推荐（0否，1是） |
| `seoTitle` | `string` | SEO标题 |
| `seoDescription` | `string` | SEO描述 |
| `seoKeywords` | `string` | SEO关键词（逗号分隔） |
| `deleted` | `number` | 删除标记（0未删除，1已删除） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询笔记列表

### 接口信息
- **URL**: `GET /api/document/docNote/page`
- **功能**: 分页查询笔记列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | - | 用户ID |
| `noteName` | `string` | 否 | - | 笔记名称 |
| `status` | `number` | 否 | - | 状态 |
| `auditStatus` | `number` | 否 | - | 审核状态 |
| `broadCode` | `string` | 否 | - | 分类编码 |
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
        "userId": "1",
        "author": {
          "id": "1",
          "name": "作者昵称",
          "avatar": "https://example.com/avatar.jpg"
        },
        "noteName": "笔记标题",
        "noteTags": "tag1,tag2",
        "content": "笔记内容",
        "description": "笔记简介",
        "coverFile": {
          "fileId": "file-abc123",
          "fileUrl": "https://example.com/cover.jpg"
        },
        "broadCode": "tech",
        "narrowCode": "java",
        "noteGrade": 1,
        "noteMode": 1,
        "suitableUsers": "初学者",
        "auditStatus": 1,
        "status": 1,
        "stats": {
          "views": 1234,
          "likes": 56,
          "favorites": 32,
          "isLiked": false,
          "isFavorited": false
        },
        "publishTime": "2026-02-15 10:30:00",
        "isPinned": 0,
        "isRecommended": 1,
        "seoTitle": "SEO标题",
        "seoDescription": "SEO描述",
        "seoKeywords": "keyword1,keyword2",
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

## 3. 获取笔记详细信息

### 接口信息
- **URL**: `GET /api/document/docNote/{id}`
- **功能**: 获取笔记详细信息（包含封面文件信息、作者信息和统计信息）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "userId": "1",
    "author": {
      "id": "1",
      "name": "作者昵称",
      "avatar": "https://example.com/avatar.jpg"
    },
    "noteName": "笔记标题",
    "noteTags": "tag1,tag2",
    "content": "笔记内容",
    "description": "笔记简介",
    "coverFile": {
      "fileId": "file-abc123",
      "fileUrl": "https://example.com/cover.jpg"
    },
    "broadCode": "tech",
    "narrowCode": "java",
    "noteGrade": 1,
    "noteMode": 1,
    "suitableUsers": "初学者",
    "auditStatus": 1,
    "status": 1,
    "stats": {
      "views": 1234,
      "likes": 56,
      "favorites": 32,
      "isLiked": false,
      "isFavorited": false
    },
    "publishTime": "2026-02-15 10:30:00",
    "isPinned": 0,
    "isRecommended": 1,
    "seoTitle": "SEO标题",
    "seoDescription": "SEO描述",
    "seoKeywords": "keyword1,keyword2",
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 笔记ID（Long类型自动转为string） |
| `userId` | `string` | 用户ID（Long类型自动转为string） |
| `author` | `object` | 作者信息 |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称（优先使用昵称，若无则使用用户名） |
| `author.avatar` | `string` | 作者头像URL |
| `noteName` | `string` | 笔记名称 |
| `noteTags` | `string` | 笔记标签（逗号分隔） |
| `content` | `string` | 文档内容 |
| `description` | `string` | 笔记简介/描述 |
| `coverFile` | `object` | 封面文件信息 |
| `coverFile.fileId` | `string` | 封面图片文件ID |
| `coverFile.fileUrl` | `string` | 封面图片文件URL |
| `broadCode` | `string` | 大类编码 |
| `narrowCode` | `string` | 小类编码 |
| `noteGrade` | `number` | 笔记等级 |
| `noteMode` | `number` | 笔记模式 |
| `suitableUsers` | `string` | 适合人群 |
| `auditStatus` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `status` | `number` | 笔记状态（1发布，2下架，3草稿） |
| `stats` | `object` | 统计信息（从Redis缓存获取） |
| `stats.views` | `number` | 浏览量 |
| `stats.likes` | `number` | 点赞数 |
| `stats.favorites` | `number` | 收藏数 |
| `stats.isLiked` | `boolean` | 是否已点赞 |
| `stats.isFavorited` | `boolean` | 是否已收藏 |
| `publishTime` | `string` | 笔记发布时间 |
| `isPinned` | `number` | 是否置顶（0否，1是） |
| `isRecommended` | `number` | 是否推荐（0否，1是） |
| `seoTitle` | `string` | SEO标题 |
| `seoDescription` | `string` | SEO描述 |
| `seoKeywords` | `string` | SEO关键词（逗号分隔） |
| `deleted` | `number` | 删除标记（0未删除，1已删除） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

### 失败响应（笔记不存在）

```json
{
  "code": 500,
  "msg": "笔记不存在",
  "data": null
}
```

---

## 4. 获取笔记交互数据

### 接口信息
- **URL**: `GET /api/document/docNote/{id}/interaction`
- **功能**: 获取笔记交互数据（浏览量、点赞量、收藏量、用户交互状态）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | 当前用户ID（用于判断用户是否已点赞/收藏） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "viewCount": 1234,
    "likeCount": 56,
    "collectCount": 23,
    "hasLiked": true,
    "hasCollected": false
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `success` | `boolean` | 是否成功 |
| `viewCount` | `number` | 浏览量（从Redis缓存获取） |
| `likeCount` | `number` | 点赞量（从Redis缓存获取） |
| `collectCount` | `number` | 收藏量（从Redis缓存获取） |
| `hasLiked` | `boolean/null` | 是否已点赞（用户未登录时为null） |
| `hasCollected` | `boolean/null` | 是否已收藏（用户未登录时为null） |

---

## 5. 新增笔记

### 接口信息
- **URL**: `POST /api/document/docNote`
- **功能**: 新增笔记

### 请求体

```json
{
  "userId": "1",
  "noteName": "新笔记标题",
  "noteTags": "tag1,tag2",
  "content": "笔记内容",
  "description": "笔记简介",
  "coverFileId": "1",
  "broadCode": "tech",
  "narrowCode": "java",
  "noteGrade": 1,
  "noteMode": 1,
  "suitableUsers": "初学者",
  "status": 1,
  "auditStatus": 0,
  "publishTime": "2026-02-15 10:30:00",
  "cover": "https://example.com/cover.jpg",
  "isPinned": 0,
  "isRecommended": 0,
  "seoTitle": "SEO标题",
  "seoDescription": "SEO描述",
  "seoKeywords": "keyword1,keyword2"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 是 | 用户ID |
| `noteName` | `string` | 是 | 笔记名称 |
| `noteTags` | `string` | 否 | 笔记标签（逗号分隔） |
| `content` | `string` | 是 | 文档内容 |
| `description` | `string` | 否 | 笔记简介/描述 |
| `coverFileId` | `string` | 否 | 封面图片文件ID |
| `broadCode` | `string` | 是 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |
| `noteGrade` | `number` | 否 | 笔记等级 |
| `noteMode` | `number` | 否 | 笔记模式 |
| `suitableUsers` | `string` | 否 | 适合人群 |
| `status` | `number` | 否 | 笔记状态（默认1） |
| `auditStatus` | `number` | 否 | 审核状态（默认0） |
| `publishTime` | `string` | 否 | 笔记发布时间 |
| `cover` | `string` | 否 | 封面图片URL |
| `isPinned` | `number` | 否 | 是否置顶（默认0） |
| `isRecommended` | `number` | 否 | 是否推荐（默认0） |
| `seoTitle` | `string` | 否 | SEO标题 |
| `seoDescription` | `string` | 否 | SEO描述 |
| `seoKeywords` | `string` | 否 | SEO关键词（逗号分隔） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 修改笔记

### 接口信息
- **URL**: `PUT /api/document/docNote`
- **功能**: 修改笔记

### 请求体

```json
{
  "id": "1",
  "noteName": "修改后的标题",
  "noteTags": "tag1,tag2",
  "content": "修改后的内容",
  "description": "修改后的简介",
  "coverFileId": "2",
  "broadCode": "life",
  "narrowCode": "python",
  "noteGrade": 2,
  "noteMode": 1,
  "suitableUsers": "进阶用户",
  "status": 1,
  "auditStatus": 1,
  "publishTime": "2026-02-16 10:30:00",
  "cover": "https://example.com/new-cover.jpg",
  "isPinned": 1,
  "isRecommended": 1,
  "seoTitle": "修改后的SEO标题",
  "seoDescription": "修改后的SEO描述",
  "seoKeywords": "newkeyword1,newkeyword2"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |
| `noteName` | `string` | 否 | 笔记名称 |
| `noteTags` | `string` | 否 | 笔记标签（逗号分隔） |
| `content` | `string` | 否 | 文档内容 |
| `description` | `string` | 否 | 笔记简介/描述 |
| `coverFileId` | `string` | 否 | 封面图片文件ID |
| `broadCode` | `string` | 否 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |
| `noteGrade` | `number` | 否 | 笔记等级 |
| `noteMode` | `number` | 否 | 笔记模式 |
| `suitableUsers` | `string` | 否 | 适合人群 |
| `status` | `number` | 否 | 笔记状态 |
| `auditStatus` | `number` | 否 | 审核状态 |
| `publishTime` | `string` | 否 | 笔记发布时间 |
| `cover` | `string` | 否 | 封面图片URL |
| `isPinned` | `number` | 否 | 是否置顶 |
| `isRecommended` | `number` | 否 | 是否推荐 |
| `seoTitle` | `string` | 否 | SEO标题 |
| `seoDescription` | `string` | 否 | SEO描述 |
| `seoKeywords` | `string` | 否 | SEO关键词（逗号分隔） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 7. 删除笔记

### 接口信息
- **URL**: `DELETE /api/document/docNote/{ids}`
- **功能**: 批量删除笔记

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 笔记ID列表，逗号分隔 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 8. 获取草稿列表

### 接口信息
- **URL**: `GET /api/document/docNote/draft/list`
- **功能**: 获取草稿列表

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
        "userId": "1",
        "noteName": "草稿标题",
        "noteTags": "tag1,tag2",
        "content": "草稿内容",
        "description": "草稿简介",
        "coverFileId": "1",
        "broadCode": "tech",
        "narrowCode": "java",
        "noteGrade": 1,
        "noteMode": 1,
        "suitableUsers": "初学者",
        "auditStatus": 0,
        "status": 3,
        "publishTime": null,
        "cover": "https://example.com/cover.jpg",
        "isPinned": 0,
        "isRecommended": 0,
        "seoTitle": null,
        "seoDescription": null,
        "seoKeywords": null,
        "version": 1,
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

## 9. 批量更新状态

### 接口信息
- **URL**: `PUT /api/document/docNote/status/batch`
- **功能**: 批量更新笔记状态

### 请求体

```json
{
  "ids": ["1", "2", "3"],
  "status": "published"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `array` | 是 | 笔记ID列表 |
| `status` | `string` | 是 | 目标状态：published（发布）、offline（下架） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 10. 批量迁移分类

### 接口信息
- **URL**: `PUT /api/document/docNote/category/batch`
- **功能**: 批量迁移笔记分类

### 请求体

```json
{
  "ids": ["1", "2", "3"],
  "category": "life"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `array` | 是 | 笔记ID列表 |
| `category` | `string` | 是 | 目标分类编码 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 11. 切换置顶状态

### 接口信息
- **URL**: `PUT /api/document/docNote/{id}/pinned`
- **功能**: 切换笔记置顶状态

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 失败响应（笔记不存在）

```json
{
  "code": 500,
  "msg": "笔记不存在",
  "data": null
}
```

---

## 12. 切换推荐状态

### 接口信息
- **URL**: `PUT /api/document/docNote/{id}/recommended`
- **功能**: 切换笔记推荐状态

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
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
| `data` | `object/array/boolean/null` | 响应数据 |
