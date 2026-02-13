package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysDictType;
import com.zsk.system.mapper.SysDictTypeMapper;
import com.zsk.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

/**
 * 字典类型管理 服务层实现
 *
 * @author zsk
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {
}
