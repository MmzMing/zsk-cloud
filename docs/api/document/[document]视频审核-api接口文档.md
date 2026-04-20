# [document] 视频审核 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/video/audit/queue` | GET | `DocVideoAuditController.java` | 获取审核队列 |
| `/api/document/video/audit/detail/{videoId}` | GET | `DocVideoAuditController.java` | 获取审核详情 |
| `/api/document/video/audit/submit` | POST | `DocVideoAuditController.java` | 提交审核结果 |
| `/api/document/video/audit/submitBatch` | POST | `DocVideoAuditController.java` | 批量提交审核结果 |
| `/api/document/video/audit/logs` | GET | `DocVideoAuditController.java` | 获取审核日志 |
| `/api/document/video/audit/violation-reasons` | GET | `DocVideoAuditController.java` | 获取违规原因列表 |

---

## 1. 获取审核队列

### 接口信息
- **URL**: `GET /api/document/video/audit/queue`
- **功能**: 获取视频审核队列

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `auditStatus` | `number` | 否 | - | 审核状态（0待审核，1已通过，2已拒绝） |
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
        "videoId": "1",
        "videoTitle": "视频标题",
        "coverUrl": "https://example.com/cover.jpg",
        "uploaderId": "1",
        "uploaderName": "用户1",
        "uploadTime": "2026-02-15 10:30:00",
        "auditStatus": 0,
        "duration": "05:30"
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
| `videoId` | `string` | 视频ID（后端使用Jackson转为string类型） |
| `videoTitle` | `string` | 视频标题 |
| `coverUrl` | `string` | 封面URL |
| `uploaderId` | `string` | 上传者ID |
| `uploaderName` | `string` | 上传者名称 |
| `uploadTime` | `string` | 上传时间 |
| `auditStatus` | `number` | 审核状态（0待审核，1已通过，2已拒绝） |
| `duration` | `string` | 视频时长 |

---

## 2. 获取审核详情

### 接口信息
- **URL**: `GET /api/document/video/audit/detail/{videoId}`
- **功能**: 获取视频审核详情

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "videoId": "1",
    "videoTitle": "视频标题",
    "videoUrl": "https://example.com/video.mp4",
    "coverUrl": "https://example.com/cover.jpg",
    "uploaderId": "1",
    "uploaderName": "用户1",
    "uploadTime": "2026-02-15 10:30:00",
    "auditStatus": 0,
    "auditTime": null,
    "auditorId": null,
    "auditorName": null,
    "auditComment": null,
    "violationReason": null,
    "duration": "05:30",
    "fileSize": 10485760
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 审核记录ID |
| `videoId` | `string` | 视频ID |
| `videoTitle` | `string` | 视频标题 |
| `videoUrl` | `string` | 视频URL |
| `coverUrl` | `string` | 封面URL |
| `uploaderId` | `string` | 上传者ID |
| `uploaderName` | `string` | 上传者名称 |
| `uploadTime` | `string` | 上传时间 |
| `auditStatus` | `number` | 审核状态 |
| `auditTime` | `string` | 审核时间 |
| `auditorId` | `string` | 审核员ID |
| `auditorName` | `string` | 审核员名称 |
| `auditComment` | `string` | 审核备注 |
| `violationReason` | `string` | 违规原因 |
| `duration` | `string` | 视频时长 |
| `fileSize` | `number` | 文件大小（字节） |

---

## 3. 提交审核结果

### 接口信息
- **URL**: `POST /api/document/video/audit/submit`
- **功能**: 提交单个视频审核结果

### 请求体

```json
{
  "videoId": "1",
  "auditResult": "approved",
  "auditComment": "内容合规，审核通过",
  "violationReason": null
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |
| `auditResult` | `string` | 是 | 审核结果：approved（通过）、rejected（拒绝） |
| `auditComment` | `string` | 否 | 审核备注 |
| `violationReason` | `string` | 否 | 违规原因（拒绝时必填） |

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
- **URL**: `POST /api/document/video/audit/submitBatch`
- **功能**: 批量提交视频审核结果

### 请求体

```json
{
  "videoIds": ["1", "2", "3"],
  "auditResult": "approved",
  "auditComment": "批量审核通过"
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoIds` | `array` | 是 | 视频ID列表 |
| `auditResult` | `string` | 是 | 审核结果：approved（通过）、rejected（拒绝） |
| `auditComment` | `string` | 否 | 审核备注 |
| `violationReason` | `string` | 否 | 违规原因（拒绝时必填） |

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
- **URL**: `GET /api/document/video/audit/logs`
- **功能**: 获取审核日志列表

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
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
        "videoId": "1",
        "videoTitle": "视频标题",
        "auditorId": "1",
        "auditorName": "审核员1",
        "auditResult": "approved",
        "auditComment": "审核通过",
        "auditTime": "2026-02-15 10:35:00"
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
| `videoId` | `string` | 视频ID |
| `videoTitle` | `string` | 视频标题 |
| `auditorId` | `string` | 审核员ID |
| `auditorName` | `string` | 审核员名称 |
| `auditResult` | `string` | 审核结果 |
| `auditComment` | `string` | 审核备注 |
| `auditTime` | `string` | 审核时间 |

---

## 6. 获取违规原因列表

### 接口信息
- **URL**: `GET /api/document/video/audit/violation-reasons`
- **功能**: 获取违规原因列表

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "code": "violation-001",
      "name": "色情低俗",
      "description": "包含色情、低俗内容"
    },
    {
      "code": "violation-002",
      "name": "暴力恐怖",
      "description": "包含暴力、恐怖内容"
    },
    {
      "code": "violation-003",
      "name": "政治敏感",
      "description": "包含政治敏感内容"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `code` | `string` | 违规代码 |
| `name` | `string` | 违规类型名称 |
| `description` | `string` | 违规说明 |

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
| `data` | `object/array/null` | 响应数据 |
