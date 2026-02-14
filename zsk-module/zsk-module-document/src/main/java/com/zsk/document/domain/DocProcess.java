package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 文件处理任务表对象 doc_process
 * 
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_process")
@Schema(description = "文件处理任务对象")
public class DocProcess extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件标识 */
    @Schema(description = "文件标识")
    private String fileId;

    /** 文件名称 */
    @Schema(description = "文件名称")
    private String fileName;

    /** 存储桶 */
    @Schema(description = "存储桶")
    private String bucket;

    /** 存储路径 */
    @Schema(description = "存储路径")
    private String filePath;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    /** 失败次数 */
    @Schema(description = "失败次数")
    private Integer failCount;

    /** 访问地址 */
    @Schema(description = "访问地址")
    private String url;

    /** 失败原因 */
    @Schema(description = "失败原因")
    private String errorMsg;
}
