package com.zsk.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.CacheSysInfo;
import com.zsk.system.domain.SysCacheLog;
import com.zsk.system.domain.dto.CacheKeyQueryDTO;
import com.zsk.system.domain.dto.CacheKeyRefreshDTO;
import com.zsk.system.domain.dto.CacheTtlRefreshDTO;
import com.zsk.system.domain.dto.CacheWarmupDTO;
import com.zsk.system.domain.vo.*;
import com.zsk.system.service.ICacheSysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理 服务层实现
 * <p>
 * 提供缓存实例管理、缓存键操作、缓存统计等功能实现
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

    /**
     * 获取缓存键名列表
     *
     * @param cacheName 缓存名称（可选，用于过滤特定缓存类型）
     * @return 缓存键名集合
     */
    @Override
    public Collection<String> getCacheKeys(String cacheName) {
        String pattern = CacheConstants.CACHE_PREFIX + (cacheName != null && !cacheName.isEmpty() ? cacheName + ":*" : "*");
        return redisService.keys(pattern);
    }

    /**
     * 获取缓存信息列表（分页）
     *
     * <p>功能说明：
     * <ul>
     *   <li>根据缓存名称过滤缓存键列表</li>
     *   <li>遍历所有匹配的键，构建缓存详细信息</li>
     *   <li>按缓存键名排序，确保分页结果的稳定性</li>
     *   <li>执行分页计算，返回指定页码的数据</li>
     * </ul>
     *
     * <p>修复说明：
     * <ul>
     *   <li>添加按 cacheKey 排序逻辑，解决分页顺序不一致问题</li>
     *   <li>Redis KEYS 命令返回的集合是无序的，每次迭代顺序可能不同</li>
     *   <li>排序后保证每次查询结果顺序一致，分页跳转时不会出现数据重复或遗漏</li>
     * </ul>
     *
     * @param pageQuery 分页参数，包含页码(pageNum)和每页大小(pageSize)
     * @param queryDTO  查询条件，包含缓存名称用于过滤
     * @return 分页后的缓存信息列表，包含数据列表、总数、页码、每页大小
     */
    @Override
    public PageResult<CacheSysInfo> getCacheInfoList(PageQuery pageQuery, CacheKeyQueryDTO queryDTO) {
        // 1. 获取查询条件中的缓存名称（可为空，为空时查询所有缓存）
        String cacheName = queryDTO != null ? queryDTO.getCacheName() : null;

        // 2. 根据缓存名称获取匹配的缓存键集合（Redis KEYS 命令返回无序集合）
        Collection<String> keys = getCacheKeys(cacheName);

        // 3. 遍历所有键，构建缓存详细信息列表
        List<CacheSysInfo> allCacheInfoList = new ArrayList<>();
        for (String key : keys) {
            try {
                CacheSysInfo cacheInfo = buildCacheInfo(key);
                if (cacheInfo != null) {
                    allCacheInfoList.add(cacheInfo);
                }
            } catch (Exception e) {
                // 单个键处理失败不影响其他键，记录警告日志继续处理
                log.warn("获取缓存信息失败: {}", key, e);
            }
        }

        // 4. 【关键修复】按缓存键名排序，确保分页结果的稳定性
        // Redis KEYS 返回的集合是无序的，必须排序后再分页，否则分页结果不可预测
        allCacheInfoList.sort(Comparator.comparing(CacheSysInfo::getCacheKey));

        // 5. 计算分页参数
        Long pageNum = pageQuery.getPageNum();
        Long pageSize = pageQuery.getPageSize();
        long total = allCacheInfoList.size();

        // 6. 计算分页起止索引，处理边界情况（避免越界）
        int start = (pageNum.intValue() - 1) * pageSize.intValue();
        int end = Math.min(start + pageSize.intValue(), allCacheInfoList.size());

        // 7. 截取分页数据并返回
        List<CacheSysInfo> pageList = allCacheInfoList.subList(start, end);
        return PageResult.of(pageList, total, pageNum, pageSize);
    }

    /**
     * 获取缓存详细信息
     *
     * @param cacheKey 缓存键名
     * @return 缓存详细信息，若键不存在返回null
     */
    @Override
    public CacheSysInfo getCacheInfo(String cacheKey) {
        return buildCacheInfo(cacheKey);
    }

    /**
     * 获取缓存值
     *
     * @param cacheKey 缓存键名
     * @return 缓存值对象
     */
    @Override
    public Object getCacheValue(String cacheKey) {
        return redisService.getCacheObject(cacheKey);
    }

    /**
     * 删除指定缓存键列表
     *
     * @param cacheKeys 缓存键名集合
     * @return 删除的缓存数量
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
     * @return 删除的缓存数量
     */
    @Override
    public long clearCacheByName(String cacheName) {
        Collection<String> keys = getCacheKeys(cacheName);
        return deleteCache(keys);
    }

    /**
     * 清空所有缓存
     *
     * @return 删除的缓存数量
     */
    @Override
    public long clearAllCache() {
        Collection<String> keys = redisService.keys(CacheConstants.CACHE_PREFIX + "*");
        return deleteCache(keys);
    }

    /**
     * 刷新缓存过期时间
     *
     * @param ttlDTO 包含缓存键名和过期时间的DTO
     * @return 是否刷新成功
     */
    @Override
    public boolean refreshTtl(CacheTtlRefreshDTO ttlDTO) {
        if (!hasCacheKey(ttlDTO.getCacheKey())) {
            return false;
        }
        return redisService.expire(ttlDTO.getCacheKey(), ttlDTO.getTtl(), TimeUnit.SECONDS);
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
            CacheTtlRefreshDTO ttlDTO = new CacheTtlRefreshDTO();
            ttlDTO.setCacheKey(entry.getKey());
            ttlDTO.setTtl(entry.getValue());
            if (refreshTtl(ttlDTO)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 缓存预热
     *
     * @param warmupDTO 预热参数（包含缓存名称列表）
     * @return 预热结果列表
     */
    @Override
    public List<CacheWarmupResultVO> warmupCache(CacheWarmupDTO warmupDTO) {
        List<CacheWarmupResultVO> results = new ArrayList<>();

        List<String> cacheNames = warmupDTO != null ? warmupDTO.getCacheNames() : null;
        if (cacheNames == null || cacheNames.isEmpty()) {
            cacheNames = getDefaultWarmupCacheNames();
        }

        for (String cacheName : cacheNames) {
            CacheWarmupResultVO result = new CacheWarmupResultVO();
            result.setCacheName(cacheName);
            long startTime = System.currentTimeMillis();
            try {
                warmupCacheByName(cacheName);
                result.setSuccess(true);
                result.setCount(1);
                log.info("缓存预热成功: {}", cacheName);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
                log.error("缓存预热失败: {}", cacheName, e);
            }
            result.setDuration(System.currentTimeMillis() - startTime);
            results.add(result);
        }

        return results;
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息（包含键总数、内存使用、命中率、QPS等）
     */
    @Override
    public CacheStatisticsVO getCacheStatistics() {
        CacheStatisticsVO statistics = new CacheStatisticsVO();

        Collection<String> allKeys = redisService.keys(CacheConstants.CACHE_PREFIX + "*");
        statistics.setTotalKeys((long) allKeys.size());

        long totalSize = 0;
        for (String key : allKeys) {
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

        statistics.setMemoryUsed(totalSize);

        CacheRedisInfoVO redisInfo = getRedisInfo();
        statistics.setHitRate(redisInfo.getHitRate());
        statistics.setQps(redisInfo.getOpsPerSecond());

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
     * 获取缓存实例列表
     *
     * @return 实例列表（包含实例ID、名称、类型、状态等信息）
     */
    @Override
    public List<CacheInstanceVO> getInstances() {
        List<CacheInstanceVO> instances = new ArrayList<>();
        CacheInstanceVO instance = new CacheInstanceVO();

        CacheRedisInfoVO info = getRedisInfo();

        instance.setInstanceId(INSTANCE_ID);
        instance.setInstanceName("主缓存实例");
        instance.setCacheType("Redis");
        instance.setStatus("running");
        instance.setMemoryUsed(info.getUsedMemory());
        instance.setCacheCount(info.getTotalKeys());
        instance.setHitRate(info.getHitRate());
        instance.setQps(info.getOpsPerSecond());

        instances.add(instance);
        return instances;
    }

    /**
     * 获取缓存日志
     *
     * @param instanceId 实例ID（可选，为空则查询所有实例）
     * @return 日志列表（按操作时间倒序，最多100条）
     */
    @Override
    public List<SysCacheLog> getLogs(String instanceId) {
        Query query = new Query();
        if (StrUtil.isNotBlank(instanceId)) {
            query.addCriteria(Criteria.where("instanceId").is(instanceId));
        }
        query.with(Sort.by(Sort.Direction.DESC, "operTime"));
        query.limit(10);
        return mongoTemplate.find(query, SysCacheLog.class);
    }

    /**
     * 获取缓存分布饼图数据
     *
     * @return 缓存名称汇总数据（{name: 'login', value: 100}）
     */
    @Override
    public List<CachePieVO> getCacheDistribution() {
        Collection<String> allKeys = redisService.keys(CacheConstants.CACHE_PREFIX + "*");
        Map<String, Long> cacheCountMap = new HashMap<>();

        for (String key : allKeys) {
            String cacheName = extractCacheCategory(key);
            cacheCountMap.merge(cacheName, 1L, Long::sum);
        }

        List<CachePieVO> pieData = new ArrayList<>();
        for (Map.Entry<String, Long> entry : cacheCountMap.entrySet()) {
            CachePieVO pieVO = new CachePieVO();
            pieVO.setName(entry.getKey());
            pieVO.setValue(entry.getValue());
            pieData.add(pieVO);
        }

        pieData.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return pieData;
    }

    /**
     * 获取内存使用仪表盘数据
     *
     * @return 内存使用仪表盘数据（当前值、最大值等）
     */
    @Override
    public GaugeDataPoint getMemoryUsage() {
        CacheRedisInfoVO info = getRedisInfo();

        GaugeDataPoint gauge = new GaugeDataPoint();
        gauge.setName("内存使用率");
        gauge.setMin(0D);

        Long usedMemory = info.getUsedMemory();
        Long maxMemory = info.getMaxMemory();

        if (usedMemory != null) {
            gauge.setValue(usedMemory.doubleValue());
        } else {
            gauge.setValue(0D);
        }

        if (maxMemory != null && maxMemory > 0) {
            gauge.setMax(maxMemory.doubleValue());
        } else {
            gauge.setMax((double) (1024L * 1024L * 1024L * 2L));
        }

        return gauge;
    }

    /**
     * 获取缓存键名列表（分页）
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询条件（包含关键字）
     * @return 分页后的缓存键名列表
     */
    @Override
    public PageResult<String> getKeys(PageQuery pageQuery, CacheKeyQueryDTO queryDTO) {
        String keyword = queryDTO.getKeyword();
        Long pageNum = pageQuery.getPageNum();
        Long pageSize = pageQuery.getPageSize();

        Set<String> allKeys = redisTemplate.keys(StrUtil.isBlank(keyword) ? "*" : "*" + keyword + "*");
        List<String> keyList = new ArrayList<>();
        long total = 0;

        if (allKeys != null) {
            total = allKeys.size();
            List<String> sortedKeys = new ArrayList<>(allKeys);
            sortedKeys.sort(String::compareTo);

            int start = (pageNum.intValue() - 1) * pageSize.intValue();
            int end = Math.min(start + pageSize.intValue(), sortedKeys.size());

            for (int i = start; i < end; i++) {
                keyList.add(sortedKeys.get(i));
            }
        }

        return PageResult.of(keyList, total, pageNum, pageSize);
    }

    /**
     * 刷新缓存键
     *
     * @param refreshDTO 刷新参数（包含缓存键名）
     * @return 是否刷新成功
     */
    @Override
    public boolean refreshKey(CacheKeyRefreshDTO refreshDTO) {
        String key = refreshDTO.getKey();
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

    /**
     * 删除缓存键
     *
     * @param key 缓存键名
     * @return 是否删除成功
     */
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

    /**
     * 批量刷新缓存键
     *
     * @param keys 缓存键名列表
     * @return 是否批量刷新成功
     */
    @Override
    public boolean batchRefreshKeys(List<String> keys) {
        try {
            for (String key : keys) {
                CacheKeyRefreshDTO refreshDTO = new CacheKeyRefreshDTO();
                refreshDTO.setKey(key);
                refreshKey(refreshDTO);
            }
            saveLog(INSTANCE_ID, "batchRefresh", "批量刷新缓存键: " + keys.size() + "个", "success");
            return true;
        } catch (Exception e) {
            log.error("批量刷新缓存键失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除缓存键
     *
     * @param keys 缓存键名列表
     * @return 是否批量删除成功
     */
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

    /**
     * 清空缓存实例
     *
     * @return 是否清空成功
     */
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

    /**
     * 获取Redis服务器信息
     *
     * @return Redis服务器信息（包含版本、模式、连接数、内存使用、命中率等）
     */
    @Override
    public CacheRedisInfoVO getRedisInfo() {
        CacheRedisInfoVO info = new CacheRedisInfoVO();
        try {
            Properties properties = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
            if (properties != null) {
                info.setRedisVersion(properties.getProperty("redis_version"));
                info.setMode(properties.getProperty("redis_mode", "standalone"));
                info.setConnectedClients(Integer.parseInt(properties.getProperty("connected_clients", "0")));

                String usedMemoryStr = properties.getProperty("used_memory", "0");
                info.setUsedMemory(Long.parseLong(usedMemoryStr));

                String maxMemoryStr = properties.getProperty("maxmemory", "0");
                info.setMaxMemory(maxMemoryStr.equals("0") ? null : Long.parseLong(maxMemoryStr));

                long hits = Long.parseLong(properties.getProperty("keyspace_hits", "0"));
                long misses = Long.parseLong(properties.getProperty("keyspace_misses", "0"));
                double hitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0;
                info.setHitRate(Math.round(hitRate * 100.0) / 100.0);

                info.setOpsPerSecond(Long.parseLong(properties.getProperty("instantaneous_ops_per_sec", "0")));
                info.setUptimeInSeconds(Long.parseLong(properties.getProperty("uptime_in_seconds", "0")));

                String dbInfo = properties.getProperty("db0", "keys=0");
                int keysStart = dbInfo.indexOf("keys=");
                if (keysStart >= 0) {
                    int keysEnd = dbInfo.indexOf(",", keysStart);
                    String keysStr = keysEnd >= 0 ? dbInfo.substring(keysStart + 5, keysEnd) : dbInfo.substring(keysStart + 5);
                    info.setTotalKeys(Long.parseLong(keysStr));
                }

                if (info.getMaxMemory() != null && info.getMaxMemory() > 0) {
                    info.setMemoryUsage(Math.round((double) info.getUsedMemory() / info.getMaxMemory() * 100 * 100.0) / 100.0);
                }
            }
        } catch (Exception e) {
            log.error("获取Redis信息失败: {}", e.getMessage());
        }
        return info;
    }

    /**
     * 构建缓存详细信息
     *
     * <p>功能说明：
     * <ul>
     *   <li>校验缓存键是否存在</li>
     *   <li>提取缓存键名、分类名称、过期时间等基本信息</li>
     *   <li>获取缓存值并计算数据大小</li>
     *   <li>处理非字符串类型的缓存键，避免 WRONGTYPE 错误</li>
     * </ul>
     *
     * <p>修复说明：
     * <ul>
     *   <li>添加 Redis 类型检查，解决 WRONGTYPE 错误</li>
     *   <li>Redis 支持多种数据类型（String/Hash/List/Set/ZSet）</li>
     *   <li>只有 String 类型才能使用 GET 命令获取值</li>
     *   <li>非 String 类型的键会显示类型名称，而不是抛出异常</li>
     * </ul>
     *
     * @param cacheKey 缓存键名
     * @return 缓存详细信息（包含键名、分类、TTL、值、数据类型、大小），若键不存在返回null
     */
    private CacheSysInfo buildCacheInfo(String cacheKey) {
        // 1. 校验缓存键是否存在，不存在直接返回null
        if (!hasCacheKey(cacheKey)) {
            return null;
        }

        // 2. 创建缓存信息对象并设置基本属性
        CacheSysInfo cacheInfo = new CacheSysInfo();
        cacheInfo.setCacheKey(cacheKey);
        cacheInfo.setCacheName(extractCacheCategory(cacheKey));

        // 3. 获取缓存过期时间（TTL）并格式化显示
        Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        cacheInfo.setTtl(ttl);
        cacheInfo.setTtlDesc(formatTtl(ttl));

        // 4. 【关键修复】获取缓存值前先检查数据类型，避免 WRONGTYPE 错误
        // Redis 中不同数据类型的操作命令不同：
        // - String: GET/SET
        // - Hash: HGET/HSET
        // - List: LPUSH/LPOP
        // - Set: SADD/SMEMBERS
        // - ZSet: ZADD/ZRANGE
        // 使用 GET 命令操作非 String 类型的键会抛出 WRONGTYPE 异常
        try {
            // 先获取键的类型（返回 DataType 枚举）
            DataType dataType = redisTemplate.execute((RedisCallback<DataType>) connection ->
                    connection.type(cacheKey.getBytes(StandardCharsets.UTF_8))
            );

            // 只有 String 类型才能使用 opsForValue().get() 获取值
            if (DataType.STRING == dataType) {
                Object value = redisTemplate.opsForValue().get(cacheKey);
                if (value != null) {
                    cacheInfo.setCacheValue(truncateValue(value.toString()));
                    cacheInfo.setDataType(value.getClass().getSimpleName());
                    cacheInfo.setDataSize(estimateObjectSize(value));
                }
            } else {
                cacheInfo.setDataType(dataType != null ? dataType.name().toUpperCase() : "UNKNOWN");
                handleNonStringType(cacheKey, dataType, cacheInfo);
            }
        } catch (Exception e) {
            // 获取值失败时记录调试日志，设置友好的错误提示
            log.debug("获取缓存值失败: {}", cacheKey, e);
            cacheInfo.setDataType("UNKNOWN");
            cacheInfo.setCacheValue("获取值失败");
        }

        // 5. 设置创建时间（实际为查询时间）
        cacheInfo.setCreateTime(System.currentTimeMillis());
        return cacheInfo;
    }

    /**
     * 从缓存键名中提取缓存分类
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
     * 格式化TTL过期时间为可读字符串
     *
     * @param ttl 过期时间（秒）
     * @return 格式化后的时间描述
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
     * 截断缓存值长度（超过200字符时截断）
     *
     * @param value 缓存值字符串
     * @return 截断后的字符串
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
     * 处理非 String 类型的缓存数据
     *
     * @param cacheKey  缓存键名
     * @param dataType  数据类型
     * @param cacheInfo 缓存信息对象
     */
    private void handleNonStringType(String cacheKey, DataType dataType, CacheSysInfo cacheInfo) {
        try {
            switch (dataType) {
                case SET:
                    Set<Object> setMembers = redisTemplate.opsForSet().members(cacheKey);
                    Long setSize = redisTemplate.opsForSet().size(cacheKey);
                    if (setMembers != null && !setMembers.isEmpty()) {
                        cacheInfo.setCacheValue(truncateValue(setMembers.toString()));
                        cacheInfo.setDataSize(setSize != null ? setSize : (long) setMembers.size());
                    } else {
                        cacheInfo.setCacheValue("[]");
                        cacheInfo.setDataSize(0L);
                    }
                    break;
                case HASH:
                    Map<Object, Object> hashEntries = redisTemplate.opsForHash().entries(cacheKey);
                    Long hashSize = redisTemplate.opsForHash().size(cacheKey);
                    if (hashEntries != null && !hashEntries.isEmpty()) {
                        cacheInfo.setCacheValue(truncateValue(hashEntries.toString()));
                        cacheInfo.setDataSize(hashSize != null ? hashSize : (long) hashEntries.size());
                    } else {
                        cacheInfo.setCacheValue("{}");
                        cacheInfo.setDataSize(0L);
                    }
                    break;
                case LIST:
                    List<Object> listValues = redisTemplate.opsForList().range(cacheKey, 0, 20);
                    Long listSize = redisTemplate.opsForList().size(cacheKey);
                    if (listValues != null && !listValues.isEmpty()) {
                        cacheInfo.setCacheValue(truncateValue(listValues.toString()));
                        cacheInfo.setDataSize(listSize != null ? listSize : (long) listValues.size());
                    } else {
                        cacheInfo.setCacheValue("[]");
                        cacheInfo.setDataSize(0L);
                    }
                    break;
                case ZSET:
                    Set<Object> zsetMembers = redisTemplate.opsForZSet().range(cacheKey, 0, 20);
                    Long zsetSize = redisTemplate.opsForZSet().size(cacheKey);
                    if (zsetMembers != null && !zsetMembers.isEmpty()) {
                        cacheInfo.setCacheValue(truncateValue(zsetMembers.toString()));
                        cacheInfo.setDataSize(zsetSize != null ? zsetSize : (long) zsetMembers.size());
                    } else {
                        cacheInfo.setCacheValue("[]");
                        cacheInfo.setDataSize(0L);
                    }
                    break;
                default:
                    cacheInfo.setCacheValue("[" + dataType.name().toUpperCase() + " 类型]");
                    cacheInfo.setDataSize(0L);
            }
        } catch (Exception e) {
            log.debug("获取非String类型缓存值失败: {}", cacheKey, e);
            cacheInfo.setCacheValue("[" + dataType.name().toUpperCase() + " 类型]");
            cacheInfo.setDataSize(0L);
        }
    }

    /**
     * 估算对象大小（字节）
     *
     * @param obj 对象
     * @return 估算的大小（字节）
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
     * 获取默认需要预热的缓存名称列表
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
     * 按名称预热缓存
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

    /**
     * 保存缓存操作日志到MongoDB
     *
     * @param instanceId 实例ID
     * @param operType   操作类型（refresh/delete/batchRefresh/batchDelete/clear）
     * @param message    操作消息
     * @param result     操作结果（success/fail）
     */
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