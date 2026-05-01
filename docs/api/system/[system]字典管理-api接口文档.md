# 字典管理 API接口文档

## 1. 接口列表

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| /api/system/dict/type/list | GET | SysDictTypeController.java | 查询字典类型列表 |
| /api/system/dict/type/page | GET | SysDictTypeController.java | 分页查询字典类型列表 |
| /api/system/dict/type/{id} | GET | SysDictTypeController.java | 获取字典类型详细信息 |
| /api/system/dict/type | POST | SysDictTypeController.java | 新增字典类型 |
| /api/system/dict/type | PUT | SysDictTypeController.java | 修改字典类型 |
| /api/system/dict/type/{ids} | DELETE | SysDictTypeController.java | 删除字典类型 |
| /api/system/dict/type/version/{dictType} | GET | SysDictTypeController.java | 获取指定字典类型的缓存版本号 |
| /api/system/dict/type/cache/warmUp | POST | SysDictTypeController.java | 缓存预热 |
| /api/system/dict/type/cache/tags | GET | SysDictTypeController.java | 获取所有已缓存的字典类型标签 |
| /api/system/dict/type/cache/tag/{tag} | GET | SysDictTypeController.java | 按标签获取缓存（带版本号） |
| /api/system/dict/type/cache/all | GET | SysDictTypeController.java | 获取全部缓存数据（带版本号） |
| /api/system/dict/type/cache/refresh/{dictType} | POST | SysDictTypeController.java | 刷新指定字典类型的缓存 |
| /api/system/dict/type/cache/{dictType} | DELETE | SysDictTypeController.java | 删除指定字典类型的缓存 |
| /api/system/dict/type/cache/all | DELETE | SysDictTypeController.java | 清空所有字典缓存 |
| /api/system/dict/data/list | GET | SysDictDataController.java | 查询字典数据列表 |
| /api/system/dict/data/page | GET | SysDictDataController.java | 分页查询字典数据列表 |
| /api/system/dict/data/type/{dictType} | GET | SysDictDataController.java | 根据字典类型查询数据 |
| /api/system/dict/data/{id} | GET | SysDictDataController.java | 获取字典数据详细信息 |
| /api/system/dict/data | POST | SysDictDataController.java | 新增字典数据 |
| /api/system/dict/data | PUT | SysDictDataController.java | 修改字典数据 |
| /api/system/dict/data/{ids} | DELETE | SysDictDataController.java | 删除字典数据 |
| /api/system/dict/data/toggleStatus | PUT | SysDictDataController.java | 切换字典状态 |
| /api/system/dict/data/batchToggleStatus | PUT | SysDictDataController.java | 批量切换字典状态 |

## 1.1 数据模型

### DictCacheVO（带版本号的字典数据）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| version | long | 缓存版本号（时间戳），用于前端判断本地缓存是否需要刷新 |
| data | Array&lt;SysDictData&gt; | 字典数据列表 |

**响应示例**:

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "version": 1714521600000,
    "data": [
      {
        "id": 1,
        "dictType": "sys_user_sex",
        "dictLabel": "男",
        "dictValue": "0",
        "dictSort": 1,
        "status": "0"
      },
      {
        "id": 2,
        "dictType": "sys_user_sex",
        "dictLabel": "女",
        "dictValue": "1",
        "dictSort": 2,
        "status": "0"
      }
    ]
  }
}
```

## 1.2 前端缓存使用指南

前端通过版本号机制实现字典数据缓存和增量更新，减少重复请求。

**使用流程**:

```javascript
// 1. 检查缓存版本
const verRes = await fetch('/api/system/dict/type/version/sys_user_sex');
const serverVer = await verRes.json().data; // 缓存不存在返回 0
const localVer = localStorage.getItem('dict_ver_sys_user_sex') || '0';

// 2. 版本变更时拉取最新数据
if (serverVer > parseInt(localVer)) {
  const dataRes = await fetch('/api/system/dict/type/cache/tag/sys_user_sex');
  const { version, data } = await dataRes.json().data;
  localStorage.setItem('dict_ver_sys_user_sex', version);
  localStorage.setItem('dict_data_sys_user_sex', JSON.stringify(data));
} else {
  // 3. 使用本地缓存
  const data = JSON.parse(localStorage.getItem('dict_data_sys_user_sex'));
}
```

**一次性加载所有字典**:

```javascript
// 调用 GET /api/system/dict/type/cache/all
const res = await fetch('/api/system/dict/type/cache/all');
const allDicts = await res.json().data;
// allDicts = {
//   "sys_user_sex": { "version": 1714521600000, "data": [...] },
//   "sys_yes_no":   { "version": 1714521600000, "data": [...] }
// }

Object.entries(allDicts).forEach(([type, { version, data }]) => {
  localStorage.setItem(`dict_ver_${type}`, version);
  localStorage.setItem(`dict_data_${type}`, JSON.stringify(data));
});
```

## 2. 接口详情

### 2.1 查询字典类型列表

**路径**: `GET /api/system/dict/type/list`

**功能描述**: 查询字典类型列表

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictName | string | 否 | 字典名称（模糊查询） |
| dictType | string | 否 | 字典类型（模糊查询） |
| status | string | 否 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "dictName": "用户状态",
      "dictType": "user_status",
      "status": "0",
      "remark": "用户状态字典"
    }
  ]
}
```

---

### 2.2 新增字典类型

**路径**: `POST /api/system/dict/type`

**功能描述**: 新增字典类型

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictName | string | 是 | 字典名称 |
| dictType | string | 是 | 字典类型标识 |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "dictName": "用户状态",
  "dictType": "user_status",
  "status": "0",
  "remark": "用户状态字典"
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

### 2.3 根据字典类型查询字典数据

**路径**: `GET /api/system/dict/data/type/{dictType}`

**功能描述**: 根据字典类型查询字典数据列表

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型标识 |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1",
      "dictType": "user_status",
      "dictLabel": "正常",
      "dictValue": "0",
      "dictSort": 1,
      "status": "0"
    },
    {
      "id": "2",
      "dictType": "user_status",
      "dictLabel": "停用",
      "dictValue": "1",
      "dictSort": 2,
      "status": "0"
    }
  ]
}
```

---

### 2.4 新增字典数据

**路径**: `POST /api/system/dict/data`

**功能描述**: 新增字典数据

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dictType | string | 是 | 字典类型标识 |
| dictLabel | string | 是 | 字典标签（显示值） |
| dictValue | string | 是 | 字典值（实际值） |
| dictSort | int | 否 | 排序，默认0 |
| status | string | 否 | 状态，默认0（正常） |
| remark | string | 否 | 备注 |

**请求示例**:

```json
{
  "dictType": "user_status",
  "dictLabel": "正常",
  "dictValue": "0",
  "dictSort": 1,
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

### 2.5 切换字典状态

**路径**: `PUT /api/system/dict/data/toggleStatus`

**功能描述**: 切换字典数据状态

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 是 | 字典ID |
| status | string | 是 | 状态（0正常 1停用） |

**成功响应** (200):

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```