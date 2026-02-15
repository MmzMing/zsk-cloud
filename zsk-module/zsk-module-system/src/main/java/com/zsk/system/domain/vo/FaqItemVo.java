package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * FAQ项 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FAQ项")
public class FaqItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    @Schema(description = "唯一标识")
    private String id;

    /** 问题 */
    @Schema(description = "问题")
    private String question;

    /** 回答 */
    @Schema(description = "回答")
    private String answer;
}
