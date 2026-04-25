package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocNoteDtl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 笔记详情Mapper接口
 * <p>
 * 继承 MyBatis-Plus BaseMapper，提供基础的 CRUD 操作。
 * 复杂 SQL 需写在 XML 中，简单查询可使用注解方式。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
@Mapper
public interface DocNoteDtlMapper extends BaseMapper<DocNoteDtl> {

    /**
     * 根据笔记ID查询笔记详情
     * <p>
     * 通过 note_id 字段查询对应的笔记内容详情。
     * 由于 note_id 有唯一索引，查询结果最多只有一条记录。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记详情对象，若不存在则返回 null
     */
    @Select("SELECT * FROM document_note_dtl WHERE note_id = #{noteId} AND deleted = 0")
    DocNoteDtl selectByNoteId(@Param("noteId") Long noteId);

    /**
     * 根据笔记ID删除笔记详情（逻辑删除）
     * <p>
     * 使用逻辑删除方式，保留数据便于恢复和审计。
     * 实际执行的是 UPDATE 语句，将 deleted 字段设置为 1。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 影响行数
     */
    @Select("UPDATE document_note_dtl SET deleted = 1 WHERE note_id = #{noteId}")
    int deleteByNoteId(@Param("noteId") Long noteId);
}
