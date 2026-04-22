package com.zsk.system.service;

import com.zsk.system.domain.vo.SysDashboardOverviewVo;

import java.util.List;

/**
 * 仪表盘 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysDashboardService {

    /**
     * 获取概览数据
     *
     * @return 概览数据列表
     */
    List<SysDashboardOverviewVo> getOverview();
}
