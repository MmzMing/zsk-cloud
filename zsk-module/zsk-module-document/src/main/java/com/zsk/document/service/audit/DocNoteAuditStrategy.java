package com.zsk.document.service.audit;

import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.enums.AuditTargetType;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.document.domain.DocNote;
import com.zsk.document.domain.vo.DocAuditQueueVO;
import com.zsk.document.mapper.DocNoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 文档审核策略
 *
 * <p>处理文档（笔记）类型内容在审核流程中的差异化行为，
 * 包括构建审核队列展示项、回写主表审核状态、获取文档标题等。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Component
@RequiredArgsConstructor
public class DocNoteAuditStrategy implements AuditTargetStrategy {

    /**
     * 文档Mapper
     */
    private final DocNoteMapper noteMapper;

    @Override
    public AuditTargetType getTargetType() {
        return AuditTargetType.NOTE;
    }

    @Override
    public DocAuditQueueVO buildQueueItem(Long targetId) {
        DocNote note = noteMapper.selectById(targetId);
        if (note == null) {
            return null;
        }
        DocAuditQueueVO vo = new DocAuditQueueVO();
        vo.setTargetId(note.getId());
        vo.setTargetType(AuditTargetType.NOTE.getCode());
        vo.setTitle(note.getNoteName());
        vo.setBroadCode(note.getBroadCode());
        vo.setUploaderId(note.getUserId());
        vo.setStatus(AuditStatus.codeToValue(note.getAuditStatus()));
        vo.setRiskLevel("low");
        vo.setIsAiChecked(false);
        vo.setCreateTime(Objects.toString(note.getCreateTime(), ""));
        return vo;
    }

    @Override
    public void updateAuditStatus(Long targetId, Integer auditStatus, String auditMind) {
        DocNote update = new DocNote();
        update.setId(targetId);
        update.setAuditStatus(auditStatus);
        noteMapper.updateById(update);
    }

    @Override
    public String getTargetTitle(Long targetId) {
        DocNote note = noteMapper.selectById(targetId);
        return note != null ? note.getNoteName() : null;
    }

    @Override
    public Long getTargetUserId(Long targetId) {
        DocNote note = noteMapper.selectById(targetId);
        return note != null ? note.getUserId() : null;
    }

    @Override
    public String getViolationDictType() {
        return DictTypeConstants.DOCUMENT_VIOLATION_REASON;
    }
}
