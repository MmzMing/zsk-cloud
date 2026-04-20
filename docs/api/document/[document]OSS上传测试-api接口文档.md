# [document] OSS上传测试 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/oss/test/upload` | POST | `DocOssTestController.java` | 测试文件上传 |

---

## 1. 测试文件上传

### 接口信息
- **URL**: `POST /api/document/oss/test/upload`
- **功能**: 测试OSS文件上传功能

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `MultipartFile` | 是 | 待上传的测试文件 |

### 成功响应

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": "https://document-bucket.oss-cn-hangzhou.aliyuncs.com/test/abc123.jpg"
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `data` | `string` | 上传后的文件访问URL |

### 失败响应

```json
{
  "code": 500,
  "msg": "上传失败：连接超时",
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
  "data": ""
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
| `data` | `string/null` | 响应数据（成功时为文件URL） |
