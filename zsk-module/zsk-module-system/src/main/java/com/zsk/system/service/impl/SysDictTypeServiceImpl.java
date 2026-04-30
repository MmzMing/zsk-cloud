package com.zsk.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.domain.SysDictType;
import com.zsk.system.mapper.SysDictTypeMapper;
import com.zsk.system.service.ISysDictDataService;
import com.zsk.system.service.ISysDictTypeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 字典类型管理 服务层实现
 *
 * 缓存设计说明：
 * <p>
 * 一、缓存 Key 命名规则：
 * 1. 字典标签集合（Set）：dict:tags
 *    - 存储所有已缓存的字典类型（dictType）集合
 *    - 用于快速查询当前系统中缓存了哪些字典类型
 *    - 示例成员：sys_common_status, sys_yes_no, doc_audit_status
 * <p>
 * 2. 字典数据缓存（List）：dict:data:{dictType}
 *    - 存储某个字典类型下的所有字典数据列表（按 dictSort 排序）
 *    - 用于根据标签快速获取对应的字典数据
 *    - 示例 key：dict:data:sys_common_status, dict:data:sys_yes_no
 * <p>
 * 二、缓存标签区分方式：
 * - 标签即 dictType 值（如 sys_common_status、sys_yes_no）
 * - 通过 dict:tags Set 可以获取所有已缓存的标签
 * - 通过 dict:data:{tag} 可以获取对应标签的字典数据
 * <p>
 * 三、缓存获取流程：
 * 1. 获取所有标签：调用 getCacheTags() 返回 Set<String>
 * 2. 根据标签获取数据：调用 getCacheByTag(tag) 返回 List<SysDictData>
 * 3. 获取全部缓存数据：调用 getAllCacheData() 返回 Map<String, List<SysDictData>>
 * <p>
 * 四、缓存过期策略：
 * - 默认过期时间：24 小时
 * - 数据更新时自动刷新对应标签的缓存
 *
 * @author wuhuaming
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final RedisService redisService;

    private final ISysDictDataService dictDataService;

    /**
     * 缓存 Key 前缀
     */
    private static final String DICT_TAGS_KEY = "dict:tags";

    private static final String DICT_DATA_KEY_PREFIX = "dict:data:";

    /**
     * 缓存过期时间：24 小时
     */
    private static final long DICT_CACHE_EXPIRE_HOURS = 24;

    /**
     * 应用启动后自动执行缓存预热
     */
    @PostConstruct
    public void init() {
        log.info("[字典缓存] 应用启动，开始执行缓存预热...");
        warmUpCache();
        log.info("[字典缓存] 缓存预热完成");
    }

    /**
     * 缓存预热：加载所有正常状态的字典类型及其数据到Redis
     * <p>
     * 执行流程：
     * 1. 查询所有状态为 "0"（正常）的字典类型
     * 2. 对每个字典类型，查询其对应的字典数据列表
     * 3. 将字典类型添加到 dict:tags Set 中
     * 4. 将字典数据列表存储到 dict:data:{dictType} 中
     */
    @Override
    public void warmUpCache() {
        log.info("[字典缓存] 开始缓存预热");

        try {
            List<SysDictType> dictTypeList = this.lambdaQuery()
                    .eq(SysDictType::getStatus, "0")
                    .list();

            if (CollUtil.isEmpty(dictTypeList)) {
                log.warn("[字典缓存] 未找到正常状态的字典类型，缓存预热跳过");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (SysDictType dictType : dictTypeList) {
                try {
                    String type = dictType.getDictType();
                    if (StrUtil.isBlank(type)) {
                        log.warn("[字典缓存] 字典类型为空，跳过: id={}", dictType.getId());
                        failCount++;
                        continue;
                    }

                    List<SysDictData> dataList = dictDataService.lambdaQuery()
                            .eq(SysDictData::getDictType, type)
                            .eq(SysDictData::getStatus, "0")
                            .orderByAsc(SysDictData::getDictSort)
                            .list();

                    redisService.setSetCacheObject(DICT_TAGS_KEY, type);

                    String dataKey = DICT_DATA_KEY_PREFIX + type;
                    redisService.setCacheList(dataKey, dataList);
                    redisService.expire(dataKey, DICT_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

                    successCount++;
                    log.debug("[字典缓存] 预热成功: dictType={}, 数据量={}", type, dataList.size());
                } catch (Exception e) {
                    failCount++;
                    log.error("[字典缓存] 预热失败: dictType={}", dictType.getDictType(), e);
                }
            }

            log.info("[字典缓存] 缓存预热完成: 总数={}, 成功={}, 失败={}", dictTypeList.size(), successCount, failCount);
        } catch (Exception e) {
            log.error("[字典缓存] 缓存预热异常", e);
        }
    }

    /**
     * 获取所有已缓存的字典类型标签集合
     * <p>
     * 示例返回值：["sys_common_status", "sys_yes_no", "doc_audit_status"]
     * <p>
     * 使用方式：
     * Set<String> tags = dictTypeService.getCacheTags();
     * for (String tag : tags) {
     *     List<SysDictData> data = dictTypeService.getCacheByTag(tag);
     * }
     *
     * @return 字典类型标签集合（dictType 值集合）
     */
    @Override
    public Set<String> getCacheTags() {
        log.debug("[字典缓存] 获取所有缓存标签");
        Set<String> tags = redisService.getCacheSet(DICT_TAGS_KEY);
        return tags != null ? tags : Set.of();
    }

    /**
     * 根据字典类型标签获取缓存的字典数据列表
     * <p>
     * 示例：getCacheByTag("sys_common_status")
     * 返回：[{dictLabel: "正常", dictValue: "0", ...}, {dictLabel: "停用", dictValue: "1", ...}]
     *
     * @param tag 字典类型标签（dictType 值，如 sys_common_status）
     * @return 字典数据列表，如果缓存不存在则返回空列表
     */
    @Override
    public List<SysDictData> getCacheByTag(String tag) {
        if (StrUtil.isBlank(tag)) {
            log.warn("[字典缓存] 标签为空，返回空列表");
            return new ArrayList<>();
        }

        String dataKey = DICT_DATA_KEY_PREFIX + tag;
        List<SysDictData> dataList = redisService.getCacheList(dataKey);
        return dataList != null ? dataList : new ArrayList<>();
    }

    /**
     * 获取所有已缓存的字典数据（按标签分组）
     * <p>
     * 示例返回值：
     * {
     *     "sys_common_status": [{dictLabel: "正常", ...}, {dictLabel: "停用", ...}],
     *     "sys_yes_no": [{dictLabel: "是", ...}, {dictLabel: "否", ...}]
     * }
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

    /**
     * 刷新单个字典类型的缓存
     * <p>
     * 使用场景：字典数据变更时调用，确保缓存与数据库一致
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
            deleteCache(dictType);

            SysDictType sysDictType = this.lambdaQuery()
                    .eq(SysDictType::getDictType, dictType)
                    .one();

            if (sysDictType != null && "0".equals(sysDictType.getStatus())) {
                List<SysDictData> dataList = dictDataService.lambdaQuery()
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getStatus, "0")
                        .orderByAsc(SysDictData::getDictSort)
                        .list();

                redisService.setSetCacheObject(DICT_TAGS_KEY, dictType);

                String dataKey = DICT_DATA_KEY_PREFIX + dictType;
                redisService.setCacheList(dataKey, dataList);
                redisService.expire(dataKey, DICT_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

                log.info("[字典缓存] 刷新成功: dictType={}, 数据量={}", dictType, dataList.size());
            } else {
                log.info("[字典缓存] 字典类型不存在或已停用: dictType={}", dictType);
            }
        } catch (Exception e) {
            log.error("[字典缓存] 刷新缓存失败: dictType={}", dictType, e);
        }
    }

    /**
     * 删除单个字典类型的缓存
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
            String dataKey = DICT_DATA_KEY_PREFIX + dictType;
            redisService.deleteObject(dataKey);

            redisService.removeSetCacheObject(DICT_TAGS_KEY, dictType);

            log.info("[字典缓存] 删除成功: dictType={}", dictType);
        } catch (Exception e) {
            log.error("[字典缓存] 删除缓存失败: dictType={}", dictType, e);
        }
    }

    /**
     * 清空所有字典缓存
     * <p>
     * 使用场景：批量更新字典数据后、系统维护时调用
     */
    @Override
    public void clearAllCache() {
        log.info("[字典缓存] 清空所有缓存");
        try {
            Set<String> tags = getCacheTags();
            if (CollUtil.isNotEmpty(tags)) {
                List<String> keys = tags.stream()
                        .map(tag -> DICT_DATA_KEY_PREFIX + tag)
                        .collect(Collectors.toList());
                redisService.deleteObject(keys);
            }

            redisService.deleteObject(DICT_TAGS_KEY);

            log.info("[字典缓存] 清空所有缓存完成");
        } catch (Exception e) {
            log.error("[字典缓存] 清空所有缓存异常", e);
        }
    }
}
