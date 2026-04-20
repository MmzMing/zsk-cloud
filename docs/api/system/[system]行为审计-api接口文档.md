# 行为审计 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/monitor/behavior/users | GET | SysBehaviorController.java | 获取行为审计用户列表 |
| /api/system/monitor/behavior/timeline | GET | SysBehaviorController.java | 获取用户行为时间轴 |
| /api/system/monitor/behavior/events | GET | SysBehaviorController.java | 获取行为审计事件列表 |

## 2. 接口详情

### 2.1 获取行为审计用户列表

**路径**: `GET /api/system/monitor/behavior/users`

**功能描述**: 获取有行为记录的用户列表

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      "eventCount": 100,
      "lastActiveTime": "2026-02-15 10:00:00"
    }
  ]
}
```

---

### 2.2 获取用户行为时间轴

**路径**: `GET /api/system/monitor/behavior/timeline`

**功能描述**: 获取指定用户的行为时间轴数据

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 是 | 用户ID |
| range | string | 否 | 时间范围（today/yesterday/7d/30d），默认today |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "time": "2026-02-15 09:00:00",
      "type": "login",
      "description": "用户登录",
      "detail": "登录IP: 192.168.1.100"
    },
    {
      "time": "2026-02-15 09:30:00",
      "type": "operation",
      "description": "查看用户列表",
      "detail": "访问路径: /api/system/user/list"
    }
  ]
}
```

---

### 2.3 获取行为审计事件列表

**路径**: `GET /api/system/monitor/behavior/events`

**功能描述**: 获取行为审计事件列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | string | 否 | 用户ID（可选，筛选指定用户） |
| keyword | string | 否 | 关键字搜索 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "userId": 1,
      "userName": "admin",
      "type": "login",
      "description": "用户登录",
      "detail": "登录IP: 192.168.1.100",
      "createTime": "2026-02-15 09:00:00"
    }
  ]
}
```