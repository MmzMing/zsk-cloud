package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记信息Mapper接口
 *
 * @author wuhuaming
 */
@Mapper
public interface DocNoteMapper extends BaseMapper<DocNote> {

    /**
     * 统计文档总浏览量
     *
     * @return 总浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM document_note WHERE deleted = 0")
    Long sumViewCount();

    /**
     * 按时间范围统计文档浏览量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM document_note WHERE deleted = 0 AND create_time BETWEEN #{startTime} AND #{endTime}")
    Long sumViewCountByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询审核队列
     *
     * <p>根据审核状态筛选文档审核队列，支持分页。</p>
     *
     * @param auditStatus 审核状态
     * @param offset      偏移量
     * @param limit       每页条数
     * @return 文档列表
     */
    @Select("<script>" +
            "SELECT id, note_name, user_id, broad_code, audit_status, create_time " +
            "FROM document_note WHERE deleted = 0 " +
            "<if test='auditStatus != null'> AND audit_status = #{auditStatus} </if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<DocNote> selectAuditQueue(@Param("auditStatus") Integer auditStatus,
                                   @Param("offset") long offset,
                                   @Param("limit") long limit);

    /**
     * 统计审核队列数量
     *
     * @param auditStatus 审核状态
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM document_note WHERE deleted = 0 " +
            "<if test='auditStatus != null'> AND audit_status = #{auditStatus} </if>" +
            "</script>")
    long countAuditQueue(@Param("auditStatus") Integer auditStatus);
}
