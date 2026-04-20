# [document] 文档分类标签 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/note/category/list` | GET | `DocNoteCategoryController.java` | 获取文档分类列表 |
| `/api/document/note/tag/list` | GET | `DocNoteCategoryController.java` | 获取文档标签列表 |

---

## 1. 获取文档分类列表

### 接口信息
- **URL**: `GET /api/document/note/category/list`
- **功能**: 获取文档分类树形结构列表（从字典服务获取）

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
      "name": "技术",
      "children": [
        {
          "id": "tech-java",
          "name": "Java",
          "children": []
        },
        {
          "id": "tech-frontend",
          "name": "前端",
          "children": []
        }
      ]
    },
    {
      "id": "life",
      "name": "生活",
      "children": [
        {
          "id": "life-travel",
          "name": "旅行",
          "children": []
        }
      ]
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 分类编码（字典值） |
| `name` | `string` | 分类名称（字典标签） |
| `children` | `array` | 子分类列表 |

---

## 2. 获取文档标签列表

### 接口信息
- **URL**: `GET /api/document/note/tag/list`
- **功能**: 获取文档标签列表（从字典服务获取）

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
      "label": "Spring Boot",
      "value": "spring-boot"
    },
    {
      "label": "Vue",
      "value": "vue"
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
