package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.constant.DictTypeConstants;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.common.core.exception.BusinessException;
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
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 视频审核详情Service业务层处理
 *
 * <p>提供视频审核队列查询、审核详情获取、审核结果提交（单条/批量）、审核日志查询及违规原因获取等功能。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocVideoAuditServiceImpl extends ServiceImpl<DocVideoAuditMapper, DocVideoAudit> implements IDocVideoAuditService {

    /**
     * 审核类型：人工审核
     */
    private static final String AUDIT_TYPE_MANUAL = "manual";

    /**
     * 风险等级：低
     */
    private static final String RISK_LEVEL_LOW = "low";

    /**
     * 视频Mapper
     */
    private final DocVideoMapper videoMapper;

    /**
     * 远程字典服务
     */
    private final RemoteDictService remoteDictService;

    /**
     * 获取审核队列
     *
     * <p>根据审核状态筛选视频审核队列，支持分页查询。</p>
     *
     * @param auditStatus 审核状态（可选，null则查询全部状态）
     * @param pageQuery   分页参数（pageNum、pageSize）
     * @return 审核队列分页结果，包含视频基本信息、审核状态、风险等级等
     */
    @Override
    public PageResult<VideoAuditQueueVO> getAuditQueue(Integer auditStatus, PageQuery pageQuery) {
        log.info("获取审核队列, auditStatus={}, pageNum={}, pageSize={}",
                auditStatus, pageQuery.getPageNum(), pageQuery.getPageSize());

        // 计算分页偏移量
        long offset = calculateOffset(pageQuery);

        // 查询审核队列数据
        List<DocVideo> videoList = videoMapper.selectAuditQueue(auditStatus, offset, pageQuery.getPageSize());

        // 转换为VO列表
        List<VideoAuditQueueVO> voList = videoList.stream()
                .map(this::convertToQueueVO)
                .toList();

        // 查询总记录数
        long total = videoMapper.countAuditQueue(auditStatus);

        log.info("获取审核队列完成, 共{}条记录", total);
        return PageResult.of(voList, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 获取审核详情
     *
     * <p>根据视频ID查询最新的审核记录，按创建时间倒序取第一条。</p>
     *
     * @param videoId 视频ID（必填）
     * @return 最新的审核详情，若视频ID无效或记录不存在则返回null
     * @throws BusinessException 当videoId为null时抛出参数错误异常
     */
    @Override
    public DocVideoAudit getAuditDetail(Long videoId) {
        if (videoId == null || videoId <= 0) {
            log.warn("获取审核详情失败, 视频ID无效: {}", videoId);
            throw new BusinessException("视频ID不能为空");
        }

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
     * <p>单条视频审核结果提交，包含以下步骤：</p>
     * <ol>
     *   <li>校验视频是否存在</li>
     *   <li>更新视频审核状态与审核意见</li>
     *   <li>创建人工审核记录</li>
     *   <li>回写审核记录ID到视频表</li>
     * </ol>
     *
     * @param request 审核提交请求（videoId、auditStatus必填）
     * @return true-提交成功
     * @throws BusinessException 当视频不存在或参数无效时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAudit(AuditSubmitRequest request) {
        // 参数校验
        validateSubmitRequest(request);

        Long videoId = request.getVideoId();
        Integer auditStatus = request.getAuditStatus();

        log.info("提交审核结果, videoId={}, auditStatus={}, auditorId={}",
                videoId, auditStatus, SecurityUtils.getUserId());

        // 1. 校验视频是否存在
        DocVideo existVideo = videoMapper.selectById(videoId);
        if (existVideo == null) {
            log.error("提交审核结果失败, 视频不存在, videoId={}", videoId);
            throw new BusinessException("视频不存在");
        }

        // 2. 更新视频审核状态
        DocVideo videoUpdate = new DocVideo();
        videoUpdate.setId(videoId);
        videoUpdate.setAuditStatus(auditStatus);
        videoUpdate.setAuditMind(request.getAuditMind());
        videoMapper.updateById(videoUpdate);

        // 3. 创建审核记录
        DocVideoAudit audit = buildAuditRecord(videoId, auditStatus, request.getAuditMind());
        this.save(audit);

        // 4. 更新视频关联的审核ID
        DocVideo auditIdUpdate = new DocVideo();
        auditIdUpdate.setId(videoId);
        auditIdUpdate.setAuditId(audit.getId());
        videoMapper.updateById(auditIdUpdate);

        log.info("提交审核结果完成, videoId={}, auditId={}", videoId, audit.getId());
        return true;
    }

    /**
     * 批量提交审核结果
     *
     * <p>对多个视频执行相同的审核结果批量提交。采用逐条处理策略，每条独立事务控制，
     * 避免单条失败导致全部回滚。如需原子性批量操作，请调用方自行保证业务一致性。</p>
     *
     * @param request 批量审核提交请求（videoIds、auditStatus必填）
     * @return true-全部提交完成（部分失败会记录错误日志但不中断流程）
     * @throws BusinessException 当请求参数无效时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAuditBatch(AuditBatchSubmitRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getVideoIds())) {
            log.warn("批量提交审核结果失败, 视频ID列表为空");
            throw new BusinessException("视频ID列表不能为空");
        }
        if (request.getAuditStatus() == null) {
            log.warn("批量提交审核结果失败, 审核状态为空");
            throw new BusinessException("审核状态不能为空");
        }

        List<Long> videoIds = request.getVideoIds();
        Integer auditStatus = request.getAuditStatus();
        String auditMind = request.getAuditMind();

        log.info("批量提交审核结果, videoIds={}, auditStatus={}, count={}",
                videoIds, auditStatus, videoIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long videoId : videoIds) {
            try {
                AuditSubmitRequest submitRequest = new AuditSubmitRequest();
                submitRequest.setVideoId(videoId);
                submitRequest.setAuditStatus(auditStatus);
                submitRequest.setAuditMind(auditMind);
                submitAudit(submitRequest);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("批量审核单条处理失败, videoId={}, error={}", videoId, e.getMessage(), e);
            }
        }

        log.info("批量提交审核结果完成, 成功{}条, 失败{}条", successCount, failCount);

        // 若全部失败，抛出异常使事务回滚
        if (failCount == videoIds.size()) {
            throw new BusinessException("批量审核全部失败，请检查视频状态");
        }

        return true;
    }

    /**
     * 获取审核日志
     *
     * <p>查询所有人工审核操作日志，按审核时间倒序排列，支持分页。</p>
     *
     * @param pageQuery 分页参数（pageNum、pageSize）
     * @return 审核日志分页结果，包含视频标题、审核人、审核时间、审核结果等
     */
    @Override
    public PageResult<VideoAuditLogVO> getAuditLogs(PageQuery pageQuery) {
        log.info("获取审核日志, pageNum={}, pageSize={}", pageQuery.getPageNum(), pageQuery.getPageSize());

        // 计算分页偏移量
        long offset = calculateOffset(pageQuery);

        // 查询审核日志列表
        List<VideoAuditLogVO> list = baseMapper.selectAuditLogs(offset, pageQuery.getPageSize());

        // 查询总记录数
        long total = baseMapper.countAuditLogs();

        log.info("获取审核日志完成, 共{}条记录", total);
        return PageResult.of(list, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 获取违规原因列表
     *
     * <p>从远程字典服务获取视频违规原因字典数据，转换为前端需要的VO格式。</p>
     *
     * @return 违规原因列表（id-label结构），若字典服务异常则返回空列表
     */
    @Override
    public List<ViolationReasonVO> getViolationReasons() {
        log.info("获取违规原因列表");

        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(DictTypeConstants.VIDEO_VIOLATION_REASON);

            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.warn("获取违规原因列表失败, 响应异常, code={}", result != null ? result.getCode() : "null");
                return Collections.emptyList();
            }

            List<ViolationReasonVO> list = result.getData().stream()
                    .map(this::convertToViolationReasonVO)
                    .toList();

            log.info("获取违规原因列表完成, 共{}条", list.size());
            return list;

        } catch (Exception e) {
            log.error("从字典服务获取违规原因失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算分页偏移量
     *
     * @param pageQuery 分页参数
     * @return 偏移量（offset）
     */
    private long calculateOffset(PageQuery pageQuery) {
        return (pageQuery.getPageNum() - 1L) * pageQuery.getPageSize();
    }

    /**
     * 将DocVideo实体转换为审核队列VO
     *
     * @param video 视频实体
     * @return 审核队列VO
     */
    private VideoAuditQueueVO convertToQueueVO(DocVideo video) {
        VideoAuditQueueVO vo = new VideoAuditQueueVO();
        vo.setId(video.getId());
        vo.setVideoTitle(video.getVideoTitle());
        vo.setBroadCode(video.getBroadCode());
        vo.setStatus(AuditStatus.codeToValue(video.getAuditStatus()));
        vo.setRiskLevel(RISK_LEVEL_LOW);
        vo.setIsAiChecked(false);
        vo.setCreateTime(Objects.toString(video.getCreateTime(), ""));
        return vo;
    }

    /**
     * 将字典数据转换为违规原因VO
     *
     * @param dict 字典数据
     * @return 违规原因VO
     */
    private ViolationReasonVO convertToViolationReasonVO(SysDictDataApi dict) {
        ViolationReasonVO vo = new ViolationReasonVO();
        vo.setId(dict.getDictValue());
        vo.setLabel(dict.getDictLabel());
        return vo;
    }

    /**
     * 校验审核提交请求参数
     *
     * @param request 审核提交请求
     * @throws BusinessException 参数无效时抛出
     */
    private void validateSubmitRequest(AuditSubmitRequest request) {
        if (request == null) {
            throw new BusinessException("审核请求不能为空");
        }
        if (request.getVideoId() == null || request.getVideoId() <= 0) {
            throw new BusinessException("视频ID不能为空");
        }
        if (request.getAuditStatus() == null) {
            throw new BusinessException("审核状态不能为空");
        }
        // 校验审核状态值是否合法（0-待审核 1-通过 2-驳回）
        if (request.getAuditStatus() < 0 || request.getAuditStatus() > 2) {
            throw new BusinessException("审核状态值不合法");
        }
    }

    /**
     * 构建审核记录实体
     *
     * @param videoId     视频ID
     * @param auditStatus 审核状态
     * @param auditMind   审核意见
     * @return 审核记录实体
     */
    private DocVideoAudit buildAuditRecord(Long videoId, Integer auditStatus, String auditMind) {
        DocVideoAudit audit = new DocVideoAudit();
        audit.setVideoId(videoId);
        audit.setAuditType(AUDIT_TYPE_MANUAL);
        audit.setAuditStatus(auditStatus);
        audit.setAuditMind(auditMind);
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditorId(SecurityUtils.getUserId());
        audit.setAuditorName(SecurityUtils.getUserName());
        return audit;
    }
}
