package com.zsk.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.log.domain.OperLog;
import com.zsk.system.domain.dto.SysLogQueryDTO;
import com.zsk.system.domain.vo.SysRecentLogVo;
import com.zsk.system.service.ISysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理日志 服务实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl implements ISysLogService {

    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询管理日志
     *
     * @param pageQuery 分页参数
     * @param queryDTO  查询条件
     * @return 分页日志结果
     */
    @Override
    public PageResult<SysRecentLogVo> pageLogs(PageQuery pageQuery, SysLogQueryDTO queryDTO) {
        Query query = buildQuery(queryDTO);

        /** 统计总数 */
        long total = mongoTemplate.count(query, OperLog.class);

        /** 分页查询 */
        query.with(PageRequest.of(pageQuery.getPageNum().intValue() - 1, pageQuery.getPageSize().intValue()));
        query.with(Sort.by(Sort.Direction.DESC, "operTime"));

        List<OperLog> operLogs = mongoTemplate.find(query, OperLog.class);

        /** 转换结果 */
        List<SysRecentLogVo> list = new ArrayList<>();
        for (OperLog operLog : operLogs) {
            list.add(convertToVo(operLog));
        }

        return PageResult.of(list, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 批量删除管理日志
     *
     * @param ids 日志ID列表
     * @return 是否成功
     */
    @Override
    public boolean deleteLogByIds(List<String> ids) {
        try {
            /** 根据ID列表批量删除 */
            Query query = new Query(Criteria.where("id").in(ids));
            mongoTemplate.remove(query, OperLog.class);
            return true;
        } catch (Exception e) {
            log.error("批量删除管理日志失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建查询条件
     *
     * @param queryDTO 查询条件
     * @return 查询对象
     */
    private Query buildQuery(SysLogQueryDTO queryDTO) {
        Query query = new Query();

        /** 根据分类过滤 */
        if (StrUtil.isNotBlank(queryDTO.getCategory())) {
            Criteria criteria = buildCategoryCriteria(queryDTO.getCategory());
            if (criteria != null) {
                query.addCriteria(criteria);
            }
        }

        /** 根据操作人过滤 */
        if (StrUtil.isNotBlank(queryDTO.getOperator())) {
            query.addCriteria(Criteria.where("operName").regex(queryDTO.getOperator()));
        }

        /** 根据请求URL过滤 */
        if (StrUtil.isNotBlank(queryDTO.getRequestUrl())) {
            query.addCriteria(Criteria.where("operUrl").regex(queryDTO.getRequestUrl()));
        }

        /** 根据请求方式过滤 */
        if (StrUtil.isNotBlank(queryDTO.getRequestMethod())) {
            query.addCriteria(Criteria.where("requestMethod").is(queryDTO.getRequestMethod()));
        }

        /** 根据操作状态过滤 */
        if (queryDTO.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(queryDTO.getStatus()));
        }

        /** 根据模块标题过滤 */
        if (StrUtil.isNotBlank(queryDTO.getTitle())) {
            query.addCriteria(Criteria.where("title").regex(queryDTO.getTitle()));
        }

        /** 根据业务类型过滤 */
        if (queryDTO.getBusinessType() != null) {
            query.addCriteria(Criteria.where("businessType").is(queryDTO.getBusinessType()));
        }

        /** 根据操作时间范围过滤 */
        if (StrUtil.isNotBlank(queryDTO.getBeginTime()) && StrUtil.isNotBlank(queryDTO.getEndTime())) {
            LocalDateTime beginTime = LocalDateTime.parse(queryDTO.getBeginTime(), FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(queryDTO.getEndTime(), FORMATTER);
            query.addCriteria(Criteria.where("operTime").gte(beginTime).lte(endTime));
        } else if (StrUtil.isNotBlank(queryDTO.getBeginTime())) {
            LocalDateTime beginTime = LocalDateTime.parse(queryDTO.getBeginTime(), FORMATTER);
            query.addCriteria(Criteria.where("operTime").gte(beginTime));
        } else if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
            LocalDateTime endTime = LocalDateTime.parse(queryDTO.getEndTime(), FORMATTER);
            query.addCriteria(Criteria.where("operTime").lte(endTime));
        }

        return query;
    }

    /**
     * 根据分类构建查询条件
     *
     * @param category 分类
     * @return 查询条件
     */
    private Criteria buildCategoryCriteria(String category) {
        return switch (category) {
            case "content" -> Criteria.where("operUrl").regex("/document|/video|/note");
            case "user" -> Criteria.where("operUrl").regex("/user|/role|/permission");
            case "system" -> Criteria.where("operUrl").regex("/config|/dict|/menu|/dept");
            default -> null;
        };
    }

    /**
     * 转换为视图对象
     *
     * @param operLog 操作日志
     * @return 视图对象
     */
    private SysRecentLogVo convertToVo(OperLog operLog) {
        SysRecentLogVo vo = new SysRecentLogVo();
        vo.setId(operLog.getId());
        vo.setCategory(determineCategory(operLog.getOperUrl()));
        vo.setOperator(operLog.getOperName() != null ? operLog.getOperName() : "系统");
        vo.setAction(determineAction(operLog.getBusinessType(), operLog.getTitle()));
        vo.setDetail(buildDetail(operLog));
        vo.setCreatedAt(operLog.getOperTime() != null ? operLog.getOperTime().format(FORMATTER) : "");
        vo.setRequestMethod(operLog.getRequestMethod());
        vo.setRequestUrl(operLog.getOperUrl());
        vo.setRequestParam(operLog.getOperParam());
        vo.setResponseResult(operLog.getJsonResult());
        vo.setStatus(operLog.getStatus());
        vo.setCostTime(operLog.getCostTime());
        vo.setOperIp(operLog.getOperIp());
        return vo;
    }

    /**
     * 根据URL确定分类
     *
     * @param url 请求URL
     * @return 分类
     */
    private String determineCategory(String url) {
        if (url == null) {
            return "system";
        }
        if (url.contains("/document") || url.contains("/video") || url.contains("/note")) {
            return "content";
        }
        if (url.contains("/user") || url.contains("/role") || url.contains("/permission")) {
            return "user";
        }
        return "system";
    }

    /**
     * 确定动作名称
     *
     * @param businessType 业务类型
     * @param title        标题
     * @return 动作名称
     */
    private String determineAction(Integer businessType, String title) {
        if (StrUtil.isNotBlank(title)) {
            return title;
        }
        if (businessType == null) {
            return "操作";
        }
        return switch (businessType) {
            case 1 -> "新增";
            case 2 -> "修改";
            case 3 -> "删除";
            case 4 -> "授权";
            case 5 -> "导出";
            case 6 -> "导入";
            default -> "操作";
        };
    }

    /**
     * 构建详细描述
     *
     * @param operLog 操作日志
     * @return 详细描述
     */
    private String buildDetail(OperLog operLog) {
        StringBuilder detail = new StringBuilder();
        if (operLog.getTitle() != null) {
            detail.append(operLog.getTitle());
        }
        if (operLog.getOperUrl() != null) {
            if (detail.length() > 0) {
                detail.append(" - ");
            }
            detail.append(operLog.getOperUrl());
        }
        return detail.toString();
    }
}
