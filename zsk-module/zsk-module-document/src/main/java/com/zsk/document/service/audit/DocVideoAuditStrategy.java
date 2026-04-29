package com.zsk.document.service.audit;

import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.enums.AuditTargetType;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.vo.DocAuditQueueVO;
import com.zsk.document.mapper.DocVideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 视频审核策略
 *
 * <p>处理视频类型内容在审核流程中的差异化行为，
 * 包括构建审核队列展示项、回写主表审核状态及审核意见、获取视频标题等。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Component
@RequiredArgsConstructor
public class DocVideoAuditStrategy implements AuditTargetStrategy {

    /**
     * 视频Mapper
     */
    private final DocVideoMapper videoMapper;

    @Override
    public AuditTargetType getTargetType() {
        return AuditTargetType.VIDEO;
    }

    @Override
    public DocAuditQueueVO buildQueueItem(Long targetId) {
        DocVideo video = videoMapper.selectById(targetId);
        if (video == null) {
            return null;
        }
        DocAuditQueueVO vo = new DocAuditQueueVO();
        vo.setTargetId(video.getId());
        vo.setTargetType(AuditTargetType.VIDEO.getCode());
        vo.setTitle(video.getVideoTitle());
        vo.setBroadCode(video.getBroadCode());
        vo.setUploaderId(video.getUserId());
        vo.setStatus(AuditStatus.codeToValue(video.getAuditStatus()));
        vo.setRiskLevel("low");
        vo.setIsAiChecked(false);
        vo.setCreateTime(Objects.toString(video.getCreateTime(), ""));
        return vo;
    }

    @Override
    public void updateAuditStatus(Long targetId, Integer auditStatus, String auditMind) {
        DocVideo update = new DocVideo();
        update.setId(targetId);
        update.setAuditStatus(auditStatus);
        update.setAuditMind(auditMind);
        videoMapper.updateById(update);
    }

    @Override
    public String getTargetTitle(Long targetId) {
        DocVideo video = videoMapper.selectById(targetId);
        return video != null ? video.getVideoTitle() : null;
    }

    @Override
    public Long getTargetUserId(Long targetId) {
        DocVideo video = videoMapper.selectById(targetId);
        return video != null ? video.getUserId() : null;
    }

    @Override
    public String getViolationDictType() {
        return DictTypeConstants.VIDEO_VIOLATION_REASON;
    }
}
