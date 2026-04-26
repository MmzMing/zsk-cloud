package com.zsk.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 笔记全量视图对象
 * <p>
 * 继承 {@link DocNoteListVo} 已有的元信息 + 作者 + 封面 + 统计字段，
 * 额外补充 Markdown 正文内容，供阅读页一次拿到全量数据。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "笔记全量视图对象（含正文）")
public class DocNoteFullVO extends DocNoteListVo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 笔记正文内容（Markdown 格式）
     * <p>
     * 由前端 remark-gfm 等工具渲染为 HTML 展示。
     * 仅在详情/阅读页返回，列表接口不包含此字段以避免大字段拖慢查询。
     * </p>
     */
    @Schema(description = "笔记正文（Markdown格式）")
    private String content;
}
