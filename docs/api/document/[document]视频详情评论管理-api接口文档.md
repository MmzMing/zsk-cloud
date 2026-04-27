# [document] 视频详情评论管理 API 接口文档

> **版本**: 2.0  
> **更新日期**: 2026-04-27  
> **说明**: 采用B站式评论结构，所有回复统一挂在根评论下，不存在层级嵌套。评论点赞数从Redis获取，不再存储在数据库表中。

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/docVideoComment/list` | GET | `DocVideoCommentController.java` | 查询视频评论列表（后台） |
| `/api/document/docVideoComment/page` | GET | `DocVideoCommentController.java` | 分页查询视频评论列表（后台） |
| `/api/document/docVideoComment/{id}` | GET | `DocVideoCommentController.java` | 获取视频评论详细信息（后台） |
| `/api/document/docVideoComment` | POST | `DocVideoCommentController.java` | 新增视频评论（后台） |
| `/api/document/docVideoComment` | PUT | `DocVideoCommentController.java` | 修改视频评论（后台） |
| `/api/document/docVideoComment/{ids}` | DELETE | `DocVideoCommentController.java` | 删除视频评论（后台） |
| `/api/document/docVideoComment/comments/{videoId}` | GET | `DocVideoCommentController.java` | 获取视频评论列表（前台） |
| `/api/document/docVideoComment/comment` | POST | `DocVideoCommentController.java` | 发表视频评论（前台） |
| `/api/document/docVideoComment/comment/like/{commentId}` | POST | `DocVideoCommentController.java` | 切换评论点赞状态（前台） |

---

## 后台管理接口

### 1. 查询视频评论列表

#### 接口信息
- **URL**: `GET /api/document/docVideoComment/list`
- **功能**: 查询视频评论列表

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 否 | 视频ID |
| `commentUserId` | `string` | 否 | 评论人ID |
| `commentContent` | `string` | 否 | 评论内容（支持模糊查询） |
| `parentCommentId` | `string` | 否 | 父评论ID |
| `replyUserId` | `string` | 否 | 回复人ID |
| `auditStatus` | `number` | 否 | 审核状态（0-待审核 1-审核通过 2-审核驳回） |
| `status` | `number` | 否 | 评论状态（1-正常 2-隐藏 3-删除） |
| `deleted` | `number` | 否 | 删除标记（0未删除，1已删除） |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "videoId": "1",
      "commentUserId": "1",
      "commentContent": "评论内容",
      "parentCommentId": null,
      "replyUserId": null,
      "auditStatus": 1,
      "status": 1,
      "version": 0,
      "deleted": 0,
      "createTime": "2026-02-15 10:30:00",
      "updateTime": "2026-02-15 10:30:00"
    }
  ]
}
```

#### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 评论ID（后端使用Jackson转为string类型） |
| `videoId` | `string` | 视频ID |
| `commentUserId` | `string` | 评论人ID |
| `commentContent` | `string` | 评论内容 |
| `parentCommentId` | `string` | 父评论ID（NULL表示根评论） |
| `replyUserId` | `string` | 回复人ID（记录被回复的用户ID） |
| `auditStatus` | `number` | 审核状态 |
| `status` | `number` | 评论状态 |
| `version` | `number` | 乐观锁版本号 |
| `deleted` | `number` | 删除标记 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

---

### 2. 分页查询视频评论列表

#### 接口信息
- **URL**: `GET /api/document/docVideoComment/page`
- **功能**: 分页查询视频评论列表

#### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `videoId` | `string` | 否 | - | 视频ID |
| `commentUserId` | `string` | 否 | - | 评论人ID |
| `commentContent` | `string` | 否 | - | 评论内容 |
| `auditStatus` | `number` | 否 | - | 审核状态 |
| `status` | `number` | 否 | - | 评论状态 |
| `deleted` | `number` | 否 | - | 删除标记 |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "videoId": "1",
        "commentUserId": "1",
        "commentContent": "评论内容",
        "parentCommentId": null,
        "replyUserId": null,
        "auditStatus": 1,
        "status": 1,
        "version": 0,
        "deleted": 0,
        "createTime": "2026-02-15 10:30:00",
        "updateTime": "2026-02-15 10:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

### 3. 获取视频评论详细信息

#### 接口信息
- **URL**: `GET /api/document/docVideoComment/{id}`
- **功能**: 获取视频评论详细信息

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 评论ID |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1",
    "videoId": "1",
    "commentUserId": "1",
    "commentContent": "评论内容",
    "parentCommentId": null,
    "replyUserId": null,
    "auditStatus": 1,
    "status": 1,
    "version": 0,
    "deleted": 0,
    "createTime": "2026-02-15 10:30:00",
    "updateTime": "2026-02-15 10:30:00"
  }
}
```

---

### 4. 新增视频评论

#### 接口信息
- **URL**: `POST /api/document/docVideoComment`
- **功能**: 新增视频评论（后台管理直接操作）

#### 请求体

```json
{
  "videoId": "1",
  "commentUserId": "1",
  "commentContent": "评论内容",
  "parentCommentId": null,
  "replyUserId": null,
  "auditStatus": 1,
  "status": 1
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |
| `commentUserId` | `string` | 是 | 评论人ID |
| `commentContent` | `string` | 是 | 评论内容 |
| `parentCommentId` | `string` | 否 | 父评论ID（NULL表示根评论） |
| `replyUserId` | `string` | 否 | 回复人ID |
| `auditStatus` | `number` | 否 | 审核状态（默认0） |
| `status` | `number` | 否 | 评论状态（默认1） |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

### 5. 修改视频评论

#### 接口信息
- **URL**: `PUT /api/document/docVideoComment`
- **功能**: 修改视频评论

#### 请求体

```json
{
  "id": "1",
  "commentContent": "修改后的评论内容",
  "auditStatus": 1,
  "status": 1
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | `string` | 是 | 评论ID |
| `commentContent` | `string` | 否 | 评论内容 |
| `auditStatus` | `number` | 否 | 审核状态 |
| `status` | `number` | 否 | 评论状态 |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

### 6. 删除视频评论

#### 接口信息
- **URL**: `DELETE /api/document/docVideoComment/{ids}`
- **功能**: 批量删除视频评论

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `ids` | `string` | 是 | 评论ID列表，逗号分隔 |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

## 前台业务接口

### 7. 获取视频评论列表（前台）

#### 接口信息
- **URL**: `GET /api/document/docVideoComment/comments/{videoId}`
- **功能**: 获取视频的评论列表，支持分页和排序。采用B站式评论结构：所有回复统一挂在根评论下，不存在层级嵌套。评论点赞数从Redis实时获取。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `pageNum` | `number` | 否 | 1 | 页码 |
| `pageSize` | `number` | 否 | 10 | 每页数量 |
| `sort` | `string` | 否 | `new` | 排序方式：`hot`（热门，按点赞数降序）/ `new`（最新，按时间降序） |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1",
        "content": "评论内容",
        "createdAt": "2026-02-15 10:30:00",
        "likes": 5,
        "isLiked": false,
        "author": {
          "id": "1",
          "name": "用户1",
          "avatar": ""
        },
        "replies": [
          {
            "id": "2",
            "content": "回复内容",
            "createdAt": "2026-02-15 10:35:00",
            "likes": 2,
            "isLiked": false,
            "author": {
              "id": "2",
              "name": "用户2",
              "avatar": ""
            },
            "replyTo": {
              "id": "1",
              "name": "用户1"
            }
          }
        ]
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

#### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | `string` | 评论ID |
| `content` | `string` | 评论内容 |
| `createdAt` | `string` | 创建时间 |
| `likes` | `number` | 点赞数（从Redis实时获取） |
| `isLiked` | `boolean` | 当前用户是否点赞 |
| `author.id` | `string` | 作者ID |
| `author.name` | `string` | 作者名称 |
| `author.avatar` | `string` | 作者头像 |
| `replies` | `array` | 回复列表（B站结构，统一挂在根评论下） |
| `replies[].replyTo` | `object` | 回复对象信息（用于显示"A回复B"） |
| `replies[].replyTo.id` | `string` | 被回复用户ID |
| `replies[].replyTo.name` | `string` | 被回复用户名称 |

---

### 8. 发表视频评论（前台）

#### 接口信息
- **URL**: `POST /api/document/docVideoComment/comment`
- **功能**: 发表评论或回复评论（已登录用户可用）。采用B站式评论结构：parentCommentId统一记录根评论ID，replyUserId记录被回复的用户ID。

#### 请求体

```json
{
  "videoId": "1",
  "content": "评论内容",
  "parentId": null,
  "replyToId": null
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | `string` | 是 | 视频ID |
| `content` | `string` | 是 | 评论内容 |
| `parentId` | `string` | 否 | 父评论ID（根评论时传null，回复时传根评论ID） |
| `replyToId` | `string` | 否 | 回复用户ID（直接回复根评论时传null，回复某条评论时传该评论的用户ID） |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "3",
    "content": "评论内容",
    "createdAt": "2026-02-15 10:40:00",
    "likes": 0,
    "isLiked": false,
    "author": {
      "id": "1",
      "name": "用户1",
      "avatar": ""
    }
  }
}
```

#### 失败响应（未登录）

```json
{
  "code": 500,
  "msg": "请先登录",
  "data": null
}
```

#### 失败响应（参数校验）

```json
{
  "code": 500,
  "msg": "视频ID不能为空",
  "data": null
}
```

---

### 9. 切换评论点赞状态（前台）

#### 接口信息
- **URL**: `POST /api/document/docVideoComment/comment/like/{commentId}`
- **功能**: 切换评论点赞状态（已登录用户可用）。点赞数从Redis获取，不再同步到数据库。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `commentId` | `string` | 是 | 评论ID |

#### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "status": true,
    "count": 6
  }
}
```

#### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `success` | `boolean` | 操作是否成功 |
| `status` | `boolean` | 当前点赞状态（true为已点赞） |
| `count` | `number` | 点赞总数（从Redis实时获取） |

#### 失败响应（未登录）

```json
{
  "code": 500,
  "msg": "请先登录",
  "data": null
}
```

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
