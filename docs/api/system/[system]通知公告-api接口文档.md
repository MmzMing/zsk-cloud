# 通知公告 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/notice/list | GET | SysNoticeController.java | 查询通知公告列表 |
| /api/system/notice/{id} | GET | SysNoticeController.java | 获取通知公告详细信息 |
| /api/system/notice | POST | SysNoticeController.java | 新增通知公告 |
| /api/system/notice | PUT | SysNoticeController.java | 修改通知公告 |
| /api/system/notice/{ids} | DELETE | SysNoticeController.java | 删除通知公告 |

## 2. 接口详情

### 2.1 查询通知公告列表

**路径**: `GET /api/system/notice/list`

**功能描述**: 查询通知公告列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| noticeTitle | string | 否 | 公告标题（模糊查询） |
| status | string | 否 | 状态（0正常 1关闭） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "noticeTitle": "系统升级通知",
      "noticeType": "1",
      "content": "系统将于今晚22:00进行升级维护...",
      "status": "0",
      "createTime": "2026-02-15 10:00:00"
    }
  ]
}
```

---

### 2.2 获取通知公告详细信息

**路径**: `GET /api/system/notice/{id}`

**功能描述**: 根据公告ID获取通知公告详细信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 公告ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "noticeTitle": "系统升级通知",
    "noticeType": "1",
    "content": "<p>系统将于今晚22:00进行升级维护...</p>",
    "status": "0",
    "createBy": "admin",
    "createTime": "2026-02-15 10:00:00",
    "updateBy": "admin",
    "updateTime": "2026-02-15 10:00:00"
  }
}
```

---

### 2.3 新增通知公告

**路径**: `POST /api/system/notice`

**功能描述**: 新增通知公告

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| noticeTitle | string | 是 | 公告标题 |
| noticeType | string | 是 | 公告类型（1通知 2公告） |
| content | string | 是 | 公告内容 |
| status | string | 否 | 状态，默认0（正常） |

**请求示例**:

```json
{
  "noticeTitle": "系统升级通知",
  "noticeType": "1",
  "content": "<p>系统将于今晚22:00进行升级维护...</p>",
  "status": "0"
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

---

### 2.4 修改通知公告

**路径**: `PUT /api/system/notice`

**功能描述**: 修改通知公告

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 公告ID |
| noticeTitle | string | 否 | 公告标题 |
| noticeType | string | 否 | 公告类型 |
| content | string | 否 | 公告内容 |
| status | string | 否 | 状态 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.5 删除通知公告

**路径**: `DELETE /api/system/notice/{ids}`

**功能描述**: 删除通知公告（支持批量删除）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<string> | 是 | 公告ID列表，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```