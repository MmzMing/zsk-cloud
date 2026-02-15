package com.zsk.system.service.impl;

import com.zsk.common.log.domain.OperLog;
import com.zsk.system.domain.vo.SysRecentLogResponseVo;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理日志 服务实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl implements ISysLogService {

    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public SysRecentLogResponseVo getRecentLogs(String category, Integer page, Integer pageSize) {
        SysRecentLogResponseVo response = new SysRecentLogResponseVo();

        /** 构建查询条件 */
        Query query = new Query();

        /** 根据分类过滤 */
        if (category != null && !category.isEmpty()) {
            Criteria criteria = buildCategoryCriteria(category);
            if (criteria != null) {
                query.addCriteria(criteria);
            }
        }

        /** 统计总数 */
        long total = mongoTemplate.count(query, OperLog.class);

        /** 分页查询 */
        query.with(PageRequest.of(page - 1, pageSize));
        query.with(Sort.by(Sort.Direction.DESC, "operTime"));

        List<OperLog> operLogs = mongoTemplate.find(query, OperLog.class);

        /** 转换结果 */
        List<SysRecentLogVo> list = new ArrayList<>();
        for (OperLog operLog : operLogs) {
            SysRecentLogVo vo = convertToVo(operLog);
            list.add(vo);
        }

        response.setList(list);
        response.setTotal(total);

        return response;
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
     * @param title 标题
     * @return 动作名称
     */
    private String determineAction(Integer businessType, String title) {
        if (title != null && !title.isEmpty()) {
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
