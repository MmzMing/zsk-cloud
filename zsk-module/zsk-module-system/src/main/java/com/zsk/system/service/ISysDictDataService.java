package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysDictData;

import java.util.List;

/**
 * 字典数据管理 服务层
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface ISysDictDataService extends IService<SysDictData> {

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 切换字典状态
     *
     * @param id     字典ID
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    boolean toggleStatus(Long id, String status);

    /**
     * 批量切换字典状态
     *
     * @param ids    字典ID列表
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    boolean batchToggleStatus(List<Long> ids, String status);

    /**
     * 根据字典类型和字典值查询字典标签
     * <p>
     * 用于内部服务调用或 Feign 远程调用，
     * 将字典值（如 "1"、"0"）转换为对应的中文标签（如 "男"、"女"）。
     *
     * @param dictType  字典类型（如 sys_user_sex）
     * @param dictValue 字典值（如 1）
     * @return 字典标签，未找到时返回 null
     */
    String selectDictLabel(String dictType, String dictValue);
}
