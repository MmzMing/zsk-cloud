package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "分集信息")
public class DocVideoDtlVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分集ID")
    private Long id;

    @Schema(description = "分集标题")
    private String title;

    @Schema(description = "分集视频地址")
    private String videoUrl;

    @Schema(description = "分集时长")
    private String duration;
}
