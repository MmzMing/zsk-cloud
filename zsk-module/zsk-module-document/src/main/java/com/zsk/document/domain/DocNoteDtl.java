package com.zsk.document.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 笔记详情表对象 doc_note_dtl
 * <p>
 * 存储笔记的 Markdown 内容详情，与 document_note 表是一对一关系。
 * 将笔记内容与笔记基础信息分离，便于独立管理和扩展。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document_note_dtl")
@Schema(description = "笔记详情对象")
public class DocNoteDtl extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联笔记ID（关联 document_note.id）
     * <p>
     * 一对一关联，每个笔记对应唯一的内容详情记录。
     * 通过唯一索引 uk_dnd_note_id 保证数据一致性。
     * </p>
     */
    @Schema(description = "关联笔记ID")
    private Long noteId;

    /**
     * 笔记内容（Markdown 格式）
     * <p>
     * 存储用户编辑的原始 Markdown 文本内容。
     * 前端使用 remark-gfm 等工具渲染为 HTML 展示。
     * 采用 Markdown 格式存储的优势：
     * 1. 体积小：纯文本，体积仅为 HTML 的 1/5 ~ 1/10
     * 2. 可迁移：换渲染引擎、换样式、换前端框架时数据无需改动
     * 3. 安全性高：纯文本不会被注入 XSS
     * 4. 支持编辑：用户编辑时直接拿原始 md 文本，无需从 HTML 还原
     * </p>
     */
    @Schema(description = "笔记内容（Markdown格式）")
    private String content;

    /**
     * 乐观锁版本号
     * <p>
     * 用于防止并发更新冲突，每次更新时自动递增。
     * MyBatis-Plus 乐观锁机制会自动校验版本号。
     * </p>
     */
    @Schema(description = "乐观锁版本号")
    private Long version;
}
