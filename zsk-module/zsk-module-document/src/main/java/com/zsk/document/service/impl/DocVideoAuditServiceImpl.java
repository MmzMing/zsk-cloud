package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocVideo;
import com.zsk.document.domain.DocVideoAudit;
import com.zsk.document.domain.vo.*;
import com.zsk.document.mapper.DocVideoAuditMapper;
import com.zsk.document.mapper.DocVideoMapper;
import com.zsk.document.service.IDocVideoAuditService;
import com.zsk.system.api.RemoteDictService;
import com.zsk.system.api.domain.SysDictDataApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 视频审核详情Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoAuditServiceImpl extends ServiceImpl<DocVideoAuditMapper, DocVideoAudit> implements IDocVideoAuditService {

    private final DocVideoMapper videoMapper;
    private final RemoteDictService remoteDictService;

    /**
     * 获取审核队列
     *
     * @param auditStatus 审核状态（可选）
     * @param pageQuery   分页参数
     * @return 审核队列分页结果
     */
    @Override
    public PageResult<VideoAuditQueueVO> getAuditQueue(Integer auditStatus, PageQuery pageQuery) {
        log.info("获取审核队列, auditStatus={}, pageNum={}, pageSize={}", auditStatus, pageQuery.getPageNum(), pageQuery.getPageSize());
        List<VideoAuditQueueVO> list = new ArrayList<>();

        List<DocVideo> videoList = videoMapper.selectAuditQueue(auditStatus,
                (pageQuery.getPageNum() - 1) * pageQuery.getPageSize(),
                pageQuery.getPageSize());

        for (DocVideo detail : videoList) {
            VideoAuditQueueVO vo = new VideoAuditQueueVO();
            vo.setId(detail.getId());
            vo.setVideoTitle(detail.getVideoTitle());
            vo.setBroadCode(detail.getBroadCode());
            vo.setStatus(AuditStatus.codeToValue(detail.getAuditStatus()));
            vo.setRiskLevel("low");
            vo.setIsAiChecked(false);
            vo.setCreateTime(detail.getCreateTime() != null ? detail.getCreateTime().toString() : "");
            list.add(vo);
        }

        long total = videoMapper.countAuditQueue(auditStatus);

        log.info("获取审核队列完成, 共{}条记录", total);
        return PageResult.of(list, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 获取审核详情
     *
     * @param videoId 视频ID
     * @return 审核详情
     */
    @Override
    public DocVideoAudit getAuditDetail(Long videoId) {
        log.info("获取审核详情, videoId={}", videoId);
        DocVideoAudit audit = this.lambdaQuery()
                .eq(DocVideoAudit::getVideoId, videoId)
                .orderByDesc(DocVideoAudit::getCreateTime)
                .last("LIMIT 1")
                .one();
        if (audit == null) {
            log.warn("审核详情不存在, videoId={}", videoId);
        }
        return audit;
    }

    /**
     * 提交审核结果
     *
     * @param request 审核提交请求
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAudit(AuditSubmitRequest request) {
        log.info("提交审核结果, videoId={}, auditStatus={}", request.getVideoId(), request.getAuditStatus());
        // 1. 更新视频审核状态
        DocVideo detail = new DocVideo();
        detail.setId(request.getVideoId());
        detail.setAuditStatus(request.getAuditStatus());
        detail.setAuditMind(request.getAuditMind());
        videoMapper.updateById(detail);

        // 2. 创建审核记录
        DocVideoAudit audit = new DocVideoAudit();
        audit.setVideoId(request.getVideoId());
        audit.setAuditType("manual");
        audit.setAuditStatus(request.getAuditStatus());
        audit.setAuditMind(request.getAuditMind());
        audit.setAuditTime(LocalDateTime.now());
        // 设置审核人信息（从上下文获取当前用户）
        audit.setAuditorId(SecurityUtils.getUserId());
        audit.setAuditorName(SecurityUtils.getUserName());
        this.save(audit);

        // 3. 更新视频关联的审核ID
        DocVideo updateDetail = new DocVideo();
        updateDetail.setId(request.getVideoId());
        updateDetail.setAuditId(audit.getId());
        videoMapper.updateById(updateDetail);

        log.info("提交审核结果完成, videoId={}, auditId={}", request.getVideoId(), audit.getId());
        return true;
    }

    /**
     * 批量提交审核结果
     *
     * @param request 批量审核提交请求
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAuditBatch(AuditBatchSubmitRequest request) {
        log.info("批量提交审核结果, videoIds={}, auditStatus={}", request.getVideoIds(), request.getAuditStatus());
        if (request.getVideoIds() == null || request.getVideoIds().isEmpty()) {
            log.warn("批量提交审核结果失败, 视频ID列表为空");
            return false;
        }
        for (Long videoId : request.getVideoIds()) {
            AuditSubmitRequest submitRequest = new AuditSubmitRequest();
            submitRequest.setVideoId(videoId);
            submitRequest.setAuditStatus(request.getAuditStatus());
            submitRequest.setAuditMind(request.getAuditMind());
            submitAudit(submitRequest);
        }
        log.info("批量提交审核结果完成, 共{}条", request.getVideoIds().size());
        return true;
    }

    /**
     * 获取审核日志
     *
     * @param pageQuery 分页参数
     * @return 审核日志分页结果
     */
    @Override
    public PageResult<VideoAuditLogVO> getAuditLogs(PageQuery pageQuery) {
        log.info("获取审核日志, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());
        List<VideoAuditLogVO> list = baseMapper.selectAuditLogs(
                (pageQuery.getPageNum() - 1) * pageQuery.getPageSize(),
                pageQuery.getPageSize());
        long total = baseMapper.countAuditLogs();
        log.info("获取审核日志完成, 共{}条记录", total);
        return PageResult.of(list, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 获取违规原因列表（从字典服务获取）
     *
     * @return 违规原因列表
     */
    @Override
    public List<ViolationReasonVO> getViolationReasons() {
        log.info("获取违规原因列表");
        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(DictTypeConstants.VIDEO_VIOLATION_REASON);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                List<ViolationReasonVO> list = result.getData().stream()
                        .map(dict -> {
                            ViolationReasonVO vo = new ViolationReasonVO();
                            vo.setId(dict.getDictValue());
                            vo.setLabel(dict.getDictLabel());
                            return vo;
                        })
                        .toList();
                log.info("获取违规原因列表完成, 共{}条", list.size());
                return list;
            }
            log.warn("获取违规原因列表失败, 响应异常");
        } catch (Exception e) {
            log.error("从字典服务获取违规原因失败: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}
