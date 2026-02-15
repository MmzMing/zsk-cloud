package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.document.api.RemoteDocumentService;
import com.zsk.document.api.domain.DocAnalysisMetricApi;
import com.zsk.document.api.domain.DocStatisticsApi;
import com.zsk.document.api.domain.DocTimeDistributionApi;
import com.zsk.document.api.domain.DocTrafficItemApi;
import com.zsk.document.api.domain.DocTrendItemApi;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.vo.SysAnalysisMetricVo;
import com.zsk.system.domain.vo.SysDashboardOverviewVo;
import com.zsk.system.domain.vo.SysDashboardTrafficVo;
import com.zsk.system.domain.vo.SysDashboardTrendVo;
import com.zsk.system.domain.vo.SysTimeDistributionVo;
import com.zsk.system.mapper.SysUserMapper;
import com.zsk.system.service.ISysDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 仪表盘 服务实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDashboardServiceImpl implements ISysDashboardService {

    private final SysUserMapper userMapper;
    private final RemoteDocumentService remoteDocumentService;

    @Override
    public List<SysDashboardOverviewVo> getOverview() {
        List<SysDashboardOverviewVo> list = new ArrayList<>();

        /** 用户总数 */
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
        );
        list.add(createItem("users", "用户总数", String.valueOf(userCount), "", "系统注册用户数量"));

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

        /** 总访问量 */
        Long noteViewCount = docStats != null ? docStats.getNoteViewCount() : 0L;
        Long videoViewCount = docStats != null ? docStats.getVideoViewCount() : 0L;
        Long totalViewCount = noteViewCount + videoViewCount;
        list.add(createItem("views", "总访问量", String.valueOf(totalViewCount), "", "文档和视频总浏览量"));

        return list;
    }

    @Override
    public List<SysDashboardTrafficVo> getTraffic(String range) {
        try {
            R<List<DocTrafficItemApi>> result = remoteDocumentService.getTrafficStatistics(range, CommonConstants.INNER);
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<SysDashboardTrafficVo> list = new ArrayList<>();
                for (DocTrafficItemApi item : result.getData()) {
                    list.add(new SysDashboardTrafficVo(item.getType(), item.getDate(), item.getValue()));
                }
                return list;
            }
            log.warn("获取流量统计数据失败: {}", result != null ? result.getMsg() : "返回结果为空");
        } catch (Exception e) {
            log.error("调用文档服务获取流量统计数据异常", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<SysDashboardTrendVo> getTrend(String range) {
        try {
            R<List<DocTrendItemApi>> result = remoteDocumentService.getTrendStatistics(range, CommonConstants.INNER);
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<SysDashboardTrendVo> list = new ArrayList<>();
                for (DocTrendItemApi item : result.getData()) {
                    list.add(new SysDashboardTrendVo(item.getDate(), item.getValue()));
                }
                return list;
            }
            log.warn("获取趋势数据失败: {}", result != null ? result.getMsg() : "返回结果为空");
        } catch (Exception e) {
            log.error("调用文档服务获取趋势数据异常", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<SysAnalysisMetricVo> getAnalysisMetrics() {
        try {
            R<List<DocAnalysisMetricApi>> result = remoteDocumentService.getAnalysisMetrics(CommonConstants.INNER);
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<SysAnalysisMetricVo> list = new ArrayList<>();
                for (DocAnalysisMetricApi item : result.getData()) {
                    list.add(new SysAnalysisMetricVo(item.getKey(), item.getLabel(), item.getValue(), item.getDelta(), item.getDescription(), item.getTone()));
                }
                return list;
            }
            log.warn("获取分析指标数据失败: {}", result != null ? result.getMsg() : "返回结果为空");
        } catch (Exception e) {
            log.error("调用文档服务获取分析指标数据异常", e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<SysTimeDistributionVo> getTimeDistribution(String date, String step) {
        try {
            R<List<DocTimeDistributionApi>> result = remoteDocumentService.getTimeDistribution(date, step, CommonConstants.INNER);
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<SysTimeDistributionVo> list = new ArrayList<>();
                for (DocTimeDistributionApi item : result.getData()) {
                    list.add(new SysTimeDistributionVo(item.getType(), item.getTime(), item.getValue()));
                }
                return list;
            }
            log.warn("获取时间分布数据失败: {}", result != null ? result.getMsg() : "返回结果为空");
        } catch (Exception e) {
            log.error("调用文档服务获取时间分布数据异常", e);
        }
        return Collections.emptyList();
    }

    /**
     * 远程调用获取文档统计数据
     *
     * @return 文档统计数据
     */
    private DocStatisticsApi getDocStatistics() {
        try {
            R<DocStatisticsApi> result = remoteDocumentService.getStatisticsOverview(CommonConstants.INNER);
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
     * @param current 当前值
     * @param lastWeek 上周新增值
     * @return 变化率字符串（如：+12.5%）
     */
    private String calculateDelta(Long current, Long lastWeek) {
        if (lastWeek == null || lastWeek == 0) {
            return "";
        }
        if (current == null || current == 0) {
            return "-100%";
        }
        /** 计算上周之前的数量 */
        Long previousTotal = current - lastWeek;
        if (previousTotal <= 0) {
            return "+" + lastWeek;
        }
        /** 计算增长率 */
        double rate = (double) lastWeek / previousTotal * 100;
        return String.format("+%.1f%%", rate);
    }

    /**
     * 创建概览数据项
     *
     * @param key 唯一标识
     * @param label 显示标签
     * @param value 当前数值
     * @param delta 变化量
     * @param description 描述说明
     * @return 概览数据项
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
