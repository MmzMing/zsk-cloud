package com.zsk.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zsk.common.log.domain.OperLog;
import com.zsk.system.service.ISysBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 行为审计 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysBehaviorServiceImpl implements ISysBehaviorService {

    private final MongoTemplate mongoTemplate;

    /**
     * 获取行为审计用户列表
     *
     * @return 用户列表
     */
    @Override
    public List<Map<String, Object>> getUsers() {
        List<Map<String, Object>> users = new ArrayList<>();

        /** 从操作日志聚合用户信息 */
        GroupOperation groupByUser = Aggregation.group("operName")
                .first("operName").as("userName")
                .count().as("operCount")
                .max("operTime").as("lastOperTime")
                .first("operIp").as("lastOperIp");

        Aggregation aggregation = Aggregation.newAggregation(groupByUser);
        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, OperLog.class, Map.class);

        for (Map result : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", result.get("_id"));
            user.put("name", result.get("userName"));
            user.put("role", "用户");
            user.put("department", "未知");
            user.put("lastLoginAt", result.get("lastOperTime"));
            user.put("lastLoginIp", result.get("lastOperIp") != null ? result.get("lastOperIp").toString() : "");

            /** 根据操作次数计算风险等级 */
            Integer operCount = (Integer) result.get("operCount");
            String riskLevel = calculateRiskLevelByCount(operCount);
            user.put("riskLevel", riskLevel);

            users.add(user);
        }

        /** 按操作次数降序排序 */
        users.sort((a, b) -> {
            Integer countA = (Integer) a.getOrDefault("operCount", 0);
            Integer countB = (Integer) b.getOrDefault("operCount", 0);
            return countB.compareTo(countA);
        });

        return users;
    }

    /**
     * 获取用户行为时间轴
     *
     * @param userId 用户ID
     * @param range 时间范围
     * @return 行为数据点列表
     */
    @Override
    public List<Map<String, Object>> getTimeline(String userId, String range) {
        List<Map<String, Object>> timeline = new ArrayList<>();

        /** 计算时间范围 */
        LocalDateTime startTime = calculateStartTime(range);
        LocalDateTime endTime = LocalDateTime.now();

        /** 查询操作日志 */
        Query query = new Query();
        query.addCriteria(Criteria.where("operName").is(userId));
        query.addCriteria(Criteria.where("operTime").gte(startTime).lte(endTime));
        query.with(Sort.by(Sort.Direction.ASC, "operTime"));

        List<OperLog> logs = mongoTemplate.find(query, OperLog.class);

        /** 按时间分组统计 */
        Map<String, Integer> countByTime = new LinkedHashMap<>();
        DateTimeFormatter formatter = getFormatter(range);

        for (OperLog log : logs) {
            if (log.getOperTime() != null) {
                String timeKey = log.getOperTime().format(formatter);
                countByTime.merge(timeKey, 1, Integer::sum);
            }
        }

        /** 转换为时间轴数据 */
        for (Map.Entry<String, Integer> entry : countByTime.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("userId", userId);
            point.put("range", range);
            point.put("time", entry.getKey());
            point.put("count", entry.getValue());
            timeline.add(point);
        }

        return timeline;
    }

    /**
     * 获取行为审计事件列表
     *
     * @param userId 用户ID
     * @param keyword 关键字
     * @return 事件列表
     */
    @Override
    public List<Map<String, Object>> getEvents(String userId, String keyword) {
        List<Map<String, Object>> events = new ArrayList<>();

        /** 查询操作日志 */
        Query query = new Query();
        if (StrUtil.isNotBlank(userId)) {
            query.addCriteria(Criteria.where("operName").is(userId));
        }
        if (StrUtil.isNotBlank(keyword)) {
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(keyword, "i"),
                    Criteria.where("operUrl").regex(keyword, "i"),
                    Criteria.where("operParam").regex(keyword, "i")
            );
            query.addCriteria(keywordCriteria);
        }
        query.addCriteria(Criteria.where("operTime").gte(LocalDateTime.now().minusDays(7)));
        query.with(Sort.by(Sort.Direction.DESC, "operTime"));
        query.limit(100);

        List<OperLog> logs = mongoTemplate.find(query, OperLog.class);

        /** 转换为事件列表 */
        for (OperLog log : logs) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", log.getId());
            event.put("userId", log.getOperName());
            event.put("time", log.getOperTime() != null ? log.getOperTime().toString() : "");
            event.put("action", getActionType(log.getBusinessType()));
            event.put("module", log.getTitle() != null ? log.getTitle() : "");
            event.put("detail", buildDetail(log));
            event.put("riskLevel", log.getStatus() != null && log.getStatus() == 1 ? "high" : "low");
            events.add(event);
        }

        return events;
    }

    /**
     * 计算用户风险等级
     *
     * @param userId 用户ID
     * @return 风险等级
     */
    @Override
    public String calculateRiskLevel(Long userId) {
        return "low";
    }

    /**
     * 根据操作次数计算风险等级
     *
     * @param count 操作次数
     * @return 风险等级
     */
    private String calculateRiskLevelByCount(Integer count) {
        if (count == null) return "low";
        if (count > 100) return "high";
        if (count > 50) return "medium";
        return "low";
    }

    /**
     * 根据时间范围计算开始时间
     *
     * @param range 时间范围
     * @return 开始时间
     */
    private LocalDateTime calculateStartTime(String range) {
        return switch (range) {
            case "today" -> LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            case "7d" -> LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            case "30d" -> LocalDateTime.now().minus(30, ChronoUnit.DAYS);
            default -> LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        };
    }

    /**
     * 根据时间范围获取格式化器
     *
     * @param range 时间范围
     * @return 格式化器
     */
    private DateTimeFormatter getFormatter(String range) {
        return switch (range) {
            case "today" -> DateTimeFormatter.ofPattern("HH:mm");
            case "7d", "30d" -> DateTimeFormatter.ofPattern("MM-dd");
            default -> DateTimeFormatter.ofPattern("HH:mm");
        };
    }

    /**
     * 获取动作类型
     *
     * @param businessType 业务类型
     * @return 动作类型
     */
    private String getActionType(Integer businessType) {
        if (businessType == null) return "其他";
        return switch (businessType) {
            case 1 -> "新增";
            case 2 -> "修改";
            case 3 -> "删除";
            case 4 -> "查询";
            case 5 -> "导出";
            case 6 -> "导入";
            default -> "其他";
        };
    }

    /**
     * 构建详情描述
     *
     * @param log 操作日志
     * @return 详情描述
     */
    private String buildDetail(OperLog log) {
        StringBuilder detail = new StringBuilder();
        if (StrUtil.isNotBlank(log.getOperUrl())) {
            detail.append(log.getOperUrl());
        }
        if (StrUtil.isNotBlank(log.getOperParam())) {
            if (detail.length() > 0) detail.append(" ");
            String param = log.getOperParam();
            if (param.length() > 100) {
                param = param.substring(0, 100) + "...";
            }
            detail.append(param);
        }
        return detail.toString();
    }
}
