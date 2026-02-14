package com.zsk.common.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS配置属性
 * 
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Data
@ConfigurationProperties(prefix = "zsk.oss")
public class OssProperties {
    /** 是否开启 */
    private Boolean enabled = false;

    /** 对象存储服务的URL */
    private String endpoint;

    /** 访问密钥 */
    private String accessKey;

    /** 安全密钥 */
    private String secretKey;

    /** 默认桶名称 */
    private String bucketName;

    /** 存储类型: minio, aliyun */
    private String type = "minio";

    /** 访问域名（自定义域名，非必填） */
    private String domain;
}
