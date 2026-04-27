# [document] 视频合集管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docVideo/collection/list` | GET | `DocVideoCollectionController.java` | 查询合集列表 |
| `/api/document/docVideo/collection/page` | GET | `DocVideoCollectionController.java` | 分页查询合集列表 |
| `/api/document/docVideo/collection/{id}` | GET | `DocVideoCollectionController.java` | 获取合集详情 |
| `/api/document/docVideo/collection` | POST | `DocVideoCollectionController.java` | 新增合集 |
| `/api/document/docVideo/collection` | PUT | `DocVideoCollectionController.java` | 修改合集 |
| `/api/document/docVideo/collection/{ids}` | DELETE | `DocVideoCollectionController.java` | 删除合集 |
| `/api/document/docVideo/collection/{collectionId}/videos` | GET | `DocVideoCollectionController.java` | 获取合集内视频列表 |
| `/api/document/docVideo/collection/{collectionId}/videos` | POST | `DocVideoCollectionController.java` | 添加视频到合集 |
| `/api/document/docVideo/collection/{collectionId}/videos/{videoId}` | DELETE | `DocVideoCollectionController.java` | 从合集中移除视频 |
| `/api/document/docVideo/collection/{collectionId}/videos/order` | PUT | `DocVideoCollectionController.java` | 更新合集内视频排序 |

---

## 1. 查询合集列表

### 接口信息
- **URL**: `GET /api/document/docVideo/collection/list`
- **功能**: 查询当前登录用户的视频合集列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionName` | `string` | 否 | 合集名称（支持模糊查询） |
| `status` | `number` | 否 | 状态（1-正常，2-下架） |
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
      "collectionName": "Java学习合集",
      "description": "Java从入门到精通系列视频",
      "coverImage": "https://example.com/cover.jpg",
      "status": 1,
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
| `id` | `string` | 合集ID |
| `userId` | `string` | 用户ID |
| `collectionName` | `string` | 合集名称 |
| `description` | `string` | 合集描述 |
| `coverImage` | `string` | 封面图片URL |
| `status` | `number` | 状态（1-正常，2-下架） |
| `deleted` | `number` | 删除标记（0-未删除，1-已删除） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询合集列表

### 接口信息
- **URL**: `GET /api/document/docVideo/collection/page`
- **功能**: 分页查询当前登录用户的视频合集列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `collectionName` | `string` | 否 | - | 合集名称 |
| `status` | `number` | 否 | - | 状态 |
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
        "collectionName": "Java学习合集",
        "description": "Java从入门到精通系列视频",
        "coverImage": "https://example.com/cover.jpg",
        "status": 1,
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

## 3. 获取合集详情

### 接口信息
- **URL**: `GET /api/document/docVideo/collection/{id}`
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
    "userId": "1",
    "collectionName": "Java学习合集",
    "description": "Java从入门到精通系列视频",
    "coverImage": "https://example.com/cover.jpg",
    "status": 1,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00",
    "videos": [
      {
        "id": "1",
        "userId": "1",
        "user": {
          "id": "1",
          "name": "作者昵称",
          "avatar": "https://example.com/avatar.jpg"
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
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 合集ID |
| `userId` | `string` | 用户ID |
| `collectionName` | `string` | 合集名称 |
| `description` | `string` | 合集描述 |
| `coverImage` | `string` | 封面图片URL |
| `status` | `number` | 状态 |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |
| `videos` | `array` | 合集内视频列表（结构同视频列表响应） |

---

## 4. 新增合集

### 接口信息
- **URL**: `POST /api/document/docVideo/collection`
- **功能**: 新增视频合集

### 请求体

```json
{
  "userId": "1",
  "collectionName": "Java学习合集",
  "description": "Java从入门到精通系列视频",
  "coverImage": "https://example.com/cover.jpg",
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 是 | 用户ID |
| `collectionName` | `string` | 是 | 合集名称 |
| `description` | `string` | 否 | 合集描述 |
| `coverImage` | `string` | 否 | 封面图片URL |
| `status` | `number` | 否 | 状态（默认1） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改合集

### 接口信息
- **URL**: `PUT /api/document/docVideo/collection`
- **功能**: 修改视频合集

### 请求体

```json
{
  "id": "1",
  "collectionName": "修改后的合集名称",
  "description": "修改后的描述",
  "coverImage": "https://example.com/newcover.jpg",
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 合集ID |
| `collectionName` | `string` | 否 | 合集名称 |
| `description` | `string` | 否 | 合集描述 |
| `coverImage` | `string` | 否 | 封面图片URL |
| `status` | `number` | 否 | 状态 |

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
- **URL**: `DELETE /api/document/docVideo/collection/{ids}`
- **功能**: 批量删除视频合集

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 合集ID列表，逗号分隔 |

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
- **URL**: `GET /api/document/docVideo/collection/{collectionId}/videos`
- **功能**: 获取指定合集内的视频列表

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionId` | `string` | 是 | 合集ID |

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
      "thumbnail": {
        "fileId": "2",
        "fileUrl": "https://example.com/cover1.jpg"
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

---

## 8. 添加视频到合集

### 接口信息
- **URL**: `POST /api/document/docVideo/collection/{collectionId}/videos`
- **功能**: 将视频添加到指定合集

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionId` | `string` | 是 | 合集ID |

### 请求体

```json
{
  "videoId": "1",
  "sortOrder": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |
| `sortOrder` | `number` | 否 | 排序顺序（默认0） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 9. 从合集中移除视频

### 接口信息
- **URL**: `DELETE /api/document/docVideo/collection/{collectionId}/videos/{videoId}`
- **功能**: 从指定合集中移除视频

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionId` | `string` | 是 | 合集ID |
| `videoId` | `string` | 是 | 视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 10. 更新合集内视频排序

### 接口信息
- **URL**: `PUT /api/document/docVideo/collection/{collectionId}/videos/order`
- **功能**: 更新合集内视频的排序顺序

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `collectionId` | `string` | 是 | 合集ID |

### 请求体

```json
{
  "videoOrders": [
    {
      "videoId": "1",
      "sortOrder": 1
    },
    {
      "videoId": "2",
      "sortOrder": 2
    }
  ]
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoOrders` | `array` | 是 | 视频排序列表 |
| `videoOrders[].videoId` | `string` | 是 | 视频ID |
| `videoOrders[].sortOrder` | `number` | 是 | 排序顺序 |

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
| `data` | `object/array/boolean/null` | 响应数据 |
