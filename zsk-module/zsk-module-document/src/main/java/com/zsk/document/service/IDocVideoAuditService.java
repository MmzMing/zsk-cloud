package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocVideoAudit;
import com.zsk.document.domain.vo.*;

import java.util.List;

/**
 * 视频审核详情Service接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface IDocVideoAuditService extends IService<DocVideoAudit> {

    /**
     * 获取审核队列
     *
     * @param auditStatus 审核状态（可选）
     * @param pageQuery 分页参数
     * @return 审核队列分页结果
     */
    PageResult<VideoAuditQueueVO> getAuditQueue(Integer auditStatus, PageQuery pageQuery);

    /**
     * 获取审核详情
     *
     * @param videoId 视频ID
     * @return 审核详情
     */
    DocVideoAudit getAuditDetail(Long videoId);

    /**
     * 提交审核结果
     *
     * @param request 审核提交请求
     * @return 是否成功
     */
    boolean submitAudit(AuditSubmitRequest request);

    /**
     * 批量提交审核结果
     *
     * @param request 批量审核提交请求
     * @return 是否成功
     */
    boolean submitAuditBatch(AuditBatchSubmitRequest request);

    /**
     * 获取审核日志
     *
     * @param pageQuery 分页参数
     * @return 审核日志分页结果
     */
    PageResult<VideoAuditLogVO> getAuditLogs(PageQuery pageQuery);

    /**
     * 获取违规原因列表
     *
     * @return 违规原因列表
     */
    List<ViolationReasonVO> getViolationReasons();
}
