package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.domain.SysDictType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典类型管理 服务层
 *
 * @author wuhuaming
 */
public interface ISysDictTypeService extends IService<SysDictType> {

    /**
     * 缓存预热：加载所有正常状态的字典类型及其数据到Redis
     */
    void warmUpCache();

    /**
     * 获取所有已缓存的字典类型标签集合
     *
     * @return 字典类型标签集合（如：sys_common_status, sys_yes_no 等）
     */
    Set<String> getCacheTags();

    /**
     * 根据字典类型标签获取缓存的字典数据列表
     *
     * @param tag 字典类型标签（dictType值）
     * @return 字典数据列表
     */
    List<SysDictData> getCacheByTag(String tag);

    /**
     * 获取所有已缓存的字典数据（按标签分组）
     *
     * @return Map<字典类型, 字典数据列表>
     */
    Map<String, List<SysDictData>> getAllCacheData();

    /**
     * 刷新单个字典类型的缓存
     *
     * @param dictType 字典类型
     */
    void refreshCache(String dictType);

    /**
     * 删除单个字典类型的缓存
     *
     * @param dictType 字典类型
     */
    void deleteCache(String dictType);

    /**
     * 清空所有字典缓存
     */
    void clearAllCache();
}
