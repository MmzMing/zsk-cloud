package com.zsk.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.mapper.SysDictDataMapper;
import com.zsk.system.service.ISysDictDataService;
import com.zsk.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字典数据管理 服务层实现
 * <p>
 * 缓存一致性说明：
 * 字典数据的增删改操作会自动调用 refreshCache 维护 Redis 缓存，
 * refreshCache 内部已包含版本号递增逻辑，无需额外调用 incrementDictVersion。
 * <ul>
 *   <li>新增字典数据 → refreshCache（含版本号递增）</li>
 *   <li>修改字典数据 → refreshCache（含版本号递增）</li>
 *   <li>删除字典数据 → refreshCache（含版本号递增）</li>
 *   <li>切换状态 → refreshCache（含版本号递增）</li>
 * </ul>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    private final ISysDictTypeService dictTypeService;

    // ==================== 字典数据 CRUD（含缓存维护） ====================

    /**
     * 新增字典数据
     * <p>
     * 保存后自动刷新该字典类型对应的缓存（含版本号递增），
     * 确保前端能感知到数据变更。
     *
     * @param entity 字典数据对象
     * @return 是否保存成功
     */
    @Override
    public boolean save(SysDictData entity) {
        boolean result = super.save(entity);
        if (result) {
            try {
                dictTypeService.refreshCache(entity.getDictType());
            } catch (Exception e) {
                log.error("[字典缓存] 新增字典数据后刷新缓存失败, dictType={}", entity.getDictType(), e);
            }
        }
        return result;
    }

    /**
     * 修改字典数据
     * <p>
     * 若 dictType 发生变更，需同时刷新旧类型和新类型的缓存；
     * 修改后自动刷新缓存（含版本号递增）。
     *
     * @param entity 字典数据对象（必须包含 id）
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysDictData entity) {
        SysDictData oldData = this.getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            try {
                String oldDictType = oldData != null ? oldData.getDictType() : null;
                String newDictType = entity.getDictType();

                if (oldDictType != null && newDictType != null && !oldDictType.equals(newDictType)) {
                    dictTypeService.deleteCache(oldDictType);
                    dictTypeService.refreshCache(newDictType);
                } else {
                    String dictType = newDictType != null ? newDictType : oldDictType;
                    if (dictType != null) {
                        dictTypeService.refreshCache(dictType);
                    }
                }
            } catch (Exception e) {
                log.error("[字典缓存] 修改字典数据后刷新缓存失败, id={}", entity.getId(), e);
            }
        }
        return result;
    }

    /**
     * 批量删除字典数据
     * <p>
     * 删除前先查询受影响的 dictType 集合，
     * 删除后按类型刷新缓存（含版本号递增），避免重复刷新。
     *
     * @param idList 字典数据主键 ID 集合
     * @return 是否删除成功
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean removeByIds(Collection<?> idList) {
        List<SysDictData> dataList = this.listByIds((Collection<? extends Serializable>) idList);
        boolean result = super.removeByIds(idList);
        if (result) {
            try {
                Set<String> affectedTypes = dataList.stream()
                        .map(SysDictData::getDictType)
                        .collect(Collectors.toSet());
                for (String dictType : affectedTypes) {
                    dictTypeService.refreshCache(dictType);
                }
            } catch (Exception e) {
                log.error("[字典缓存] 删除字典数据后刷新缓存失败", e);
            }
        }
        return result;
    }

    // ==================== 查询 ====================

    /**
     * 根据字典类型查询字典数据
     * <p>
     * 查询结果按 dictSort 升序排列，包含所有状态的数据。
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        log.info("根据字典类型查询字典数据, dictType={}", dictType);
        return this.lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort)
                .list();
    }

    // ==================== 状态切换（含缓存维护） ====================

    /**
     * 切换字典状态
     * <p>
     * 切换后自动刷新该字典类型对应的缓存（含版本号递增）。
     *
     * @param id     字典ID
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    @Override
    public boolean toggleStatus(Long id, String status) {
        log.info("切换字典状态, id={}, status={}", id, status);
        SysDictData oldData = this.getById(id);
        if (oldData == null) {
            log.warn("切换字典状态失败，字典数据不存在, id={}", id);
            return false;
        }

        boolean result = this.lambdaUpdate()
                .eq(SysDictData::getId, id)
                .set(SysDictData::getStatus, status)
                .update();

        if (result) {
            try {
                dictTypeService.refreshCache(oldData.getDictType());
            } catch (Exception e) {
                log.error("[字典缓存] 切换字典状态后刷新缓存失败, dictType={}", oldData.getDictType(), e);
            }
        }
        log.info("切换字典状态完成, id={}, result={}", id, result);
        return result;
    }

    /**
     * 批量切换字典状态
     * <p>
     * 使用单条 SQL 批量更新状态，避免循环单条更新导致的 N+1 问题；
     * 更新后按受影响的字典类型刷新缓存（含版本号递增），避免重复刷新。
     *
     * @param ids    字典ID列表
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    @Override
    public boolean batchToggleStatus(List<Long> ids, String status) {
        log.info("批量切换字典状态, ids={}, status={}", ids, status);
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        List<SysDictData> dataList = this.listByIds(ids);
        if (CollUtil.isEmpty(dataList)) {
            log.warn("批量切换字典状态失败，字典数据不存在, ids={}", ids);
            return false;
        }

        Set<String> affectedTypes = dataList.stream()
                .map(SysDictData::getDictType)
                .collect(Collectors.toSet());

        boolean result = this.lambdaUpdate()
                .in(SysDictData::getId, ids)
                .set(SysDictData::getStatus, status)
                .update();

        if (result) {
            try {
                for (String dictType : affectedTypes) {
                    dictTypeService.refreshCache(dictType);
                }
            } catch (Exception e) {
                log.error("[字典缓存] 批量切换字典状态后刷新缓存失败", e);
            }
        }
        log.info("批量切换字典状态完成, 数量={}", ids.size());
        return result;
    }

    // ==================== 字典值转标签 ====================

    /**
     * 根据字典类型和字典值查询字典标签
     * <p>
     * 将字典值（如 "1"、"0"）转换为对应的中文标签（如 "男"、"女"），
     * 用于 Feign 远程调用或内部服务调用，前端无需再转换。
     *
     * @param dictType  字典类型（如 sys_user_sex）
     * @param dictValue 字典值（如 1）
     * @return 字典标签，未找到时返回 null
     */
    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        if (dictType == null || dictValue == null) {
            return null;
        }
        SysDictData dictData = this.lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getDictValue, dictValue)
                .eq(SysDictData::getStatus, "0")
                .one();
        return dictData != null ? dictData.getDictLabel() : null;
    }
}
