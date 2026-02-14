package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 字典类型对象 sys_dict_type
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
@Schema(description = "字典类型对象")
public class SysDictType extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称")
    private String dictName;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）")
    private String status;
}
