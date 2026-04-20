# 缓存管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/cache/instances | GET | CacheSysController.java | 获取缓存实例列表 |
| /api/system/cache/logs | GET | CacheSysController.java | 获取缓存日志 |
| /api/system/cache/trend/hitRate | GET | CacheSysController.java | 获取缓存命中率趋势 |
| /api/system/cache/trend/qps | GET | CacheSysController.java | 获取缓存QPS趋势 |
| /api/system/cache/info | GET | CacheSysController.java | 获取Redis信息 |
| /api/system/cache/keys | GET | CacheSysController.java | 获取缓存键名列表 |
| /api/system/cache/list | GET | CacheSysController.java | 获取缓存信息列表 |
| /api/system/cache/info/{cacheKey} | GET | CacheSysController.java | 获取缓存详细信息 |
| /api/system/cache/value/{cacheKey} | GET | CacheSysController.java | 获取缓存值 |
| /api/system/cache/exists/{cacheKey} | GET | CacheSysController.java | 判断缓存是否存在 |
| /api/system/cache/statistics | GET | CacheSysController.java | 获取缓存统计信息 |
| /api/system/cache/keys/refresh | POST | CacheSysController.java | 刷新缓存键 |
| /api/system/cache/keys/batchRefresh | POST | CacheSysController.java | 批量刷新缓存键 |
| /api/system/cache/keys/batchDelete | POST | CacheSysController.java | 批量删除缓存键 |
| /api/system/cache/warmup | POST | CacheSysController.java | 缓存预热 |
| /api/system/cache/instances/clear | POST | CacheSysController.java | 清空缓存实例 |
| /api/system/cache/refreshTtl | PUT | CacheSysController.java | 刷新缓存过期时间 |
| /api/system/cache/refreshTtlBatch | PUT | CacheSysController.java | 批量刷新缓存过期时间 |
| /api/system/cache/keys/{key} | DELETE | CacheSysController.java | 删除缓存键 |
| /api/system/cache/delete | DELETE | CacheSysController.java | 删除缓存 |
| /api/system/cache/clear/{cacheName} | DELETE | CacheSysController.java | 清空指定名称的缓存 |
| /api/system/cache/clearAll | DELETE | CacheSysController.java | 清空所有缓存 |

## 2. 接口详情

### 2.1 获取缓存键名列表

**路径**: `GET /api/system/cache/keys`

**功能描述**: 获取缓存键名列表，支持分页和关键字搜索

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cacheName | string | 否 | 缓存名称 |
| keyword | string | 否 | 关键字搜索 |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页大小，默认10 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "keys": ["user:1", "user:2", "config:site"],
    "total": 3
  }
}
```

---

### 2.2 获取缓存值

**路径**: `GET /api/system/cache/value/{cacheKey}`

**功能描述**: 获取指定缓存键的值

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cacheKey | string | 是 | 缓存键名 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "userName": "admin",
    "nickName": "管理员"
  }
}
```

---

### 2.3 删除缓存键

**路径**: `DELETE /api/system/cache/keys/{key}`

**功能描述**: 删除指定的缓存键

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| key | string | 是 | 缓存键名 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 2.4 清空所有缓存

**路径**: `DELETE /api/system/cache/clearAll`

**功能描述**: 清空所有缓存

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": 100
}
```

---

### 2.5 缓存预热

**路径**: `POST /api/system/cache/warmup`

**功能描述**: 执行缓存预热

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cacheNames | List<string> | 否 | 需要预热的缓存名称列表，不传则预热所有 |

**请求示例**:

```json
["userCache", "configCache"]
```

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "successCount": 2,
    "failCount": 0,
    "message": "缓存预热完成"
  }
}
```