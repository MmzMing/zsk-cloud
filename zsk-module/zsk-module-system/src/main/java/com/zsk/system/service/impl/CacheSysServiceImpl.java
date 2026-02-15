package com.zsk.system.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.CacheSysInfo;
import com.zsk.system.service.ICacheSysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存系统管理 服务层实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheSysServiceImpl implements ICacheSysService {

    /**
     * Redis服务
     */
    private final RedisService redisService;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有缓存键名列表
     *
     * @param cacheName 缓存名称（可选，用于过滤）
     * @return 缓存键名列表
     */
    @Override
    public Collection<String> getCacheKeys(String cacheName) {
        String pattern = CacheConstants.CACHE_PREFIX + (cacheName != null && !cacheName.isEmpty() ? cacheName + ":*" : "*");
        return redisService.keys(pattern);
    }

    /**
     * 获取缓存信息列表
     *
     * @param cacheName 缓存名称（可选，用于过滤）
     * @return 缓存信息列表
     */
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

    /**
     * 获取缓存详细信息
     *
     * @param cacheKey 缓存键名
     * @return 缓存信息
     */
    @Override
    public CacheSysInfo getCacheInfo(String cacheKey) {
        return buildCacheInfo(cacheKey);
    }

    /**
     * 获取缓存值
     *
     * @param cacheKey 缓存键名
     * @return 缓存值
     */
    @Override
    public Object getCacheValue(String cacheKey) {
        return redisService.getCacheObject(cacheKey);
    }

    /**
     * 删除缓存
     *
     * @param cacheKeys 缓存键名列表
     * @return 删除数量
     */
    @Override
    public long deleteCache(Collection<String> cacheKeys) {
        if (cacheKeys == null || cacheKeys.isEmpty()) {
            return 0;
        }
        return redisService.deleteObject(cacheKeys);
    }

    /**
     * 清空指定名称的缓存
     *
     * @param cacheName 缓存名称
     * @return 删除数量
     */
    @Override
    public long clearCacheByName(String cacheName) {
        Collection<String> keys = getCacheKeys(cacheName);
        return deleteCache(keys);
    }

    /**
     * 清空所有缓存
     *
     * @return 删除数量
     */
    @Override
    public long clearAllCache() {
        Collection<String> keys = redisService.keys(CacheConstants.CACHE_PREFIX + "*");
        return deleteCache(keys);
    }

    /**
     * 刷新缓存过期时间
     *
     * @param cacheKey 缓存键名
     * @param ttl      过期时间（秒）
     * @return 是否成功
     */
    @Override
    public boolean refreshTtl(String cacheKey, long ttl) {
        if (!hasCacheKey(cacheKey)) {
            return false;
        }
        return redisService.expire(cacheKey, ttl, TimeUnit.SECONDS);
    }

    /**
     * 批量刷新缓存过期时间
     *
     * @param cacheKeyTtlMap 缓存键名与过期时间的映射
     * @return 成功刷新的数量
     */
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

    /**
     * 缓存预热
     *
     * @param cacheNames 需要预热的缓存名称列表
     * @return 预热结果（包含成功数、失败数、成功列表、失败列表）
     */
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

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息（包含总键数、分类统计、预估大小等）
     */
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

    /**
     * 判断缓存是否存在
     *
     * @param cacheKey 缓存键名
     * @return 是否存在
     */
    @Override
    public boolean hasCacheKey(String cacheKey) {
        return Boolean.TRUE.equals(redisService.hasKey(cacheKey));
    }

    /**
     * 构建缓存信息对象
     *
     * @param cacheKey 缓存键名
     * @return 缓存信息对象，不存在则返回null
     */
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

    /**
     * 从缓存键名中提取缓存分类名称
     *
     * @param cacheKey 缓存键名
     * @return 缓存分类名称
     */
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

    /**
     * 格式化过期时间描述
     *
     * @param ttl 过期时间（秒）
     * @return 格式化后的过期时间描述
     */
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

    /**
     * 截断缓存值字符串
     *
     * @param value 原始值
     * @return 截断后的值（最大200字符）
     */
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

    /**
     * 估算对象大小（字节）
     *
     * @param obj 待估算对象
     * @return 预估大小（字节）
     */
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

    /**
     * 获取默认预热缓存名称列表
     *
     * @return 默认预热缓存名称列表
     */
    private List<String> getDefaultWarmupCacheNames() {
        List<String> cacheNames = new ArrayList<>();
        cacheNames.add("dict");
        cacheNames.add("config");
        return cacheNames;
    }

    /**
     * 根据缓存名称执行缓存预热
     *
     * @param cacheName 缓存名称
     */
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

    /**
     * 预热字典缓存
     */
    private void warmupDictCache() {
        log.info("开始预热字典缓存...");
    }

    /**
     * 预热配置缓存
     */
    private void warmupConfigCache() {
        log.info("开始预热配置缓存...");
    }
}
