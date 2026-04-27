package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocNoteAudit;
import com.zsk.document.domain.vo.*;

import java.util.List;

/**
 * 文档审核详情Service接口
 *
 * <p>提供文档审核队列查询、审核详情获取、审核结果提交（单条/批量）、审核日志查询及违规原因获取等功能。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface IDocNoteAuditService extends IService<DocNoteAudit> {

    /**
     * 获取审核队列
     *
     * @param auditStatus 审核状态（可选）
     * @param pageQuery   分页参数
     * @return 审核队列分页结果
     */
    PageResult<NoteAuditQueueVO> getAuditQueue(Integer auditStatus, PageQuery pageQuery);

    /**
     * 获取审核详情
     *
     * @param noteId 文档ID
     * @return 审核详情
     */
    DocNoteAudit getAuditDetail(Long noteId);

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
    PageResult<NoteAuditLogVO> getAuditLogs(PageQuery pageQuery);

    /**
     * 获取违规原因列表
     *
     * @return 违规原因列表
     */
    List<ViolationReasonVO> getViolationReasons();
}
