package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocUserInteraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户交互关系Mapper接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Mapper
public interface DocUserInteractionMapper extends BaseMapper<DocUserInteraction> {

    /**
     * 查询用户交互状态
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param interactionType 交互类型
     * @return 交互记录
     */
    @Select("SELECT * FROM doc_user_interaction WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND interaction_type = #{interactionType} ORDER BY create_time DESC LIMIT 1")
    DocUserInteraction selectByUserAndTarget(@Param("userId") Long userId,
                                              @Param("targetType") Integer targetType,
                                              @Param("targetId") Long targetId,
                                              @Param("interactionType") Integer interactionType);

    /**
     * 统计目标的交互数量
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param interactionType 交互类型
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM doc_user_interaction WHERE target_type = #{targetType} AND target_id = #{targetId} AND interaction_type = #{interactionType} AND status = 1")
    Long countByTarget(@Param("targetType") Integer targetType,
                       @Param("targetId") Long targetId,
                       @Param("interactionType") Integer interactionType);

    /**
     * 取消交互
     *
     * @param id 记录ID
     * @return 影响行数
     */
    @Update("UPDATE doc_user_interaction SET status = 0 WHERE id = #{id}")
    int cancelInteraction(@Param("id") Long id);
}
