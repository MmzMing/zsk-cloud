# [document] 笔记详情管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docNoteDtl/{noteId}` | GET | `DocNoteDtlController.java` | 根据笔记ID获取正文内容 |
| `/api/document/docNoteDtl` | POST | `DocNoteDtlController.java` | 新增或更新笔记正文（幂等） |
| `/api/document/docNoteDtl/{noteIds}` | DELETE | `DocNoteDtlController.java` | 批量删除笔记正文 |
| `/api/document/docNoteDtl/upload` | POST | `DocNoteDtlController.java` | 上传MD文件并保存 |

---

## 1. 根据笔记ID获取正文内容

### 接口信息
- **URL**: `GET /api/document/docNoteDtl/{noteId}`
- **功能**: 通过 note_id 查询对应的 Markdown 正文内容。由于 `document_note_dtl.note_id` 有唯一索引，最多返回一条记录

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 是 | 笔记ID（关联 document_note.id） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "tenantId": "0",
    "noteId": "1",
    "content": "# 笔记正文\n\nMarkdown 格式的正文内容...",
    "version": "0",
    "createName": "admin",
    "createTime": "2026-04-26 10:30:00",
    "updateName": "admin",
    "updateTime": "2026-04-26 10:30:00",
    "deleted": 0
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 详情记录主键ID |
| `tenantId` | `string` | 租户ID |
| `noteId` | `string` | 关联笔记ID |
| `content` | `string` | 笔记正文（Markdown 格式） |
| `version` | `string` | 乐观锁版本号（每次更新递增） |
| `createName` | `string` | 创建者名称 |
| `createTime` | `string` | 创建时间 |
| `updateName` | `string` | 更新者名称 |
| `updateTime` | `string` | 更新时间 |
| `deleted` | `number` | 删除标记（0-未删除，1-已删除） |

### 失败响应（详情不存在）

```json
{
  "code": 500,
  "msg": "笔记详情不存在",
  "data": null
}
```

---

## 2. 新增或更新笔记正文（幂等）

### 接口信息
- **URL**: `POST /api/document/docNoteDtl`
- **功能**: 保存笔记正文内容。若该 noteId 已有记录则更新（含乐观锁校验），不存在则新增

### 请求体

```json
{
  "noteId": "1",
  "content": "# 笔记正文\n\nMarkdown 格式的正文内容..."
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 是 | 关联笔记ID（对应 document_note.id） |
| `content` | `string` | 是 | 笔记正文内容（Markdown 格式） |

### 处理流程

1. 校验 noteId 和 content 非空
2. 查询 note_id 是否已有记录 → **已存在**：执行 UPDATE，通过 version 乐观锁防止并发冲突 → **不存在**：执行 INSERT，version 初始为 0

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

### 失败响应（参数校验失败）

```json
{
  "code": 500,
  "msg": "参数错误：笔记内容不能为空",
  "data": null
}
```

### 失败响应（并发冲突）

```json
{
  "code": 500,
  "msg": "更新失败：数据已被其他用户修改，请刷新后重试",
  "data": null
}
```

---

## 3. 批量删除笔记正文

### 接口信息
- **URL**: `DELETE /api/document/docNoteDtl/{noteIds}`
- **功能**: 批量逻辑删除笔记正文记录（设置 deleted = 1），使用 IN 语句批量更新避免 N+1 问题

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteIds` | `string` | 是 | 笔记ID列表，逗号分隔（如 `1,2,3`） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 4. 上传MD文件并保存

### 接口信息
- **URL**: `POST /api/document/docNoteDtl/upload`
- **功能**: 接收前端上传的 Markdown 文件，解析内容后保存到 `document_note_dtl` 表
- **Content-Type**: `multipart/form-data`

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `noteId` | `string` | 是 | 关联笔记ID |
| `file` | `file` | 是 | 上传的 MD 文件 |

### 文件校验规则

- 仅支持 `.md` 和 `.markdown` 后缀
- 文件内容使用 UTF-8 编码读取
- 文件内容和文件名均不能为空

### 处理流程

1. 校验 noteId 和 file 非空
2. 校验文件后缀（`.md` / `.markdown`）
3. UTF-8 读取文件内容
4. 校验内容非空
5. 调用 `saveOrUpdateByNoteId` 保存到数据库

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

### 失败响应（文件格式不支持）

```json
{
  "code": 500,
  "msg": "文件格式不支持：仅支持 .md 和 .markdown 格式的文件",
  "data": null
}
```

### 失败响应（文件为空）

```json
{
  "code": 500,
  "msg": "参数错误：文件不能为空",
  "data": null
}
```

---

## 与聚合接口的关系

| 场景 | 推荐接口 | 说明 |
| :--- | :--- | :--- |
| 仅修改正文内容 | `POST /docNoteDtl` | 不碰元信息，轻量操作 |
| 从MD文件导入正文 | `POST /docNoteDtl/upload` | 文件导入场景 |
| 阅读页（元信息+正文） | `GET /docNoteDtlAggregate/{id}/full` | 聚合接口，一次拿全量 |
| 创建笔记（标题+正文） | `POST /docNoteDtlAggregate/full` | 聚合接口，事务保证 |

> **注意**：`POST /docNoteDtl` 是幂等的，存在则更新、不存在则新增。它只操作 `document_note_dtl` 表，不更新 `document_note.update_time`。如需同时更新元信息和正文，请使用聚合接口 `PUT /docNoteDtlAggregate/{id}/full`。

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
| `data` | `object/boolean/null` | 响应数据 |
