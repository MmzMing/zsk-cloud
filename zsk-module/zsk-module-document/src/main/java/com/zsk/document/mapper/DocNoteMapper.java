package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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
     * @param endTime 结束时间
     * @return 浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM document_note WHERE deleted = 0 AND create_time BETWEEN #{startTime} AND #{endTime}")
    Long sumViewCountByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
