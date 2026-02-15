package com.zsk.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zsk.common.log.domain.OperLog;
import com.zsk.system.service.IOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl implements IOperLogService {

    private final MongoTemplate mongoTemplate;

    /**
     * 查询操作日志列表
     *
     * @param title 模块标题
     * @param operName 操作人员
     * @param status 操作状态
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 日志列表
     */
    @Override
    public List<OperLog> selectOperLogList(String title, String operName, Integer status, Integer pageNum, Integer pageSize) {
        Query query = buildQuery(title, operName, status);
        query.with(PageRequest.of(pageNum - 1, pageSize));
        query.with(Sort.by(Sort.Direction.DESC, "operTime"));
        return mongoTemplate.find(query, OperLog.class);
    }

    /**
     * 查询操作日志总数
     *
     * @param title 模块标题
     * @param operName 操作人员
     * @param status 操作状态
     * @return 总数
     */
    @Override
    public long countOperLog(String title, String operName, Integer status) {
        Query query = buildQuery(title, operName, status);
        return mongoTemplate.count(query, OperLog.class);
    }

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 是否成功
     */
    @Override
    public boolean deleteOperLogByIds(List<String> ids) {
        try {
            Query query = new Query(Criteria.where("id").in(ids));
            mongoTemplate.remove(query, OperLog.class);
            return true;
        } catch (Exception e) {
            log.error("批量删除操作日志失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 清空操作日志
     *
     * @return 是否成功
     */
    @Override
    public boolean clearOperLog() {
        try {
            mongoTemplate.dropCollection(OperLog.class);
            return true;
        } catch (Exception e) {
            log.error("清空操作日志失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建查询条件
     *
     * @param title 模块标题
     * @param operName 操作人员
     * @param status 操作状态
     * @return 查询对象
     */
    private Query buildQuery(String title, String operName, Integer status) {
        Query query = new Query();
        if (StrUtil.isNotBlank(title)) {
            query.addCriteria(Criteria.where("title").regex(title));
        }
        if (StrUtil.isNotBlank(operName)) {
            query.addCriteria(Criteria.where("operName").regex(operName));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        return query;
    }
}
