# [document] OSS上传测试 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docOssTest/upload` | POST | `DocOssTestController.java` | 测试OSS文件上传 |
| `/api/document/docOssTest/download/{fileName}` | GET | `DocOssTestController.java` | 测试OSS文件下载 |
| `/api/document/docOssTest/delete/{fileName}` | DELETE | `DocOssTestController.java` | 测试OSS文件删除 |
| `/api/document/docOssTest/url/{fileName}` | GET | `DocOssTestController.java` | 获取OSS文件访问URL |

---

## 1. 测试OSS文件上传

### 接口信息
- **URL**: `POST /api/document/docOssTest/upload`
- **功能**: 测试OSS文件上传功能

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `MultipartFile` | 是 | 要上传的文件 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "fileName": "test-file.pdf",
    "fileUrl": "https://example-bucket.oss-cn-hangzhou.aliyuncs.com/test-file.pdf",
    "fileSize": "1024"
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `fileName` | `string` | 上传后的文件名 |
| `fileUrl` | `string` | 文件访问URL |
| `fileSize` | `string` | 文件大小（字节） |

---

## 2. 测试OSS文件下载

### 接口信息
- **URL**: `GET /api/document/docOssTest/download/{fileName}`
- **功能**: 测试OSS文件下载功能

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileName` | `string` | 是 | 要下载的文件名 |

### 成功响应

文件流下载，响应头包含 `Content-Disposition: attachment; filename="文件名"`

---

## 3. 测试OSS文件删除

### 接口信息
- **URL**: `DELETE /api/document/docOssTest/delete/{fileName}`
- **功能**: 测试OSS文件删除功能

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileName` | `string` | 是 | 要删除的文件名 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 4. 获取OSS文件访问URL

### 接口信息
- **URL**: `GET /api/document/docOssTest/url/{fileName}`
- **功能**: 获取OSS文件的临时访问URL

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileName` | `string` | 是 | 文件名 |

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `expireTime` | `number` | 否 | 3600 | URL过期时间（秒） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": "https://example-bucket.oss-cn-hangzhou.aliyuncs.com/test-file.pdf?Expires=1234567890&OSSAccessKeyId=..."
}
```

### 响应说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `data` | `string` | 带签名的临时访问URL |

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
| `data` | `object/string/boolean/null` | 响应数据 |
