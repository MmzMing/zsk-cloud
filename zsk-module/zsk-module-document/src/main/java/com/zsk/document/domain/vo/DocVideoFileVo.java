package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频文件信息视图对象")
public class DocVideoFileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "缩略图文件信息（一对一绑定）")
    private DocFileInfoVo thumbnail;

    @Schema(description = "视频文件信息（一对一绑定）")
    private DocFileInfoVo video;
}