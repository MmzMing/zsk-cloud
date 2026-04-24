package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.RemoteDocAllContentService;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.vo.SysDashboardOverviewVo;
import com.zsk.system.mapper.SysUserMapper;
import com.zsk.system.service.ISysDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 仪表盘 服务实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDashboardServiceImpl implements ISysDashboardService {

    private final SysUserMapper userMapper;
    private final RemoteDocAllContentService remoteDocAllContentService;

    /**
     * 获取仪表盘概览数据
     *
     * @return 概览数据列表，包含用户总数、文档总数、视频总数和总访问量
     */
    @Override
    public List<SysDashboardOverviewVo> getOverview() {
        List<SysDashboardOverviewVo> list = new ArrayList<>();

        /** 用户总数 */
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
        );

        /** 最近一周新增用户数 */
        LocalDateTime lastWeekStart = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long lastWeekUserCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .ge(SysUser::getCreateTime, lastWeekStart)
        );
        String userDelta = calculateDelta(userCount, lastWeekUserCount);
        list.add(createItem("users", "用户总数", String.valueOf(userCount), userDelta, "系统注册用户数量"));

        /** 远程调用获取文档统计数据 */
        DocStatisticsApi docStats = getDocStatistics();

        /** 文档总数 */
        Long noteCount = docStats != null ? docStats.getNoteCount() : 0L;
        Long lastWeekNoteCount = docStats != null ? docStats.getLastWeekNoteCount() : 0L;
        String noteDelta = calculateDelta(noteCount, lastWeekNoteCount);
        list.add(createItem("docs", "文档总数", String.valueOf(noteCount), noteDelta, "已发布文档数量"));

        /** 视频总数 */
        Long videoCount = docStats != null ? docStats.getVideoCount() : 0L;
        Long lastWeekVideoCount = docStats != null ? docStats.getLastWeekVideoCount() : 0L;
        String videoDelta = calculateDelta(videoCount, lastWeekVideoCount);
        list.add(createItem("videos", "视频总数", String.valueOf(videoCount), videoDelta, "已发布视频数量"));

        /** 评论数 */
        Long commentCount = docStats != null ? docStats.getCommentCount() : 0L;
        Long lastWeekCommentCount = docStats != null ? docStats.getLastWeekCommentCount() : 0L;
        String commentDelta = calculateDelta(commentCount, lastWeekCommentCount);
        list.add(createItem("comments", "评论数", String.valueOf(commentCount), commentDelta, "文档和视频评论总数"));

        return list;
    }

    /**
     * 远程调用获取文档统计数据
     *
     * @return 文档统计数据，包含文档数、视频数等信息；若调用失败则返回 null
     */
    private DocStatisticsApi getDocStatistics() {
        try {
            R<DocStatisticsApi> result = remoteDocAllContentService.getContentStats(CommonConstants.INNER);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
            log.warn("获取文档统计数据失败: {}", result != null ? result.getMsg() : "返回结果为空");
        } catch (Exception e) {
            log.error("调用文档服务获取统计数据异常", e);
        }
        return null;
    }

    /**
     * 计算变化率
     *
     * @param current  当前值
     * @param lastWeek 上周新增值
     * @return 变化率字符串（如：+12.5%）；若上周新增值为 0 则返回空字符串；若当前值为 0 则返回 "-100%"
     */
    private String calculateDelta(Long current, Long lastWeek) {
        if (lastWeek == null || lastWeek == 0) {
            return "";
        }
        if (current == null || current == 0) {
            return "-100%";
        }
        Long previousTotal = current - lastWeek;
        if (previousTotal <= 0) {
            return "+" + lastWeek;
        }
        double rate = (double) lastWeek / previousTotal * 100;
        return String.format("+%.1f%%", rate);
    }

    /**
     * 创建概览数据项
     *
     * @param key         唯一标识
     * @param label       显示标签
     * @param value       当前数值
     * @param delta       变化量
     * @param description 描述说明
     * @return 概览数据项实例
     */
    private SysDashboardOverviewVo createItem(String key, String label, String value, String delta, String description) {
        SysDashboardOverviewVo item = new SysDashboardOverviewVo();
        item.setKey(key);
        item.setLabel(label);
        item.setValue(value);
        item.setDelta(delta);
        item.setDescription(description);
        return item;
    }
}
