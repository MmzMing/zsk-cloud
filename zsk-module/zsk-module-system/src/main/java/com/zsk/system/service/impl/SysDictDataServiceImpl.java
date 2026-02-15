package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysDictData;
import com.zsk.system.mapper.SysDictDataMapper;
import com.zsk.system.service.ISysDictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据管理 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return this.lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort)
                .list();
    }

    /**
     * 切换字典状态
     *
     * @param id 字典ID
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    @Override
    public boolean toggleStatus(Long id, String status) {
        SysDictData dictData = new SysDictData();
        dictData.setId(id);
        dictData.setStatus(status);
        return this.updateById(dictData);
    }

    /**
     * 批量切换字典状态
     *
     * @param ids 字典ID列表
     * @param status 状态（0正常 1停用）
     * @return 是否成功
     */
    @Override
    public boolean batchToggleStatus(List<Long> ids, String status) {
        for (Long id : ids) {
            SysDictData dictData = new SysDictData();
            dictData.setId(id);
            dictData.setStatus(status);
            if (!this.updateById(dictData)) {
                return false;
            }
        }
        return true;
    }
}
