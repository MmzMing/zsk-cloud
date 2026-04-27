package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocNoteAudit;
import com.zsk.document.domain.vo.NoteAuditLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档审核详情Mapper接口
 *
 * <p>提供文档审核记录的CRUD操作及审核日志查询。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Mapper
public interface DocNoteAuditMapper extends BaseMapper<DocNoteAudit> {

    /**
     * 查询审核日志
     *
     * <p>关联 document_note 表获取文档标题，按审核时间倒序排列。</p>
     *
     * @param offset 偏移量
     * @param limit  每页条数
     * @return 审核日志列表
     */
    @Select("SELECT a.id, a.note_id as noteId, n.note_name as noteName, " +
            "a.auditor_name as auditorName, a.audit_time as auditTime, " +
            "CASE a.audit_status WHEN 1 THEN 'approved' WHEN 2 THEN 'rejected' ELSE 'pending' END as result, " +
            "a.audit_mind as auditMind " +
            "FROM document_note_audit a " +
            "LEFT JOIN document_note n ON a.note_id = n.id " +
            "WHERE a.deleted = 0 " +
            "ORDER BY a.audit_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<NoteAuditLogVO> selectAuditLogs(@Param("offset") long offset, @Param("limit") long limit);

    /**
     * 统计审核日志数量
     *
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM document_note_audit WHERE deleted = 0")
    long countAuditLogs();
}
