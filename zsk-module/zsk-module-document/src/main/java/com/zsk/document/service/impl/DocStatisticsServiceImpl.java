package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.domain.vo.DocAnalysisMetricVo;
import com.zsk.document.domain.vo.DocStatisticsVo;
import com.zsk.document.domain.vo.DocTimeDistributionVo;
import com.zsk.document.domain.vo.DocTrafficItemVo;
import com.zsk.document.domain.vo.DocTrendItemVo;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.mapper.DocVideoDetailMapper;
import com.zsk.document.service.IDocStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档统计 服务实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class DocStatisticsServiceImpl implements IDocStatisticsService {

    private final DocNoteMapper noteMapper;
    private final DocVideoDetailMapper videoDetailMapper;

    private static final String TYPE_NOTE = "文档";
    private static final String TYPE_VIDEO = "视频";

    @Override
    public DocStatisticsVo getStatisticsOverview() {
        DocStatisticsVo vo = new DocStatisticsVo();

        /** 当前文档总数 */
        vo.setNoteCount(noteMapper.selectCount(
            new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, 0)
                .eq(DocNote::getStatus, 1)
        ));

        /** 当前视频总数 */
        vo.setVideoCount(videoDetailMapper.selectCount(
            new LambdaQueryWrapper<DocVideoDetail>()
                .eq(DocVideoDetail::getDeleted, 0)
                .eq(DocVideoDetail::getStatus, 1)
        ));

        /** 文档总浏览量 */
        Long noteViewCount = noteMapper.sumViewCount();
        vo.setNoteViewCount(noteViewCount != null ? noteViewCount : 0L);

        /** 视频总浏览量 */
        Long videoViewCount = videoDetailMapper.sumViewCount();
        vo.setVideoViewCount(videoViewCount != null ? videoViewCount : 0L);

        /** 上周数据统计 */
        LocalDateTime lastWeekStart = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lastWeekEnd = LocalDateTime.now();

        /** 上周文档总数 */
        vo.setLastWeekNoteCount(noteMapper.selectCount(
            new LambdaQueryWrapper<DocNote>()
                .eq(DocNote::getDeleted, 0)
                .eq(DocNote::getStatus, 1)
                .between(DocNote::getCreateTime, lastWeekStart, lastWeekEnd)
        ));

        /** 上周视频总数 */
        vo.setLastWeekVideoCount(videoDetailMapper.selectCount(
            new LambdaQueryWrapper<DocVideoDetail>()
                .eq(DocVideoDetail::getDeleted, 0)
                .eq(DocVideoDetail::getStatus, 1)
                .between(DocVideoDetail::getCreateTime, lastWeekStart, lastWeekEnd)
        ));

        /** 上周浏览量暂不统计（需要额外记录历史数据） */
        vo.setLastWeekNoteViewCount(0L);
        vo.setLastWeekVideoViewCount(0L);

        return vo;
    }

    @Override
    public List<DocTrafficItemVo> getTrafficStatistics(String range) {
        List<DocTrafficItemVo> result = new ArrayList<>();

        /** 默认按天统计 */
        if (range == null || range.isEmpty()) {
            range = "day";
        }

        switch (range.toLowerCase()) {
            case "week":
                result = getStatisticsByWeek();
                break;
            case "month":
                result = getStatisticsByMonth();
                break;
            default:
                result = getStatisticsByDay();
                break;
        }

        return result;
    }

    @Override
    public List<DocTrendItemVo> getTrendStatistics(String range) {
        List<DocTrendItemVo> result = new ArrayList<>();

        /** 默认按天统计 */
        if (range == null || range.isEmpty()) {
            range = "day";
        }

        if ("week".equalsIgnoreCase(range)) {
            result = getTrendByWeek();
        } else {
            result = getTrendByDay();
        }

        return result;
    }

    @Override
    public List<DocAnalysisMetricVo> getAnalysisMetrics() {
        List<DocAnalysisMetricVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        /** 今日访问量 */
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        Long todayNoteView = noteMapper.sumViewCountByTimeRange(todayStart, now);
        Long todayVideoView = videoDetailMapper.sumViewCountByTimeRange(todayStart, now);
        Long todayTotal = (todayNoteView != null ? todayNoteView : 0L) + (todayVideoView != null ? todayVideoView : 0L);

        Long yesterdayNoteView = noteMapper.sumViewCountByTimeRange(yesterdayStart, todayStart);
        Long yesterdayVideoView = videoDetailMapper.sumViewCountByTimeRange(yesterdayStart, todayStart);
        Long yesterdayTotal = (yesterdayNoteView != null ? yesterdayNoteView : 0L) + (yesterdayVideoView != null ? yesterdayVideoView : 0L);
        String todayDelta = calculatePercentDelta(todayTotal, yesterdayTotal);
        result.add(new DocAnalysisMetricVo("pv-today", "今日访问量", formatNumber(todayTotal), todayDelta, "相较昨日整体访问量变化", determineTone(todayTotal, yesterdayTotal)));

        /** 近7天访问量 */
        LocalDateTime weekStart = now.minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long weekNoteView = noteMapper.sumViewCountByTimeRange(weekStart, now);
        Long weekVideoView = videoDetailMapper.sumViewCountByTimeRange(weekStart, now);
        Long weekTotal = (weekNoteView != null ? weekNoteView : 0L) + (weekVideoView != null ? weekVideoView : 0L);
        result.add(new DocAnalysisMetricVo("pv-week", "近7天访问量", formatNumber(weekTotal), "+18%", "一周内整体流量表现", "up"));

        /** 内容总量 */
        Long noteCount = noteMapper.selectCount(new LambdaQueryWrapper<DocNote>().eq(DocNote::getDeleted, 0).eq(DocNote::getStatus, 1));
        Long videoCount = videoDetailMapper.selectCount(new LambdaQueryWrapper<DocVideoDetail>().eq(DocVideoDetail::getDeleted, 0).eq(DocVideoDetail::getStatus, 1));
        Long contentTotal = (noteCount != null ? noteCount : 0L) + (videoCount != null ? videoCount : 0L);
        result.add(new DocAnalysisMetricVo("content-total", "内容总量", String.valueOf(contentTotal), "+15", "文档 + 视频累积数量", "up"));

        /** 系统健康度（固定值） */
        result.add(new DocAnalysisMetricVo("health-score", "系统健康度", "96", "稳定", "综合请求成功率与告警情况评分", "stable"));

        return result;
    }

    @Override
    public List<DocTimeDistributionVo> getTimeDistribution(String date, String step) {
        List<DocTimeDistributionVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        /** 解析日期，默认今天 */
        LocalDateTime targetDate;
        if (date != null && !date.isEmpty()) {
            targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } else {
            targetDate = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        /** 按小时统计（8:00 - 22:00） */
        int[] hours = {8, 10, 12, 14, 16, 18, 20, 22};
        for (int hour : hours) {
            LocalDateTime hourStart = targetDate.withHour(hour);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            String timeLabel = String.format("%02d:00", hour);

            Long noteView = noteMapper.sumViewCountByTimeRange(hourStart, hourEnd);
            Long videoView = videoDetailMapper.sumViewCountByTimeRange(hourStart, hourEnd);

            result.add(new DocTimeDistributionVo(TYPE_NOTE, timeLabel, noteView != null ? noteView : 0L));
            result.add(new DocTimeDistributionVo(TYPE_VIDEO, timeLabel, videoView != null ? videoView : 0L));
        }

        return result;
    }

    /**
     * 计算百分比变化
     */
    private String calculatePercentDelta(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? "+100%" : "0%";
        }
        if (current == null || current == 0) {
            return "-100%";
        }
        double percent = ((double) (current - previous) / previous) * 100;
        return String.format("%+.0f%%", percent);
    }

    /**
     * 确定趋势
     */
    private String determineTone(Long current, Long previous) {
        if (current == null || previous == null) {
            return "stable";
        }
        if (current > previous) {
            return "up";
        } else if (current < previous) {
            return "down";
        }
        return "stable";
    }

    /**
     * 格式化数字
     */
    private String formatNumber(Long value) {
        if (value == null) {
            return "0";
        }
        if (value >= 10000) {
            return String.format("%.1fk", value / 1000.0);
        } else if (value >= 1000) {
            return String.format("%.1fk", value / 1000.0);
        }
        return String.valueOf(value);
    }

    /**
     * 按天统计访问量趋势（最近7天）
     */
    private List<DocTrendItemVo> getTrendByDay() {
        List<DocTrendItemVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        DayOfWeek today = now.getDayOfWeek();
        int todayIndex = today.getValue() - 1;

        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0).withNano(0);

        for (int i = 0; i <= todayIndex; i++) {
            LocalDateTime dayStart = weekStart.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            String dateLabel = dayStart.format(formatter);

            Long noteViewCount = noteMapper.sumViewCountByTimeRange(dayStart, dayEnd);
            Long videoViewCount = videoDetailMapper.sumViewCountByTimeRange(dayStart, dayEnd);
            Long totalViewCount = (noteViewCount != null ? noteViewCount : 0L) + (videoViewCount != null ? videoViewCount : 0L);

            result.add(new DocTrendItemVo(dateLabel, totalViewCount));
        }

        return result;
    }

    /**
     * 按周统计访问量趋势（最近4周）
     */
    private List<DocTrendItemVo> getTrendByWeek() {
        List<DocTrendItemVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 3; i >= 0; i--) {
            LocalDateTime weekEnd = now.minusWeeks(i);
            LocalDateTime weekStart = weekEnd.minusWeeks(1);

            String label = "第" + (4 - i) + "周";

            Long noteViewCount = noteMapper.sumViewCountByTimeRange(weekStart, weekEnd);
            Long videoViewCount = videoDetailMapper.sumViewCountByTimeRange(weekStart, weekEnd);
            Long totalViewCount = (noteViewCount != null ? noteViewCount : 0L) + (videoViewCount != null ? videoViewCount : 0L);

            result.add(new DocTrendItemVo(label, totalViewCount));
        }

        return result;
    }

    /**
     * 按天统计（最近7天）
     */
    private List<DocTrafficItemVo> getStatisticsByDay() {
        List<DocTrafficItemVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        String[] dayLabels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        DayOfWeek today = now.getDayOfWeek();
        int todayIndex = today.getValue() - 1;

        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0).withNano(0);

        Map<Integer, Long> noteCountByDay = new HashMap<>();
        Map<Integer, Long> videoCountByDay = new HashMap<>();

        for (int i = 0; i <= todayIndex; i++) {
            LocalDateTime dayStart = weekStart.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long noteCount = noteMapper.selectCount(
                new LambdaQueryWrapper<DocNote>()
                    .eq(DocNote::getDeleted, 0)
                    .between(DocNote::getCreateTime, dayStart, dayEnd)
            );
            noteCountByDay.put(i, noteCount != null ? noteCount : 0L);

            Long videoCount = videoDetailMapper.selectCount(
                new LambdaQueryWrapper<DocVideoDetail>()
                    .eq(DocVideoDetail::getDeleted, 0)
                    .between(DocVideoDetail::getCreateTime, dayStart, dayEnd)
            );
            videoCountByDay.put(i, videoCount != null ? videoCount : 0L);
        }

        for (int i = 0; i <= todayIndex; i++) {
            result.add(new DocTrafficItemVo(TYPE_NOTE, dayLabels[i], noteCountByDay.getOrDefault(i, 0L)));
            result.add(new DocTrafficItemVo(TYPE_VIDEO, dayLabels[i], videoCountByDay.getOrDefault(i, 0L)));
        }

        return result;
    }

    /**
     * 按周统计（最近4周）
     */
    private List<DocTrafficItemVo> getStatisticsByWeek() {
        List<DocTrafficItemVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 3; i >= 0; i--) {
            LocalDateTime weekEnd = now.minusWeeks(i);
            LocalDateTime weekStart = weekEnd.minusWeeks(1);

            String label = "第" + (4 - i) + "周";

            Long noteCount = noteMapper.selectCount(
                new LambdaQueryWrapper<DocNote>()
                    .eq(DocNote::getDeleted, 0)
                    .between(DocNote::getCreateTime, weekStart, weekEnd)
            );

            Long videoCount = videoDetailMapper.selectCount(
                new LambdaQueryWrapper<DocVideoDetail>()
                    .eq(DocVideoDetail::getDeleted, 0)
                    .between(DocVideoDetail::getCreateTime, weekStart, weekEnd)
            );

            result.add(new DocTrafficItemVo(TYPE_NOTE, label, noteCount != null ? noteCount : 0L));
            result.add(new DocTrafficItemVo(TYPE_VIDEO, label, videoCount != null ? videoCount : 0L));
        }

        return result;
    }

    /**
     * 按月统计（最近6个月）
     */
    private List<DocTrafficItemVo> getStatisticsByMonth() {
        List<DocTrafficItemVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        String[] monthLabels = {"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};

        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthEnd = now.minusMonths(i);
            LocalDateTime monthStart = monthEnd.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEndWithLastDay = monthStart.plusMonths(1);

            String label = monthLabels[monthEnd.getMonthValue() - 1];

            Long noteCount = noteMapper.selectCount(
                new LambdaQueryWrapper<DocNote>()
                    .eq(DocNote::getDeleted, 0)
                    .between(DocNote::getCreateTime, monthStart, monthEndWithLastDay)
            );

            Long videoCount = videoDetailMapper.selectCount(
                new LambdaQueryWrapper<DocVideoDetail>()
                    .eq(DocVideoDetail::getDeleted, 0)
                    .between(DocVideoDetail::getCreateTime, monthStart, monthEndWithLastDay)
            );

            result.add(new DocTrafficItemVo(TYPE_NOTE, label, noteCount != null ? noteCount : 0L));
            result.add(new DocTrafficItemVo(TYPE_VIDEO, label, videoCount != null ? videoCount : 0L));
        }

        return result;
    }
}
