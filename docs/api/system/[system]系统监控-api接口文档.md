# 系统监控 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/monitor/data | GET | SysMonitorController.java | 获取服务器实时监控数据 |
| /api/system/monitor/overview | GET | SysMonitorController.java | 获取服务器监控概览 |
| /api/system/monitor/trend | GET | SysMonitorController.java | 获取服务器监控趋势 |
| /api/system/monitor/collect | POST | SysMonitorController.java | 手动触发监控数据采集 |
| /api/system/monitor/clean | DELETE | SysMonitorController.java | 清理过期监控数据 |

## 2. 接口详情

### 2.1 获取服务器实时监控数据

**路径**: `GET /api/system/monitor/data`

**功能描述**: 获取服务器实时监控数据点列表

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "time": "2026-02-15 10:00:00",
      "value": 25.5,
      "metric": "cpu"
    },
    {
      "time": "2026-02-15 10:00:00",
      "value": 60.2,
      "metric": "memory"
    },
    {
      "time": "2026-02-15 10:00:00",
      "value": 45.8,
      "metric": "disk"
    },
    {
      "time": "2026-02-15 10:00:00",
      "value": 12.3,
      "metric": "network"
    },
    {
      "time": "2026-02-15 10:00:00",
      "value": 55.6,
      "metric": "jvmHeap"
    }
  ]
}
```

---

### 2.2 获取服务器监控概览

**路径**: `GET /api/system/monitor/overview`

**功能描述**: 获取服务器监控概览数据

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "cpu": 25.5,
    "memory": 60.2,
    "disk": 45.8,
    "network": 12.3,
    "jvmHeap": 55.6,
    "jvmThread": 128,
    "hostName": "zsk-server",
    "hostIp": "192.168.1.100",
    "osName": "Linux"
  }
}
```

---

### 2.3 获取服务器监控趋势

**路径**: `GET /api/system/monitor/trend`

**功能描述**: 获取指定指标的监控趋势数据

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| metric | string | 否 | 指标类型：cpu/memory/disk/network/jvmHeap/jvmThread，默认cpu |
| range | string | 否 | 时间范围：1h/6h/24h/7d，默认1h |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "time": "2026-02-15 09:00:00",
      "value": 22.3,
      "metric": "cpu"
    },
    {
      "time": "2026-02-15 09:30:00",
      "value": 28.5,
      "metric": "cpu"
    },
    {
      "time": "2026-02-15 10:00:00",
      "value": 25.5,
      "metric": "cpu"
    }
  ]
}
```

---

### 2.4 手动触发监控数据采集

**路径**: `POST /api/system/monitor/collect`

**功能描述**: 手动触发一次监控数据采集

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.5 清理过期监控数据

**路径**: `DELETE /api/system/monitor/clean`

**功能描述**: 清理指定天数前的过期监控数据

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| days | Integer | 否 | 保留天数，默认7天 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```