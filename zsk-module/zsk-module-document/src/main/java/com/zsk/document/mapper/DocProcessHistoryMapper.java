package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocProcessHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件处理历史Mapper接口
 * 
 * @author wuhuaming
 */
@Mapper
public interface DocProcessHistoryMapper extends BaseMapper<DocProcessHistory> {
}
