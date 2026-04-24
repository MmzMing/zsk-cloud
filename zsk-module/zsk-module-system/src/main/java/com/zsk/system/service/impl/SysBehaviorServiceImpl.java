package com.zsk.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.log.domain.OperLog;
import com.zsk.system.domain.dto.SysBehaviorQuery;
import com.zsk.system.domain.vo.SysBehaviorDetailVO;
import com.zsk.system.domain.vo.SysBehaviorEventVO;
import com.zsk.system.domain.vo.SysBehaviorUserVO;
import com.zsk.system.service.ISysBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 行为审计 服务层实现 (v2)
 * <p>
 * 数据源：MongoDB {@code sys_oper_log}。
 * <ol>
 *   <li>用户列表：聚合 operName 维度，统计行为次数 + 计算风险等级</li>
 *   <li>行为列表：多条件分页查询（用户/类型/时间范围/IP），列表参数响应做截断</li>
 *   <li>行为详情：根据 _id 返回完整 OperLog</li>
 * </ol>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysBehaviorServiceImpl implements ISysBehaviorService {

    /**
     * 列表场景下参数/响应字段截断长度
     */
    private static final int LIST_PREVIEW_MAX_LEN = 200;

    /** MongoDB 操作模板 */
    private final MongoTemplate mongoTemplate;

    /**
     * 获取行为审计用户列表
     * <p>
     * 按 operName 聚合统计每个用户的行为次数、最近操作时间、IP，并计算风险等级
     *
     * @return 用户行为聚合列表
     */
    @Override
    public List<SysBehaviorUserVO> listBehaviorUsers() {
        log.info("获取行为审计用户列表");

        // 按 operName 聚合
        GroupOperation group = Aggregation.group("operName")
                .first("operName").as("operName")
                .count().as("operCount")
                .max("operTime").as("lastOperTime")
                .last("operIp").as("lastOperIp");
        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, "operCount"));
        Aggregation aggregation = Aggregation.newAggregation(group, sort);

        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, OperLog.class, Map.class);

        List<SysBehaviorUserVO> users = new ArrayList<>();
        for (Map result : results) {
            String operName = result.get("operName") != null ? result.get("operName").toString() : null;
            if (StrUtil.isBlank(operName)) {
                continue;
            }
            SysBehaviorUserVO vo = new SysBehaviorUserVO();
            vo.setOperName(operName);
            Number operCount = (Number) result.get("operCount");
            vo.setOperCount(operCount != null ? operCount.longValue() : 0L);
            vo.setLastOperTime(toLocalDateTime(result.get("lastOperTime")));
            vo.setLastOperIp(result.get("lastOperIp") != null ? result.get("lastOperIp").toString() : null);
            vo.setRiskLevel(calcRiskLevel(vo.getOperCount()));
            users.add(vo);
        }

        log.info("获取行为审计用户列表完成, 数量={}", users.size());
        return users;
    }

    /**
     * 分页查询用户行为列表
     * <p>
     * 支持按用户/业务类型/标题/IP/状态/时间范围等多条件分页查询，
     * 列表场景下参数和响应字段会做截断处理
     *
     * @param query 查询条件
     * @return 分页行为事件列表
     */
    @Override
    public PageResult<SysBehaviorEventVO> pageEvents(SysBehaviorQuery query) {
        log.info("分页查询用户行为列表, query={}", query);

        Query mongoQuery = buildEventQuery(query);

        // 1. 总数
        long total = mongoTemplate.count(mongoQuery, OperLog.class);
        if (total == 0) {
            log.info("分页查询用户行为列表完成, 总数=0");
            return PageResult.of(List.of(), 0L, query.getPageNum(), query.getPageSize());
        }

        // 2. 分页 + 排序（最近时间倒序）
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        mongoQuery.with(Sort.by(Sort.Direction.DESC, "operTime"))
                .skip((pageNum - 1) * pageSize)
                .limit((int) pageSize);

        List<OperLog> logs = mongoTemplate.find(mongoQuery, OperLog.class);

        // 3. 转 VO（列表场景做参数/响应截断）
        List<SysBehaviorEventVO> records = new ArrayList<>(logs.size());
        for (OperLog operLog : logs) {
            records.add(toEventVO(operLog));
        }

        log.info("分页查询用户行为列表完成, 总数={}, 当前页数量={}", total, records.size());
        return PageResult.of(records, total, pageNum, pageSize);
    }

    /**
     * 获取行为详情
     * <p>
     * 根据行为记录ID返回完整的操作日志详情，包含请求参数和响应结果
     *
     * @param id 行为记录ID
     * @return 行为详情
     * @throws BusinessException 行为记录ID为空或不存在时抛出
     */
    @Override
    public SysBehaviorDetailVO getDetail(String id) {
        log.info("获取行为详情, id={}", id);

        if (StrUtil.isBlank(id)) {
            log.warn("行为记录ID不能为空");
            throw new BusinessException("行为记录ID不能为空");
        }
        OperLog operLog = mongoTemplate.findById(id, OperLog.class);
        if (operLog == null) {
            log.warn("行为记录不存在或已被清理, id={}", id);
            throw new BusinessException("行为记录不存在或已被清理: " + id);
        }

        log.info("获取行为详情完成, id={}", id);
        return toDetailVO(operLog);
    }

    // ====================== 私有方法 ======================

    /**
     * 构建行为列表查询条件
     * <p>
     * 根据查询DTO组装MongoDB查询条件，支持：
     * <ul>
     *   <li>操作人精确匹配</li>
     *   <li>业务类型精确匹配</li>
     *   <li>标题模糊匹配</li>
     *   <li>IP模糊匹配</li>
     *   <li>状态精确匹配</li>
     *   <li>操作时间范围查询</li>
     * </ul>
     *
     * @param query 查询条件DTO
     * @return MongoDB查询对象
     */
    private Query buildEventQuery(SysBehaviorQuery query) {
        Query mongoQuery = new Query();
        if (StrUtil.isNotBlank(query.getUserName())) {
            // 操作人精确匹配（前端从用户列表选择）
            mongoQuery.addCriteria(Criteria.where("operName").is(query.getUserName()));
        }
        if (query.getBusinessType() != null) {
            mongoQuery.addCriteria(Criteria.where("businessType").is(query.getBusinessType()));
        }
        if (StrUtil.isNotBlank(query.getTitle())) {
            mongoQuery.addCriteria(Criteria.where("title").regex(query.getTitle(), "i"));
        }
        if (StrUtil.isNotBlank(query.getOperIp())) {
            mongoQuery.addCriteria(Criteria.where("operIp").regex(query.getOperIp(), "i"));
        }
        if (query.getStatus() != null) {
            mongoQuery.addCriteria(Criteria.where("status").is(query.getStatus()));
        }
        if (query.getBeginTime() != null && query.getEndTime() != null) {
            mongoQuery.addCriteria(Criteria.where("operTime")
                    .gte(query.getBeginTime()).lte(query.getEndTime()));
        } else if (query.getBeginTime() != null) {
            mongoQuery.addCriteria(Criteria.where("operTime").gte(query.getBeginTime()));
        } else if (query.getEndTime() != null) {
            mongoQuery.addCriteria(Criteria.where("operTime").lte(query.getEndTime()));
        }
        return mongoQuery;
    }

    /**
     * OperLog → SysBehaviorEventVO（列表，截断）
     * <p>
     * 将操作日志实体转换为列表视图对象，参数和响应字段会做截断处理
     *
     * @param operLog 操作日志实体
     * @return 行为事件视图对象
     */
    private SysBehaviorEventVO toEventVO(OperLog operLog) {
        SysBehaviorEventVO vo = new SysBehaviorEventVO();
        vo.setId(operLog.getId());
        vo.setOperName(operLog.getOperName());
        vo.setTitle(operLog.getTitle());
        vo.setBusinessType(operLog.getBusinessType());
        vo.setActionType(actionName(operLog.getBusinessType()));
        vo.setOperUrl(operLog.getOperUrl());
        vo.setRequestMethod(operLog.getRequestMethod());
        vo.setOperParam(truncate(operLog.getOperParam()));
        vo.setJsonResult(truncate(operLog.getJsonResult()));
        vo.setOperIp(operLog.getOperIp());
        vo.setOperLocation(operLog.getOperLocation());
        vo.setStatus(operLog.getStatus());
        vo.setOperTime(operLog.getOperTime());
        vo.setCostTime(operLog.getCostTime());
        return vo;
    }

    /**
     * OperLog → SysBehaviorDetailVO（详情，完整字段）
     * <p>
     * 将操作日志实体转换为详情视图对象，保留完整字段不做截断
     *
     * @param operLog 操作日志实体
     * @return 行为详情视图对象
     */
    private SysBehaviorDetailVO toDetailVO(OperLog operLog) {
        SysBehaviorDetailVO vo = new SysBehaviorDetailVO();
        vo.setId(operLog.getId());
        vo.setOperName(operLog.getOperName());
        vo.setTitle(operLog.getTitle());
        vo.setBusinessType(operLog.getBusinessType());
        vo.setActionType(actionName(operLog.getBusinessType()));
        vo.setMethod(operLog.getMethod());
        vo.setRequestMethod(operLog.getRequestMethod());
        vo.setOperUrl(operLog.getOperUrl());
        vo.setOperIp(operLog.getOperIp());
        vo.setOperLocation(operLog.getOperLocation());
        vo.setOperParam(operLog.getOperParam());
        vo.setJsonResult(operLog.getJsonResult());
        vo.setStatus(operLog.getStatus());
        vo.setErrorMsg(operLog.getErrorMsg());
        vo.setOperTime(operLog.getOperTime());
        vo.setCostTime(operLog.getCostTime());
        return vo;
    }

    /**
     * 业务类型代码转名称
     * <p>
     * 与 OperLog.businessType 注释保持一致
     *
     * @param businessType 业务类型代码
     * @return 业务类型名称
     */
    private String actionName(Integer businessType) {
        if (businessType == null) {
            return "其它";
        }
        return switch (businessType) {
            case 1 -> "新增";
            case 2 -> "修改";
            case 3 -> "删除";
            case 4 -> "授权";
            case 5 -> "导出";
            case 6 -> "导入";
            case 7 -> "强退";
            case 8 -> "生成代码";
            case 9 -> "清空数据";
            case 10 -> "查询";
            default -> "其它";
        };
    }

    /**
     * 列表场景截断字符串
     * <p>
     * 当字符串长度超过 {@link #LIST_PREVIEW_MAX_LEN} 时截断并追加省略号
     *
     * @param text 原始字符串
     * @return 截断后的字符串
     */
    private String truncate(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        return text.length() > LIST_PREVIEW_MAX_LEN
                ? text.substring(0, LIST_PREVIEW_MAX_LEN) + "..."
                : text;
    }

    /**
     * 计算风险等级
     * <p>
     * 根据用户行为次数计算风险等级：
     * <ul>
     *   <li>high：操作次数 &gt; 100</li>
     *   <li>medium：操作次数 &gt; 50</li>
     *   <li>low：其他情况</li>
     * </ul>
     *
     * @param count 操作次数
     * @return 风险等级（high/medium/low）
     */
    private String calcRiskLevel(Long count) {
        if (count == null) {
            return "low";
        }
        if (count > 100) {
            return "high";
        }
        if (count > 50) {
            return "medium";
        }
        return "low";
    }

    /**
     * MongoDB 聚合返回的时间统一转 LocalDateTime
     * <p>
     * 支持 LocalDateTime 和 Date 两种类型的转换
     *
     * @param value MongoDB聚合返回的时间对象
     * @return 转换后的 LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }
}
