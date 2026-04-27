# [document] 视频管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docVideo/list` | GET | `DocVideoController.java` | 查询视频列表 |
| `/api/document/docVideo/page` | GET | `DocVideoController.java` | 分页查询视频列表 |
| `/api/document/docVideo/{id}` | GET | `DocVideoController.java` | 获取视频详情 |
| `/api/document/docVideo/{id}/interaction` | GET | `DocVideoController.java` | 获取视频交互数据 |
| `/api/document/docVideo/{id}/view` | POST | `DocVideoController.java` | 增加视频浏览量 |
| `/api/document/docVideo` | POST | `DocVideoController.java` | 新增视频 |
| `/api/document/docVideo/upload` | POST | `DocVideoController.java` | 上传视频文件并保存 |
| `/api/document/docVideo` | PUT | `DocVideoController.java` | 修改视频 |
| `/api/document/docVideo/{ids}` | DELETE | `DocVideoController.java` | 删除视频 |
| `/api/document/docVideo/draft/list` | GET | `DocVideoController.java` | 获取草稿列表 |
| `/api/document/docVideo/draft` | POST | `DocVideoController.java` | 保存草稿 |
| `/api/document/docVideo/draft/publish/{id}` | PUT | `DocVideoController.java` | 发布草稿 |
| `/api/document/docVideo/status/batch` | PUT | `DocVideoController.java` | 批量更新视频状态 |
| `/api/document/docVideo/{id}/pinned` | PUT | `DocVideoController.java` | 切换置顶状态 |
| `/api/document/docVideo/{id}/recommended` | PUT | `DocVideoController.java` | 切换推荐状态 |

---

## 1. 查询视频列表

### 接口信息
- **URL**: `GET /api/document/docVideo/list`
- **功能**: 查询视频列表（包含文件信息、作者信息、统计信息）

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | 用户ID |
| `videoTitle` | `string` | 否 | 视频标题（支持模糊查询） |
| `status` | `number` | 否 | 状态（1-正常，2-下架，3-草稿） |
| `auditStatus` | `number` | 否 | 审核状态（0-待审核，1-已通过，2-已驳回） |
| `broadCode` | `string` | 否 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |
| `deleted` | `number` | 否 | 删除标记（0-未删除，1-已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "userId": "1",
      "user": {
        "id": "1",
        "name": "作者昵称",
        "avatar": "https://example.com/avatar.jpg"
      },
      "videoTitle": "视频标题",
      "tags": "tag1,tag2",
      "fileContent": "视频描述",
      "videoFile": {
        "video": {
          "fileId": "1",
          "fileUrl": "https://example.com/video.mp4"
        },
        "thumbnail": {
          "fileId": "2",
          "fileUrl": "https://example.com/cover.jpg"
        }
      },
      "broadCode": "tech",
      "narrowCode": "java",
      "auditStatus": 1,
      "status": 1,
      "statsInfo": {
        "views": 1234,
        "likes": 56,
        "favorites": 32
      },
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
| `id` | `string` | 视频ID |
| `userId` | `string` | 用户ID |
| `user` | `object` | 用户信息 |
| `user.id` | `string` | 用户ID |
| `user.name` | `string` | 用户名称（优先昵称） |
| `user.avatar` | `string` | 用户头像URL |
| `videoTitle` | `string` | 视频标题 |
| `tags` | `string` | 标签（逗号分隔） |
| `fileContent` | `string` | 视频描述/文本内容 |
| `videoFile` | `object` | 视频文件信息 |
| `videoFile.video` | `object` | 视频文件 |
| `videoFile.video.fileId` | `string` | 视频文件ID |
| `videoFile.video.fileUrl` | `string` | 视频播放URL |
| `videoFile.thumbnail` | `object` | 缩略图文件 |
| `videoFile.thumbnail.fileId` | `string` | 缩略图文件ID |
| `videoFile.thumbnail.fileUrl` | `string` | 缩略图URL |
| `broadCode` | `string` | 大类编码 |
| `narrowCode` | `string` | 小类编码 |
| `auditStatus` | `number` | 审核状态（0-待审核，1-已通过，2-已驳回） |
| `status` | `number` | 状态（1-正常，2-下架，3-草稿） |
| `statsInfo` | `object` | 统计信息（从Redis缓存获取） |
| `statsInfo.views` | `number` | 浏览量 |
| `statsInfo.likes` | `number` | 点赞数 |
| `statsInfo.favorites` | `number` | 收藏数 |
| `isPinned` | `number` | 是否置顶（0-否，1-是） |
| `isRecommended` | `number` | 是否推荐（0-否，1-是） |
| `deleted` | `number` | 删除标记（0-未删除，1-已删除） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询视频列表

### 接口信息
- **URL**: `GET /api/document/docVideo/page`
- **功能**: 分页查询视频列表（包含文件信息、作者信息、统计信息）

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | - | 用户ID |
| `videoTitle` | `string` | 否 | - | 视频标题 |
| `status` | `number` | 否 | - | 状态 |
| `auditStatus` | `number` | 否 | - | 审核状态 |
| `broadCode` | `string` | 否 | - | 大类编码 |
| `narrowCode` | `string` | 否 | - | 小类编码 |
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
        "user": {
          "id": "1",
          "name": "作者昵称",
          "avatar": "https://example.com/avatar.jpg"
        },
        "videoTitle": "视频标题",
        "tags": "tag1,tag2",
        "fileContent": "视频描述",
        "videoFile": {
          "video": {
            "fileId": "1",
            "fileUrl": "https://example.com/video.mp4"
          },
          "thumbnail": {
            "fileId": "2",
            "fileUrl": "https://example.com/cover.jpg"
          }
        },
        "broadCode": "tech",
        "narrowCode": "java",
        "auditStatus": 1,
        "status": 1,
        "statsInfo": {
          "views": 1234,
          "likes": 56,
          "favorites": 32
        },
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

## 3. 获取视频详情

### 接口信息
- **URL**: `GET /api/document/docVideo/{id}`
- **功能**: 获取视频详情（包含文件信息、分集信息）

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
    "userId": "1",
    "user": {
      "id": "1",
      "name": "作者昵称",
      "avatar": "https://example.com/avatar.jpg"
    },
    "videoTitle": "视频标题",
    "tags": "tag1,tag2",
    "fileContent": "视频描述",
    "videoFile": {
      "video": {
        "fileId": "1",
        "fileUrl": "https://example.com/video.mp4"
      },
      "thumbnail": {
        "fileId": "2",
        "fileUrl": "https://example.com/cover.jpg"
      }
    },
    "broadCode": "tech",
    "narrowCode": "java",
    "metaData": "{\"resolution\":\"1920x1080\",\"duration\":\"3600\"}",
    "auditStatus": 1,
    "status": 1,
    "videoDtl": {
      "id": "1",
      "title": "第一集",
      "videoUrl": "https://example.com/ep1.mp4",
      "duration": "1200"
    },
    "isPinned": 0,
    "isRecommended": 1,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 视频ID |
| `userId` | `string` | 用户ID |
| `user` | `object` | 用户信息 |
| `user.id` | `string` | 用户ID |
| `user.name` | `string` | 用户名称 |
| `user.avatar` | `string` | 用户头像URL |
| `videoTitle` | `string` | 视频标题 |
| `tags` | `string` | 标签（逗号分隔） |
| `fileContent` | `string` | 视频描述/文本内容 |
| `videoFile` | `object` | 视频文件信息 |
| `videoFile.video` | `object` | 视频文件 |
| `videoFile.video.fileId` | `string` | 视频文件ID |
| `videoFile.video.fileUrl` | `string` | 视频播放URL |
| `videoFile.thumbnail` | `object` | 缩略图文件 |
| `videoFile.thumbnail.fileId` | `string` | 缩略图文件ID |
| `videoFile.thumbnail.fileUrl` | `string` | 缩略图URL |
| `broadCode` | `string` | 大类编码 |
| `narrowCode` | `string` | 小类编码 |
| `metaData` | `string` | 元数据（JSON格式） |
| `auditStatus` | `number` | 审核状态 |
| `status` | `number` | 状态 |
| `videoDtl` | `object` | 分集信息 |
| `videoDtl.id` | `string` | 分集ID |
| `videoDtl.title` | `string` | 分集标题 |
| `videoDtl.videoUrl` | `string` | 分集视频地址 |
| `videoDtl.duration` | `string` | 分集时长 |
| `isPinned` | `number` | 是否置顶 |
| `isRecommended` | `number` | 是否推荐 |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 4. 获取视频交互数据

### 接口信息
- **URL**: `GET /api/document/docVideo/{id}/interaction`
- **功能**: 获取视频交互数据（浏览量、点赞量、收藏量、用户交互状态）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

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

## 5. 增加视频浏览量

### 接口信息
- **URL**: `POST /api/document/docVideo/{id}/view`
- **功能**: 用户浏览视频时调用，增加对应视频的浏览计数

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | 用户ID（用于防止同一用户短时间内重复计数） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 6. 新增视频

### 接口信息
- **URL**: `POST /api/document/docVideo`
- **功能**: 新增视频

### 请求体

```json
{
  "userId": "1",
  "videoTitle": "新视频标题",
  "fileId": "1",
  "coverFileId": "2",
  "tags": "tag1,tag2",
  "fileContent": "视频描述",
  "broadCode": "tech",
  "narrowCode": "java",
  "metaData": "{\"resolution\":\"1920x1080\"}",
  "status": 1,
  "auditStatus": 0,
  "isPinned": 0,
  "isRecommended": 0
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 是 | 用户ID |
| `videoTitle` | `string` | 是 | 视频标题 |
| `fileId` | `string` | 否 | 视频文件ID |
| `coverFileId` | `string` | 否 | 封面图片文件ID |
| `tags` | `string` | 否 | 标签（逗号分隔） |
| `fileContent` | `string` | 否 | 视频描述/文本内容 |
| `broadCode` | `string` | 是 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |
| `metaData` | `string` | 否 | 元数据（JSON格式） |
| `status` | `number` | 否 | 状态（默认1） |
| `auditStatus` | `number` | 否 | 审核状态（默认0） |
| `isPinned` | `number` | 否 | 是否置顶（默认0） |
| `isRecommended` | `number` | 否 | 是否推荐（默认0） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 7. 上传视频文件并保存

### 接口信息
- **URL**: `POST /api/document/docVideo/upload`
- **功能**: 上传视频文件并保存视频信息
- **Content-Type**: `multipart/form-data`

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `file` | 是 | 视频文件 |
| `userId` | `string` | 是 | 用户ID |
| `videoTitle` | `string` | 是 | 视频标题 |
| `tags` | `string` | 否 | 标签 |
| `fileContent` | `string` | 否 | 视频描述 |
| `broadCode` | `string` | 是 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 8. 修改视频

### 接口信息
- **URL**: `PUT /api/document/docVideo`
- **功能**: 修改视频

### 请求体

```json
{
  "id": "1",
  "videoTitle": "修改后的标题",
  "fileId": "1",
  "coverFileId": "3",
  "tags": "newtag1,newtag2",
  "fileContent": "修改后的描述",
  "broadCode": "life",
  "narrowCode": "python",
  "metaData": "{\"resolution\":\"4K\"}",
  "status": 1,
  "auditStatus": 1,
  "isPinned": 1,
  "isRecommended": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |
| `videoTitle` | `string` | 否 | 视频标题 |
| `fileId` | `string` | 否 | 视频文件ID |
| `coverFileId` | `string` | 否 | 封面图片文件ID |
| `tags` | `string` | 否 | 标签 |
| `fileContent` | `string` | 否 | 视频描述 |
| `broadCode` | `string` | 否 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |
| `metaData` | `string` | 否 | 元数据 |
| `status` | `number` | 否 | 状态 |
| `auditStatus` | `number` | 否 | 审核状态 |
| `isPinned` | `number` | 否 | 是否置顶 |
| `isRecommended` | `number` | 否 | 是否推荐 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 9. 删除视频

### 接口信息
- **URL**: `DELETE /api/document/docVideo/{ids}`
- **功能**: 批量删除视频

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 视频ID列表，逗号分隔 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 10. 获取草稿列表

### 接口信息
- **URL**: `GET /api/document/docVideo/draft/list`
- **功能**: 获取状态为草稿的视频列表

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
        "user": {
          "id": "1",
          "name": "作者昵称",
          "avatar": "https://example.com/avatar.jpg"
        },
        "videoTitle": "草稿标题",
        "tags": "tag1,tag2",
        "fileContent": "草稿描述",
        "videoFile": {
          "video": {
            "fileId": "1",
            "fileUrl": "https://example.com/video.mp4"
          },
          "thumbnail": {
            "fileId": "2",
            "fileUrl": "https://example.com/cover.jpg"
          }
        },
        "broadCode": "tech",
        "narrowCode": "java",
        "auditStatus": 0,
        "status": 3,
        "statsInfo": {
          "views": 0,
          "likes": 0,
          "favorites": 0
        },
        "isPinned": 0,
        "isRecommended": 0,
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

## 11. 保存草稿

### 接口信息
- **URL**: `POST /api/document/docVideo/draft`
- **功能**: 保存视频草稿

### 请求体

```json
{
  "userId": "1",
  "videoTitle": "草稿标题",
  "fileId": "1",
  "coverFileId": "2",
  "tags": "tag1,tag2",
  "fileContent": "草稿描述",
  "broadCode": "tech",
  "narrowCode": "java"
}
```

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": "1"
}
```

---

## 12. 发布草稿

### 接口信息
- **URL**: `PUT /api/document/docVideo/draft/publish/{id}`
- **功能**: 将草稿状态变更为正常，并设置审核状态为待审核

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 草稿ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 13. 批量更新视频状态

### 接口信息
- **URL**: `PUT /api/document/docVideo/status/batch`
- **功能**: 批量修改视频的状态字段

### 请求体

```json
{
  "ids": ["1", "2", "3"],
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `array` | 是 | 视频ID列表 |
| `status` | `number` | 是 | 目标状态（1-正常，2-下架，3-草稿） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 14. 切换置顶状态

### 接口信息
- **URL**: `PUT /api/document/docVideo/{id}/pinned`
- **功能**: 切换视频置顶状态

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `pinned` | `number` | 是 | 置顶状态（0-否，1-是） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 15. 切换推荐状态

### 接口信息
- **URL**: `PUT /api/document/docVideo/{id}/recommended`
- **功能**: 切换视频推荐状态

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `recommended` | `number` | 是 | 推荐状态（0-否，1-是） |

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
