package com.zsk.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.domain.SysDictType;
import com.zsk.system.domain.dto.DictCacheItem;
import com.zsk.system.mapper.SysDictDataMapper;
import com.zsk.system.mapper.SysDictTypeMapper;
import com.zsk.system.service.ISysDictTypeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 字典类型管理 服务层实现
 *
 * 缓存设计说明（Value 打包模式）：
 * <p>
 * 一、缓存 Key 命名规则：
 * 1. 字典标签集合（Set）：zsk:dict:tags
 *    - 存储所有已缓存的字典类型（dictType）集合
 *    - 示例成员：sys_common_status, sys_yes_no, doc_audit_status
 * <p>
 * 2. 字典数据缓存（Value/DictCacheItem）：zsk:dict:data:{dictType}
 *    - 存储某个字典类型下的版本号 + 字典数据列表
 *    - DictCacheItem { version: 5, data: [{dictLabel:"男", ...}, ...] }
 *    - 版本号与数据打包在同一个 Value 中，原子读取，强一致
 * <p>
 * 3. 字典缓存全局版本号（String/Long）：zsk:dict:version
 *    - 任何字典类型或字典数据的增删改都会递增此版本号
 *    - 前端可通过比较全局版本号决定是否需要重新拉取字典数据
 * <p>
 * 二、Redis Key 总数：2 + N（N = 字典类型数量）
 *    - zsk:dict:tags        × 1
 *    - zsk:dict:version     × 1
 *    - zsk:dict:data:{type} × N
 * <p>
 * 三、版本控制机制：
 * - 全局版本号：独立 Key（zsk:dict:version），INCR 原子递增
 * - 按类型版本号：内嵌在 DictCacheItem.version 中，与数据同 Value
 * - 刷新缓存时：先 INCR 全局版本号 → 将新版本号写入 DictCacheItem
 * - 读取缓存时：一次 GET 即可同时获得版本号和数据，保证强一致
 * <p>
 * 四、缓存一致性保障：
 * - 字典类型增删改 → 自动刷新缓存（版本号内嵌更新）
 * - 字典数据增删改 → 自动刷新缓存（版本号内嵌更新）
 * - 状态切换 → 自动刷新缓存（版本号内嵌更新）
 * - 删除类型 → 清理缓存 + 递增全局版本号
 *
 * @author wuhuaming
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final RedisService redisService;

    private final SysDictDataMapper dictDataMapper;

    private final RedissonClient redissonClient;

    // ==================== 初始化 ====================

    /**
     * 应用启动后自动执行缓存预热
     */
    @PostConstruct
    public void init() {
        log.info("[字典缓存] 应用启动，开始执行缓存预热...");
        warmUpCache();
        log.info("[字典缓存] 缓存预热完成");
    }

    // ==================== 字典类型 CRUD（含缓存维护） ====================

    /**
     * 新增字典类型
     * <p>
     * 保存后自动刷新该类型的缓存（含版本号递增），
     * 确保前端能感知到数据变更。
     *
     * @param entity 字典类型对象
     * @return 是否保存成功
     */
    @Override
    public boolean save(SysDictType entity) {
        boolean result = super.save(entity);
        if (result) {
            try {
                refreshCache(entity.getDictType());
            } catch (Exception e) {
                log.error("[字典缓存] 新增字典类型后刷新缓存失败, dictType={}", entity.getDictType(), e);
            }
        }
        return result;
    }

    /**
     * 修改字典类型
     * <p>
     * 若 dictType 编码发生变更，需同时处理旧类型和新类型的缓存；
     * 修改后自动刷新缓存（含版本号递增）。
     *
     * @param entity 字典类型对象（必须包含 id）
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysDictType entity) {
        SysDictType oldEntity = this.getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            try {
                String oldDictType = oldEntity != null ? oldEntity.getDictType() : null;
                String newDictType = entity.getDictType();

                if (oldDictType != null && newDictType != null && !oldDictType.equals(newDictType)) {
                    deleteCache(oldDictType);
                    refreshCache(newDictType);
                } else {
                    String dictType = newDictType != null ? newDictType : oldDictType;
                    if (dictType != null) {
                        refreshCache(dictType);
                    }
                }
            } catch (Exception e) {
                log.error("[字典缓存] 修改字典类型后刷新缓存失败, id={}", entity.getId(), e);
            }
        }
        return result;
    }

    /**
     * 批量删除字典类型
     * <p>
     * 删除前先查询受影响的 dictType 编码，
     * 删除后清理对应缓存并递增全局版本号。
     *
     * @param idList 字典类型主键 ID 集合
     * @return 是否删除成功
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean removeByIds(Collection<?> idList) {
        List<SysDictType> typesToDelete = this.listByIds((Collection<? extends Serializable>) idList);
        boolean result = super.removeByIds(idList);
        if (result) {
            try {
                for (SysDictType type : typesToDelete) {
                    deleteCache(type.getDictType());
                }
            } catch (Exception e) {
                log.error("[字典缓存] 删除字典类型后清理缓存失败", e);
            }
        }
        return result;
    }

    // ==================== 缓存预热 ====================

    /**
     * 缓存预热：加载所有正常状态的字典类型及其数据到Redis
     * <p>
     * 执行流程：
     * 1. 获取分布式锁，防止多实例并发预热
     * 2. 先递增全局版本号，获取本次预热使用的版本号
     * 3. 查询所有状态为 "0"（正常）的字典类型
     * 4. 按分片大小将字典类型分组
     * 5. 使用虚拟线程池并发处理每个分片
     * 6. 每个分片内顺序处理字典类型，将版本号+数据打包为 DictCacheItem 写入 Redis
     * 7. 等待所有分片完成，统计结果
     * 8. 释放分布式锁
     */
    @Override
    public void warmUpCache() {
        log.info("[字典缓存] 开始缓存预热");

        RLock lock = redissonClient.getLock(CacheConstants.CACHE_DICT_WARMUP_LOCK);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(CacheConstants.CACHE_DICT_WARMUP_LOCK_WAIT_MINUTES, CacheConstants.CACHE_DICT_WARMUP_LOCK_LEASE_MINUTES, TimeUnit.MINUTES);
            if (!isLocked) {
                log.warn("[字典缓存] 获取分布式锁失败，可能有其他实例正在预热，跳过本次预热");
                return;
            }

            log.info("[字典缓存] 获取分布式锁成功，开始预热");

            long warmupVersion = redisService.increment(CacheConstants.CACHE_DICT_VERSION, 1);

            List<SysDictType> dictTypeList = this.lambdaQuery()
                    .eq(SysDictType::getStatus, "0")
                    .list();

            if (CollUtil.isEmpty(dictTypeList)) {
                log.warn("[字典缓存] 未找到正常状态的字典类型，缓存预热跳过");
                return;
            }

            log.info("[字典缓存] 待预热字典类型总数: {}, 预热版本号: {}", dictTypeList.size(), warmupVersion);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            List<List<SysDictType>> shards = CollUtil.split(dictTypeList, CacheConstants.CACHE_DICT_WARMUP_SHARD_SIZE);
            log.info("[字典缓存] 分片数量: {}, 每片大小: {}", shards.size(), CacheConstants.CACHE_DICT_WARMUP_SHARD_SIZE);

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = shards.stream()
                        .map(shard -> CompletableFuture.runAsync(() -> processShard(shard, warmupVersion, successCount, failCount), executor))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            log.info("[字典缓存] 缓存预热完成: 总数={}, 成功={}, 失败={}", dictTypeList.size(), successCount.get(), failCount.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[字典缓存] 缓存预热被中断", e);
        } catch (Exception e) {
            log.error("[字典缓存] 缓存预热异常", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[字典缓存] 释放分布式锁");
            }
        }
    }

    /**
     * 处理一个分片的字典类型预热
     * <p>
     * 将版本号与字典数据打包为 DictCacheItem 写入 Redis，
     * 保证版本号与数据的强一致性。
     *
     * @param shard         分片数据
     * @param warmupVersion 本次预热使用的版本号
     * @param successCount  成功计数器
     * @param failCount     失败计数器
     */
    private Void processShard(List<SysDictType> shard, long warmupVersion, AtomicInteger successCount, AtomicInteger failCount) {
        for (SysDictType dictType : shard) {
            try {
                String type = dictType.getDictType();
                if (StrUtil.isBlank(type)) {
                    log.warn("[字典缓存] 字典类型为空，跳过: id={}", dictType.getId());
                    failCount.incrementAndGet();
                    continue;
                }

                List<SysDictData> dataList = dictDataMapper.selectList(
                        Wrappers.<SysDictData>lambdaQuery()
                                .eq(SysDictData::getDictType, type)
                                .eq(SysDictData::getStatus, "0")
                                .orderByAsc(SysDictData::getDictSort)
                );

                DictCacheItem item = new DictCacheItem();
                item.setVersion(warmupVersion);
                item.setData(dataList);

                redisService.setSetCacheObject(CacheConstants.CACHE_DICT_TAGS, type);

                String dataKey = CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + type;
                redisService.setCacheObject(dataKey, item);
                redisService.expire(dataKey, CacheConstants.CACHE_DICT_EXPIRE_HOURS, TimeUnit.HOURS);

                successCount.incrementAndGet();
                log.debug("[字典缓存] 预热成功: dictType={}, 数据量={}, version={}", type, dataList.size(), warmupVersion);
            } catch (Exception e) {
                failCount.incrementAndGet();
                log.error("[字典缓存] 预热失败: dictType={}", dictType.getDictType(), e);
            }
        }
        return null;
    }

    // ==================== 缓存查询 ====================

    /**
     * 获取所有已缓存的字典类型标签集合
     *
     * @return 字典类型标签集合（dictType 值集合）
     */
    @Override
    public Set<String> getCacheTags() {
        log.debug("[字典缓存] 获取所有缓存标签");
        Set<String> tags = redisService.getCacheSet(CacheConstants.CACHE_DICT_TAGS);
        return tags != null ? tags : Set.of();
    }

    /**
     * 根据字典类型标签获取缓存的字典数据列表
     * <p>
     * 从 DictCacheItem 中提取 data 字段返回，
     * 版本号可通过 {@link #getDictVersion(String)} 单独获取。
     *
     * @param tag 字典类型标签（dictType 值）
     * @return 字典数据列表，如果缓存不存在则返回空列表
     */
    @Override
    public List<SysDictData> getCacheByTag(String tag) {
        if (StrUtil.isBlank(tag)) {
            log.warn("[字典缓存] 标签为空，返回空列表");
            return new ArrayList<>();
        }

        String dataKey = CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + tag;
        DictCacheItem item = redisService.getCacheObject(dataKey);
        if (item == null || item.getData() == null) {
            return new ArrayList<>();
        }
        return item.getData();
    }

    /**
     * 获取所有已缓存的字典数据（按标签分组）
     *
     * @return Map<字典类型标签, 字典数据列表>
     */
    @Override
    public Map<String, List<SysDictData>> getAllCacheData() {
        log.debug("[字典缓存] 获取所有缓存数据");
        Set<String> tags = getCacheTags();
        Map<String, List<SysDictData>> result = new LinkedHashMap<>();

        for (String tag : tags) {
            List<SysDictData> dataList = getCacheByTag(tag);
            result.put(tag, dataList);
        }

        return result;
    }

    // ==================== 缓存维护 ====================

    /**
     * 刷新单个字典类型的缓存
     * <p>
     * 先递增全局版本号，再从数据库重新加载该类型的字典数据，
     * 将版本号与数据打包为 DictCacheItem 写入 Redis，
     * 保证版本号与数据的强一致性。
     *
     * @param dictType 字典类型（dictType 值）
     */
    @Override
    public void refreshCache(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            log.warn("[字典缓存] 刷新缓存时字典类型为空");
            return;
        }

        log.info("[字典缓存] 刷新缓存: dictType={}", dictType);
        try {
            long newVersion = redisService.increment(CacheConstants.CACHE_DICT_VERSION, 1);

            deleteCacheQuietly(dictType);

            SysDictType sysDictType = this.lambdaQuery()
                    .eq(SysDictType::getDictType, dictType)
                    .one();

            if (sysDictType != null && "0".equals(sysDictType.getStatus())) {
                List<SysDictData> dataList = dictDataMapper.selectList(
                        Wrappers.<SysDictData>lambdaQuery()
                                .eq(SysDictData::getDictType, dictType)
                                .eq(SysDictData::getStatus, "0")
                                .orderByAsc(SysDictData::getDictSort)
                );

                DictCacheItem item = new DictCacheItem();
                item.setVersion(newVersion);
                item.setData(dataList);

                redisService.setSetCacheObject(CacheConstants.CACHE_DICT_TAGS, dictType);

                String dataKey = CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + dictType;
                redisService.setCacheObject(dataKey, item);
                redisService.expire(dataKey, CacheConstants.CACHE_DICT_EXPIRE_HOURS, TimeUnit.HOURS);

                log.info("[字典缓存] 刷新成功: dictType={}, 数据量={}, version={}", dictType, dataList.size(), newVersion);
            } else {
                log.info("[字典缓存] 字典类型不存在或已停用: dictType={}", dictType);
            }
        } catch (Exception e) {
            log.error("[字典缓存] 刷新缓存失败: dictType={}", dictType, e);
        }
    }

    /**
     * 删除单个字典类型的缓存
     * <p>
     * 删除缓存数据后递增全局版本号，通知前端数据已变更。
     *
     * @param dictType 字典类型（dictType 值）
     */
    @Override
    public void deleteCache(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return;
        }

        log.info("[字典缓存] 删除缓存: dictType={}", dictType);
        try {
            String dataKey = CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + dictType;
            redisService.deleteObject(dataKey);

            redisService.removeSetCacheObject(CacheConstants.CACHE_DICT_TAGS, dictType);

            redisService.increment(CacheConstants.CACHE_DICT_VERSION, 1);

            log.info("[字典缓存] 删除成功: dictType={}", dictType);
        } catch (Exception e) {
            log.error("[字典缓存] 删除缓存失败: dictType={}", dictType, e);
        }
    }

    /**
     * 静默删除缓存（不递增版本号）
     * <p>
     * 仅在 refreshCache 内部使用，因为 refreshCache 已经在写入前递增了版本号，
     * 无需在删除时再次递增。
     *
     * @param dictType 字典类型（dictType 值）
     */
    private void deleteCacheQuietly(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return;
        }
        try {
            String dataKey = CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + dictType;
            redisService.deleteObject(dataKey);
            redisService.removeSetCacheObject(CacheConstants.CACHE_DICT_TAGS, dictType);
        } catch (Exception e) {
            log.error("[字典缓存] 静默删除缓存失败: dictType={}", dictType, e);
        }
    }

    /**
     * 清空所有字典缓存
     * <p>
     * 删除所有数据缓存和标签集合后，递增全局版本号通知前端。
     */
    @Override
    public void clearAllCache() {
        log.info("[字典缓存] 清空所有缓存");
        try {
            Set<String> tags = getCacheTags();
            if (CollUtil.isNotEmpty(tags)) {
                List<String> keys = tags.stream()
                        .map(tag -> CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + tag)
                        .collect(Collectors.toList());
                redisService.deleteObject(keys);
            }

            redisService.deleteObject(CacheConstants.CACHE_DICT_TAGS);

            redisService.increment(CacheConstants.CACHE_DICT_VERSION, 1);

            log.info("[字典缓存] 清空所有缓存完成");
        } catch (Exception e) {
            log.error("[字典缓存] 清空所有缓存异常", e);
        }
    }

    // ==================== 版本控制 ====================

    /**
     * 获取字典缓存全局版本号
     * <p>
     * 前端可在每次加载时查询此版本号，与本地缓存的版本号比较，
     * 若不一致则重新拉取字典数据。
     *
     * @return 全局版本号，若不存在返回 0
     */
    @Override
    public long getDictVersion() {
        Object version = redisService.getCacheObject(CacheConstants.CACHE_DICT_VERSION);
        if (version == null) {
            return 0L;
        }
        return ((Number) version).longValue();
    }

    /**
     * 获取指定字典类型的缓存版本号
     * <p>
     * 从 DictCacheItem 中读取 version 字段，
     * 版本号与字典数据存储在同一个 Redis Value 中，保证强一致性。
     *
     * @param dictType 字典类型编码
     * @return 该类型的版本号，若缓存不存在返回 0
     */
    @Override
    public long getDictVersion(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return 0L;
        }
        String dataKey = CacheConstants.CACHE_DICT_DATA_KEY_PREFIX + dictType;
        DictCacheItem item = redisService.getCacheObject(dataKey);
        if (item == null) {
            return 0L;
        }
        return item.getVersion();
    }
}
