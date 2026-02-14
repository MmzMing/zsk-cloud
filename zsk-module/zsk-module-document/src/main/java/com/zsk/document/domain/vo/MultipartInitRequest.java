package com.zsk.document.domain.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "分片上传初始化请求")
public class MultipartInitRequest {
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件类型")
    private String contentType;
    @Schema(description = "文件MD5")
    private String md5;
}
