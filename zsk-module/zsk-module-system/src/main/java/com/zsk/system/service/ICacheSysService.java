package com.zsk.system.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.CacheSysInfo;
import com.zsk.system.domain.SysCacheLog;
import com.zsk.system.domain.dto.CacheKeyQueryDTO;
import com.zsk.system.domain.dto.CacheKeyRefreshDTO;
import com.zsk.system.domain.dto.CacheTtlRefreshDTO;
import com.zsk.system.domain.dto.CacheWarmupDTO;
import com.zsk.system.domain.vo.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存管理 服务层接口
 * <p>
 * 提供缓存实例管理、缓存键操作、缓存统计等功能
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface ICacheSysService {

    /**
     * 获取所有缓存键名列表
     *
     * @param cacheName 缓存名称（可选，用于过滤）
     * @return 缓存键名列表
     */
    Collection<String> getCacheKeys(String cacheName);

    /**
     * 获取缓存信息列表（分页）
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询条件
     * @return 缓存信息列表
     */
    PageResult<CacheSysInfo> getCacheInfoList(PageQuery pageQuery, CacheKeyQueryDTO queryDTO);

    /**
     * 获取缓存详细信息
     *
     * @param cacheKey 缓存键名
     * @return 缓存信息
     */
    CacheSysInfo getCacheInfo(String cacheKey);

    /**
     * 获取缓存值
     *
     * @param cacheKey 缓存键名
     * @return 缓存值
     */
    Object getCacheValue(String cacheKey);

    /**
     * 删除缓存
     *
     * @param cacheKeys 缓存键名列表
     * @return 删除数量
     */
    long deleteCache(Collection<String> cacheKeys);

    /**
     * 清空指定名称的缓存
     *
     * @param cacheName 缓存名称
     * @return 删除数量
     */
    long clearCacheByName(String cacheName);

    /**
     * 清空所有缓存
     *
     * @return 删除数量
     */
    long clearAllCache();

    /**
     * 刷新缓存过期时间
     *
     * @param ttlDTO 缓存键名和过期时间
     * @return 是否成功
     */
    boolean refreshTtl(CacheTtlRefreshDTO ttlDTO);

    /**
     * 批量刷新缓存过期时间
     *
     * @param cacheKeyTtlMap 缓存键名与过期时间的映射
     * @return 成功刷新的数量
     */
    int refreshTtlBatch(Map<String, Long> cacheKeyTtlMap);

    /**
     * 缓存预热
     *
     * @param warmupDTO 需要预热的缓存名称列表
     * @return 预热结果列表
     */
    List<CacheWarmupResultVO> warmupCache(CacheWarmupDTO warmupDTO);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStatisticsVO getCacheStatistics();

    /**
     * 判断缓存是否存在
     *
     * @param cacheKey 缓存键名
     * @return 是否存在
     */
    boolean hasCacheKey(String cacheKey);

    /**
     * 获取缓存实例信息
     *
     * @return 实例信息列表
     */
    List<CacheInstanceVO> getInstances();

    /**
     * 获取缓存日志列表
     *
     * @param instanceId 实例ID
     * @return 日志列表
     */
    List<SysCacheLog> getLogs(String instanceId);

    /**
     * 获取缓存分布饼图数据
     *
     * @return 缓存名称汇总数据（{name: 'login', value: 100}）
     */
    List<CachePieVO> getCacheDistribution();

    /**
     * 获取内存使用仪表盘数据
     *
     * @return 内存使用仪表盘数据（当前值、最大值等）
     */
    GaugeDataPoint getMemoryUsage();

    /**
     * 获取缓存键列表（分页）
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询条件
     * @return 键列表及总数
     */
    PageResult<String> getKeys(PageQuery pageQuery, CacheKeyQueryDTO queryDTO);

    /**
     * 刷新缓存键
     *
     * @param refreshDTO 缓存键名
     * @return 是否成功
     */
    boolean refreshKey(CacheKeyRefreshDTO refreshDTO);

    /**
     * 删除缓存键
     *
     * @param key 键名
     * @return 是否成功
     */
    boolean deleteKey(String key);

    /**
     * 批量刷新缓存键
     *
     * @param keys 键名列表
     * @return 是否成功
     */
    boolean batchRefreshKeys(List<String> keys);

    /**
     * 批量删除缓存键
     *
     * @param keys 键名列表
     * @return 是否成功
     */
    boolean batchDeleteKeys(List<String> keys);

    /**
     * 清空缓存实例
     *
     * @return 是否成功
     */
    boolean clearInstance();

    /**
     * 获取Redis信息
     *
     * @return Redis信息
     */
    CacheRedisInfoVO getRedisInfo();
}