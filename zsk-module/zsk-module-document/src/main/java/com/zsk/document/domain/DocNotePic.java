package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 笔记多图关联表对象 doc_note_pic
 * 
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_note_pic")
@Schema(description = "笔记多图关联对象")
public class DocNotePic extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 笔记ID */
    @Schema(description = "笔记ID")
    private Long noteId;

    /** 图片URL */
    @Schema(description = "图片URL")
    private String picUrl;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sort;
}
