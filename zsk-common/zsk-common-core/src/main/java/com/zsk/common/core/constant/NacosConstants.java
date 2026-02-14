package com.zsk.common.core.constant;

/**
 * Nacos常量
 *
 * @author wuhuaming
 */
public class NacosConstants {

    /**
     * 配置中心默认命名空间
     */
    public static final String DEFAULT_NAMESPACE = "public";

    /**
     * 配置分组 - 默认
     */
    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

    /**
     * 配置分组 - 网关
     */
    public static final String GATEWAY_GROUP = "GATEWAY_GROUP";

    /**
     * 配置分组 - 认证中心
     */
    public static final String AUTH_GROUP = "AUTH_GROUP";

    /**
     * 配置文件后缀
     */
    public static final String CONFIG_FILE_EXTENSION = "yaml";

    /**
     * 路由配置Data ID
     */
    public static final String ROUTE_DATA_ID = "zsk-gateway-routes";

    /**
     * 限流规则配置Data ID
     */
    public static final String FLOW_RULE_DATA_ID = "zsk-sentinel-flow-rules";

    /**
     * 降级规则配置Data ID
     */
    public static final String DEGRADE_RULE_DATA_ID = "zsk-sentinel-degrade-rules";

    /**
     * 配置文件格式
     */
    public static final String CONFIG_TYPE = "yaml";

    private NacosConstants() {
        throw new AssertionError("常量类禁止实例化");
    }
}
