package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.DocAudit;
import com.zsk.document.domain.vo.*;

import java.util.List;

/**
 * 统一审核服务接口
 *
 * <p>支持文档、视频、评论等多种内容类型的审核流程，
 * 通过策略模式分发不同内容类型的差异化逻辑。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
public interface IDocAuditService extends IService<DocAudit> {

    /**
     * 获取审核队列
     *
     * <p>根据目标类型和审核状态筛选审核队列，支持分页查询。
     * targetType 为空时查询全部类型。</p>
     *
     * @param targetType 目标类型（可选，null则查询全部类型）
     * @param auditStatus 审核状态（可选，null则查询全部状态）
     * @param pageQuery  分页参数
     * @return 审核队列分页结果
     */
    PageResult<DocAuditQueueVO> getAuditQueue(Integer targetType, Integer auditStatus, PageQuery pageQuery);

    /**
     * 获取审核详情
     *
     * <p>根据目标类型和目标ID查询最新的审核记录。</p>
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 审核详情
     */
    DocAuditDetailVO getAuditDetail(Integer targetType, Long targetId);

    /**
     * 提交审核结果
     *
     * @param request 审核提交请求
     */
    void submitAudit(DocAuditSubmitRequest request);

    /**
     * 批量提交审核结果
     *
     * @param request 批量审核提交请求
     */
    void submitAuditBatch(DocAuditBatchSubmitRequest request);

    /**
     * 获取审核日志
     *
     * <p>查询审核操作日志，支持按目标类型筛选，按审核时间倒序排列。</p>
     *
     * @param targetType 目标类型（可选，null则查询全部类型）
     * @param pageQuery  分页参数
     * @return 审核日志分页结果
     */
    PageResult<DocAuditLogVO> getAuditLogs(Integer targetType, PageQuery pageQuery);

    /**
     * 获取违规原因列表
     *
     * <p>根据目标类型从字典服务获取对应的违规原因字典数据。</p>
     *
     * @param targetType 目标类型
     * @return 违规原因列表
     */
    List<DocViolationReasonVO> getViolationReasons(Integer targetType);

    /**
     * 提交内容到审核队列
     *
     * <p>当内容（文档/视频/评论）发布时调用此方法，在 document_audit 表中
     * 创建一条 audit_status=0 的待审核记录，使内容进入审核队列。</p>
     *
     * @param targetType 目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     * @param targetId   目标ID
     */
    void submitToAudit(Integer targetType, Long targetId);
}
