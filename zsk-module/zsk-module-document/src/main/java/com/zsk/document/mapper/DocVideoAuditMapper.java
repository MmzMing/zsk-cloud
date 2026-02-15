package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocVideoAudit;
import com.zsk.document.domain.vo.VideoAuditLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 视频审核详情Mapper接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Mapper
public interface DocVideoAuditMapper extends BaseMapper<DocVideoAudit> {

    /**
     * 查询审核日志
     *
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 审核日志列表
     */
    @Select("SELECT a.id, a.video_id as videoId, d.video_title as videoTitle, " +
            "a.auditor_name as auditorName, a.audit_time as auditTime, " +
            "CASE a.audit_status WHEN 1 THEN 'approved' WHEN 2 THEN 'rejected' ELSE 'pending' END as result, " +
            "a.audit_mind as auditMind " +
            "FROM document_video_audit a " +
            "LEFT JOIN document_video_detail d ON a.video_id = d.id " +
            "WHERE a.deleted = 0 " +
            "ORDER BY a.audit_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<VideoAuditLogVO> selectAuditLogs(@Param("offset") long offset, @Param("limit") long limit);

    /**
     * 统计审核日志数量
     *
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM document_video_audit WHERE deleted = 0")
    long countAuditLogs();
}
