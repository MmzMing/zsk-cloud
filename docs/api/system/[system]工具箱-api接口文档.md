# 工具箱 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/toolbox/{id} | GET | ToolboxController.java | 获取工具详情 |
| /api/system/toolbox/list | GET | ToolboxController.java | 获取工具列表 |

## 2. 接口详情

### 2.1 获取工具列表

**路径**: `GET /api/system/toolbox/list`

**功能描述**: 获取工具列表

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "title": "JSON格式化工具",
      "description": "在线JSON格式化和校验工具",
      "logo": "https://example.com/json.png",
      "tags": ["工具", "JSON"],
      "url": "/toolbox/json",
      "images": [],
      "features": ["格式化", "校验", "压缩"],
      "relatedTools": [],
      "createAt": "2026-02-15",
      "stats": {
        "views": 1000,
        "likes": 50,
        "usage": 2000
      },
      "author": {
        "name": "admin",
        "avatar": "https://example.com/avatar.jpg"
      }
    }
  ]
}
```

---

### 2.2 获取工具详情

**路径**: `GET /api/system/toolbox/{id}`

**功能描述**: 获取指定工具的详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 工具ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "title": "JSON格式化工具",
    "description": "在线JSON格式化和校验工具",
    "logo": "https://example.com/json.png",
    "tags": ["工具", "JSON"],
    "url": "/toolbox/json",
    "images": ["https://example.com/json1.png"],
    "features": ["格式化", "校验", "压缩", "高亮"],
    "relatedTools": [],
    "createAt": "2026-02-15",
    "stats": {
      "views": 1000,
      "likes": 50,
      "usage": 2000
    },
    "author": {
      "name": "admin",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "工具不存在",
  "data": null
}
```