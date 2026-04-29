package com.zsk.document.service.audit;

import com.zsk.common.core.enums.AuditTargetType;
import com.zsk.document.domain.vo.DocAuditQueueVO;

/**
 * 审核目标策略接口
 *
 * <p>定义不同内容类型在审核流程中的差异化行为。
 * 每种内容类型（文档、视频、评论等）实现此接口，
 * 由统一审核服务通过策略模式调用。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
public interface AuditTargetStrategy {

    /**
     * 获取该策略支持的审核目标类型
     *
     * @return 审核目标类型枚举
     */
    AuditTargetType getTargetType();

    /**
     * 构建审核队列展示项
     *
     * <p>根据目标ID查询内容信息，构建审核队列中展示的VO对象。</p>
     *
     * @param targetId 目标ID
     * @return 审核队列展示项，目标不存在时返回null
     */
    DocAuditQueueVO buildQueueItem(Long targetId);

    /**
     * 更新主表的审核状态
     *
     * <p>审核结果提交后，同步回写主表的 audit_status 字段。</p>
     *
     * @param targetId    目标ID
     * @param auditStatus 审核状态
     * @param auditMind   审核意见
     */
    void updateAuditStatus(Long targetId, Integer auditStatus, String auditMind);

    /**
     * 获取目标内容的标题
     *
     * <p>用于审核日志中展示内容标题。</p>
     *
     * @param targetId 目标ID
     * @return 内容标题，目标不存在时返回null
     */
    String getTargetTitle(Long targetId);

    /**
     * 获取目标内容的上传者/作者ID
     *
     * <p>用于审核通知等场景。</p>
     *
     * @param targetId 目标ID
     * @return 上传者ID，目标不存在时返回null
     */
    Long getTargetUserId(Long targetId);

    /**
     * 获取该内容类型对应的违规原因字典类型
     *
     * @return 字典类型编码
     */
    String getViolationDictType();
}
