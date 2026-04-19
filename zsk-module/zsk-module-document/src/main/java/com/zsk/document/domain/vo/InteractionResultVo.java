package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 交互结果响应（点赞/收藏）
 *
 * @author wuhuaming
 */
@Data
@Builder
@Schema(description = "交互结果响应")
public class InteractionResultVo {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "操作后的状态（是否点赞/收藏）")
    private boolean status;

    @Schema(description = "计数")
    private Long count;
}
