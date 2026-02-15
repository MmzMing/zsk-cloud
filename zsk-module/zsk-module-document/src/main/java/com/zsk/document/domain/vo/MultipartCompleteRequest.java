package com.zsk.document.domain.vo;

import com.zsk.common.oss.model.OssPart;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Schema(description = "分片上传完成请求")
public class MultipartCompleteRequest {
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "上传ID")
    private String uploadId;
    @Schema(description = "文件MD5")
    private String md5;
    @Schema(description = "分片信息")
    private List<OssPart> parts;
}
