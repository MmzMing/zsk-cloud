# [document] 文档上传任务 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/note/upload/task/list` | GET | `DocNoteUploadController.java` | 获取上传任务列表 |
| `/api/document/note/upload/task/{ids}` | DELETE | `DocNoteUploadController.java` | 删除上传任务 |
| `/api/document/note/upload/task/{id}/retry` | POST | `DocNoteUploadController.java` | 重试上传任务 |

---

## 1. 获取上传任务列表

### 接口信息
- **URL**: `GET /api/document/note/upload/task/list`
- **功能**: 获取上传任务列表

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": []
}
```

### 说明
当前版本返回空列表，后续可实现上传任务管理功能。

---

## 2. 删除上传任务

### 接口信息
- **URL**: `DELETE /api/document/note/upload/task/{ids}`
- **功能**: 删除上传任务

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 任务ID列表，逗号分隔 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 说明
当前版本直接返回成功，后续可实现真实的任务删除逻辑。

---

## 3. 重试上传任务

### 接口信息
- **URL**: `POST /api/document/note/upload/task/{id}/retry`
- **功能**: 重试失败的上传任务

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 任务ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 说明
当前版本直接返回成功，后续可实现真实的任务重试逻辑。

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
| `data` | `array/null` | 响应数据 |
