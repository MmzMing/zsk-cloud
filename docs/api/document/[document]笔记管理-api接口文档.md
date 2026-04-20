# [document] 笔记管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/note/list` | GET | `DocNoteController.java` | 查询笔记列表 |
| `/api/document/note/page` | GET | `DocNoteController.java` | 分页查询笔记列表 |
| `/api/document/note/{id}` | GET | `DocNoteController.java` | 获取笔记详细信息 |
| `/api/document/note` | POST | `DocNoteController.java` | 新增笔记 |
| `/api/document/note` | PUT | `DocNoteController.java` | 修改笔记 |
| `/api/document/note/{ids}` | DELETE | `DocNoteController.java` | 删除笔记 |
| `/api/document/note/draft/list` | GET | `DocNoteController.java` | 获取草稿列表 |
| `/api/document/note/status/batch` | PUT | `DocNoteController.java` | 批量更新状态 |
| `/api/document/note/category/batch` | PUT | `DocNoteController.java` | 批量迁移分类 |
| `/api/document/note/{id}/pinned` | PUT | `DocNoteController.java` | 切换置顶状态 |
| `/api/document/note/{id}/recommended` | PUT | `DocNoteController.java` | 切换推荐状态 |

---

## 1. 查询笔记列表

### 接口信息
- **URL**: `GET /api/document/note/list`
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
      "noteName": "笔记标题",
      "content": "笔记内容",
      "cover": "https://example.com/cover.jpg",
      "broadCode": "tech",
      "status": 1,
      "auditStatus": 1,
      "viewCount": 100,
      "likeCount": 10,
      "commentCount": 5,
      "collectCount": 8,
      "isPinned": 0,
      "isRecommended": 1,
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
| `id` | `string` | 笔记ID（后端使用Jackson转为string类型） |
| `userId` | `string` | 用户ID |
| `noteName` | `string` | 笔记名称 |
| `content` | `string` | 笔记内容 |
| `cover` | `string` | 封面图片URL |
| `broadCode` | `string` | 分类编码 |
| `status` | `number` | 状态（1发布，2下架，3草稿） |
| `auditStatus` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `viewCount` | `number` | 浏览量 |
| `likeCount` | `number` | 点赞数 |
| `commentCount` | `number` | 评论数 |
| `collectCount` | `number` | 收藏数 |
| `isPinned` | `number` | 是否置顶（0否，1是） |
| `isRecommended` | `number` | 是否推荐（0否，1是） |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询笔记列表

### 接口信息
- **URL**: `GET /api/document/note/page`
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
        "noteName": "笔记标题",
        "content": "笔记内容",
        "cover": "https://example.com/cover.jpg",
        "broadCode": "tech",
        "status": 1,
        "auditStatus": 1,
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "collectCount": 8,
        "isPinned": 0,
        "isRecommended": 1,
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
- **URL**: `GET /api/document/note/{id}`
- **功能**: 获取笔记详细信息

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
    "noteName": "笔记标题",
    "content": "笔记内容",
    "cover": "https://example.com/cover.jpg",
    "broadCode": "tech",
    "status": 1,
    "auditStatus": 1,
    "viewCount": 100,
    "likeCount": 10,
    "commentCount": 5,
    "collectCount": 8,
    "isPinned": 0,
    "isRecommended": 1,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增笔记

### 接口信息
- **URL**: `POST /api/document/note`
- **功能**: 新增笔记

### 请求体

```json
{
  "userId": "1",
  "noteName": "新笔记标题",
  "content": "笔记内容",
  "cover": "https://example.com/cover.jpg",
  "broadCode": "tech",
  "status": 1,
  "auditStatus": 0
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 是 | 用户ID |
| `noteName` | `string` | 是 | 笔记名称 |
| `content` | `string` | 是 | 笔记内容 |
| `cover` | `string` | 否 | 封面图片URL |
| `broadCode` | `string` | 是 | 分类编码 |
| `status` | `number` | 否 | 状态（默认1） |
| `auditStatus` | `number` | 否 | 审核状态（默认0） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改笔记

### 接口信息
- **URL**: `PUT /api/document/note`
- **功能**: 修改笔记

### 请求体

```json
{
  "id": "1",
  "noteName": "修改后的标题",
  "content": "修改后的内容",
  "cover": "https://example.com/new-cover.jpg",
  "broadCode": "life"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |
| `noteName` | `string` | 否 | 笔记名称 |
| `content` | `string` | 否 | 笔记内容 |
| `cover` | `string` | 否 | 封面图片URL |
| `broadCode` | `string` | 否 | 分类编码 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除笔记

### 接口信息
- **URL**: `DELETE /api/document/note/{ids}`
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

## 7. 获取草稿列表

### 接口信息
- **URL**: `GET /api/document/note/draft/list`
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
        "content": "草稿内容",
        "status": 3,
        "auditStatus": 0,
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

## 8. 批量更新状态

### 接口信息
- **URL**: `PUT /api/document/note/status/batch`
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

## 9. 批量迁移分类

### 接口信息
- **URL**: `PUT /api/document/note/category/batch`
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

## 10. 切换置顶状态

### 接口信息
- **URL**: `PUT /api/document/note/{id}/pinned`
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

## 11. 切换推荐状态

### 接口信息
- **URL**: `PUT /api/document/note/{id}/recommended`
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
