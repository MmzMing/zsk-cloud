# [document] 视频详情管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docVideo/list` | GET | `DocVideoController.java` | 查询视频列表 |
| `/api/document/docVideo/page` | GET | `DocVideoController.java` | 分页查询视频列表 |
| `/api/document/docVideo/{id}` | GET | `DocVideoController.java` | 获取视频详细信息 |
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
| `/api/document/docVideo/{id}/pinned` | PUT | `DocVideoController.java` | 切换视频置顶状态 |
| `/api/document/docVideo/{id}/recommended` | PUT | `DocVideoController.java` | 切换视频推荐状态 |

---

## 1. 查询视频列表

### 接口信息
- **URL**: `GET /api/document/docVideo/list`
- **功能**: 查询视频列表

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
      "videoFile": {
        "thumbnail": {
          "fileId": "file-cover123",
          "fileUrl": "https://example.com/cover.jpg"
        },
        "video": {
          "fileId": "file-video123",
          "fileUrl": "https://example.com/video.mp4"
        }
      },
      "broadCode": "tech",
      "tags": "tag1,tag2",
      "status": 1,
      "auditStatus": 1,
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
| `videoFile` | `object` | 视频文件信息（一对一绑定） |
| `videoFile.thumbnail` | `object` | 缩略图文件信息 |
| `videoFile.thumbnail.fileId` | `string` | 缩略图文件ID（关联document_files.file_id） |
| `videoFile.thumbnail.fileUrl` | `string` | 缩略图文件URL |
| `videoFile.video` | `object` | 视频文件信息 |
| `videoFile.video.fileId` | `string` | 视频文件ID（关联document_files.file_id） |
| `videoFile.video.fileUrl` | `string` | 视频文件URL |
| `broadCode` | `string` | 分类编码 |
| `tags` | `string` | 标签（逗号分隔） |
| `status` | `number` | 状态（1发布，2下架，3草稿） |
| `auditStatus` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `isPinned` | `number` | 是否置顶（0否，1是） |
| `isRecommended` | `number` | 是否推荐（0否，1是） |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询视频列表

### 接口信息
- **URL**: `GET /api/document/docVideo/page`
- **功能**: 分页查询视频列表

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

## 3. 获取视频详细信息

### 接口信息
- **URL**: `GET /api/document/docVideo/{id}`
- **功能**: 获取视频详细信息，同时关联查询文件信息获取封面图URL和视频播放地址

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
    "isPinned": 0,
    "isRecommended": 1,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

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
    "status": true,
    "count": 11
  }
}
```

---

## 5. 增加视频浏览量

### 接口信息
- **URL**: `POST /api/document/docVideo/{id}/view`
- **功能**: 增加视频浏览量

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

## 7. 上传视频文件并保存

### 接口信息
- **URL**: `POST /api/document/docVideo/upload`
- **功能**: 上传视频文件并保存视频信息

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `MultipartFile` | 是 | 视频文件 |
| `docVideo` | `object` | 是 | 视频信息对象 |

### 请求体（docVideo）

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

## 8. 修改视频

### 接口信息
- **URL**: `PUT /api/document/docVideo`
- **功能**: 修改视频

### 请求体

```json
{
  "id": "1",
  "videoTitle": "修改后的标题",
  "fileContent": "修改后的描述",
  "broadCode": "life",
  "tags": "tag3,tag4"
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

## 11. 保存草稿

### 接口信息
- **URL**: `POST /api/document/docVideo/draft`
- **功能**: 保存视频草稿

### 请求体

```json
{
  "userId": "1",
  "videoTitle": "草稿标题",
  "fileContent": "草稿描述",
  "broadCode": "tech",
  "tags": "tag1,tag2"
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

## 12. 发布草稿

### 接口信息
- **URL**: `PUT /api/document/docVideo/draft/publish/{id}`
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

## 13. 批量更新视频状态

### 接口信息
- **URL**: `PUT /api/document/docVideo/status/batch`
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

## 14. 切换视频置顶状态

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
| `pinned` | `number` | 是 | 置顶状态（0否，1是） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 15. 切换视频推荐状态

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
| `recommended` | `number` | 是 | 推荐状态（0否，1是） |

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
