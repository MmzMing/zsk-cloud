package com.zsk.document.service.audit;

import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.enums.AuditTargetType;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.domain.vo.DocAuditQueueVO;
import com.zsk.document.mapper.DocNoteCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 文档评论审核策略
 *
 * <p>处理文档评论类型内容在审核流程中的差异化行为，
 * 包括构建审核队列展示项、回写主表审核状态、获取评论内容等。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Component
@RequiredArgsConstructor
public class DocNoteCommentAuditStrategy implements AuditTargetStrategy {

    /**
     * 文档评论Mapper
     */
    private final DocNoteCommentMapper noteCommentMapper;

    @Override
    public AuditTargetType getTargetType() {
        return AuditTargetType.NOTE_COMMENT;
    }

    @Override
    public DocAuditQueueVO buildQueueItem(Long targetId) {
        DocNoteComment comment = noteCommentMapper.selectById(targetId);
        if (comment == null) {
            return null;
        }
        DocAuditQueueVO vo = new DocAuditQueueVO();
        vo.setTargetId(comment.getId());
        vo.setTargetType(AuditTargetType.NOTE_COMMENT.getCode());
        vo.setTitle(comment.getCommentContent());
        vo.setUploaderId(comment.getCommentUserId());
        vo.setStatus(AuditStatus.codeToValue(comment.getAuditStatus()));
        vo.setRiskLevel("low");
        vo.setIsAiChecked(false);
        vo.setCreateTime(Objects.toString(comment.getCreateTime(), ""));
        return vo;
    }

    @Override
    public void updateAuditStatus(Long targetId, Integer auditStatus, String auditMind) {
        DocNoteComment update = new DocNoteComment();
        update.setId(targetId);
        update.setAuditStatus(auditStatus);
        noteCommentMapper.updateById(update);
    }

    @Override
    public String getTargetTitle(Long targetId) {
        DocNoteComment comment = noteCommentMapper.selectById(targetId);
        return comment != null ? comment.getCommentContent() : null;
    }

    @Override
    public Long getTargetUserId(Long targetId) {
        DocNoteComment comment = noteCommentMapper.selectById(targetId);
        return comment != null ? comment.getCommentUserId() : null;
    }

    @Override
    public String getViolationDictType() {
        return DictTypeConstants.DOCUMENT_VIOLATION_REASON;
    }
}
