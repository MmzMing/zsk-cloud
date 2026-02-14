package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysConfig;
import com.zsk.system.mapper.SysConfigMapper;
import com.zsk.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

/**
 * 参数配置管理 服务层实现
 *
 * @author wuhuaming
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
}
