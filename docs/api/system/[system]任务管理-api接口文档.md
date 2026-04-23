# [system]任务管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/task/list | GET | SysTaskController.java | 获取任务列表（含依赖关系） |
| /api/system/task/{id} | GET | SysTaskController.java | 获取单个任务详情 |
| /api/system/task | POST | SysTaskController.java | 创建任务 |
| /api/system/task | PUT | SysTaskController.java | 更新任务 |
| /api/system/task/{ids} | DELETE | SysTaskController.java | 删除任务（含子任务及相关依赖关系） |

## 2. 接口详情

### 2.1 获取任务列表（含依赖关系）

**路径**: `GET /api/system/task/list`

**功能描述**: 返回所有任务及其依赖关系，用于 Gantt 图渲染

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
  "data": {
    "tasks": [
      {
        "id": 1,
        "text": "任务名称",
        "startDate": "2026-04-30",
        "duration": 5,
        "progress": 50,
        "type": "task",
        "parent": 0,
        "open": true,
        "details": "任务描述",
        "color": "#3DB9D3"
      }
    ],
    "links": [
      {
        "id": 1,
        "source": 1,
        "target": 2,
        "type": "0"
      }
    ]
  }
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| data.tasks | Array | 任务列表（扁平结构，通过 parent 表示层级） |
| data.tasks[].id | Long | 任务唯一ID |
| data.tasks[].text | String | 任务名称 |
| data.tasks[].startDate | String | 开始时间，格式：yyyy-MM-dd |
| data.tasks[].duration | Integer | 持续天数 |
| data.tasks[].progress | Integer | 完成进度 0-100 |
| data.tasks[].type | String | 任务类型：task / project / milestone |
| data.tasks[].parent | Long | 父任务ID，顶级为 0，可选 |
| data.tasks[].open | Boolean | 是否默认展开 |
| data.tasks[].details | String | 任务描述/备注 |
| data.tasks[].color | String | 任务颜色，默认：project-#7B68EE(紫)、task-#3DB9D3(青)、milestone-#FFA500(橙) |
| data.links | Array | 任务依赖关系列表 |
| data.links[].id | Long | 依赖关系唯一ID |
| data.links[].source | Long | 源任务ID（前驱任务） |
| data.links[].target | Long | 目标任务ID（后继任务） |
| data.links[].type | String | 依赖类型：0-完成开始 / 1-开始开始 / 2-完成完成 / 3-开始完成 |

---

### 2.2 获取单个任务详情

**路径**: `GET /api/system/task/{id}`

**功能描述**: 根据任务ID获取单个任务的详细信息

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 任务ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "text": "任务名称",
    "startDate": "2026-04-30",
    "duration": 5,
    "progress": 50,
    "type": "task",
    "parent": 0,
    "open": true,
    "details": "任务描述",
    "color": "#3DB9D3"
  }
}
```

**失败响应** (404):

```json
{
  "code": 404,
  "msg": "任务不存在",
  "data": null
}
```

---

### 2.3 创建任务

**路径**: `POST /api/system/task`

**功能描述**: 创建新任务

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |
| Content-Type | string | 是 | `application/json` |

**请求体**:

```json
{
  "text": "任务名称",
  "startDate": "2026-04-30",
  "duration": 5,
  "progress": 0,
  "type": "task",
  "parent": 0,
  "details": "任务描述",
  "color": "#3DB9D3"
}
```

**请求体参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 任务名称 |
| startDate | String | 是 | 开始时间，格式：yyyy-MM-dd（如：2026-04-30） |
| duration | Integer | 是 | 持续天数，不能为负数 |
| progress | Integer | 否 | 完成进度 0-100，默认 0 |
| type | String | 是 | 任务类型：task-普通任务 / project-项目 / milestone-里程碑 |
| parent | Long | 否 | 父任务ID，可选（顶级任务不传或传0） |
| details | String | 否 | 任务描述/备注 |
| color | String | 否 | 任务颜色，可选，不传则按类型取默认色 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "text": "任务名称",
    "startDate": "2026-04-30",
    "duration": 5,
    "progress": 0,
    "type": "task",
    "parent": 0,
    "open": true,
    "details": "任务描述",
    "color": "#3DB9D3"
  }
}
```

**失败响应** (400):

```json
{
  "code": 400,
  "msg": "参数校验失败：text: 任务名称不能为空",
  "data": null
}
```

---

### 2.4 更新任务

**路径**: `PUT /api/system/task`

**功能描述**: 更新任务信息（支持全量更新和部分更新）

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |
| Content-Type | string | 是 | `application/json` |

**请求体**:

```json
{
  "id": 1,
  "text": "更新后的任务名称",
  "startDate": "2026-04-30",
  "duration": 10,
  "progress": 75,
  "type": "task",
  "parent": 0,
  "details": "更新后的任务描述",
  "color": "#7B68EE"
}
```

**请求体参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 任务ID |
| text | String | 否 | 任务名称 |
| startDate | String | 否 | 开始时间，格式：yyyy-MM-dd（如：2026-04-30） |
| duration | Integer | 否 | 持续天数，不能为负数 |
| progress | Integer | 否 | 完成进度 0-100 |
| type | String | 否 | 任务类型：task-普通任务 / project-项目 / milestone-里程碑 |
| parent | Long | 否 | 父任务ID（拖拽改变层级时传），可选 |
| details | String | 否 | 任务描述/备注 |
| color | String | 否 | 任务颜色，可选 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

**失败响应** (404):

```json
{
  "code": 404,
  "msg": "任务不存在",
  "data": null
}
```

---

### 2.5 删除任务

**路径**: `DELETE /api/system/task/{ids}`

**功能描述**: 批量删除任务（含子任务及相关依赖关系）

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token，格式: `Bearer {token}` |

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 任务ID列表，格式: `/api/system/task/1,2,3` |

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
  "msg": "任务ID不能为空",
  "data": null
}
```

---

## 3. 状态码定义

| 状态码 | 含义 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |