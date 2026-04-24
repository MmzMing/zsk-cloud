package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.system.domain.SysNotice;
import com.zsk.system.mapper.SysNoticeMapper;
import com.zsk.system.service.ISysNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 通知公告管理 服务层实现
 *
 * @author wuhuaming
 */
@Slf4j
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    @Override
    public IPage<SysNotice> page(PageQuery pageQuery, SysNotice notice) {
        log.info("分页查询通知公告, title={}, type={}, status={}",
                notice.getNoticeTitle(), notice.getNoticeType(), notice.getStatus());
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.hasText(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle());
        lqw.eq(StringUtils.hasText(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType());
        lqw.eq(StringUtils.hasText(notice.getStatus()), SysNotice::getStatus, notice.getStatus());
        lqw.orderByDesc(SysNotice::getCreateTime);
        IPage<SysNotice> result = super.page(pageQuery.build(), lqw);
        log.info("分页查询通知公告完成, 总数={}", result.getTotal());
        return result;
    }

    @Override
    public List<SysNotice> listLatest() {
        log.info("获取最新通知公告");
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysNotice::getStatus, "0");
        lqw.orderByDesc(SysNotice::getCreateTime);
        lqw.last("LIMIT 5");
        List<SysNotice> result = super.list(lqw);
        log.info("获取最新通知公告完成, 数量={}", result.size());
        return result;
    }
}
