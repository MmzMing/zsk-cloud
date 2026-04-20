# [document] 视频详情管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/video/detail/list` | GET | `DocVideoDetailController.java` | 查询视频详情列表 |
| `/api/document/video/detail/page` | GET | `DocVideoDetailController.java` | 分页查询视频详情列表 |
| `/api/document/video/detail/{id}` | GET | `DocVideoDetailController.java` | 获取视频详情详细信息 |
| `/api/document/video/detail` | POST | `DocVideoDetailController.java` | 新增视频详情 |
| `/api/document/video/detail/upload` | POST | `DocVideoDetailController.java` | 上传视频文件并保存详情 |
| `/api/document/video/detail` | PUT | `DocVideoDetailController.java` | 修改视频详情 |
| `/api/document/video/detail/{ids}` | DELETE | `DocVideoDetailController.java` | 删除视频详情 |
| `/api/document/video/detail/draft/list` | GET | `DocVideoDetailController.java` | 获取草稿列表 |
| `/api/document/video/detail/draft` | POST | `DocVideoDetailController.java` | 保存草稿 |
| `/api/document/video/detail/draft/publish/{id}` | PUT | `DocVideoDetailController.java` | 发布草稿 |
| `/api/document/video/detail/status/batch` | PUT | `DocVideoDetailController.java` | 批量更新视频状态 |
| `/api/document/video/detail/{id}/pinned` | PUT | `DocVideoDetailController.java` | 切换视频置顶状态 |
| `/api/document/video/detail/{id}/recommended` | PUT | `DocVideoDetailController.java` | 切换视频推荐状态 |

---

## 1. 查询视频详情列表

### 接口信息
- **URL**: `GET /api/document/video/detail/list`
- **功能**: 查询视频详情列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | 用户ID |
| `videoTitle` | `string` | 否 | 视频标题（支持模糊查询） |
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
      "videoTitle": "视频标题",
      "fileContent": "视频描述",
      "fileId": "file-abc123",
      "videoUrl": "https://example.com/video.mp4",
      "coverUrl": "https://example.com/cover.jpg",
      "broadCode": "tech",
      "tags": "tag1,tag2",
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
| `id` | `string` | 视频ID（后端使用Jackson转为string类型） |
| `userId` | `string` | 用户ID |
| `videoTitle` | `string` | 视频标题 |
| `fileContent` | `string` | 视频描述 |
| `fileId` | `string` | 文件ID |
| `videoUrl` | `string` | 视频URL |
| `coverUrl` | `string` | 封面URL |
| `broadCode` | `string` | 分类编码 |
| `tags` | `string` | 标签（逗号分隔） |
| `status` | `number` | 状态（1发布，2下架，3草稿） |
| `auditStatus` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `viewCount` | `number` | 播放量 |
| `likeCount` | `number` | 点赞数 |
| `commentCount` | `number` | 评论数 |
| `collectCount` | `number` | 收藏数 |
| `isPinned` | `number` | 是否置顶（0否，1是） |
| `isRecommended` | `number` | 是否推荐（0否，1是） |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询视频详情列表

### 接口信息
- **URL**: `GET /api/document/video/detail/page`
- **功能**: 分页查询视频详情列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `userId` | `string` | 否 | - | 用户ID |
| `videoTitle` | `string` | 否 | - | 视频标题 |
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
        "videoTitle": "视频标题",
        "fileContent": "视频描述",
        "fileId": "file-abc123",
        "videoUrl": "https://example.com/video.mp4",
        "coverUrl": "https://example.com/cover.jpg",
        "broadCode": "tech",
        "tags": "tag1,tag2",
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

## 3. 获取视频详情详细信息

### 接口信息
- **URL**: `GET /api/document/video/detail/{id}`
- **功能**: 获取视频详情详细信息

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
    "videoTitle": "视频标题",
    "fileContent": "视频描述",
    "fileId": "file-abc123",
    "videoUrl": "https://example.com/video.mp4",
    "coverUrl": "https://example.com/cover.jpg",
    "broadCode": "tech",
    "tags": "tag1,tag2",
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

## 4. 新增视频详情

### 接口信息
- **URL**: `POST /api/document/video/detail`
- **功能**: 新增视频详情

### 请求体

```json
{
  "userId": "1",
  "videoTitle": "新视频标题",
  "fileContent": "视频描述",
  "fileId": "file-abc123",
  "broadCode": "tech",
  "tags": "tag1,tag2",
  "status": 1,
  "auditStatus": 0
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 是 | 用户ID |
| `videoTitle` | `string` | 是 | 视频标题 |
| `fileContent` | `string` | 否 | 视频描述 |
| `fileId` | `string` | 是 | 文件ID |
| `broadCode` | `string` | 是 | 分类编码 |
| `tags` | `string` | 否 | 标签（逗号分隔） |
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

## 5. 上传视频文件并保存详情

### 接口信息
- **URL**: `POST /api/document/video/detail/upload`
- **功能**: 上传视频文件并保存详情

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `MultipartFile` | 是 | 视频文件 |
| `docVideoDetail` | `object` | 是 | 视频详情对象 |

### 请求体（docVideoDetail）

```json
{
  "userId": "1",
  "videoTitle": "新视频标题",
  "fileContent": "视频描述",
  "broadCode": "tech",
  "tags": "tag1,tag2"
}
```

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 修改视频详情

### 接口信息
- **URL**: `PUT /api/document/video/detail`
- **功能**: 修改视频详情

### 请求体

```json
{
  "id": "1",
  "videoTitle": "修改后的标题",
  "fileContent": "修改后的描述",
  "broadCode": "life"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |
| `videoTitle` | `string` | 否 | 视频标题 |
| `fileContent` | `string` | 否 | 视频描述 |
| `broadCode` | `string` | 否 | 分类编码 |
| `tags` | `string` | 否 | 标签 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 7. 删除视频详情

### 接口信息
- **URL**: `DELETE /api/document/video/detail/{ids}`
- **功能**: 批量删除视频详情

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

## 8. 获取草稿列表

### 接口信息
- **URL**: `GET /api/document/video/detail/draft/list`
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
        "videoTitle": "草稿标题",
        "fileContent": "草稿描述",
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

## 9. 保存草稿

### 接口信息
- **URL**: `POST /api/document/video/detail/draft`
- **功能**: 保存视频草稿

### 请求体

```json
{
  "userId": "1",
  "videoTitle": "草稿标题",
  "fileContent": "草稿描述",
  "broadCode": "tech"
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

### 响应说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `data` | `string` | 草稿ID |

---

## 10. 发布草稿

### 接口信息
- **URL**: `PUT /api/document/video/detail/draft/publish/{id}`
- **功能**: 发布草稿为正式视频

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

## 11. 批量更新视频状态

### 接口信息
- **URL**: `PUT /api/document/video/detail/status/batch`
- **功能**: 批量更新视频状态

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
| `status` | `number` | 是 | 目标状态（1发布，2下架，3草稿） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 12. 切换视频置顶状态

### 接口信息
- **URL**: `PUT /api/document/video/detail/{id}/pinned`
- **功能**: 切换视频置顶状态

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `pinned` | `number` | 是 | 是否置顶（0否，1是） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 13. 切换视频推荐状态

### 接口信息
- **URL**: `PUT /api/document/video/detail/{id}/recommended`
- **功能**: 切换视频推荐状态

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 视频ID |

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `recommended` | `number` | 是 | 是否推荐（0否，1是） |

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
| `data` | `object/array/boolean/string/null` | 响应数据 |
