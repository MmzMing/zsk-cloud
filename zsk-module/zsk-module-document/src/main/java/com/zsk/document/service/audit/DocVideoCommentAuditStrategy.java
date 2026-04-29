package com.zsk.document.service.audit;

import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.enums.AuditTargetType;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.domain.vo.DocAuditQueueVO;
import com.zsk.document.mapper.DocVideoCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 视频评论审核策略
 *
 * <p>处理视频评论类型内容在审核流程中的差异化行为，
 * 包括构建审核队列展示项、回写主表审核状态、获取评论内容等。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Component
@RequiredArgsConstructor
public class DocVideoCommentAuditStrategy implements AuditTargetStrategy {

    /**
     * 视频评论Mapper
     */
    private final DocVideoCommentMapper videoCommentMapper;

    @Override
    public AuditTargetType getTargetType() {
        return AuditTargetType.VIDEO_COMMENT;
    }

    @Override
    public DocAuditQueueVO buildQueueItem(Long targetId) {
        DocVideoComment comment = videoCommentMapper.selectById(targetId);
        if (comment == null) {
            return null;
        }
        DocAuditQueueVO vo = new DocAuditQueueVO();
        vo.setTargetId(comment.getId());
        vo.setTargetType(AuditTargetType.VIDEO_COMMENT.getCode());
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
        DocVideoComment update = new DocVideoComment();
        update.setId(targetId);
        update.setAuditStatus(auditStatus);
        videoCommentMapper.updateById(update);
    }

    @Override
    public String getTargetTitle(Long targetId) {
        DocVideoComment comment = videoCommentMapper.selectById(targetId);
        return comment != null ? comment.getCommentContent() : null;
    }

    @Override
    public Long getTargetUserId(Long targetId) {
        DocVideoComment comment = videoCommentMapper.selectById(targetId);
        return comment != null ? comment.getCommentUserId() : null;
    }

    @Override
    public String getViolationDictType() {
        return DictTypeConstants.VIDEO_VIOLATION_REASON;
    }
}
