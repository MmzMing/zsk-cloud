package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocVideoComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频详情评论Mapper接口
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@Mapper
public interface DocVideoCommentMapper extends BaseMapper<DocVideoComment> {
}
