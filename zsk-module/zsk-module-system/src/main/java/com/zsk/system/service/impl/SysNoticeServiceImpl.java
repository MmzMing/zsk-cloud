package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysNotice;
import com.zsk.system.mapper.SysNoticeMapper;
import com.zsk.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;

/**
 * 通知公告管理 服务层实现
 *
 * @author zsk
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {
}
