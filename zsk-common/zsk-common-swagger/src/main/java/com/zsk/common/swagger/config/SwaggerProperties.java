package com.zsk.common.swagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Swagger 配置属性
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Data
@ConfigurationProperties(prefix = "zsk.swagger")
public class SwaggerProperties {

    /**
     * 是否开启swagger
     */
    private Boolean enabled = true;

    /**
     * 标题
     */
    private String title = "ZSK Cloud 接口文档";

    /**
     * 描述
     */
    private String description = "基于 SpringCloud Alibaba 的分布式微服务架构";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 联系人姓名
     */
    private String contactName = "wuhuaming";

    /**
     * 联系人URL
     */
    private String contactUrl = "https://www.zsk.com";

    /**
     * 联系人邮箱
     */
    private String contactEmail = "wuhuaming@zsk.com";

    /**
     * 许可证
     */
    private String license = "Apache 2.0";

    /**
     * 许可证URL
     */
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

    /**
     * 外部文档描述
     */
    private String externalDocsDescription = "项目源码";

    /**
     * 外部文档URL
     */
    private String externalDocsUrl = "https://github.com/zsk-cloud";

    /**
     * 扫描包
     */
    private String basePackage = "com.zsk";
}
