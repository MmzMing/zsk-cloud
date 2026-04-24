# [document] OSS配置管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docOssConfig/list` | GET | `DocOssConfigController.java` | 查询OSS配置列表 |
| `/api/document/docOssConfig/page` | GET | `DocOssConfigController.java` | 分页查询OSS配置列表 |
| `/api/document/docOssConfig/{id}` | GET | `DocOssConfigController.java` | 获取OSS配置详细信息 |
| `/api/document/docOssConfig` | POST | `DocOssConfigController.java` | 新增OSS配置 |
| `/api/document/docOssConfig` | PUT | `DocOssConfigController.java` | 修改OSS配置 |
| `/api/document/docOssConfig/{ids}` | DELETE | `DocOssConfigController.java` | 删除OSS配置 |

---

## 1. 查询OSS配置列表

### 接口信息
- **URL**: `GET /api/document/docOssConfig/list`
- **功能**: 查询OSS配置列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `configName` | `string` | 否 | 配置名称（支持模糊查询） |
| `ossType` | `string` | 否 | OSS类型（aliyun/minio等） |
| `status` | `number` | 否 | 状态（0禁用，1启用） |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "configName": "阿里云OSS",
      "ossType": "aliyun",
      "endpoint": "oss-cn-hangzhou.aliyuncs.com",
      "bucketName": "my-bucket",
      "accessKey": "LTAI****************",
      "secretKey": "********************",
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
| `id` | `string` | 配置ID（后端使用Jackson转为string类型） |
| `configName` | `string` | 配置名称 |
| `ossType` | `string` | OSS类型 |
| `endpoint` | `string` | 服务端点 |
| `bucketName` | `string` | 存储桶名称 |
| `accessKey` | `string` | 访问密钥ID（脱敏显示） |
| `secretKey` | `string` | 访问密钥Secret（脱敏显示） |
| `status` | `number` | 状态（0禁用，1启用） |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询OSS配置列表

### 接口信息
- **URL**: `GET /api/document/docOssConfig/page`
- **功能**: 分页查询OSS配置列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `configName` | `string` | 否 | - | 配置名称 |
| `ossType` | `string` | 否 | - | OSS类型 |
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
        "configName": "阿里云OSS",
        "ossType": "aliyun",
        "endpoint": "oss-cn-hangzhou.aliyuncs.com",
        "bucketName": "my-bucket",
        "accessKey": "LTAI****************",
        "secretKey": "********************",
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

## 3. 获取OSS配置详细信息

### 接口信息
- **URL**: `GET /api/document/docOssConfig/{id}`
- **功能**: 获取OSS配置详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 配置ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "configName": "阿里云OSS",
    "ossType": "aliyun",
    "endpoint": "oss-cn-hangzhou.aliyuncs.com",
    "bucketName": "my-bucket",
    "accessKey": "LTAI****************",
    "secretKey": "********************",
    "status": 1,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增OSS配置

### 接口信息
- **URL**: `POST /api/document/docOssConfig`
- **功能**: 新增OSS配置

### 请求体

```json
{
  "configName": "MinIO存储",
  "ossType": "minio",
  "endpoint": "http://localhost:9000",
  "bucketName": "my-bucket",
  "accessKey": "minioadmin",
  "secretKey": "minioadmin",
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `configName` | `string` | 是 | 配置名称 |
| `ossType` | `string` | 是 | OSS类型 |
| `endpoint` | `string` | 是 | 服务端点 |
| `bucketName` | `string` | 是 | 存储桶名称 |
| `accessKey` | `string` | 是 | 访问密钥ID |
| `secretKey` | `string` | 是 | 访问密钥Secret |
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

## 5. 修改OSS配置

### 接口信息
- **URL**: `PUT /api/document/docOssConfig`
- **功能**: 修改OSS配置

### 请求体

```json
{
  "id": "1",
  "configName": "修改后的配置",
  "endpoint": "oss-cn-beijing.aliyuncs.com",
  "status": 1
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 配置ID |
| `configName` | `string` | 否 | 配置名称 |
| `ossType` | `string` | 否 | OSS类型 |
| `endpoint` | `string` | 否 | 服务端点 |
| `bucketName` | `string` | 否 | 存储桶名称 |
| `accessKey` | `string` | 否 | 访问密钥ID |
| `secretKey` | `string` | 否 | 访问密钥Secret |
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

## 6. 删除OSS配置

### 接口信息
- **URL**: `DELETE /api/document/docOssConfig/{ids}`
- **功能**: 批量删除OSS配置

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 配置ID列表，逗号分隔 |

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
