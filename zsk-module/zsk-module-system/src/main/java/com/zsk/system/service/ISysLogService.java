package com.zsk.system.service;

import com.zsk.system.domain.vo.SysRecentLogResponseVo;

/**
 * 管理日志 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysLogService {

    /**
     * 获取最近管理日志
     *
     * @param category 分类（content/user/system）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 日志列表
     */
    SysRecentLogResponseVo getRecentLogs(String category, Integer page, Integer pageSize);
}
