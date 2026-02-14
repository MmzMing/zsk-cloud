package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocNote;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记信息Mapper接口
 * 
 * @author wuhuaming
 */
@Mapper
public interface DocNoteMapper extends BaseMapper<DocNote> {
}
