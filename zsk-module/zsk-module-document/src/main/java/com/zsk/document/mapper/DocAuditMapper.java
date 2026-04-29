package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocAudit;
import com.zsk.document.domain.vo.DocAuditLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 统一审核记录Mapper接口
 *
 * <p>提供统一审核记录的CRUD操作及审核日志查询。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Mapper
public interface DocAuditMapper extends BaseMapper<DocAudit> {

    /**
     * 查询审核日志
     *
     * <p>按审核时间倒序排列，支持按目标类型筛选。</p>
     *
     * @param targetType 目标类型（可选，null则查询全部类型）
     * @param offset     偏移量
     * @param limit      每页条数
     * @return 审核日志列表
     */
    @Select("<script>" +
            "SELECT id, target_type AS targetType, target_id AS targetId, " +
            "auditor_name AS auditorName, audit_time AS auditTime, " +
            "CASE audit_status WHEN 1 THEN 'approved' WHEN 2 THEN 'rejected' WHEN 3 THEN 'withdrawn' ELSE 'pending' END AS result, " +
            "audit_mind AS auditMind, risk_level AS riskLevel " +
            "FROM document_audit WHERE deleted = 0 " +
            "<if test='targetType != null'> AND target_type = #{targetType} </if>" +
            "ORDER BY audit_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<DocAuditLogVO> selectAuditLogs(@Param("targetType") Integer targetType,
                                         @Param("offset") long offset,
                                         @Param("limit") long limit);

    /**
     * 统计审核日志数量
     *
     * @param targetType 目标类型（可选，null则统计全部类型）
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM document_audit WHERE deleted = 0 " +
            "<if test='targetType != null'> AND target_type = #{targetType} </if>" +
            "</script>")
    long countAuditLogs(@Param("targetType") Integer targetType);
}
