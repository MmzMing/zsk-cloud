# [document] 统一审核管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docAudit/queue` | GET | `DocAuditController.java` | 获取审核队列 |
| `/api/document/docAudit/detail` | GET | `DocAuditController.java` | 获取审核详情 |
| `/api/document/docAudit/submit` | POST | `DocAuditController.java` | 提交审核结果 |
| `/api/document/docAudit/submitBatch` | POST | `DocAuditController.java` | 批量提交审核结果 |
| `/api/document/docAudit/logs` | GET | `DocAuditController.java` | 获取审核日志 |
| `/api/document/docAudit/violation-reasons` | GET | `DocAuditController.java` | 获取违规原因列表 |

---

## 通用枚举说明

### targetType 审核目标类型

| 值 | 说明 |
| :--- | :--- |
| `1` | 文档 |
| `2` | 视频 |
| `3` | 文档评论 |
| `4` | 视频评论 |

### auditStatus 审核状态

| 值 | 说明 |
| :--- | :--- |
| `0` | 待审核 |
| `1` | 审核通过 |
| `2` | 审核驳回 |
| `3` | 已撤回 |

---

## 1. 获取审核队列

### 接口信息
- **URL**: `GET /api/document/docAudit/queue`
- **功能**: 根据目标类型和审核状态筛选审核队列，支持分页查询

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `targetType` | `number` | 是 | - | 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论） |
| `auditStatus` | `number` | 否 | - | 审核状态（0-待审核 1-通过 2-驳回） |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "targetType": 1,
        "targetId": "100",
        "title": "Spring Boot 入门教程",
        "broadCode": "tech",
        "uploaderId": "10",
        "uploaderName": "张三",
        "status": "pending",
        "riskLevel": "low",
        "isAiChecked": false,
        "createTime": "2026-04-30 10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 审核记录ID |
| `targetType` | `number` | 审核目标类型 |
| `targetId` | `string` | 审核目标ID |
| `title` | `string` | 内容标题（评论类型为评论内容） |
| `broadCode` | `string` | 分类编码（评论类型为null） |
| `uploaderId` | `string` | 上传者ID |
| `uploaderName` | `string` | 上传者名称 |
| `status` | `string` | 审核状态值（pending/approved/rejected/withdrawn） |
| `riskLevel` | `string` | 风险等级（low/medium/high） |
| `isAiChecked` | `boolean` | 是否已AI审核 |
| `createTime` | `string` | 创建时间 |

---

## 2. 获取审核详情

### 接口信息
- **URL**: `GET /api/document/docAudit/detail`
- **功能**: 根据目标类型和目标ID查询最新的审核记录

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `targetType` | `number` | 是 | 审核目标类型 |
| `targetId` | `string` | 是 | 审核目标ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "targetType": 1,
    "targetId": "100",
    "targetTitle": "Spring Boot 入门教程",
    "auditType": "manual",
    "auditStatus": 2,
    "auditResult": null,
    "riskLevel": "medium",
    "auditMind": "内容包含敏感信息",
    "violationIds": "101,103",
    "auditorId": "1",
    "auditorName": "管理员",
    "auditTime": "2026-04-30 11:30:00",
    "createTime": "2026-04-30 10:00:00"
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 审核记录ID |
| `targetType` | `number` | 审核目标类型 |
| `targetId` | `string` | 审核目标ID |
| `targetTitle` | `string` | 内容标题 |
| `auditType` | `string` | 审核类型（ai/manual） |
| `auditStatus` | `number` | 审核状态（0-待审核 1-通过 2-驳回 3-已撤回） |
| `auditResult` | `string` | 审核结果详情（JSON格式，AI审核时填充） |
| `riskLevel` | `string` | 风险等级（low/medium/high） |
| `auditMind` | `string` | 审核意见 |
| `violationIds` | `string` | 违规原因ID列表（逗号分隔） |
| `auditorId` | `string` | 审核人ID |
| `auditorName` | `string` | 审核人姓名 |
| `auditTime` | `string` | 审核时间 |
| `createTime` | `string` | 创建时间 |

---

## 3. 提交审核结果

### 接口信息
- **URL**: `POST /api/document/docAudit/submit`
- **功能**: 单条内容审核结果提交

### 请求体

```json
{
  "targetType": 1,
  "targetId": "100",
  "auditStatus": 2,
  "auditMind": "内容包含敏感信息",
  "violationIds": ["101", "103"]
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `targetType` | `number` | 是 | 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论） |
| `targetId` | `string` | 是 | 审核目标ID |
| `auditStatus` | `number` | 是 | 审核状态（1-通过 2-驳回） |
| `auditMind` | `string` | 否 | 审核意见 |
| `violationIds` | `array<string>` | 否 | 违规项ID列表 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 4. 批量提交审核结果

### 接口信息
- **URL**: `POST /api/document/docAudit/submitBatch`
- **功能**: 对同一类型的多个内容批量提交审核结果，部分失败不影响其他项

### 请求体

```json
{
  "targetType": 2,
  "targetIds": ["200", "201", "202"],
  "auditStatus": 1,
  "auditMind": "批量审核通过",
  "violationIds": []
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `targetType` | `number` | 是 | 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论） |
| `targetIds` | `array<string>` | 是 | 审核目标ID列表 |
| `auditStatus` | `number` | 是 | 审核状态（1-通过 2-驳回） |
| `auditMind` | `string` | 否 | 审核意见 |
| `violationIds` | `array<string>` | 否 | 违规项ID列表 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 5. 获取审核日志

### 接口信息
- **URL**: `GET /api/document/docAudit/logs`
- **功能**: 查询审核操作日志，支持按目标类型筛选，按审核时间倒序排列

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `targetType` | `number` | 否 | - | 审核目标类型（不传则查询全部类型） |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "targetType": 1,
        "targetId": "100",
        "targetTitle": "Spring Boot 入门教程",
        "auditorName": "管理员",
        "auditTime": "2026-04-30 11:30:00",
        "result": "rejected",
        "auditMind": "内容包含敏感信息",
        "riskLevel": "medium"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 日志ID |
| `targetType` | `number` | 审核目标类型 |
| `targetId` | `string` | 审核目标ID |
| `targetTitle` | `string` | 内容标题 |
| `auditorName` | `string` | 审核人 |
| `auditTime` | `string` | 审核时间 |
| `result` | `string` | 审核结果（approved/rejected/withdrawn） |
| `auditMind` | `string` | 审核意见 |
| `riskLevel` | `string` | 风险等级 |

---

## 6. 获取违规原因列表

### 接口信息
- **URL**: `GET /api/document/docAudit/violation-reasons`
- **功能**: 根据目标类型获取对应的违规原因字典数据

### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `targetType` | `number` | 是 | 审核目标类型（1-文档 2-视频 3-文档评论 4-视频评论） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "101",
      "label": "涉黄内容"
    },
    {
      "id": "102",
      "label": "暴力内容"
    },
    {
      "id": "103",
      "label": "敏感信息"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 违规项ID |
| `label` | `string` | 违规原因标签 |

---

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
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
| `data` | `object/array/boolean/null` | 响应数据 |
