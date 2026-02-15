package com.zsk.system.service;

import com.zsk.system.domain.CacheSysInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存系统管理 服务层
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
     * 获取缓存信息列表
     *
     * @param cacheName 缓存名称（可选，用于过滤）
     * @return 缓存信息列表
     */
    List<CacheSysInfo> getCacheInfoList(String cacheName);

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
     * @param cacheKey 缓存键名
     * @param ttl      过期时间（秒）
     * @return 是否成功
     */
    boolean refreshTtl(String cacheKey, long ttl);

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
     * @param cacheNames 需要预热的缓存名称列表
     * @return 预热结果
     */
    Map<String, Object> warmupCache(List<String> cacheNames);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    Map<String, Object> getCacheStatistics();

    /**
     * 判断缓存是否存在
     *
     * @param cacheKey 缓存键名
     * @return 是否存在
     */
    boolean hasCacheKey(String cacheKey);
}
