package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocVideoCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频合集Mapper接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供对 document_video_collection 表的通用CRUD操作。
 * 复杂查询逻辑通过 Service 层组合 LambdaQueryWrapper 实现，无需额外XML配置。
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-28
 */
@Mapper
public interface DocVideoCollectionMapper extends BaseMapper<DocVideoCollection> {

}
