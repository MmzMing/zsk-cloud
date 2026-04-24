package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频Mapper接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Mapper
public interface DocVideoMapper extends BaseMapper<DocVideo> {

    /**
     * 查询审核队列
     *
     * @param auditStatus 审核状态
     * @param offset      偏移量
     * @param limit       每页条数
     * @return 视频列表
     */
    @Select("<script>" +
            "SELECT * FROM document_video WHERE deleted = 0 " +
            "<if test='auditStatus != null'> AND audit_status = #{auditStatus} </if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<DocVideo> selectAuditQueue(@Param("auditStatus") Integer auditStatus,
                                    @Param("offset") long offset,
                                    @Param("limit") long limit);

    /**
     * 统计审核队列数量
     *
     * @param auditStatus 审核状态
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM document_video WHERE deleted = 0 " +
            "<if test='auditStatus != null'> AND audit_status = #{auditStatus} </if>" +
            "</script>")
    long countAuditQueue(@Param("auditStatus") Integer auditStatus);

    /**
     * 统计视频总浏览量
     *
     * @return 总浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM document_video WHERE deleted = 0")
    Long sumViewCount();

    /**
     * 按时间范围统计视频浏览量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM document_video WHERE deleted = 0 AND create_time BETWEEN #{startTime} AND #{endTime}")
    Long sumViewCountByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
