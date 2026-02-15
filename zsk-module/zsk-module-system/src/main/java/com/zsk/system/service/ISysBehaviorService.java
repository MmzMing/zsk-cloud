package com.zsk.system.service;

import java.util.List;
import java.util.Map;

/**
 * 行为审计 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysBehaviorService {

    /**
     * 获取行为审计用户列表
     *
     * @return 用户列表
     */
    List<Map<String, Object>> getUsers();

    /**
     * 获取用户行为时间轴
     *
     * @param userId 用户ID
     * @param range 时间范围
     * @return 行为数据点列表
     */
    List<Map<String, Object>> getTimeline(String userId, String range);

    /**
     * 获取行为审计事件列表
     *
     * @param userId 用户ID
     * @param keyword 关键字
     * @return 事件列表
     */
    List<Map<String, Object>> getEvents(String userId, String keyword);

    /**
     * 计算用户风险等级
     *
     * @param userId 用户ID
     * @return 风险等级（low/medium/high）
     */
    String calculateRiskLevel(Long userId);
}
