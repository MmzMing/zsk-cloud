---
name: "api-spec"
description: "后端API接口文档规范生成器。当用户需要创建或更新API接口文档时调用，自动生成标准化的接口文档模板。"
---

# 后端API接口文档规范

## 一、文档结构

每个API接口文档遵循以下结构：

# [模块名] API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/服务名/资源名/ | GET | UserController.java | 查询用户列表 |

## 2. 接口详情

### 2.1 查询用户列表

**路径**: `GET /api/auth/users`

**功能描述**: 查询用户列表，支持分页和条件筛选

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |
| Content-Type | string | 否 | 请求体类型，默认 `application/json` |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 页码，默认0 |
| size | int | 否 | 每页大小，默认10 |
| keyword | string | 否 | 搜索关键词 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "参数错误",
  "data": null
}
```
```

## 二、命名规范

### 2.1 文件命名
- **强制**: 一个Controller类对应一个API文档文件
- **格式**: `[模块名]-控制器名-api接口文档.md`
- **示例**: `[auth]认证管理-api接口文档.md`, `[system]用户管理-api接口文档.md`

### 2.2 路径命名
- **强制**: 使用复数名词，小写，用短横线分隔
- **强制**: 认证模块路径必须以 `/api/auth` 开头
- **正确**: `/api/auth/users`, `/api/auth/login`, `/api/auth/captcha`
- **正确**: `/api/服务名/资源名/users`, `/api/服务名/资源名/orders`
- **错误**: `/api/user`, `/api/getUsers`, `/login`（缺少/api/auth前缀）

### 2.3 HTTP方法
| 方法 | 用途 |
|------|------|
| GET | 查询资源 |
| POST | 创建资源 |
| PUT | 全量更新资源 |
| DELETE | 删除资源 |

## 三、请求头规范

### 3.1 通用请求头

| 请求头 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，用于身份认证 |
| Content-Type | string | 否 | 请求体类型，常见值: `application/json`, `application/x-www-form-urlencoded`, `multipart/form-data` |
| Accept | string | 否 | 期望的响应类型，默认 `application/json` |
| X-Request-Id | string | 否 | 请求唯一标识，用于链路追踪 |
| X-Tenant-Id | string | 否 | 租户ID，多租户场景使用 |

### 3.2 认证请求头

- **Bearer Token 格式**: `Bearer {token}`
- **示例**: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### 3.3 注意事项

- 所有需要认证的接口必须携带 `Authorization` 请求头
- 文件上传接口应使用 `Content-Type: multipart/form-data`
- 建议所有请求携带 `X-Request-Id` 便于问题追踪

## 四、响应规范

### 4.1 统一响应格式

```json
{
  "code": 0,
  "msg": "success",
  "data": {}
}
```

### 4.2 状态码定义

| 状态码 | 含义 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 五、文档示例

### 参考模板

docs\skill\api-skill\templates\[system]用户详情详情-api接口文档.md

### 输出示例

生成的文档文件: `[system]用户详情详情-api接口文档.md`


