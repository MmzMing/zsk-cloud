package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocProcess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件处理任务Mapper接口
 *
 * @author wuhuaming
 */
@Mapper
public interface DocProcessMapper extends BaseMapper<DocProcess> {
}
