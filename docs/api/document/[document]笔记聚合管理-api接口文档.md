# [document] 笔记聚合管理 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docNoteDtlAggregate/full` | POST | `DocNoteAggregateController.java` | 创建笔记（元信息 + 正文） |
| `/api/document/docNoteDtlAggregate/{id}/full` | GET | `DocNoteAggregateController.java` | 获取笔记全量信息（元信息 + 正文） |
| `/api/document/docNoteDtlAggregate/{id}/full` | PUT | `DocNoteAggregateController.java` | 全量更新笔记（元信息 + 正文） |
| `/api/document/docNoteDtlAggregate/{id}/full` | DELETE | `DocNoteAggregateController.java` | 级联删除笔记（元信息 + 正文） |

---

## 1. 创建笔记（元信息 + 正文）

### 接口信息
- **URL**: `POST /api/document/docNoteDtlAggregate/full`
- **功能**: 在一个事务中同时创建笔记元信息和正文内容，保证两张表的数据一致性

### 请求体

```json
{
  "docNote": {
    "userId": "1",
    "noteName": "新笔记标题",
    "noteTags": "tag1,tag2",
    "description": "笔记简介",
    "coverFileId": "1",
    "broadCode": "tech",
    "narrowCode": "java",
    "noteGrade": 1,
    "noteMode": 1,
    "suitableUsers": "初学者",
    "status": 3,
    "auditStatus": 0,
    "publishTime": "2026-04-26 10:30:00",
    "isPinned": 0,
    "isRecommended": 0,
    "seoTitle": "SEO标题",
    "seoDescription": "SEO描述",
    "seoKeywords": "keyword1,keyword2"
  },
  "content": "# 笔记正文\n\nMarkdown 格式的正文内容..."
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docNote` | `object` | 是 | 笔记元信息对象（详见下方 docNote 字段说明） |
| `content` | `string` | 是 | 笔记正文内容（Markdown 格式） |

### docNote 字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | `string` | 是 | 用户ID |
| `noteName` | `string` | 是 | 笔记名称 |
| `noteTags` | `string` | 否 | 笔记标签（逗号分隔） |
| `description` | `string` | 否 | 笔记简介/描述 |
| `coverFileId` | `string` | 否 | 封面图片文件ID |
| `broadCode` | `string` | 是 | 大类编码 |
| `narrowCode` | `string` | 否 | 小类编码 |
| `noteGrade` | `number` | 否 | 笔记等级（1-入门 2-进阶 3-高级 4-专家） |
| `noteMode` | `number` | 否 | 笔记模式（1-公开 2-私密 3-租户 4-付费） |
| `suitableUsers` | `string` | 否 | 适合人群 |
| `status` | `number` | 否 | 笔记状态（1-发布 2-下架 3-草稿），默认3 |
| `auditStatus` | `number` | 否 | 审核状态（0-待审核 1-通过 2-驳回 3-撤回），默认0 |
| `publishTime` | `string` | 否 | 发布时间（格式：yyyy-MM-dd HH:mm:ss） |
| `isPinned` | `number` | 否 | 是否置顶（0-否 1-是） |
| `isRecommended` | `number` | 否 | 是否推荐（0-否 1-是） |
| `seoTitle` | `string` | 否 | SEO标题 |
| `seoDescription` | `string` | 否 | SEO描述 |
| `seoKeywords` | `string` | 否 | SEO关键词（逗号分隔） |

> **注意**：`docNote.id` 创建时无需传入，由后端雪花算法自动生成。

### 处理流程

1. 校验 `docNote` 和 `content` 非空
2. 插入 `document_note`（元信息），雪花算法生成主键 ID
3. 用回填的 ID 插入 `document_note_dtl`（正文）
4. 两步在同一事务中执行，任一失败则整体回滚

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

---

## 2. 获取笔记全量信息

### 接口信息
- **URL**: `GET /api/document/docNote/{id}/full`
- **功能**: 同时返回笔记元信息（含作者、封面、统计）和正文内容，供阅读页一次请求拿到全量数据

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "userId": "1",
    "author": {
      "id": "1",
      "name": "作者昵称",
      "avatar": "https://example.com/avatar.jpg"
    },
    "noteName": "笔记标题",
    "noteTags": "tag1,tag2",
    "description": "笔记简介",
    "coverFile": {
      "fileId": "1",
      "fileUrl": "https://example.com/cover.jpg"
    },
    "broadCode": "tech",
    "narrowCode": "java",
    "noteGrade": 1,
    "noteMode": 1,
    "suitableUsers": "初学者",
    "auditStatus": 1,
    "status": 1,
    "stats": {
      "views": 1234,
      "likes": 56,
      "favorites": 32
    },
    "publishTime": "2026-04-26 10:30:00",
    "isPinned": 0,
    "isRecommended": 1,
    "seoTitle": "SEO标题",
    "seoDescription": "SEO描述",
    "seoKeywords": "keyword1,keyword2",
    "deleted": 0,
    "createTime": "2026-04-26 10:30:00",
    "updateTime": "2026-04-26 10:30:00",
    "content": "# 笔记正文\n\nMarkdown 格式的正文内容..."
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 笔记ID |
| `userId` | `string` | 用户ID |
| `author` | `object` | 作者信息 |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称（优先使用昵称） |
| `author.avatar` | `string` | 作者头像URL |
| `noteName` | `string` | 笔记名称 |
| `noteTags` | `string` | 笔记标签（逗号分隔） |
| `description` | `string` | 笔记简介/描述 |
| `coverFile` | `object` | 封面文件信息 |
| `coverFile.fileId` | `string` | 封面图片文件ID |
| `coverFile.fileUrl` | `string` | 封面图片文件URL |
| `broadCode` | `string` | 大类编码 |
| `narrowCode` | `string` | 小类编码 |
| `noteGrade` | `number` | 笔记等级 |
| `noteMode` | `number` | 笔记模式 |
| `suitableUsers` | `string` | 适合人群 |
| `auditStatus` | `number` | 审核状态 |
| `status` | `number` | 笔记状态 |
| `stats` | `object` | 统计信息（来源 Redis 缓存） |
| `stats.views` | `number` | 浏览量 |
| `stats.likes` | `number` | 点赞数 |
| `stats.favorites` | `number` | 收藏数 |
| `publishTime` | `string` | 发布时间 |
| `isPinned` | `number` | 是否置顶 |
| `isRecommended` | `number` | 是否推荐 |
| `seoTitle` | `string` | SEO标题 |
| `seoDescription` | `string` | SEO描述 |
| `seoKeywords` | `string` | SEO关键词 |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |
| `content` | `string` | **笔记正文（Markdown 格式）** |

> **注意**：`content` 仅在 `/full` 接口返回，普通列表接口 `GET /docNote/list` 和 `GET /docNote/page` 不包含此字段。

### 失败响应（笔记不存在）

```json
{
  "code": 500,
  "msg": "笔记不存在",
  "data": null
}
```

---

## 3. 全量更新笔记（元信息 + 正文）

### 接口信息
- **URL**: `PUT /api/document/docNoteDtlAggregate/{id}/full`
- **功能**: 在一个事务中同时更新笔记元信息和正文内容

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 请求体

```json
{
  "docNote": {
    "id": "1",
    "noteName": "修改后的标题",
    "noteTags": "tag1,tag2,newtag",
    "description": "修改后的简介",
    "coverFileId": "2",
    "broadCode": "life",
    "narrowCode": "python",
    "noteGrade": 2,
    "noteMode": 1,
    "suitableUsers": "进阶用户",
    "status": 1,
    "auditStatus": 1,
    "isPinned": 1,
    "isRecommended": 1,
    "seoTitle": "修改后的SEO标题",
    "seoDescription": "修改后的SEO描述",
    "seoKeywords": "newkeyword1,newkeyword2"
  },
  "content": "# 修改后的正文\n\n更新后的 Markdown 内容..."
}
```

### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `docNote` | `object` | 是 | 笔记元信息对象（id 必填，其余字段可选） |
| `content` | `string` | 是 | 笔记正文内容（Markdown 格式） |

> **注意**：路径参数中的 `id` 与 `docNote.id` 必须一致。

### 处理流程

1. 校验 `docNote.id` 非空
2. 更新 `document_note`（元信息）
3. 更新 `document_note_dtl`（正文），含乐观锁校验
4. 两步在同一事务中执行，任一失败则整体回滚

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

### 失败响应（笔记不存在）

```json
{
  "code": 500,
  "msg": "更新失败：笔记不存在或已被删除",
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

## 4. 级联删除笔记

### 接口信息
- **URL**: `DELETE /api/document/docNoteDtlAggregate/{id}/full`
- **功能**: 同时逻辑删除笔记的元信息和正文，两张表在同一事务中执行

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 笔记ID |

### 处理流程

1. 逻辑删除 `document_note_dtl`（设置 deleted = 1）
2. 逻辑删除 `document_note`（设置 deleted = 1）
3. 两步在同一事务中执行

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

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
