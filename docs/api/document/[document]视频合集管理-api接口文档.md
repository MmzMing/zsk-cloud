# [document] 视频合集管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docVideoCollection/list` | GET | `DocVideoCollectionController.java` | 查询合集列表 |
| `/api/document/docVideoCollection/page` | GET | `DocVideoCollectionController.java` | 分页查询合集列表 |
| `/api/document/docVideoCollection/{id}` | GET | `DocVideoCollectionController.java` | 获取合集详情 |
| `/api/document/docVideoCollection` | POST | `DocVideoCollectionController.java` | 新增合集 |
| `/api/document/docVideoCollection` | PUT | `DocVideoCollectionController.java` | 修改合集 |
| `/api/document/docVideoCollection/{ids}` | DELETE | `DocVideoCollectionController.java` | 删除合集 |
| `/api/document/docVideoCollection/{id}/videos` | GET | `DocVideoCollectionController.java` | 获取合集内视频列表 |
| `/api/document/docVideoCollection/{id}/videos` | POST | `DocVideoCollectionController.java` | 批量添加视频到合集 |
| `/api/document/docVideoCollection/{id}/videos` | DELETE | `DocVideoCollectionController.java` | 批量从合集中移除视频 |
| `/api/document/docVideoCollection/{id}/videos/sort` | PUT | `DocVideoCollectionController.java` | 调整合集内视频排序 |

---

## 1. 查询合集列表

### 接口信息
- **URL**: `GET /api/document/docVideoCollection/list`
- **功能**: 查询当前登录用户的视频合集列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionName` | `string` | 否 | 合集名称（支持模糊查询） |
| `status` | `number` | 否 | 状态（1-公开，2-私密） |
| `deleted` | `number` | 否 | 删除标记（0-未删除，1-已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "collectionName": "Java学习合集",
      "description": "Java从入门到精通系列视频",
      "cover": {
        "fileId": "1",
        "fileUrl": "https://example.com/cover.jpg"
      },
      "videoCount": 12,
      "sortOrder": 0,
      "status": 1,
      "createTime": "2026-02-15 10:30:00",
      "updateTime": "2026-02-15 10:30:00"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 合集ID |
| `collectionName` | `string` | 合集名称 |
| `description` | `string` | 合集描述 |
| `cover` | `object` | 封面图片文件信息 |
| `cover.fileId` | `string` | 封面文件ID |
| `cover.fileUrl` | `string` | 封面文件URL |
| `videoCount` | `number` | 合集中视频数量 |
| `sortOrder` | `number` | 合集排序（越大越靠前） |
| `status` | `number` | 状态（1-公开，2-私密） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询合集列表

### 接口信息
- **URL**: `GET /api/document/docVideoCollection/page`
- **功能**: 分页查询当前登录用户的视频合集列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `collectionName` | `string` | 否 | - | 合集名称 |
| `status` | `number` | 否 | - | 状态 |
| `deleted` | `number` | 否 | - | 删除标记 |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |
| `orderByColumn` | `string` | 否 | - | 排序字段名 |
| `isAsc` | `string` | 否 | asc | 排序方向（asc/desc） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "collectionName": "Java学习合集",
        "description": "Java从入门到精通系列视频",
        "cover": {
          "fileId": "1",
          "fileUrl": "https://example.com/cover.jpg"
        },
        "videoCount": 12,
        "sortOrder": 0,
        "status": 1,
        "createTime": "2026-02-15 10:30:00",
        "updateTime": "2026-02-15 10:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `list` | `array` | 数据列表（结构同接口1） |
| `total` | `number` | 总记录数 |
| `pageNum` | `number` | 当前页码 |
| `pageSize` | `number` | 每页数量 |
| `totalPages` | `number` | 总页数 |
| `hasNext` | `boolean` | 是否有下一页 |
| `hasPrevious` | `boolean` | 是否有上一页 |

---

## 3. 获取合集详情

### 接口信息
- **URL**: `GET /api/document/docVideoCollection/{id}`
- **功能**: 获取合集详情（包含合集内视频列表）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "collectionName": "Java学习合集",
    "description": "Java从入门到精通系列视频",
    "cover": {
      "fileId": "1",
      "fileUrl": "https://example.com/cover.jpg"
    },
    "videoCount": 12,
    "sortOrder": 0,
    "status": 1,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00",
    "videoList": [
      {
        "id": "1",
        "userId": "1",
        "user": {
          "id": "1",
          "name": "作者昵称",
          "avatar": "https://example.com/avatar.jpg",
          "fans": 128,
          "isFollowing": false
        },
        "videoTitle": "Java基础入门",
        "tags": "java,basic",
        "fileContent": "Java基础入门视频",
        "videoFile": {
          "video": {
            "fileId": "1",
            "fileUrl": "https://example.com/video1.mp4"
          },
          "thumbnail": {
            "fileId": "2",
            "fileUrl": "https://example.com/cover1.jpg"
          }
        },
        "broadCode": "tech",
        "narrowCode": "java",
        "metaData": "{\"duration\":3600,\"resolution\":\"1920x1080\"}",
        "auditStatus": 1,
        "auditMind": "审核通过",
        "auditId": "100",
        "status": 1,
        "statsInfo": {
          "views": 1234,
          "likes": 56,
          "favorites": 32,
          "isLiked": true,
          "isFavorited": false
        },
        "isPinned": 0,
        "isRecommended": 1,
        "deleted": 0,
        "createTime": "2026-02-15 10:30:00",
        "updateTime": "2026-02-15 10:30:00"
      }
    ]
  }
}
```

### 响应字段说明

#### 合集基础字段

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 合集ID |
| `collectionName` | `string` | 合集名称 |
| `description` | `string` | 合集描述 |
| `cover` | `object` | 封面图片文件信息 |
| `cover.fileId` | `string` | 封面文件ID |
| `cover.fileUrl` | `string` | 封面文件URL |
| `videoCount` | `number` | 合集中视频数量 |
| `sortOrder` | `number` | 合集排序（越大越靠前） |
| `status` | `number` | 状态（1-公开，2-私密） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |
| `videoList` | `array` | 合集中的视频列表 |

#### 视频项字段

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 视频ID |
| `userId` | `string` | 作者用户ID |
| `user` | `object` | 作者信息 |
| `user.id` | `string` | 用户ID |
| `user.name` | `string` | 用户昵称 |
| `user.avatar` | `string` | 用户头像URL |
| `user.fans` | `number` | 粉丝数 |
| `user.isFollowing` | `boolean` | 当前用户是否已关注 |
| `videoTitle` | `string` | 视频标题 |
| `tags` | `string` | 标签（逗号分隔） |
| `fileContent` | `string` | 视频描述/文本内容 |
| `videoFile` | `object` | 视频文件信息 |
| `videoFile.video` | `object` | 视频播放文件 `{fileId, fileUrl}` |
| `videoFile.thumbnail` | `object` | 视频缩略图文件 `{fileId, fileUrl}` |
| `broadCode` | `string` | 大类编码 |
| `narrowCode` | `string` | 小类编码 |
| `metaData` | `string` | 元数据（JSON格式，如分辨率、时长、编码等） |
| `auditStatus` | `number` | 审核状态（0-待审核，1-审核通过，2-审核驳回） |
| `auditMind` | `string` | 审核意见 |
| `auditId` | `string` | 审核记录ID |
| `status` | `number` | 状态（1-正常，2-下架，3-草稿） |
| `statsInfo` | `object` | 统计数据 |
| `statsInfo.views` | `number` | 阅读数 |
| `statsInfo.likes` | `number` | 点赞数 |
| `statsInfo.favorites` | `number` | 收藏数 |
| `statsInfo.isLiked` | `boolean` | 当前用户是否已点赞 |
| `statsInfo.isFavorited` | `boolean` | 当前用户是否已收藏 |
| `isPinned` | `number` | 是否置顶（0-否，1-是） |
| `isRecommended` | `number` | 是否推荐（0-否，1-是） |
| `deleted` | `number` | 删除标记（0-未删除，1-已删除） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 4. 新增合集

### 接口信息
- **URL**: `POST /api/document/docVideoCollection`
- **功能**: 新增视频合集（用户ID由后端自动设置为当前登录用户）

### 请求体

```json
{
  "collectionName": "Java学习合集",
  "description": "Java从入门到精通系列视频",
  "coverFileId": "1",
  "sortOrder": 0,
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionName` | `string` | 是 | 合集名称 |
| `description` | `string` | 否 | 合集描述 |
| `coverFileId` | `string` | 否 | 封面图片文件ID（关联 document_files.id） |
| `sortOrder` | `number` | 否 | 合集排序值（越大越靠前，默认0） |
| `status` | `number` | 否 | 状态（1-公开，2-私密，默认1） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": "1"
}
```

> `data` 为新创建的合集ID。

---

## 5. 修改合集

### 接口信息
- **URL**: `PUT /api/document/docVideoCollection`
- **功能**: 修改视频合集（只能修改自己创建的合集）

### 请求体

```json
{
  "id": "1",
  "collectionName": "修改后的合集名称",
  "description": "修改后的描述",
  "coverFileId": "2",
  "sortOrder": 10,
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |
| `collectionName` | `string` | 否 | 合集名称 |
| `description` | `string` | 否 | 合集描述 |
| `coverFileId` | `string` | 否 | 封面图片文件ID（关联 document_files.id） |
| `sortOrder` | `number` | 否 | 合集排序值（越大越靠前） |
| `status` | `number` | 否 | 状态（1-公开，2-私密） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除合集

### 接口信息
- **URL**: `DELETE /api/document/docVideoCollection/{ids}`
- **功能**: 批量删除视频合集（软删除，只能删除自己创建的合集）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 合集ID列表，逗号分隔（如：1,2,3） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 7. 获取合集内视频列表

### 接口信息
- **URL**: `GET /api/document/docVideoCollection/{id}/videos`
- **功能**: 获取指定合集内的视频列表

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |

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
        "avatar": "https://example.com/avatar.jpg",
        "fans": 128,
        "isFollowing": false
      },
      "videoTitle": "Java基础入门",
      "tags": "java,basic",
      "fileContent": "Java基础入门视频",
      "videoFile": {
        "video": {
          "fileId": "1",
          "fileUrl": "https://example.com/video1.mp4"
        },
        "thumbnail": {
          "fileId": "2",
          "fileUrl": "https://example.com/cover1.jpg"
        }
      },
      "broadCode": "tech",
      "narrowCode": "java",
      "metaData": "{\"duration\":3600,\"resolution\":\"1920x1080\"}",
      "auditStatus": 1,
      "auditMind": "审核通过",
      "auditId": "100",
      "status": 1,
      "statsInfo": {
        "views": 1234,
        "likes": 56,
        "favorites": 32,
        "isLiked": true,
        "isFavorited": false
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

> 视频项字段说明参见接口3的"视频项字段"表格。

---

## 8. 批量添加视频到合集

### 接口信息
- **URL**: `POST /api/document/docVideoCollection/{id}/videos`
- **功能**: 批量将多个视频添加到指定合集（自动过滤已存在的视频，新视频默认排在末尾）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |

### 请求体

```json
{
  "videoIds": ["1", "2", "3"]
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoIds` | `array` | 是 | 视频ID列表，至少包含一个视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 9. 批量从合集中移除视频

### 接口信息
- **URL**: `DELETE /api/document/docVideoCollection/{id}/videos`
- **功能**: 批量从指定合集中移除视频（软删除，只能操作自己创建的合集）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |

### 请求体

```json
{
  "videoIds": ["1", "2"]
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoIds` | `array` | 是 | 待移除的视频ID列表，至少包含一个视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 10. 调整合集内视频排序

### 接口信息
- **URL**: `PUT /api/document/docVideoCollection/{id}/videos/sort`
- **功能**: 调整合集内视频的播放顺序。传入的视频ID列表顺序即为最终排序结果（索引0排第一，索引1排第二，以此类推）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |

### 请求体

```json
{
  "videoIds": ["2", "1", "3"]
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoIds` | `array` | 是 | 按期望顺序排列的视频ID列表，至少包含一个视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
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
| `data` | `object/array/boolean/string/null` | 响应数据 |
