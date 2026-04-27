package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocVideoCollectionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 视频合集关联Mapper接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 document_video_collection_item 表的通用CRUD操作。
 * 同时定义了两个自定义查询方法，用于高效获取合集中的视频信息和统计数量。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Mapper
public interface DocVideoCollectionItemMapper extends BaseMapper<DocVideoCollectionItem> {

    /**
     * 查询合集中的视频ID列表（按排序顺序）
     * <p>
     * 根据合集ID查询该合集中所有未删除的视频ID，按 sort_order 升序、create_time 升序排列。
     * 用于合集详情页按用户设定的顺序展示视频列表。
     * </p>
     *
     * @param collectionId 合集ID
     * @return 视频ID列表（已按排序顺序排列）
     */
    @Select("SELECT video_id FROM document_video_collection_item WHERE collection_id = #{collectionId} AND deleted = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Long> selectVideoIdsByCollectionId(@Param("collectionId") Long collectionId);

    /**
     * 统计合集中的视频数量
     * <p>
     * 统计指定合集中未删除的视频记录总数。
     * 用于 Service 层维护 video_count 冗余字段，避免频繁关联查询。
     * </p>
     *
     * @param collectionId 合集ID
     * @return 视频数量
     */
    @Select("SELECT COUNT(*) FROM document_video_collection_item WHERE collection_id = #{collectionId} AND deleted = 0")
    Long countByCollectionId(@Param("collectionId") Long collectionId);

}
