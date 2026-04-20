# [auth]认证管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/auth/register | POST | AuthController.java | 用户注册 |
| /api/auth/login | POST | AuthController.java | 用户登录 |
| /api/auth/refresh | POST | AuthController.java | 刷新令牌 |
| /api/auth/logout | POST | AuthController.java | 退出登录 |
| /api/auth/captcha | GET | AuthController.java | 获取滑块拼图验证码 |
| /api/auth/captcha/check | POST | AuthController.java | 校验滑块验证码 |
| /api/auth/public-key | GET | AuthController.java | 获取RSA公钥 |
| /api/auth/email/code | POST | AuthController.java | 发送邮箱验证码 |
| /api/auth/third-party/url | GET | AuthController.java | 获取第三方登录授权URL |
| /api/auth/third-party/callback | POST | AuthController.java | 第三方登录回调 |
| /api/auth/email/code/username | POST | AuthController.java | 根据用户名发送邮箱验证码 |
| /api/auth/password/reset/code | POST | AuthController.java | 发送密码重置验证码 |
| /api/auth/password/reset/verify | POST | AuthController.java | 验证重置验证码 |
| /api/auth/password/reset | POST | AuthController.java | 重置密码 |
| /api/auth/magic-link/send | POST | AuthController.java | 发送魔法链接 |
| /api/auth/magic-link/callback | GET | AuthController.java | 魔法链接回调 |
| /api/auth/test/hello | GET | AuthHelloController.java | 测试接口 |
| /api/auth/test/helloAuth | GET | AuthHelloController.java | 测试认证接口 |

## 2. 接口详情

### 2.1 用户注册

**路径**: `POST /api/auth/register`

**功能描述**: 用户注册接口，支持邮箱注册，有频率限制（同一邮箱10分钟内最多10次）

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | string | 是 | `application/json` |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| username | string | 是 | 用户名 |
| password | string | 是 | 密码（建议RSA加密） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.2 用户登录

**路径**: `POST /login`

**功能描述**: 用户登录接口，支持多种登录方式，有频率限制（同一用户名10分钟内最多10次）

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | string | 是 | `application/json` |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 否 | 用户名（与email二选一） |
| email | string | 否 | 邮箱地址（与username二选一） |
| password | string | 否 | 密码（普通登录时必填） |
| loginType | string | 否 | 登录类型：normal/wechat/github |
| authCode | string | 否 | 第三方授权码 |
| state | string | 否 | 第三方登录状态码 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": 7200,
    "userInfo": {
      "userId": "123456",
      "username": "testuser",
      "email": "test@example.com"
    }
  }
}
```

---

### 2.3 刷新令牌

**路径**: `POST /refresh`

**功能描述**: 使用刷新令牌刷新访问令牌有效期

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer格式的刷新令牌 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.4 退出登录

**路径**: `POST /logout`

**功能描述**: 用户退出登录，使令牌失效

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer格式的访问令牌 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.5 获取滑块拼图验证码

**路径**: `GET /captcha`

**功能描述**: 获取滑块拼图验证码，用于人机校验

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "uuid": "abc123def456",
    "backgroundImage": "data:image/png;base64,iVBORw0...",
    "slideImage": "data:image/png;base64,iVBORw0...",
    "slideWidth": 50,
    "slideHeight": 50
  }
}
```

---

### 2.6 校验滑块验证码

**路径**: `POST /captcha/check`

**功能描述**: 校验滑块验证码是否正确

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | string | 是 | `application/json` |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| uuid | string | 是 | 验证码唯一标识 |
| code | string | 是 | 用户滑动距离或验证码结果 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": "captcha_verification_token"
}
```

---

### 2.7 获取RSA公钥

**路径**: `GET /public-key`

**功能描述**: 获取RSA公钥，用于前端加密敏感数据

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...",
    "expireTime": 3600,
    "version": "1.0"
  }
}
```

---

### 2.8 发送邮箱验证码

**路径**: `POST /email/code`

**功能描述**: 向指定邮箱发送验证码，有频率限制（同一邮箱5分钟内最多5次）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| captchaVerification | string | 是 | 滑块验证码验证凭证 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.9 获取第三方登录授权URL

**路径**: `GET /third-party/url`

**功能描述**: 获取第三方平台（如微信、GitHub）的授权URL

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| loginType | string | 是 | 登录类型：wechat/github |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": "https://github.com/login/oauth/authorize?client_id=xxx&redirect_uri=xxx&state=xxx"
}
```

---

### 2.10 第三方登录回调

**路径**: `POST /third-party/callback`

**功能描述**: 第三方登录授权回调，完成登录流程

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| loginType | string | 是 | 登录类型：wechat/github |
| code | string | 是 | 授权码 |
| state | string | 是 | 状态码 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": 7200,
    "userInfo": {
      "userId": "123456",
      "username": "testuser",
      "email": "test@example.com"
    }
  }
}
```

---

### 2.11 根据用户名发送邮箱验证码

**路径**: `POST /email/code/username`

**功能描述**: 根据用户名查询邮箱并发送验证码，有频率限制（同一用户名5分钟内最多5次）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |
| captchaVerification | string | 是 | 滑块验证码验证凭证 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.12 发送密码重置验证码

**路径**: `POST /password/reset/code`

**功能描述**: 发送密码重置验证码到指定邮箱，有频率限制（同一邮箱3分钟内最多3次）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| captchaVerification | string | 是 | 滑块验证码验证凭证 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.13 验证重置验证码

**路径**: `POST /password/reset/verify`

**功能描述**: 验证密码重置验证码是否正确，返回验证令牌

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| code | string | 是 | 验证码 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": "verify_token_string"
}
```

---

### 2.14 重置密码

**路径**: `POST /password/reset`

**功能描述**: 使用验证令牌重置密码

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| verifyToken | string | 是 | 验证令牌 |
| newPassword | string | 是 | 新密码（建议RSA加密） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.15 发送魔法链接

**路径**: `POST /magic-link/send`

**功能描述**: 发送魔法链接到指定邮箱，用于无密码登录，有频率限制（同一邮箱3分钟内最多3次）

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | string | 是 | `application/json` |

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| turnstileToken | string | 是 | Cloudflare Turnstile验证凭证 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": "魔法链接已发送至您的邮箱，15分钟内有效"
}
```

---

### 2.16 魔法链接回调

**路径**: `GET /magic-link/callback`

**功能描述**: 魔法链接回调接口，验证Token后设置登录Cookie并重定向

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| token | string | 是 | 魔法链接中的Token |

**成功响应**: 302 重定向到首页

**失败响应**: 302 重定向到登录页，URL携带 `error=invalid_token` 参数

---

### 2.17 测试接口

**路径**: `GET /test/hello`

**功能描述**: 无需认证的测试接口

**成功响应** (200):

```
hello world
```

---

### 2.18 测试认证接口

**路径**: `GET /api/auth/test/helloAuth`

**功能描述**: 需要ADMIN角色权限的测试接口

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer格式的访问令牌 |

**成功响应** (200):

```
hello world
```

**失败响应** (403):

```json
{
  "code": 403,
  "msg": "无权限",
  "data": null
}
```

## 3. 状态码说明

| 状态码 | 含义 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 4. 安全说明

1. 密码等敏感信息建议使用RSA公钥加密后传输
2. 所有需要认证的接口必须携带 `Authorization` 请求头
3. 邮箱验证码有频率限制，防止恶意攻击
4. 魔法链接有效期为15分钟，使用后立即失效