# 简历 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/resume/detail | GET | ResumeController.java | 获取简历详情 |
| /api/system/resume/save | POST | ResumeController.java | 保存简历 |

## 2. 接口详情

### 2.1 获取简历详情

**路径**: `GET /api/system/resume/detail`

**功能描述**: 获取简历详情，包含多个模块

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "basic",
      "type": "basic",
      "title": "基本信息",
      "icon": "user",
      "isDeletable": false,
      "isVisible": true,
      "data": {
        "name": "张三",
        "jobIntention": "高级Java工程师",
        "age": 30,
        "gender": "男",
        "city": "北京",
        "phone": "13800138000",
        "email": "zhangsan@example.com",
        "github": "https://github.com/zhangsan",
        "summary": "5年Java开发经验...",
        "avatar": "https://example.com/avatar.jpg",
        "experience": "5年",
        "salary": "30K-40K",
        "politics": "党员",
        "status": "在职"
      },
      "content": ""
    },
    {
      "id": "experience",
      "type": "experience",
      "title": "工作经历",
      "icon": "briefcase",
      "isDeletable": false,
      "isVisible": true,
      "data": null,
      "content": "<p>2021-至今 某互联网公司 高级工程师</p>"
    }
  ]
}
```

---

### 2.2 保存简历

**路径**: `POST /api/system/resume/save`

**功能描述**: 保存简历信息

**请求体**:

```json
[
  {
    "id": "basic",
    "type": "basic",
    "title": "基本信息",
    "icon": "user",
    "isDeletable": false,
    "isVisible": true,
    "content": "",
    "data": {
      "name": "张三",
      "jobIntention": "高级Java工程师"
    }
  }
]
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```