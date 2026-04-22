# 行为审计 API接口文档

> **版本**: v2.0 (2026-04-22)
> **变更说明**: 数据源对接 MongoDB `sys_oper_log`；新增「行为详情」接口；行为列表改为分页+多条件；移除时间轴接口。

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/monitor/behavior/users | GET | SysBehaviorController.java | 获取行为审计用户列表（聚合） |
| /api/system/monitor/behavior/events | GET | SysBehaviorController.java | 分页查询用户行为列表（多条件） |
| /api/system/monitor/behavior/{id} | GET | SysBehaviorController.java | 获取行为详情（完整请求/响应） |

---

## 2. 接口详情

### 2.1 获取行为审计用户列表

**路径**: `GET /api/system/monitor/behavior/users`

**功能描述**: 从 MongoDB `sys_oper_log` 按 `operName` 聚合，得到所有有行为记录的用户列表，并基于操作次数计算风险等级（>100 high / >50 medium / 其他 low）。结果按操作次数降序。

**请求参数**: 无

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "operName": "admin",
      "operCount": 256,
      "lastOperTime": "2026-04-22 10:00:00",
      "lastOperIp": "192.168.1.100",
      "riskLevel": "high"
    },
    {
      "operName": "alice",
      "operCount": 67,
      "lastOperTime": "2026-04-21 18:30:00",
      "lastOperIp": "10.0.0.55",
      "riskLevel": "medium"
    }
  ]
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| operName | string | 操作人员（账号名，对应 `sys_user.user_name`） |
| operCount | Long | 累计行为次数 |
| lastOperTime | LocalDateTime | 最近一次行为时间 |
| lastOperIp | string | 最近一次行为IP |
| riskLevel | string | 风险等级（low / medium / high） |

---

### 2.2 分页查询用户行为列表

**路径**: `GET /api/system/monitor/behavior/events`

**功能描述**: 多条件分页查询用户行为列表，按 `operTime` 倒序。列表场景下 `operParam` / `jsonResult` 自动截断到 200 字符，需获取完整内容请调用「行为详情」接口。

**请求参数** (Query):

| 参数名 | 类型 | 必填 | 默认 | 说明 |
|--------|------|------|------|------|
| pageNum | long | 否 | 1 | 当前页码 |
| pageSize | long | 否 | 10 | 每页大小，最大 500 |
| userName | string | 否 | - | 操作人员账号（精确匹配，前端从用户列表选择） |
| businessType | int | 否 | - | 行为类型代码（见下表） |
| title | string | 否 | - | 模块标题（模糊查询） |
| operIp | string | 否 | - | 操作IP（模糊查询） |
| status | int | 否 | - | 操作状态：0正常 1异常 |
| beginTime | string | 否 | - | 行为开始时间（`yyyy-MM-dd HH:mm:ss`） |
| endTime | string | 否 | - | 行为结束时间（`yyyy-MM-dd HH:mm:ss`） |

**businessType 行为类型枚举**:

| 代码 | 名称 |
|------|------|
| 0 | 其它 |
| 1 | 新增 |
| 2 | 修改 |
| 3 | 删除 |
| 4 | 授权 |
| 5 | 导出 |
| 6 | 导入 |
| 7 | 强退 |
| 8 | 生成代码 |
| 9 | 清空数据 |
| 10 | 查询 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "65f2a1b3c4d5e6f7a8b9c0d1",
        "operName": "admin",
        "title": "用户管理",
        "businessType": 1,
        "actionType": "新增",
        "operUrl": "/api/system/users",
        "requestMethod": "POST",
        "operParam": "{\"userName\":\"alice\",\"nickName\":\"Alice\"}",
        "jsonResult": "{\"code\":0,\"msg\":\"success\",\"data\":{\"id\":2}}",
        "operIp": "192.168.1.100",
        "operLocation": "浙江省杭州市",
        "status": 0,
        "operTime": "2026-04-22 10:00:00",
        "costTime": 125
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 行为记录ID（MongoDB `_id`） |
| operName | string | 操作人员 |
| title | string | 模块标题（行为内容） |
| businessType | Integer | 行为类型代码 |
| actionType | string | 行为类型名称 |
| operUrl | string | 请求URL |
| requestMethod | string | 请求方式（GET/POST/PUT/DELETE） |
| operParam | string | 请求参数（最多 200 字符，超出截断） |
| jsonResult | string | 响应结果（最多 200 字符，超出截断） |
| operIp | string | 操作IP |
| operLocation | string | 操作地点 |
| status | Integer | 操作状态（0正常 1异常） |
| operTime | LocalDateTime | 行为时间 |
| costTime | Long | 耗时（毫秒） |

---

### 2.3 获取行为详情

**路径**: `GET /api/system/monitor/behavior/{id}`

**功能描述**: 根据行为记录ID获取完整请求/响应详情，用于前端「详情面板」展示。返回 `operParam` / `jsonResult` 完整内容（不截断），并附带方法签名、错误信息等调试字段。

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 行为记录ID（MongoDB `_id`） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "65f2a1b3c4d5e6f7a8b9c0d1",
    "operName": "admin",
    "title": "用户管理",
    "businessType": 1,
    "actionType": "新增",
    "method": "com.zsk.system.controller.SysUserController.add()",
    "requestMethod": "POST",
    "operUrl": "/api/system/users",
    "operIp": "192.168.1.100",
    "operLocation": "浙江省杭州市",
    "operParam": "{\"userName\":\"alice\",\"nickName\":\"Alice\",\"email\":\"alice@example.com\",\"phonenumber\":\"13800138000\",\"sex\":\"1\",\"roleIds\":[2,3]}",
    "jsonResult": "{\"code\":0,\"msg\":\"success\",\"data\":{\"id\":2,\"userName\":\"alice\"},\"timestamp\":\"2026-04-22 10:00:00\"}",
    "status": 0,
    "errorMsg": null,
    "operTime": "2026-04-22 10:00:00",
    "costTime": 125
  }
}
```

**失败响应** (业务异常):

```json
{
  "code": 5000,
  "msg": "行为记录不存在或已被清理: 65f2a1b3c4d5e6f7a8b9c0d1",
  "data": null
}
```

**字段说明**: 在 [2.2](#22-分页查询用户行为列表) 字段基础上额外返回：

| 字段 | 类型 | 说明 |
|------|------|------|
| method | string | 控制器方法（全限定名 + 方法签名） |
| operParam | string | 完整请求参数（不截断） |
| jsonResult | string | 完整响应结果（不截断） |
| errorMsg | string | 错误消息（status=1 时有值） |

---

## 3. 数据源说明

- **存储**: MongoDB collection `sys_oper_log`（实体 `com.zsk.common.log.domain.OperLog`）
- **采集**: 由 `zsk-common-log` 模块通过 AOP 切面 `@Log` 注解自动写入
- **关联**: `OperLog.operName` ↔ `sys_user.user_name`（用户维度通过账号名关联）
- **保留**: 历史日志可通过「操作日志」管理界面手动清理或按策略归档
