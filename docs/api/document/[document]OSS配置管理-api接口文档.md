# [document] OSS配置管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/oss/config` | GET | `DocOssConfigController.java` | 获取当前OSS配置 |
| `/api/document/oss/config` | POST | `DocOssConfigController.java` | 更新OSS配置 |

---

## 1. 获取当前OSS配置

### 接口信息
- **URL**: `GET /api/document/oss/config`
- **功能**: 获取当前OSS配置信息

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "endpoint": "oss-cn-hangzhou.aliyuncs.com",
    "accessKey": "LTAI4G...",
    "secretKey": "******",
    "bucketName": "document-bucket",
    "region": "cn-hangzhou",
    "pathStyleAccess": false,
    "customDomain": "https://docs.example.com"
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `endpoint` | `string` | OSS服务端点 |
| `accessKey` | `string` | AccessKey（部分脱敏） |
| `secretKey` | `string` | SecretKey（完全脱敏） |
| `bucketName` | `string` | 存储桶名称 |
| `region` | `string` | 区域 |
| `pathStyleAccess` | `boolean` | 是否使用路径样式访问 |
| `customDomain` | `string` | 自定义域名 |

---

## 2. 更新OSS配置

### 接口信息
- **URL**: `POST /api/document/oss/config`
- **功能**: 更新OSS配置（动态刷新）

### 请求体

```json
{
  "endpoint": "oss-cn-shanghai.aliyuncs.com",
  "accessKey": "LTAI4G...",
  "secretKey": "your-secret-key",
  "bucketName": "new-bucket",
  "region": "cn-shanghai",
  "pathStyleAccess": false,
  "customDomain": "https://new-docs.example.com"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `endpoint` | `string` | 是 | OSS服务端点 |
| `accessKey` | `string` | 是 | AccessKey |
| `secretKey` | `string` | 是 | SecretKey |
| `bucketName` | `string` | 是 | 存储桶名称 |
| `region` | `string` | 否 | 区域 |
| `pathStyleAccess` | `boolean` | 否 | 是否使用路径样式访问 |
| `customDomain` | `string` | 否 | 自定义域名 |

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
| `data` | `object/null` | 响应数据 |
