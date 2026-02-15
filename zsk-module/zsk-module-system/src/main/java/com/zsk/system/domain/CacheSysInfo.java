package com.zsk.system.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 缓存系统信息实体类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Data
public class CacheSysInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缓存键名
     */
    private String cacheKey;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存值
     */
    private String cacheValue;

    /**
     * 过期时间（秒）
     */
    private Long ttl;

    /**
     * 过期时间描述
     */
    private String ttlDesc;

    /**
     * 数据大小（字节）
     */
    private Long dataSize;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 创建时间
     */
    private Long createTime;
}
