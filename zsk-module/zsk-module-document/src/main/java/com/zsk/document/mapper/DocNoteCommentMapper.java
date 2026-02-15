package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocNoteComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记评论Mapper接口
 *
 * @author wuhuaming
 */
@Mapper
public interface DocNoteCommentMapper extends BaseMapper<DocNoteComment> {
}
