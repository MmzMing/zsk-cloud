package com.zsk.document.service;

import com.zsk.document.domain.dto.DocNoteFullDTO;
import com.zsk.document.domain.vo.DocNoteFullVO;

/**
 * 笔记聚合Service接口
 * <p>
 * 协调 {@link IDocNoteService}（元信息）与 {@link IDocNoteDtlService}（正文）
 * 的跨表操作，保证事务一致性。适用于创建/更新笔记时一次提交元信息 + 正文的场景。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-26
 */
public interface IDocNoteDtlAggregateService {

    /**
     * 创建笔记（元信息 + 正文）
     * <p>
     * 在一个事务中同时写入 document_note 和 document_note_dtl 两张表。
     * docNote.id 由雪花算法自动生成，save 后自动回填到实体中。
     * </p>
     *
     * @param dto 笔记全量DTO（包含元信息对象和正文内容）
     * @return 是否成功
     */
    boolean createNoteFull(DocNoteFullDTO dto);

    /**
     * 全量更新笔记（元信息 + 正文）
     * <p>
     * 在一个事务中同时更新 document_note 和 document_note_dtl 两张表。
     * dto.docNote.id 必填，用于定位要更新的笔记记录。
     * </p>
     *
     * @param dto 笔记全量DTO（包含元信息对象和正文内容）
     * @return 是否成功
     */
    boolean updateNoteFull(DocNoteFullDTO dto);

    /**
     * 获取笔记全量信息
     * <p>
     * 同时查询元信息（含作者、封面、统计）和正文内容，组装为全量视图对象。
     * 适用于笔记阅读页，一次请求拿到全部所需数据。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记全量视图对象（元信息 + 作者 + 封面 + 统计 + 正文），若笔记不存在则返回 null
     */
    DocNoteFullVO getNoteFull(Long noteId);

    /**
     * 删除笔记（级联删除元信息 + 正文）
     * <p>
     * 同时逻辑删除 document_note 和 document_note_dtl 两张表的记录。
     * 两个操作在同一事务中执行，保证数据一致性。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 是否成功
     */
    boolean removeNoteFull(Long noteId);
}
