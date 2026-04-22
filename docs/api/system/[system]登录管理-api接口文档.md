# 登录管理 API接口文档

> **版本**: v2.0 (2026-04-22)
> **变更说明**: 在线用户列表改为「用户维度」展示（一用户一行），通过 Redis Token Set 判断在线状态；强制下线改为按 userId 批量执行；移除手动刷新会话接口（Gateway 已实现滑动续期）。

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/login/online/page | GET | SysLoginManageController.java | 分页查询在线用户列表（用户维度） |
| /api/system/login/online/forceLogout | POST | SysLoginManageController.java | 强制下线用户（按 userId 批量） |

---

## 2. 接口详情

### 2.1 分页查询在线用户列表

**路径**: `GET /api/system/login/online/page`

**功能描述**: 分页查询当前在线用户列表。一个用户对应一条记录（合并多设备会话），通过 Redis Key `zsk:login:token:{userId}` 是否存在判断在线状态。

**请求参数** (Query):

| 参数名 | 类型 | 必填 | 默认 | 说明 |
|--------|------|------|------|------|
| pageNum | long | 否 | 1 | 当前页码 |
| pageSize | long | 否 | 10 | 每页大小，最大 500 |
| orderByColumn | string | 否 | - | 排序字段 |
| isAsc | string | 否 | asc | 排序方向（asc/desc） |
| userName | string | 否 | - | 用户账号（模糊查询） |
| nickName | string | 否 | - | 用户昵称（模糊查询） |
| ipaddr | string | 否 | - | 登录IP（模糊查询） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {
        "userId": 1,
        "userName": "admin",
        "nickName": "管理员",
        "avatar": "https://oss.zsk.cloud/avatar/1.png",
        "email": "admin@zsk.cloud",
        "status": "0",
        "ipaddr": "192.168.1.100",
        "loginLocation": "浙江省杭州市",
        "loginTime": "2026-04-22 10:00:00",
        "expireTime": "2026-04-22 12:00:00",
        "onlineDuration": 3600,
        "deviceCount": 2
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| userName | string | 用户账号 |
| nickName | string | 用户昵称 |
| avatar | string | 用户头像 URL |
| email | string | 用户邮箱 |
| status | string | 帐号状态（0正常 1停用） |
| ipaddr | string | 最后登录IP |
| loginLocation | string | 登录地点 |
| loginTime | LocalDateTime | 最近登录时间 |
| expireTime | LocalDateTime | Token 过期时间（来自 Redis TTL，每次请求滑动续期） |
| onlineDuration | Long | 在线时长（秒，从最近登录时间计算） |
| deviceCount | Integer | 当前在线设备数（同一用户多端登录数） |

---

### 2.2 强制下线用户

**路径**: `POST /api/system/login/online/forceLogout`

**功能描述**: 按用户ID批量强制下线，会清理该用户的全部 Token、角色缓存、权限缓存（一次踢掉所有设备）。

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userIds | List&lt;Long&gt; | 是 | 用户ID列表 |

**请求示例**:

```json
{
  "userIds": [1, 2, 3]
}
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

**失败响应**:

```json
{
  "code": 500,
  "msg": "强制下线失败",
  "data": null
}
```

---

## 3. 在线判定与续期机制

- **在线判定**: 仅依赖 Redis — Key `zsk:login:token:{userId}` 是 Set 结构，每个元素为一个设备的 JWT。Set 存在且非空即视为在线。
- **滑动续期**: 由 `zsk-gateway` `AuthFilter` 在每次请求时自动调用 `redisService.expire(...)` 刷新 TTL，前端无需主动调用刷新接口。
- **多设备**: 同一 userId 的多个 JWT 同时存在于 Set 中，`deviceCount` 字段反映当前设备数。
