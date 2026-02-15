package com.zsk.system.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行为审计事件实体类
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Document(collection = "sys_behavior_event")
public class SysBehaviorEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 动作类型
     */
    private String actionType;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 详细描述
     */
    private String eventDetail;

    /**
     * 风险等级（low/medium/high）
     */
    private String riskLevel;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 请求URL
     */
    private String operUrl;
}
