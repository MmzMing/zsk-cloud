package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.system.domain.SysNotice;
import com.zsk.system.mapper.SysNoticeMapper;
import com.zsk.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 通知公告管理 服务层实现
 *
 * @author wuhuaming
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    @Override
    public IPage<SysNotice> page(PageQuery pageQuery, SysNotice notice) {
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.hasText(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle());
        lqw.eq(StringUtils.hasText(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType());
        lqw.eq(StringUtils.hasText(notice.getStatus()), SysNotice::getStatus, notice.getStatus());
        lqw.orderByDesc(SysNotice::getCreateTime);
        return super.page(pageQuery.build(), lqw);
    }

    @Override
    public List<SysNotice> listLatest() {
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysNotice::getStatus, "0");
        lqw.orderByDesc(SysNotice::getCreateTime);
        lqw.last("LIMIT 5");
        return super.list(lqw);
    }
}
