package com.zsk.system.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 缓存日志实体类
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Document(collection = "sys_cache_log")
public class SysCacheLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 操作类型（refresh/delete/clear）
     */
    private String operType;

    /**
     * 操作描述
     */
    private String message;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作结果（success/fail）
     */
    private String result;
}
