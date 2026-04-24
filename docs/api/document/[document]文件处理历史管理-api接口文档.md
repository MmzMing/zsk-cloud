# [document] 文件处理历史管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docProcessHistory/list` | GET | `DocProcessHistoryController.java` | 查询文件处理历史列表 |
| `/api/document/docProcessHistory/page` | GET | `DocProcessHistoryController.java` | 分页查询文件处理历史列表 |
| `/api/document/docProcessHistory/{id}` | GET | `DocProcessHistoryController.java` | 获取文件处理历史详细信息 |
| `/api/document/docProcessHistory` | POST | `DocProcessHistoryController.java` | 新增文件处理历史 |
| `/api/document/docProcessHistory` | PUT | `DocProcessHistoryController.java` | 修改文件处理历史 |
| `/api/document/docProcessHistory/{ids}` | DELETE | `DocProcessHistoryController.java` | 删除文件处理历史 |

---

## 1. 查询文件处理历史列表

### 接口信息
- **URL**: `GET /api/document/docProcessHistory/list`
- **功能**: 查询文件处理历史列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `taskId` | `string` | 否 | 任务ID |
| `status` | `number` | 否 | 处理状态（0失败，1成功） |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "taskId": "1",
      "status": 1,
      "result": "处理成功",
      "errorMsg": "",
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
| `id` | `string` | 历史ID（后端使用Jackson转为string类型） |
| `taskId` | `string` | 任务ID |
| `status` | `number` | 处理状态（0失败，1成功） |
| `result` | `string` | 处理结果 |
| `errorMsg` | `string` | 错误信息 |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

## 2. 分页查询文件处理历史列表

### 接口信息
- **URL**: `GET /api/document/docProcessHistory/page`
- **功能**: 分页查询文件处理历史列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `taskId` | `string` | 否 | - | 任务ID |
| `status` | `number` | 否 | - | 处理状态 |
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
        "taskId": "1",
        "status": 1,
        "result": "处理成功",
        "errorMsg": "",
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

## 3. 获取文件处理历史详细信息

### 接口信息
- **URL**: `GET /api/document/docProcessHistory/{id}`
- **功能**: 获取文件处理历史详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 历史ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "taskId": "1",
    "status": 1,
    "result": "处理成功",
    "errorMsg": "",
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增文件处理历史

### 接口信息
- **URL**: `POST /api/document/docProcessHistory`
- **功能**: 新增文件处理历史

### 请求体

```json
{
  "taskId": "1",
  "status": 1,
  "result": "处理成功",
  "errorMsg": ""
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `taskId` | `string` | 是 | 任务ID |
| `status` | `number` | 是 | 处理状态 |
| `result` | `string` | 否 | 处理结果 |
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

## 5. 修改文件处理历史

### 接口信息
- **URL**: `PUT /api/document/docProcessHistory`
- **功能**: 修改文件处理历史

### 请求体

```json
{
  "id": "1",
  "status": 0,
  "errorMsg": "网络超时"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 历史ID |
| `status` | `number` | 否 | 处理状态 |
| `result` | `string` | 否 | 处理结果 |
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

## 6. 删除文件处理历史

### 接口信息
- **URL**: `DELETE /api/document/docProcessHistory/{ids}`
- **功能**: 批量删除文件处理历史

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 历史ID列表，逗号分隔 |

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
