# [document] 文件处理任务管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/process/list` | GET | `DocProcessController.java` | 查询文件处理任务列表 |
| `/api/document/process/page` | GET | `DocProcessController.java` | 分页查询文件处理任务列表 |
| `/api/document/process/{id}` | GET | `DocProcessController.java` | 获取文件处理任务详细信息 |
| `/api/document/process` | POST | `DocProcessController.java` | 新增文件处理任务 |
| `/api/document/process` | PUT | `DocProcessController.java` | 修改文件处理任务 |
| `/api/document/process/{ids}` | DELETE | `DocProcessController.java` | 删除文件处理任务 |

---

## 1. 查询文件处理任务列表

### 接口信息
- **URL**: `GET /api/document/process/list`
- **功能**: 查询文件处理任务列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileId` | `string` | 否 | 文件ID |
| `processType` | `string` | 否 | 处理类型 |
| `status` | `number` | 否 | 状态（0待处理，1处理中，2已完成，3失败） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "fileId": "file-abc123",
      "processType": "thumbnail",
      "status": 2,
      "progress": 100,
      "errorMsg": null,
      "createTime": "2026-02-15 10:30:00",
      "updateTime": "2026-02-15 10:31:00"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 任务ID（后端使用Jackson转为string类型） |
| `fileId` | `string` | 文件ID |
| `processType` | `string` | 处理类型（如thumbnail缩略图，transcode转码） |
| `status` | `number` | 状态（0待处理，1处理中，2已完成，3失败） |
| `progress` | `number` | 处理进度（0-100） |
| `errorMsg` | `string` | 错误信息（失败时显示） |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询文件处理任务列表

### 接口信息
- **URL**: `GET /api/document/process/page`
- **功能**: 分页查询文件处理任务列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `fileId` | `string` | 否 | - | 文件ID |
| `processType` | `string` | 否 | - | 处理类型 |
| `status` | `number` | 否 | - | 状态 |
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
        "processType": "thumbnail",
        "status": 2,
        "progress": 100,
        "errorMsg": null,
        "createTime": "2026-02-15 10:30:00",
        "updateTime": "2026-02-15 10:31:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 3. 获取文件处理任务详细信息

### 接口信息
- **URL**: `GET /api/document/process/{id}`
- **功能**: 获取文件处理任务详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 任务ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "fileId": "file-abc123",
    "processType": "thumbnail",
    "status": 2,
    "progress": 100,
    "errorMsg": null,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:31:00"
  }
}
```

---

## 4. 新增文件处理任务

### 接口信息
- **URL**: `POST /api/document/process`
- **功能**: 新增文件处理任务

### 请求体

```json
{
  "fileId": "file-abc123",
  "processType": "thumbnail",
  "status": 0,
  "progress": 0
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `fileId` | `string` | 是 | 文件ID |
| `processType` | `string` | 是 | 处理类型 |
| `status` | `number` | 否 | 状态（默认0待处理） |
| `progress` | `number` | 否 | 处理进度（默认0） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改文件处理任务

### 接口信息
- **URL**: `PUT /api/document/process`
- **功能**: 修改文件处理任务

### 请求体

```json
{
  "id": "1",
  "status": 2,
  "progress": 100
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 任务ID |
| `status` | `number` | 否 | 状态 |
| `progress` | `number` | 否 | 处理进度 |
| `errorMsg` | `string` | 否 | 错误信息 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除文件处理任务

### 接口信息
- **URL**: `DELETE /api/document/process/{ids}`
- **功能**: 批量删除文件处理任务

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 任务ID列表，逗号分隔 |

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
| `data` | `object/array/boolean` | 响应数据 |
