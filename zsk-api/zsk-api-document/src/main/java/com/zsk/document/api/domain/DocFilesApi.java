package com.zsk.document.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件API模型
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-19
 */
@Data
@Schema(description = "文件API模型")
public class DocFilesApi implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String fileType;

    /**
     * 存储目录
     */
    @Schema(description = "存储目录")
    private String bucket;

    /**
     * 文件id
     */
    @Schema(description = "文件id")
    private String fileId;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 存储路径
     */
    @Schema(description = "存储路径")
    private String filePath;

    /**
     * 访问地址
     */
    @Schema(description = "访问地址")
    private String url;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 上传状态（0未上传 1上传中 2已上传）
     */
    @Schema(description = "上传状态（0未上传 1上传中 2已上传）")
    private Integer status;
}