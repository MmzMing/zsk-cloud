# [document] 视频分类标签 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/video/category/list` | GET | `DocVideoCategoryController.java` | 获取视频分类列表 |
| `/api/document/video/category/tag/list` | GET | `DocVideoCategoryController.java` | 获取视频标签列表 |

---

## 1. 获取视频分类列表

### 接口信息
- **URL**: `GET /api/document/video/category/list`
- **功能**: 获取视频分类列表（从Redis缓存获取）

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "tech",
      "name": "科技",
      "icon": "icon-tech",
      "children": [
        {
          "id": "tech-programming",
          "name": "编程",
          "icon": "icon-code"
        },
        {
          "id": "tech-review",
          "name": "评测",
          "icon": "icon-review"
        }
      ]
    },
    {
      "id": "life",
      "name": "生活",
      "icon": "icon-life",
      "children": []
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 分类编码 |
| `name` | `string` | 分类名称 |
| `icon` | `string` | 图标标识 |
| `children` | `array` | 子分类列表 |

---

## 2. 获取视频标签列表

### 接口信息
- **URL**: `GET /api/document/video/category/tag/list`
- **功能**: 获取视频标签列表（从Redis缓存获取）

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "label": "Java",
      "value": "java"
    },
    {
      "label": "Python",
      "value": "python"
    },
    {
      "label": "前端",
      "value": "frontend"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `label` | `string` | 标签显示名称 |
| `value` | `string` | 标签值 |

---

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": []
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
| `data` | `array` | 响应数据 |
