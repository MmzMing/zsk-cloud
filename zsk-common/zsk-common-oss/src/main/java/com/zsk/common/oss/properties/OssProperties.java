package com.zsk.common.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS配置属性
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
@ConfigurationProperties(prefix = "zsk.oss")
public class OssProperties {
    /**
     * 是否开启
     */
    private Boolean enabled = false;

    /**
     * 对象存储服务的URL
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 安全密钥
     */
    private String secretKey;

    /**
     * 默认桶名称
     */
    private String bucketName;

    /**
     * 存储类型: minio, aliyun
     */
    private String type = "minio";

    /**
     * 访问域名（自定义域名，非必填）
     * 配置后返回直接URL（永不过期），否则返回预签名URL
     */
    private String domain;

    /**
     * 预签名URL有效期（单位：小时），默认168小时（7天）
     * MinIO最大支持7天(168小时)，阿里云OSS无上限
     * 仅在未配置domain时生效
     */
    private Integer urlExpiry = 168;
}
