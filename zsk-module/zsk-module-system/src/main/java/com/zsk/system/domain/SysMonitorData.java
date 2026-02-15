package com.zsk.system.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统监控数据实体类
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Document(collection = "sys_monitor_data")
public class SysMonitorData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * CPU使用率（百分比）
     */
    private Double cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    private Double memUsage;

    /**
     * 磁盘使用率（百分比）
     */
    private Double diskUsage;

    /**
     * 网络使用率（百分比）
     */
    private Double netUsage;

    /**
     * JVM堆内存使用率（百分比）
     */
    private Double jvmHeapUsage;

    /**
     * JVM非堆内存使用率（百分比）
     */
    private Double jvmNonHeapUsage;

    /**
     * JVM线程数
     */
    private Integer jvmThreadCount;

    /**
     * 服务器主机名
     */
    private String hostName;

    /**
     * 服务器IP
     */
    private String hostIp;

    /**
     * 操作系统名称
     */
    private String osName;

    /**
     * 操作系统版本
     */
    private String osVersion;
}
