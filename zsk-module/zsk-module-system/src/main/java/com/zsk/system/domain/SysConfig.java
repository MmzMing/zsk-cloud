package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 参数配置对象 sys_config
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
@Schema(description = "参数配置对象")
public class SysConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称")
    private String configName;

    /**
     * 参数键名
     */
    @Schema(description = "参数键名")
    private String configKey;

    /**
     * 参数键值
     */
    @Schema(description = "参数键值")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Schema(description = "系统内置（Y是 N否）")
    private String configType;
}
