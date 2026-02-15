package com.zsk.system.service;

import com.zsk.common.log.domain.OperLog;

import java.util.List;

/**
 * 操作日志 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface IOperLogService {

    /**
     * 查询操作日志列表
     *
     * @param title 模块标题
     * @param operName 操作人员
     * @param status 操作状态
     *param pageNum 页码
     * @param pageSize 每页大小
     * @return 日志列表
     */
    List<OperLog> selectOperLogList(String title, String operName, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 查询操作日志总数
     *
     * @param title 模块标题
     * @param operName 操作人员
     * @param status 操作状态
     * @return 总数
     */
    long countOperLog(String title, String operName, Integer status);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 是否成功
     */
    boolean deleteOperLogByIds(List<String> ids);

    /**
     * 清空操作日志
     *
     * @return 是否成功
     */
    boolean clearOperLog();
}
