package com.zsk.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.system.domain.SysNotice;

import java.util.List;

/**
 * 通知公告管理 服务层
 *
 * @author wuhuaming
 */
public interface ISysNoticeService extends IService<SysNotice> {

    IPage<SysNotice> page(PageQuery pageQuery, SysNotice notice);

    List<SysNotice> listLatest();
}
