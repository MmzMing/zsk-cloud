package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysDictData;

import java.util.List;

/**
 * 字典数据管理 服务层
 *
 * @author zsk
 */
public interface ISysDictDataService extends IService<SysDictData> {
    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectDictDataByType(String dictType);
}
