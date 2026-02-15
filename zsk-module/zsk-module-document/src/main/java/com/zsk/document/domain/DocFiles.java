package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文件表对象 doc_files
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_files")
@Schema(description = "文件对象")
public class DocFiles extends TenantEntity {
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
