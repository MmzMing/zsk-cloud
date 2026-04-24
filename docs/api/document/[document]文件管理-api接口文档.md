# [document] 文件管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docFiles/list` | GET | `DocFilesController.java` | 查询文件列表 |
| `/api/document/docFiles/page` | GET | `DocFilesController.java` | 分页查询文件列表 |
| `/api/document/docFiles/{id}` | GET | `DocFilesController.java` | 获取文件详细信息 |
| `/api/document/docFiles` | POST | `DocFilesController.java` | 新增文件 |
| `/api/document/docFiles` | PUT | `DocFilesController.java` | 修改文件 |
| `/api/document/docFiles/{ids}` | DELETE | `DocFilesController.java` | 删除文件 |
| `/api/document/docFiles/upload` | POST | `DocFilesController.java` | 上传文件 |
| `/api/document/docFiles/download/{id}` | GET | `DocFilesController.java` | 下载文件 |

---

## 1. 查询文件列表

### 接口信息
- **URL**: `GET /api/document/docFiles/list`
- **功能**: 查询文件列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileName` | `string` | 否 | 文件名称（支持模糊查询） |
| `fileType` | `string` | 否 | 文件类型 |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "fileName": "测试文件.pdf",
      "fileType": "pdf",
      "fileSize": "1024",
      "filePath": "/uploads/test.pdf",
      "fileUrl": "https://example.com/uploads/test.pdf",
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
| `id` | `string` | 文件ID（后端使用Jackson转为string类型） |
| `fileName` | `string` | 文件名称 |
| `fileType` | `string` | 文件类型 |
| `fileSize` | `string` | 文件大小（字节） |
| `filePath` | `string` | 文件存储路径 |
| `fileUrl` | `string` | 文件访问URL |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询文件列表

### 接口信息
- **URL**: `GET /api/document/docFiles/page`
- **功能**: 分页查询文件列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `fileName` | `string` | 否 | - | 文件名称 |
| `fileType` | `string` | 否 | - | 文件类型 |
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
        "fileName": "测试文件.pdf",
        "fileType": "pdf",
        "fileSize": "1024",
        "filePath": "/uploads/test.pdf",
        "fileUrl": "https://example.com/uploads/test.pdf",
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

## 3. 获取文件详细信息

### 接口信息
- **URL**: `GET /api/document/docFiles/{id}`
- **功能**: 获取文件详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文件ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "fileName": "测试文件.pdf",
    "fileType": "pdf",
    "fileSize": "1024",
    "filePath": "/uploads/test.pdf",
    "fileUrl": "https://example.com/uploads/test.pdf",
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增文件

### 接口信息
- **URL**: `POST /api/document/docFiles`
- **功能**: 新增文件记录

### 请求体

```json
{
  "fileName": "新文件.pdf",
  "fileType": "pdf",
  "fileSize": "2048",
  "filePath": "/uploads/new.pdf",
  "fileUrl": "https://example.com/uploads/new.pdf"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileName` | `string` | 是 | 文件名称 |
| `fileType` | `string` | 是 | 文件类型 |
| `fileSize` | `string` | 是 | 文件大小（字节） |
| `filePath` | `string` | 是 | 文件存储路径 |
| `fileUrl` | `string` | 是 | 文件访问URL |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改文件

### 接口信息
- **URL**: `PUT /api/document/docFiles`
- **功能**: 修改文件信息

### 请求体

```json
{
  "id": "1",
  "fileName": "修改后的文件.pdf",
  "fileType": "pdf"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文件ID |
| `fileName` | `string` | 否 | 文件名称 |
| `fileType` | `string` | 否 | 文件类型 |
| `fileSize` | `string` | 否 | 文件大小 |
| `filePath` | `string` | 否 | 文件存储路径 |
| `fileUrl` | `string` | 否 | 文件访问URL |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除文件

### 接口信息
- **URL**: `DELETE /api/document/docFiles/{ids}`
- **功能**: 批量删除文件

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

## 7. 上传文件

### 接口信息
- **URL**: `POST /api/document/docFiles/upload`
- **功能**: 上传文件到服务器

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file` | `MultipartFile` | 是 | 文件对象 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "fileName": "上传文件.pdf",
    "fileType": "pdf",
    "fileSize": "1024",
    "filePath": "/uploads/upload-file.pdf",
    "fileUrl": "https://example.com/uploads/upload-file.pdf"
  }
}
```

---

## 8. 下载文件

### 接口信息
- **URL**: `GET /api/document/docFiles/download/{id}`
- **功能**: 下载指定文件

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 文件ID |

### 成功响应

文件流下载，响应头包含 `Content-Disposition: attachment; filename="文件名"`

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
