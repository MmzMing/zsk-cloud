package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 技术栈项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技术栈项")
public class TechStackVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    @Schema(description = "唯一标识")
    private String id;

    /** 名称 */
    @Schema(description = "名称")
    private String name;

    /** 描述 */
    @Schema(description = "描述")
    private String description;
}
