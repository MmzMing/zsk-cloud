# [document] 文件管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/files/page` | GET | `DocFilesController.java` | 分页查询文件列表 |
| `/api/document/files/upload` | POST | `DocFilesController.java` | 上传文件 |
| `/api/document/files/{ids}` | DELETE | `DocFilesController.java` | 删除文件 |
| `/api/document/files/multipart/init` | POST | `DocFilesController.java` | 初始化分片上传 |
| `/api/document/files/multipart/upload` | POST | `DocFilesController.java` | 上传分片 |
| `/api/document/files/multipart/complete` | POST | `DocFilesController.java` | 完成分片上传 |

---

## 1. 分页查询文件列表

### 接口信息
- **URL**: `GET /api/document/files/page`
- **功能**: 分页查询文件列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `fileId` | `string` | 否 | - | 文件ID |
| `fileName` | `string` | 否 | - | 文件名（支持模糊查询） |
| `fileType` | `string` | 否 | - | 文件类型 |
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
        "fileId": "file-abc123",
        "fileName": "example.jpg",
        "fileType": "jpg",
        "fileSize": 102400,
        "url": "https://example.com/files/file-abc123.jpg",
        "bucketName": "document",
        "objectKey": "files/file-abc123.jpg",
        "status": 1,
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

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 记录ID（后端使用Jackson转为string类型） |
| `fileId` | `string` | 文件唯一标识 |
| `fileName` | `string` | 文件名 |
| `fileType` | `string` | 文件类型（如jpg、png、mp4等） |
| `fileSize` | `number` | 文件大小（字节） |
| `url` | `string` | 文件访问URL |
| `bucketName` | `string` | OSS存储桶名称 |
| `objectKey` | `string` | OSS对象键 |
| `status` | `number` | 状态（0禁用，1启用） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 上传文件

### 接口信息
- **URL**: `POST /api/document/files/upload`
- **功能**: 上传文件到OSS存储

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `MultipartFile` | 是 | 待上传的文件 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "fileId": "file-abc123",
    "fileName": "example.jpg",
    "fileType": "jpg",
    "fileSize": 102400,
    "url": "https://example.com/files/file-abc123.jpg",
    "bucketName": "document",
    "objectKey": "files/file-abc123.jpg",
    "status": 1,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 3. 删除文件

### 接口信息
- **URL**: `DELETE /api/document/files/{ids}`
- **功能**: 删除文件（同时删除OSS文件和数据库记录）

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 文件ID列表，逗号分隔 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 4. 初始化分片上传

### 接口信息
- **URL**: `POST /api/document/files/multipart/init`
- **功能**: 初始化分片上传，返回uploadId用于后续操作

### 请求体

```json
{
  "fileName": "large-file.zip",
  "fileType": "zip",
  "fileSize": 104857600,
  "md5": "abc123def456"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileName` | `string` | 是 | 文件名 |
| `fileType` | `string` | 是 | 文件类型 |
| `fileSize` | `number` | 是 | 文件大小（字节） |
| `md5` | `string` | 否 | 文件MD5校验值 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": "upload-abc123-def456"
}
```

### 响应说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `data` | `string` | 分片上传ID（uploadId） |

---

## 5. 上传分片

### 接口信息
- **URL**: `POST /api/document/files/multipart/upload`
- **功能**: 上传单个分片

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `uploadId` | `string` | 是 | 分片上传ID |
| `partNumber` | `number` | 是 | 分片序号（从1开始） |
| `file` | `MultipartFile` | 是 | 分片文件 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": "etag-abc123"
}
```

### 响应说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `data` | `string` | 分片ETag值（用于后续合并） |

---

## 6. 完成分片上传

### 接口信息
- **URL**: `POST /api/document/files/multipart/complete`
- **功能**: 完成分片上传，合并所有分片

### 请求体

```json
{
  "uploadId": "upload-abc123-def456",
  "parts": [
    {
      "partNumber": 1,
      "eTag": "etag-001"
    },
    {
      "partNumber": 2,
      "eTag": "etag-002"
    }
  ]
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `uploadId` | `string` | 是 | 分片上传ID |
| `parts` | `array` | 是 | 分片列表 |
| `parts[].partNumber` | `number` | 是 | 分片序号 |
| `parts[].eTag` | `string` | 是 | 分片ETag值 |

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
| `data` | `object/array/boolean/string` | 响应数据 |
