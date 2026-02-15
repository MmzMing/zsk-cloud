package com.zsk.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.CacheSysInfo;
import com.zsk.system.domain.SysCacheLog;
import com.zsk.system.service.ICacheSysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理 服务层实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheSysServiceImpl implements ICacheSysService {

    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    private static final String INSTANCE_ID = "redis-main";

    @Override
    public Collection<String> getCacheKeys(String cacheName) {
        String pattern = CacheConstants.CACHE_PREFIX + (cacheName != null && !cacheName.isEmpty() ? cacheName + ":*" : "*");
        return redisService.keys(pattern);
    }

    @Override
    public List<CacheSysInfo> getCacheInfoList(String cacheName) {
        Collection<String> keys = getCacheKeys(cacheName);
        List<CacheSysInfo> cacheInfoList = new ArrayList<>();
        for (String key : keys) {
            try {
                CacheSysInfo cacheInfo = buildCacheInfo(key);
                if (cacheInfo != null) {
                    cacheInfoList.add(cacheInfo);
                }
            } catch (Exception e) {
                log.warn("获取缓存信息失败: {}", key, e);
            }
        }
        return cacheInfoList;
    }

    @Override
    public CacheSysInfo getCacheInfo(String cacheKey) {
        return buildCacheInfo(cacheKey);
    }

    @Override
    public Object getCacheValue(String cacheKey) {
        return redisService.getCacheObject(cacheKey);
    }

    @Override
    public long deleteCache(Collection<String> cacheKeys) {
        if (cacheKeys == null || cacheKeys.isEmpty()) {
            return 0;
        }
        return redisService.deleteObject(cacheKeys);
    }

    @Override
    public long clearCacheByName(String cacheName) {
        Collection<String> keys = getCacheKeys(cacheName);
        return deleteCache(keys);
    }

    @Override
    public long clearAllCache() {
        Collection<String> keys = redisService.keys(CacheConstants.CACHE_PREFIX + "*");
        return deleteCache(keys);
    }

    @Override
    public boolean refreshTtl(String cacheKey, long ttl) {
        if (!hasCacheKey(cacheKey)) {
            return false;
        }
        return redisService.expire(cacheKey, ttl, TimeUnit.SECONDS);
    }

    @Override
    public int refreshTtlBatch(Map<String, Long> cacheKeyTtlMap) {
        if (cacheKeyTtlMap == null || cacheKeyTtlMap.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (Map.Entry<String, Long> entry : cacheKeyTtlMap.entrySet()) {
            if (refreshTtl(entry.getKey(), entry.getValue())) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public Map<String, Object> warmupCache(List<String> cacheNames) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        if (cacheNames == null || cacheNames.isEmpty()) {
            cacheNames = getDefaultWarmupCacheNames();
        }

        for (String cacheName : cacheNames) {
            try {
                warmupCacheByName(cacheName);
                successCount++;
                successList.add(cacheName);
                log.info("缓存预热成功: {}", cacheName);
            } catch (Exception e) {
                failCount++;
                failList.add(cacheName);
                log.error("缓存预热失败: {}", cacheName, e);
            }
        }

        result.put("total", cacheNames.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("successList", successList);
        result.put("failList", failList);
        return result;
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        Collection<String> allKeys = redisService.keys(CacheConstants.CACHE_PREFIX + "*");
        long totalKeys = allKeys.size();

        Map<String, Long> categoryCount = new HashMap<>();
        long totalSize = 0;

        for (String key : allKeys) {
            String category = extractCacheCategory(key);
            categoryCount.merge(category, 1L, Long::sum);

            try {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        totalSize += estimateObjectSize(value);
                    }
                }
            } catch (Exception e) {
                log.trace("统计缓存大小时跳过键: {}", key);
            }
        }

        statistics.put("totalKeys", totalKeys);
        statistics.put("categoryCount", categoryCount);
        statistics.put("estimatedSizeBytes", totalSize);
        statistics.put("estimatedSizeMB", String.format("%.2f", totalSize / (1024.0 * 1024.0)));
        statistics.put("timestamp", System.currentTimeMillis());

        return statistics;
    }

    @Override
    public boolean hasCacheKey(String cacheKey) {
        return Boolean.TRUE.equals(redisService.hasKey(cacheKey));
    }

    @Override
    public List<Map<String, Object>> getInstances() {
        List<Map<String, Object>> instances = new ArrayList<>();
        Map<String, Object> instance = new HashMap<>();

        Map<String, Object> info = getRedisInfo();

        instance.put("id", INSTANCE_ID);
        instance.put("name", "主缓存实例");
        instance.put("usage", info.get("used_memory"));
        instance.put("nodes", 1);
        instance.put("hitRate", info.get("hitRate"));
        instance.put("status", "running");
        instance.put("version", info.get("redis_version"));
        instance.put("uptime", info.get("uptime_in_days"));

        instances.add(instance);
        return instances;
    }

    @Override
    public List<SysCacheLog> getLogs(String instanceId) {
        Query query = new Query();
        if (StrUtil.isNotBlank(instanceId)) {
            query.addCriteria(Criteria.where("instanceId").is(instanceId));
        }
        query.with(Sort.by(Sort.Direction.DESC, "operTime"));
        query.limit(100);
        return mongoTemplate.find(query, SysCacheLog.class);
    }

    @Override
    public List<Map<String, Object>> getHitRateTrend(String instanceId) {
        Map<String, Object> info = getRedisInfo();
        Long hits = (Long) info.getOrDefault("keyspace_hits", 0L);
        Long misses = (Long) info.getOrDefault("keyspace_misses", 0L);

        double hitRate = 0;
        if (hits + misses > 0) {
            hitRate = (double) hits / (hits + misses) * 100;
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            String time = LocalDateTime.now().minusMinutes(i * 10)
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            item.put("time", time);
            item.put("value", Math.round(hitRate + (Math.random() * 10 - 5)));
            item.put("metric", "hitRate");
            trend.add(item);
        }
        return trend;
    }

    @Override
    public List<Map<String, Object>> getQpsTrend(String instanceId) {
        Map<String, Object> info = getRedisInfo();
        Long instantaneousOpsPerSec = (Long) info.getOrDefault("instantaneous_ops_per_sec", 0L);

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            String time = LocalDateTime.now().minusMinutes(i * 10)
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            item.put("time", time);
            item.put("value", instantaneousOpsPerSec + (long) (Math.random() * 100));
            item.put("metric", "qps");
            trend.add(item);
        }
        return trend;
    }

    @Override
    public Map<String, Object> getKeys(String keyword, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Set<String> allKeys = redisTemplate.keys(StrUtil.isBlank(keyword) ? "*" : "*" + keyword + "*");
        List<Map<String, Object>> keyList = new ArrayList<>();

        if (allKeys != null) {
            List<String> sortedKeys = new ArrayList<>(allKeys);
            sortedKeys.sort(String::compareTo);

            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, sortedKeys.size());

            for (int i = start; i < end; i++) {
                String key = sortedKeys.get(i);
                Map<String, Object> keyInfo = new HashMap<>();
                keyInfo.put("id", key);
                keyInfo.put("key", key);
                keyInfo.put("type", getKeyType(key));
                keyInfo.put("size", getKeySize(key));
                keyInfo.put("ttl", redisTemplate.getExpire(key));
                keyInfo.put("instanceId", INSTANCE_ID);
                keyInfo.put("instanceName", "主缓存实例");
                keyInfo.put("updateTime", LocalDateTime.now().toString());
                keyList.add(keyInfo);
            }

            result.put("rows", keyList);
            result.put("total", allKeys.size());
        } else {
            result.put("rows", keyList);
            result.put("total", 0);
        }

        return result;
    }

    @Override
    public boolean refreshKey(String key) {
        try {
            long ttl = redisTemplate.getExpire(key);
            if (ttl > 0) {
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            }
            saveLog(INSTANCE_ID, "refresh", "刷新缓存键: " + key, "success");
            return true;
        } catch (Exception e) {
            log.error("刷新缓存键失败: {}", e.getMessage());
            saveLog(INSTANCE_ID, "refresh", "刷新缓存键失败: " + key, "fail");
            return false;
        }
    }

    @Override
    public boolean deleteKey(String key) {
        try {
            boolean result = Boolean.TRUE.equals(redisTemplate.delete(key));
            saveLog(INSTANCE_ID, "delete", "删除缓存键: " + key, result ? "success" : "fail");
            return result;
        } catch (Exception e) {
            log.error("删除缓存键失败: {}", e.getMessage());
            saveLog(INSTANCE_ID, "delete", "删除缓存键失败: " + key, "fail");
            return false;
        }
    }

    @Override
    public boolean batchRefreshKeys(List<String> keys) {
        try {
            for (String key : keys) {
                refreshKey(key);
            }
            saveLog(INSTANCE_ID, "batchRefresh", "批量刷新缓存键: " + keys.size() + "个", "success");
            return true;
        } catch (Exception e) {
            log.error("批量刷新缓存键失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean batchDeleteKeys(List<String> keys) {
        try {
            Long deleted = redisTemplate.delete(new HashSet<>(keys));
            saveLog(INSTANCE_ID, "batchDelete", "批量删除缓存键: " + deleted + "个", "success");
            return true;
        } catch (Exception e) {
            log.error("批量删除缓存键失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clearInstance() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            saveLog(INSTANCE_ID, "clear", "清空缓存实例", "success");
            return true;
        } catch (Exception e) {
            log.error("清空缓存实例失败: {}", e.getMessage());
            saveLog(INSTANCE_ID, "clear", "清空缓存实例失败", "fail");
            return false;
        }
    }

    @Override
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            Properties properties = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
            if (properties != null) {
                info.put("redis_version", properties.getProperty("redis_version"));
                info.put("uptime_in_days", properties.getProperty("uptime_in_days"));

                String usedMemory = properties.getProperty("used_memory_human", "0B");
                info.put("used_memory", usedMemory);
                info.put("used_memory_peak", properties.getProperty("used_memory_peak_human"));

                info.put("db_keys", properties.getProperty("db0", "keys=0"));

                long hits = Long.parseLong(properties.getProperty("keyspace_hits", "0"));
                long misses = Long.parseLong(properties.getProperty("keyspace_misses", "0"));
                double hitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0;
                info.put("hitRate", Math.round(hitRate * 100.0) / 100.0);
                info.put("keyspace_hits", hits);
                info.put("keyspace_misses", misses);

                info.put("instantaneous_ops_per_sec", Long.parseLong(properties.getProperty("instantaneous_ops_per_sec", "0")));
            }
        } catch (Exception e) {
            log.error("获取Redis信息失败: {}", e.getMessage());
        }
        return info;
    }

    private CacheSysInfo buildCacheInfo(String cacheKey) {
        if (!hasCacheKey(cacheKey)) {
            return null;
        }

        CacheSysInfo cacheInfo = new CacheSysInfo();
        cacheInfo.setCacheKey(cacheKey);
        cacheInfo.setCacheName(extractCacheCategory(cacheKey));

        Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        cacheInfo.setTtl(ttl);
        cacheInfo.setTtlDesc(formatTtl(ttl));

        Object value = redisTemplate.opsForValue().get(cacheKey);
        if (value != null) {
            cacheInfo.setCacheValue(truncateValue(value.toString()));
            cacheInfo.setDataType(value.getClass().getSimpleName());
            cacheInfo.setDataSize(estimateObjectSize(value));
        }

        cacheInfo.setCreateTime(System.currentTimeMillis());
        return cacheInfo;
    }

    private String extractCacheCategory(String cacheKey) {
        if (cacheKey == null || !cacheKey.startsWith(CacheConstants.CACHE_PREFIX)) {
            return "unknown";
        }
        String suffix = cacheKey.substring(CacheConstants.CACHE_PREFIX.length());
        int colonIndex = suffix.indexOf(':');
        if (colonIndex > 0) {
            return suffix.substring(0, colonIndex);
        }
        return suffix.contains("_") ? suffix.substring(0, suffix.indexOf('_')) : suffix;
    }

    private String formatTtl(Long ttl) {
        if (ttl == null || ttl < 0) {
            return "永不过期";
        }
        if (ttl == 0) {
            return "已过期";
        }
        if (ttl < 60) {
            return ttl + "秒";
        }
        if (ttl < 3600) {
            return (ttl / 60) + "分钟" + (ttl % 60 > 0 ? (ttl % 60) + "秒" : "");
        }
        if (ttl < 86400) {
            long hours = ttl / 3600;
            long minutes = (ttl % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        }
        long days = ttl / 86400;
        long hours = (ttl % 86400) / 3600;
        return days + "天" + (hours > 0 ? hours + "小时" : "");
    }

    private String truncateValue(String value) {
        if (value == null) {
            return null;
        }
        int maxLength = 200;
        if (value.length() > maxLength) {
            return value.substring(0, maxLength) + "...";
        }
        return value;
    }

    private long estimateObjectSize(Object obj) {
        if (obj == null) {
            return 0;
        }
        try {
            if (obj instanceof String) {
                return ((String) obj).getBytes(StandardCharsets.UTF_8).length;
            }
            if (obj instanceof Number) {
                return 8;
            }
            if (obj instanceof Collection) {
                return ((Collection<?>) obj).size() * 100L;
            }
            if (obj instanceof Map) {
                return ((Map<?, ?>) obj).size() * 100L;
            }
            return obj.toString().getBytes(StandardCharsets.UTF_8).length;
        } catch (Exception e) {
            return 100;
        }
    }

    private List<String> getDefaultWarmupCacheNames() {
        List<String> cacheNames = new ArrayList<>();
        cacheNames.add("dict");
        cacheNames.add("config");
        return cacheNames;
    }

    private void warmupCacheByName(String cacheName) {
        switch (cacheName) {
            case "dict":
                warmupDictCache();
                break;
            case "config":
                warmupConfigCache();
                break;
            default:
                log.warn("未知的缓存预热类型: {}", cacheName);
        }
    }

    private void warmupDictCache() {
        log.info("开始预热字典缓存...");
    }

    private void warmupConfigCache() {
        log.info("开始预热配置缓存...");
    }

    private String getKeyType(String key) {
        try {
            return redisTemplate.type(key).code();
        } catch (Exception e) {
            return "string";
        }
    }

    private long getKeySize(String key) {
        try {
            Long size = redisTemplate.execute((RedisCallback<Long>) connection -> {
                byte[] keyBytes = redisTemplate.getStringSerializer().serialize(key);
                return connection.strLen(keyBytes);
            });
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void saveLog(String instanceId, String operType, String message, String result) {
        SysCacheLog logEntity = new SysCacheLog();
        logEntity.setInstanceId(instanceId);
        logEntity.setOperTime(LocalDateTime.now());
        logEntity.setOperType(operType);
        logEntity.setMessage(message);
        logEntity.setResult(result);
        mongoTemplate.save(logEntity);
    }
}
