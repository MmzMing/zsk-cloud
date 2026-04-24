package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Redis信息 视图对象
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
@Schema(description = "Redis信息")
public class CacheRedisInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Redis版本
     */
    @Schema(description = "Redis版本")
    private String redisVersion;

    /**
     * 运行模式
     */
    @Schema(description = "运行模式")
    private String mode;

    /**
     * 连接客户端数
     */
    @Schema(description = "连接客户端数")
    private Integer connectedClients;

    /**
     * 已使用内存（字节）
     */
    @Schema(description = "已使用内存（字节）")
    private Long usedMemory;

    /**
     * 最大内存（字节）
     */
    @Schema(description = "最大内存（字节）")
    private Long maxMemory;

    /**
     * 内存使用率
     */
    @Schema(description = "内存使用率")
    private Double memoryUsage;

    /**
     * 键总数
     */
    @Schema(description = "键总数")
    private Long totalKeys;

    /**
     * 过期键数
     */
    @Schema(description = "过期键数")
    private Long expiredKeys;

    /**
     * 命中率
     */
    @Schema(description = "命中率")
    private Double hitRate;

    /**
     * 每秒操作数
     */
    @Schema(description = "每秒操作数")
    private Long opsPerSecond;

    /**
     * 运行时长（秒）
     */
    @Schema(description = "运行时长（秒）")
    private Long uptimeInSeconds;
}