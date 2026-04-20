# [document] 笔记图片管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/note/pic/list` | GET | `DocNotePicController.java` | 查询笔记图片列表 |
| `/api/document/note/pic/page` | GET | `DocNotePicController.java` | 分页查询笔记图片列表 |
| `/api/document/note/pic/{id}` | GET | `DocNotePicController.java` | 获取笔记图片详细信息 |
| `/api/document/note/pic` | POST | `DocNotePicController.java` | 新增笔记图片 |
| `/api/document/note/pic` | PUT | `DocNotePicController.java` | 修改笔记图片 |
| `/api/document/note/pic/{ids}` | DELETE | `DocNotePicController.java` | 删除笔记图片 |

---

## 1. 查询笔记图片列表

### 接口信息
- **URL**: `GET /api/document/note/pic/list`
- **功能**: 查询笔记图片列表

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 否 | 笔记ID |
| `picUrl` | `string` | 否 | 图片URL（支持模糊查询） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "noteId": "10",
      "picUrl": "https://example.com/image1.jpg",
      "picSort": 1,
      "createTime": "2026-02-15 10:30:00"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 图片ID（后端使用Jackson转为string类型） |
| `noteId` | `string` | 所属笔记ID |
| `picUrl` | `string` | 图片URL |
| `picSort` | `number` | 排序序号 |
| `createTime` | `string` | 创建时间 |

---

## 2. 分页查询笔记图片列表

### 接口信息
- **URL**: `GET /api/document/note/pic/page`
- **功能**: 分页查询笔记图片列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `noteId` | `string` | 否 | - | 笔记ID |
| `picUrl` | `string` | 否 | - | 图片URL |
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
        "noteId": "10",
        "picUrl": "https://example.com/image1.jpg",
        "picSort": 1,
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

## 3. 获取笔记图片详细信息

### 接口信息
- **URL**: `GET /api/document/note/pic/{id}`
- **功能**: 获取笔记图片详细信息

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 图片ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "noteId": "10",
    "picUrl": "https://example.com/image1.jpg",
    "picSort": 1,
    "createTime": "2026-02-15 10:30:00"
  }
}
```

---

## 4. 新增笔记图片

### 接口信息
- **URL**: `POST /api/document/note/pic`
- **功能**: 新增笔记图片

### 请求体

```json
{
  "noteId": "10",
  "picUrl": "https://example.com/new-image.jpg",
  "picSort": 2
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 是 | 所属笔记ID |
| `picUrl` | `string` | 是 | 图片URL |
| `picSort` | `number` | 否 | 排序序号（默认0） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 5. 修改笔记图片

### 接口信息
- **URL**: `PUT /api/document/note/pic`
- **功能**: 修改笔记图片

### 请求体

```json
{
  "id": "1",
  "picUrl": "https://example.com/updated-image.jpg",
  "picSort": 3
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 图片ID |
| `picUrl` | `string` | 否 | 图片URL |
| `picSort` | `number` | 否 | 排序序号 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 6. 删除笔记图片

### 接口信息
- **URL**: `DELETE /api/document/note/pic/{ids}`
- **功能**: 批量删除笔记图片

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 图片ID列表，逗号分隔 |

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
