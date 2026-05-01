package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.domain.SysDictType;
import com.zsk.system.domain.vo.DictCacheVO;

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
     * 根据字典类型标签获取带版本号的字典数据
     * <p>
     * 返回包含版本号和数据列表的 DictCacheVO 对象，
     * 前端可通过版本号判断本地缓存是否需要更新。
     *
     * @param tag 字典类型标签（dictType值）
     * @return 带版本号的字典数据
     */
    DictCacheVO getCacheVOByTag(String tag);

    /**
     * 获取所有已缓存的字典数据（按标签分组）
     *
     * @return Map<字典类型, 字典数据列表>
     */
    Map<String, List<SysDictData>> getAllCacheData();

    /**
     * 获取所有已缓存的字典数据（按标签分组，带版本号）
     * <p>
     * 返回 Map<字典类型, DictCacheVO>，每个字典类型包含版本号和对应数据列表，
     * 前端可通过版本号判断本地缓存是否需要更新。
     *
     * @return Map<字典类型, 带版本号的字典数据>
     */
    Map<String, DictCacheVO> getAllCacheVOData();

    /**
     * 刷新单个字典类型的缓存
     * <p>
     * 先递增全局版本号，再从数据库重新加载该类型的字典数据，
     * 将版本号与数据打包为 DictCacheItem 写入 Redis，
     * 保证版本号与数据的强一致性。
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

    // ==================== 版本控制 ====================

    /**
     * 获取指定字典类型的缓存版本号
     * <p>
     * 版本号内嵌在 DictCacheItem 中，与字典数据存储在同一个 Redis Value 里。
     *
     * @param dictType 字典类型编码
     * @return 该类型的版本号，若缓存不存在返回 0
     */
    long getDictVersion(String dictType);
}
