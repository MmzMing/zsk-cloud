# 任务管理（Gantt）API接口文档

> 本文档供后端开发参考，定义仪表盘「任务管理」Gantt 图所需的全部接口。
> 前端使用 `@svar-ui/react-gantt` 组件，数据模型需严格匹配下方字段定义。

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/task/list | GET | SysTaskController.java | 获取任务列表（含层级关系） |
| /api/system/task/{id} | GET | SysTaskController.java | 获取单个任务详情 |
| /api/system/task | POST | SysTaskController.java | 创建任务 |
| /api/system/task | PUT | SysTaskController.java | 更新任务 |
| /api/system/task/{ids} | DELETE | SysTaskController.java | 删除任务 |
| /api/system/task/link/list | GET | SysTaskLinkController.java | 获取任务依赖关系列表 |
| /api/system/task/link | POST | SysTaskLinkController.java | 创建任务依赖 |
| /api/system/task/link/{ids} | DELETE | SysTaskLinkController.java | 删除任务依赖 |

## 2. 数据模型

### 2.1 任务（Task）

> 对应 Gantt 组件 `ITask` 接口，前端直接将 `data[]` 传入 `<Gantt tasks={data} />`。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 任务唯一ID |
| text | string | 是 | 任务名称 |
| start | string | 是 | 开始时间，ISO 8601 格式（`2026-04-20T00:00:00`），前端转为 `Date` |
| duration | number | 是 | 持续天数（单位：天） |
| progress | number | 是 | 完成进度，0-100 整数 |
| type | string | 是 | 任务类型：`task`-普通任务 / `summary`-汇总任务 / `milestone`-里程碑 |
| parent | number | 否 | 父任务ID，顶级任务传 `0` 或不传 |
| open | boolean | 否 | 汇总任务是否默认展开，默认 `true` |
| details | string | 否 | 任务描述/备注 |

**type 说明**：

| 值 | 含义 | 特征 |
|------|------|------|
| `task` | 普通任务 | 有开始时间和持续天数 |
| `summary` | 汇总任务（父级） | 自身不设结束日期，时间范围由子任务自动计算 |
| `milestone` | 里程碑 | `duration` 为 `0`，仅标记时间点 |

### 2.2 任务依赖（Link）

> 对应 Gantt 组件 `ILink` 接口，前端直接将 `data[]` 传入 `<Gantt links={data} />`。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 依赖关系唯一ID |
| source | number | 是 | 源任务ID（前驱任务） |
| target | number | 是 | 目标任务ID（后继任务） |
| type | string | 是 | 依赖类型，见下表 |

**type 取值**：

| 值 | 含义 |
|------|------|
| `e2s` | 完成-开始（Finish to Start），最常用 |
| `s2s` | 开始-开始（Start to Start） |
| `e2e` | 完成-完成（Finish to Finish） |
| `s2e` | 开始-完成（Start to Finish） |

### 2.3 数据库表设计参考

**sys_task 表**：

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tenant_id | BIGINT | 租户ID |
| text | VARCHAR(200) | 任务名称 |
| start_time | DATETIME | 开始时间 |
| duration | INT | 持续天数 |
| progress | INT | 进度 0-100 |
| type | VARCHAR(20) | task / summary / milestone |
| parent_id | BIGINT | 父任务ID，顶级为 0 |
| open_flag | TINYINT(1) | 是否展开，默认 1 |
| details | VARCHAR(500) | 备注 |
| deleted | TINYINT(1) | 是否已删除(0否 1是) |
| create_name | VARCHAR(100) | 创建者姓名 |
| create_time | DATETIME | 创建时间 |
| update_name | VARCHAR(100) | 更新者姓名 |
| update_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

**sys_task_link 表**：

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| source_id | BIGINT | 源任务ID |
| target_id | BIGINT | 目标任务ID |
| type | VARCHAR(10) | e2s / s2s / e2e / s2e |
| deleted | TINYINT(1) | 是否已删除(0否 1是) |
| create_name | VARCHAR(100) | 创建者姓名 |
| create_time | DATETIME | 创建时间 |
| update_name | VARCHAR(100) | 更新者姓名 |
| update_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

## 3. 接口详情

### 3.1 获取任务列表

**路径**: `GET /api/system/task/list`

**功能描述**: 获取全部任务及依赖关系，用于 Gantt 图渲染

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer token |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "tasks": [
      {
        "id": 1,
        "text": "知识库 v2.1 开发",
        "start": "2026-04-20T00:00:00",
        "duration": 15,
        "progress": 45,
        "type": "summary",
        "parent": 0,
        "open": true
      },
      {
        "id": 2,
        "text": "仪表盘模块开发",
        "start": "2026-04-20T00:00:00",
        "duration": 5,
        "progress": 80,
        "type": "task",
        "parent": 1,
        "open": true
      },
      {
        "id": 3,
        "text": "知识图谱功能",
        "start": "2026-04-25T00:00:00",
        "duration": 4,
        "progress": 20,
        "type": "task",
        "parent": 1,
        "open": true
      },
      {
        "id": 4,
        "text": "v2.1 正式发布",
        "start": "2026-05-05T00:00:00",
        "duration": 0,
        "progress": 0,
        "type": "milestone",
        "parent": 0,
        "open": true
      }
    ],
    "links": [
      {
        "id": 1,
        "source": 2,
        "target": 3,
        "type": "e2s"
      }
    ]
  }
}
```

**响应字段说明**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.tasks | Task[] | 任务列表（扁平结构，通过 parent 表示层级） |
| data.links | Link[] | 任务依赖关系列表 |

> **重要**：`tasks` 返回扁平数组，不是嵌套树。Gantt 组件根据 `parent` 字段自动构建层级。

---

### 3.2 获取单个任务详情

**路径**: `GET /api/system/task/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 任务ID |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 2,
    "text": "仪表盘模块开发",
    "start": "2026-04-20T00:00:00",
    "duration": 5,
    "progress": 80,
    "type": "task",
    "parent": 1,
    "open": true,
    "details": "包含概览卡片、统计图表、公告模块"
  }
}
```

---

### 3.3 创建任务

**路径**: `POST /api/system/task`

**请求体**:

```json
{
  "text": "API 接口优化",
  "start": "2026-04-29T00:00:00",
  "duration": 3,
  "progress": 0,
  "type": "task",
  "parent": 1,
  "details": "优化查询性能"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | string | 是 | 任务名称 |
| start | string | 是 | 开始时间 ISO 8601 |
| duration | number | 是 | 持续天数 |
| progress | number | 否 | 进度 0-100，默认 0 |
| type | string | 是 | task / summary / milestone |
| parent | number | 否 | 父任务ID，默认 0 |
| details | string | 否 | 备注 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 5,
    "text": "API 接口优化",
    "start": "2026-04-29T00:00:00",
    "duration": 3,
    "progress": 0,
    "type": "task",
    "parent": 1,
    "open": true
  }
}
```

> 返回创建后的完整任务对象（含生成的 `id`），前端用于更新本地 Gantt 数据。

---

### 3.4 更新任务

**路径**: `PUT /api/system/task`

**功能描述**: 更新任务信息。Gantt 组件拖拽调整时间、修改进度、编辑名称等操作均触发此接口。

**请求体**:

```json
{
  "id": 2,
  "text": "仪表盘模块开发",
  "start": "2026-04-21T00:00:00",
  "duration": 6,
  "progress": 90,
  "type": "task",
  "parent": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 任务ID |
| text | string | 否 | 任务名称 |
| start | string | 否 | 开始时间 |
| duration | number | 否 | 持续天数 |
| progress | number | 否 | 进度 |
| type | string | 否 | 任务类型 |
| parent | number | 否 | 父任务ID（拖拽改变层级时传） |
| details | string | 否 | 备注 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 3.5 删除任务

**路径**: `DELETE /api/system/task/{ids}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | string | 是 | 任务ID，多个用逗号分隔（如 `3,4,5`） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

> **注意**：删除汇总任务（summary）时需同步删除其所有子任务及相关依赖关系。

---

### 3.6 获取任务依赖列表

**路径**: `GET /api/system/task/link/list`

**功能描述**: 获取全部任务依赖关系（通常随 3.1 一并返回，此接口作为独立刷新用）

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    { "id": 1, "source": 2, "target": 3, "type": "e2s" },
    { "id": 2, "source": 3, "target": 4, "type": "e2s" }
  ]
}
```

---

### 3.7 创建任务依赖

**路径**: `POST /api/system/task/link`

**请求体**:

```json
{
  "source": 2,
  "target": 3,
  "type": "e2s"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| source | number | 是 | 源任务ID |
| target | number | 是 | 目标任务ID |
| type | string | 是 | 依赖类型：e2s / s2s / e2e / s2e |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 3,
    "source": 2,
    "target": 3,
    "type": "e2s"
  }
}
```

**校验规则**：
- `source` 和 `target` 不能相同
- 不能创建循环依赖（A→B→C→A）
- 同一对 source-target 不能重复创建

---

### 3.8 删除任务依赖

**路径**: `DELETE /api/system/task/link/{ids}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | string | 是 | 依赖ID，多个用逗号分隔 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

## 4. 错误响应

所有接口统一错误格式：

```json
{
  "code": 500,
  "msg": "具体错误描述",
  "data": null
}
```

常见业务错误码：

| code | 说明 |
|------|------|
| 400 | 参数校验失败 |
| 401 | 未登录或 token 过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

## 5. 前端对接说明

前端收到响应后需要做的数据转换：

```typescript
// 将 ISO 字符串转为 Date 对象（Gantt 组件要求 Date 类型）
const tasks = data.tasks.map(task => ({
  ...task,
  start: new Date(task.start),
}))

// links 无需转换，直接传入
<Gantt tasks={tasks} links={data.links} scales={scales} />
```

**前端调用时机**：
- 页面加载 → 调用 3.1 获取任务列表（包含 tasks + links）
- 用户在 Gantt 中拖拽/编辑 → 调用 3.4 更新任务
- 用户新建任务 → 调用 3.3 创建任务
- 用户删除任务 → 调用 3.5 删除任务
- 用户连线/删线 → 调用 3.7 / 3.8 管理依赖
