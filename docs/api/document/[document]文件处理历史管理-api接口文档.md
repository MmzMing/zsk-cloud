# [document] 文件处理历史管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/process/history/list` | GET | `DocProcessHistoryController.java` | 查询文件处理历史列表 |
| `/api/document/process/history/page` | GET | `DocProcessHistoryController.java` | 分页查询文件处理历史列表 |
| `/api/document/process/history/{id}` | GET | `DocProcessHistoryController.java` | 获取文件处理历史详细信息 |
| `/api/document/process/history` | POST | `DocProcessHistoryController.java` | 新增文件处理历史 |
| `/api/document/process/history` | PUT | `DocProcessHistoryController.java` | 修改文件处理历史 |
| `/api/document/process/history/{ids}` | DELETE | `DocProcessHistoryController.java` | 删除文件处理历史 |

---

## 1. 查询文件处理历史列表

### 接口信息
- **URL**: `GET /api/document/process/history/list`
- **功能**: 查询文件处理历史列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `processId` | `string` | 否 | 处理任务ID |
| `fileId` | `string` | 否 | 文件ID |
| `operation` | `string` | 否 | 操作类型 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "processId": "1",
      "fileId": "file-abc123",
      "operation": "start",
      "operationDesc": "开始处理",
      "operator": "system",
      "createTime": "2026-02-15 10:30:00"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 历史记录ID（后端使用Jackson转为string类型） |
| `processId` | `string` | 关联的处理任务ID |
| `fileId` | `string` | 文件ID |
| `operation` | `string` | 操作类型（start开始，progress进度更新，complete完成，error错误） |
| `operationDesc` | `string` | 操作描述 |
| `operator` | `string` | 操作人 |
| `createTime` | `string` | 创建时间 |

---

## 2. 分页查询文件处理历史列表

### 接口信息
- **URL**: `GET /api/document/process/history/page`
- **功能**: 分页查询文件处理历史列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `processId` | `string` | 否 | - | 处理任务ID |
| `fileId` | `string` | 否 | - | 文件ID |
| `operation` | `string` | 否 | - | 操作类型 |
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
        "processId": "1",
        "fileId": "file-abc123",
        "operation": "start",
        "operationDesc": "开始处理",
        "operator": "system",
        "createTime": "2026-02-15 10:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 3. 获取文件处理历史详细信息

### 接口信息
- **URL**: `GET /api/document/process/history/{id}`
- **功能**: 获取文件处理历史详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 历史记录ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "processId": "1",
    "fileId": "file-abc123",
    "operation": "start",
    "operationDesc": "开始处理",
    "operator": "system",
    "createTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增文件处理历史

### 接口信息
- **URL**: `POST /api/document/process/history`
- **功能**: 新增文件处理历史记录

### 请求体

```json
{
  "processId": "1",
  "fileId": "file-abc123",
  "operation": "complete",
  "operationDesc": "处理完成",
  "operator": "system"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `processId` | `string` | 是 | 处理任务ID |
| `fileId` | `string` | 是 | 文件ID |
| `operation` | `string` | 是 | 操作类型 |
| `operationDesc` | `string` | 是 | 操作描述 |
| `operator` | `string` | 否 | 操作人（默认system） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改文件处理历史

### 接口信息
- **URL**: `PUT /api/document/process/history`
- **功能**: 修改文件处理历史记录

### 请求体

```json
{
  "id": "1",
  "operationDesc": "更新后的描述"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 历史记录ID |
| `operationDesc` | `string` | 是 | 操作描述 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除文件处理历史

### 接口信息
- **URL**: `DELETE /api/document/process/history/{ids}`
- **功能**: 批量删除文件处理历史

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 历史记录ID列表，逗号分隔 |

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
