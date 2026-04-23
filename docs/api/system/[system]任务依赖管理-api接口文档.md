# [system]任务依赖管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/task/link/list | GET | SysTaskLinkController.java | 获取任务依赖关系列表 |
| /api/system/task/link | POST | SysTaskLinkController.java | 创建任务依赖 |
| /api/system/task/link/{ids} | DELETE | SysTaskLinkController.java | 删除任务依赖 |

## 2. 接口详情

### 2.1 获取任务依赖关系列表

**路径**: `GET /api/system/task/link/list`

**功能描述**: 获取全部任务依赖关系

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |

**请求参数**: 无

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "source": 1,
      "target": 2,
      "type": "0"
    }
  ]
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| data | Array | 任务依赖关系列表 |
| data[].id | Long | 依赖关系唯一ID |
| data[].source | Long | 源任务ID（前驱任务） |
| data[].target | Long | 目标任务ID（后继任务） |
| data[].type | String | 依赖类型：0-完成开始 / 1-开始开始 / 2-完成完成 / 3-开始完成（dhtmlx原生格式） |

---

### 2.2 创建任务依赖

**路径**: `POST /api/system/task/link`

**功能描述**: 创建任务依赖关系，支持循环依赖检测

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |
| Content-Type | string | 是 | `application/json` |

**请求体**:

```json
{
  "source": 1,
  "target": 2,
  "type": "0"
}
```

**请求体参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| source | Long | 是 | 源任务ID（前驱任务） |
| target | Long | 是 | 目标任务ID（后继任务） |
| type | String | 是 | 依赖类型：0-完成开始(e2s) / 1-开始开始(s2s) / 2-完成完成(e2e) / 3-开始完成(s2e)（dhtmlx原生格式） |

**依赖类型说明**（dhtmlx原生格式）:

| 类型值 | 含义 | 说明 |
|--------|------|------|
| 0 | 完成开始 | 源任务完成后，目标任务才能开始（最常用） |
| 1 | 开始开始 | 源任务开始后，目标任务才能开始 |
| 2 | 完成完成 | 源任务完成后，目标任务才能完成 |
| 3 | 开始完成 | 源任务开始后，目标任务才能完成 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "source": 1,
    "target": 2,
    "type": "0"
  }
}
```

**失败响应** (400 - 循环依赖):

```json
{
  "code": 400,
  "msg": "检测到循环依赖，无法创建依赖关系",
  "data": null
}
```

**失败响应** (400 - 参数错误):

```json
{
  "code": 400,
  "msg": "参数校验失败：source: 源任务ID不能为空",
  "data": null
}
```

---

### 2.3 删除任务依赖

**路径**: `DELETE /api/system/task/link/{ids}`

**功能描述**: 批量删除任务依赖关系

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 依赖关系ID列表，格式: `/api/system/task/link/1,2,3` |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "依赖ID不能为空",
  "data": null
}
```

---

## 3. 状态码定义

| 状态码 | 含义 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 / 循环依赖 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 4. 循环依赖检测说明

系统会自动检测以下循环依赖场景：

| 场景 | 示例 | 说明 |
|------|------|------|
| 自依赖 | source=1, target=1 | 任务不能依赖自身 |
| 直接循环 | A→B, B→A | 形成直接循环依赖 |
| 间接循环 | A→B, B→C, C→A | 形成间接循环依赖链 |

当检测到循环依赖时，接口会返回 `code=400` 错误，不允许创建该依赖关系。