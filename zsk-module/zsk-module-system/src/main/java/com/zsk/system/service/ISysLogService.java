package com.zsk.system.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.dto.SysLogQueryDTO;
import com.zsk.system.domain.vo.SysRecentLogVo;

import java.util.List;

/**
 * 管理日志 服务接口
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public interface ISysLogService {

    /**
     * 分页查询管理日志
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询条件
     * @return 分页日志结果
     */
    PageResult<SysRecentLogVo> pageLogs(PageQuery pageQuery, SysLogQueryDTO queryDTO);

    /**
     * 批量删除管理日志
     *
     * @param ids 日志ID列表
     * @return 是否成功
     */
    boolean deleteLogByIds(List<String> ids);
}
