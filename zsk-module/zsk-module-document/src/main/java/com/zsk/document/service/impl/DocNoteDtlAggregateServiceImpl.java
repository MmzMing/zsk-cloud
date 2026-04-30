package com.zsk.document.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.dto.DocNoteDtlDTO;
import com.zsk.document.domain.dto.DocNoteFullDTO;
import com.zsk.document.domain.vo.DocNoteFullVO;
import com.zsk.document.domain.vo.DocNoteListVo;
import com.zsk.document.service.IDocAuditService;
import com.zsk.document.service.IDocNoteDtlAggregateService;
import com.zsk.document.service.IDocNoteDtlService;
import com.zsk.document.service.IDocNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 笔记聚合Service业务层处理
 * <p>
 * 协调 {@link IDocNoteService}（元信息）与 {@link IDocNoteDtlService}（正文）
 * 的跨表操作，在事务边界内保证 document_note 和 document_note_dtl 两张表的数据一致性。
 * 遵循 Service 层约束：负责核心业务逻辑、事务控制，方法粒度适中，单一职责。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocNoteDtlAggregateServiceImpl implements IDocNoteDtlAggregateService {

    /**
     * 笔记元信息Service
     */
    private final IDocNoteService docNoteService;

    private final IDocNoteDtlService docNoteDtlService;

    private final IDocAuditService docAuditService;

    /**
     * 创建笔记（元信息 + 正文）
     * <p>
     * 在一个事务中同时写入 document_note 和 document_note_dtl 两张表。
     * 处理流程：
     * 1. 从 DTO 中提取 DocNote 元信息对象
     * 2. 调用 docNoteService.save() 插入元信息，save 后 docNote.id 由雪花算法自动回填
     * 3. 用回填的 noteId 构建 DocNoteDtlDTO，调用 saveOrUpdateByNoteId 保存正文
     * </p>
     *
     * @param dto 笔记全量DTO（包含元信息对象和正文内容）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNoteFull(DocNoteFullDTO dto) {
        log.info("开始创建笔记全量信息");

        DocNote docNote = dto.getDocNote();
        String content = dto.getContent();

        // 1. 插入笔记元信息
        boolean noteSaved = docNoteService.save(docNote);
        if (!noteSaved) {
            log.error("创建笔记失败, 元信息插入失败");
            throw new BusinessException("创建笔记失败");
        }
        Long noteId = docNote.getId();
        log.info("笔记元信息创建成功, noteId={}", noteId);

        // 2. 保存笔记正文
        DocNoteDtlDTO dtlDTO = new DocNoteDtlDTO();
        dtlDTO.setNoteId(noteId);
        dtlDTO.setContent(content);

        boolean dtlSaved = docNoteDtlService.saveOrUpdateByNoteId(dtlDTO);
        if (!dtlSaved) {
            log.error("创建笔记失败, 正文保存失败, noteId={}", noteId);
            throw new BusinessException("创建笔记失败");
        }

        log.info("笔记全量信息创建完成, noteId={}", noteId);

        // 3. 如果笔记状态为发布（status=1），提交到审核队列
        if (docNote.getStatus() != null && docNote.getStatus() == 1) {
            try {
                docAuditService.submitToAudit(1, noteId);
            } catch (Exception e) {
                log.error("笔记提交审核失败, noteId={}, error={}", noteId, e.getMessage(), e);
            }
        }

        return true;
    }

    /**
     * 全量更新笔记（元信息 + 正文）
     * <p>
     * 在一个事务中同时更新 document_note 和 document_note_dtl 两张表。
     * 处理流程：
     * 1. 从 DTO 中提取 DocNote 元信息对象（id 必填）
     * 2. 调用 docNoteService.updateById() 更新元信息
     * 3. 构建 DocNoteDtlDTO，调用 saveOrUpdateByNoteId 保存正文
     * </p>
     *
     * @param dto 笔记全量DTO（包含元信息对象和正文内容）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNoteFull(DocNoteFullDTO dto) {
        log.info("开始全量更新笔记");

        DocNote docNote = dto.getDocNote();
        String content = dto.getContent();

        if (docNote.getId() == null) {
            log.error("全量更新笔记失败, docNote.id 为空");
            throw new BusinessException("参数错误：笔记ID不能为空");
        }

        Long noteId = docNote.getId();

        // 1. 更新笔记元信息
        boolean noteUpdated = docNoteService.updateById(docNote);
        if (!noteUpdated) {
            log.warn("更新笔记元信息失败, noteId={}", noteId);
            throw new BusinessException("更新失败：笔记不存在或已被删除");
        }
        log.info("笔记元信息更新成功, noteId={}", noteId);

        // 2. 保存笔记正文
        DocNoteDtlDTO dtlDTO = new DocNoteDtlDTO();
        dtlDTO.setNoteId(noteId);
        dtlDTO.setContent(content);

        boolean dtlSaved = docNoteDtlService.saveOrUpdateByNoteId(dtlDTO);
        if (!dtlSaved) {
            log.error("全量更新笔记失败, 正文保存失败, noteId={}", noteId);
            throw new BusinessException("更新失败");
        }

        log.info("笔记全量更新完成, noteId={}", noteId);
        return true;
    }

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
    @Override
    public DocNoteFullVO getNoteFull(Long noteId) {
        log.info("查询笔记全量信息, noteId={}", noteId);

        if (noteId == null) {
            log.warn("查询笔记全量信息失败, 笔记ID为空");
            return null;
        }

        // 1. 查询笔记元信息（含作者、封面、统计）
        DocNoteListVo noteDetail = docNoteService.getNoteDetail(noteId);
        if (noteDetail == null) {
            log.warn("查询笔记全量信息失败, 笔记不存在, noteId={}", noteId);
            return null;
        }

        // 2. 查询笔记正文
        DocNoteDtl dtl = docNoteDtlService.getByNoteId(noteId);

        // 3. 组装全量VO
        DocNoteFullVO fullVO = new DocNoteFullVO();
        // 复制元信息字段
        BeanUtil.copyProperties(noteDetail, fullVO);
        // 填充正文
        fullVO.setContent(dtl != null ? dtl.getContent() : null);

        log.info("查询笔记全量信息成功, noteId={}, contentLength={}",
                noteId, dtl != null && dtl.getContent() != null ? dtl.getContent().length() : 0);
        return fullVO;
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeNoteFull(Long noteId) {
        log.info("开始级联删除笔记全量信息, noteId={}", noteId);

        if (noteId == null) {
            log.warn("级联删除笔记失败, 笔记ID为空");
            return false;
        }

        // 1. 删除笔记正文
        docNoteDtlService.removeByNoteId(noteId);

        // 2. 删除笔记元信息
        boolean noteRemoved = docNoteService.removeById(noteId);
        if (!noteRemoved) {
            log.warn("删除笔记元信息失败, 笔记可能不存在, noteId={}", noteId);
        }

        log.info("笔记全量信息删除完成, noteId={}", noteId);
        return true;
    }
}
