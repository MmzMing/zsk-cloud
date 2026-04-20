# About页面 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/about/skill | GET | AboutController.java | 获取技术栈列表 |
| /api/system/about/faq | GET | AboutController.java | 获取FAQ列表 |

## 2. 接口详情

### 2.1 获取技术栈列表

**路径**: `GET /api/system/about/skill`

**功能描述**: 获取技术栈列表

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "name": "Java",
      "category": "backend",
      "level": "expert",
      "years": 5,
      "icon": "java"
    },
    {
      "id": "2",
      "name": "Spring Boot",
      "category": "backend",
      "level": "expert",
      "years": 4,
      "icon": "spring"
    }
  ]
}
```

---

### 2.2 获取FAQ列表

**路径**: `GET /api/system/about/faq`

**功能描述**: 获取常见问题列表，按分类分组

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "category": "技术问题",
      "questions": [
        {
          "id": "1-1",
          "question": "如何部署项目？",
          "answer": "请参考文档中的部署指南..."
        }
      ]
    },
    {
      "id": "2",
      "category": "使用问题",
      "questions": [
        {
          "id": "2-1",
          "question": "如何重置密码？",
          "answer": "点击登录页面的忘记密码链接..."
        }
      ]
    }
  ]
}
```