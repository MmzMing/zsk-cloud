# Token存储逻辑变更说明

## 变更概述

本次变更将Token存储逻辑从基于UUID的单Token模式改为基于用户ID的多Token集合模式，支持同一用户多设备登录。

## 变更日期

2026-04-22

## 变更原因

1. 支持同一用户多设备登录（最多5个设备）
2. 简化Token管理逻辑，移除UUID中间层
3. 提升Token验证效率

## 存储结构变更

### 变更前

```
key: zsk:login:token:{uuid}          → value: userId
key: zsk:login:roles:{uuid}          → value: Set<String>
key: zsk:login:permissions:{uuid}    → value: Set<String>
```

### 变更后

```
key: zsk:login:token:{userId}        → value: Set<String> (token集合，最多5个)
key: zsk:login:roles:{userId}        → value: Set<String>
key: zsk:login:permissions:{userId}  → value: Set<String>
```

## 核心变更点

### 1. AuthServiceImpl.generateAccessToken

**变更内容：**
- 移除UUID生成逻辑
- JWT Claims中不再包含`user_key`字段
- 使用`userId`作为Redis Key
- Token存储在Set集合中
- 支持最多5个Token，超过时删除最旧的

**关键代码：**
```java
String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;
Long tokenCount = redisService.getSetSize(tokenKey);
if (tokenCount != null && tokenCount >= 5) {
    Set<String> tokens = redisService.getCacheSet(tokenKey);
    if (tokens != null && !tokens.isEmpty()) {
        String oldestToken = tokens.iterator().next();
        redisService.removeSetCacheObject(tokenKey, oldestToken);
    }
}
redisService.setSetCacheObject(tokenKey, token);
```

### 2. AuthServiceImpl.refreshTokenTime

**变更内容：**
- 从JWT中直接获取`userId`
- 验证Token是否在用户的Token集合中
- 刷新整个用户Token集合的过期时间

### 3. AuthServiceImpl.logout

**变更内容：**
- 从JWT中获取`userId`
- 从用户的Token集合中删除指定Token
- 如果用户没有任何Token，删除roles和permissions缓存

### 4. AuthFilter.filter

**变更内容：**
- 从JWT中获取`userId`
- 验证Token是否在用户的Token集合中
- 使用`userId`获取roles和permissions
- 请求头中的`X-User-Key`改为传递`userId`

### 5. SysLoginManageServiceImpl

**变更内容：**
- `listOnlineUsers`: 遍历所有用户，获取每个用户的Token集合
- `forceLogout`: sessionId格式改为`{userId}:{token}`
- `refreshSession`: sessionId格式改为`{userId}:{token}`

### 6. RedisService

**新增方法：**
- `setSetCacheObject`: 向Set集合添加元素
- `getSetSize`: 获取Set集合大小
- `removeSetCacheObject`: 从Set集合移除元素
- `isMemberOfSet`: 判断元素是否在Set集合中

### 7. JwtUtils

**新增方法：**
- `getUserIdAsLong`: 获取Long类型的用户ID

## 会话ID格式变更

### 变更前
```
sessionId = uuid (例如: a1b2c3d4e5f6g7h8)
```

### 变更后
```
sessionId = {userId}:{token} (例如: 1:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...)
```

## 影响范围

### 需要适配的功能模块

1. **在线用户管理**
   - 在线用户列表展示（每个Token对应一个会话）
   - 强制下线功能
   - 会话刷新功能

2. **前端适配**
   - 在线用户管理页面需要适配新的sessionId格式

### 不受影响的功能

1. JWT生成和解析逻辑（仅Claims内容变化）
2. 权限验证逻辑
3. 业务逻辑层

## 数据迁移

本次变更不涉及数据迁移，因为：
1. Token存储在Redis中，重启后自动失效
2. 用户重新登录即可使用新的Token机制

## 测试要点

1. **多设备登录测试**
   - 同一用户在多个设备登录
   - 验证最多5个设备限制
   - 验证超过5个设备时删除最旧Token

2. **Token验证测试**
   - 验证Token在集合中的正确性
   - 验证Token过期后的清理

3. **登出测试**
   - 单设备登出
   - 多设备逐个登出
   - 验证最后一个Token登出后清理roles和permissions

4. **在线用户管理测试**
   - 在线用户列表展示
   - 强制下线功能
   - 会话刷新功能

## 注意事项

1. **Redis Set的局限性**
   - Redis Set是无序的，删除"最旧的"Token实际上是随机删除
   - 如需精确删除最旧Token，建议改用Redis Sorted Set（score为时间戳）

2. **性能考虑**
   - Token验证需要从Set集合中判断是否存在，时间复杂度O(1)
   - 获取在线用户列表需要遍历所有用户，性能影响可接受

3. **兼容性**
   - 旧Token在变更后无法验证，用户需要重新登录
   - 建议在低峰期进行部署

## 后续优化建议

1. 使用Redis Sorted Set替代Set，实现精确的最旧Token删除
2. 考虑增加Token设备标识，支持按设备管理Token
3. 增加Token活跃度统计，优化Token清理策略
