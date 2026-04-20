# [document] 文档统计 API 接口文档

## 接口列表

| API 路径 | HTTP 方法 | 所属文件 | 功能描述 |
| :--- | :--- | :--- | :--- |
| `/api/document/statistics/overview` | GET | `DocStatisticsController.java` | 获取文档统计概览数据 |
| `/api/document/statistics/traffic` | GET | `DocStatisticsController.java` | 获取流量统计数据 |
| `/api/document/statistics/trend` | GET | `DocStatisticsController.java` | 获取访问量趋势数据 |
| `/api/document/statistics/analysis/metrics` | GET | `DocStatisticsController.java` | 获取分析指标数据 |
| `/api/document/statistics/analysis/time-distribution` | GET | `DocStatisticsController.java` | 获取时间分布数据 |

---

## 1. 获取文档统计概览数据

### 接口信息
- **URL**: `GET /api/document/statistics/overview`
- **功能**: 获取文档统计概览数据

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalDocuments": 1000,
    "totalVideos": 500,
    "totalViews": 100000,
    "totalLikes": 10000,
    "totalComments": 5000,
    "totalCollections": 8000,
    "todayViews": 1000,
    "todayDocuments": 10,
    "todayVideos": 5
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `totalDocuments` | `number` | 文档总数 |
| `totalVideos` | `number` | 视频总数 |
| `totalViews` | `number` | 总浏览量 |
| `totalLikes` | `number` | 总点赞数 |
| `totalComments` | `number` | 总评论数 |
| `totalCollections` | `number` | 总收藏数 |
| `todayViews` | `number` | 今日浏览量 |
| `todayDocuments` | `number` | 今日新增文档数 |
| `todayVideos` | `number` | 今日新增视频数 |

---

## 2. 获取流量统计数据

### 接口信息
- **URL**: `GET /api/document/statistics/traffic`
- **功能**: 获取流量统计数据

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `range` | `string` | 否 | day | 时间维度：day（日）、week（周）、month（月） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "date": "2026-02-15",
      "views": 1000,
      "visitors": 500,
      "newVisitors": 200
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `date` | `string` | 日期 |
| `views` | `number` | 浏览量 |
| `visitors` | `number` | 访客数 |
| `newVisitors` | `number` | 新访客数 |

---

## 3. 获取访问量趋势数据

### 接口信息
- **URL**: `GET /api/document/statistics/trend`
- **功能**: 获取访问量趋势数据

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `range` | `string` | 否 | day | 时间维度：day（日）、week（周） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "time": "00:00",
      "views": 100,
      "documents": 50,
      "videos": 50
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `time` | `string` | 时间点 |
| `views` | `number` | 浏览量 |
| `documents` | `number` | 文档浏览量 |
| `videos` | `number` | 视频播放量 |

---

## 4. 获取分析指标数据

### 接口信息
- **URL**: `GET /api/document/statistics/analysis/metrics`
- **功能**: 获取分析指标数据

### 请求参数
无

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "name": "平均阅读时长",
      "value": "5分钟",
      "unit": "分钟",
      "trend": "up",
      "change": "+10%"
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `name` | `string` | 指标名称 |
| `value` | `string` | 指标值 |
| `unit` | `string` | 单位 |
| `trend` | `string` | 趋势（up上升，down下降，same持平） |
| `change` | `string` | 变化幅度 |

---

## 5. 获取时间分布数据

### 接口信息
- **URL**: `GET /api/document/statistics/analysis/time-distribution`
- **功能**: 获取时间分布数据

### 查询参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `date` | `string` | 否 | 今日 | 日期（yyyy-MM-dd格式） |
| `step` | `string` | 否 | hour | 步长：hour（小时）、half-hour（半小时） |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "time": "00:00",
      "count": 100,
      "percentage": 5
    }
  ]
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `time` | `string` | 时间段 |
| `count` | `number` | 访问次数 |
| `percentage` | `number` | 占比（%） |

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
| `data` | `object/array` | 响应数据 |
