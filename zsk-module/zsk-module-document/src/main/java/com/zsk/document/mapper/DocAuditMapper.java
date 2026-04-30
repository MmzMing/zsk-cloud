package com.zsk.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.document.domain.DocAudit;
import com.zsk.document.domain.vo.DocAuditLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 统一审核记录Mapper接口
 *
 * <p>提供统一审核记录的CRUD操作。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Mapper
public interface DocAuditMapper extends BaseMapper<DocAudit> {
}
