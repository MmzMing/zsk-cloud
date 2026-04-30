package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.enums.AuditStatus;
import com.zsk.common.core.enums.AuditTargetType;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.document.domain.DocAudit;
import com.zsk.document.domain.vo.*;
import com.zsk.document.mapper.DocAuditMapper;
import com.zsk.document.service.IDocAuditService;
import com.zsk.document.service.audit.AuditTargetStrategy;
import com.zsk.system.api.RemoteDictService;
import com.zsk.system.api.domain.SysDictDataApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一审核服务实现
 *
 * <p>通过策略模式分发不同内容类型的审核逻辑，
 * 统一处理审核队列查询、审核详情获取、审核结果提交、审核日志查询及违规原因获取等功能。</p>
 *
 * <p>核心设计：Spring 自动注入所有 {@link AuditTargetStrategy} 实现，
 * 在构造函数中构建 targetType → strategy 的映射，运行时根据 targetType 动态分发。</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-30
 */
@Slf4j
@Service
public class DocAuditServiceImpl extends ServiceImpl<DocAuditMapper, DocAudit> implements IDocAuditService {

    /**
     * 审核类型：人工审核
     */
    private static final String AUDIT_TYPE_MANUAL = "manual";

    /**
     * 风险等级：低
     */
    private static final String RISK_LEVEL_LOW = "low";

    /**
     * 目标类型 → 审核策略 映射
     *
     * <p>Key 为 {@link AuditTargetType} 枚举，Value 为对应的策略实现。
     * 在构造函数中由 Spring 自动注入的所有策略实现构建而成。</p>
     */
    private final Map<AuditTargetType, AuditTargetStrategy> strategyMap;

    /**
     * 远程字典服务
     *
     * <p>用于获取违规原因等字典数据。</p>
     */
    private final RemoteDictService remoteDictService;

    /**
     * 构造函数：自动注入所有策略实现，构建映射
     *
     * <p>Spring 会自动收集所有 {@link AuditTargetStrategy} 实现类并注入为 List，
     * 本方法将其转换为 Map 以便按 targetType 快速查找。</p>
     *
     * @param strategies       所有审核策略实现列表
     * @param remoteDictService 远程字典服务
     */
    public DocAuditServiceImpl(List<AuditTargetStrategy> strategies, RemoteDictService remoteDictService) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AuditTargetStrategy::getTargetType, Function.identity()));
        this.remoteDictService = remoteDictService;
    }

    /**
     * 获取审核队列
     *
     * <p>根据目标类型和审核状态筛选审核队列，支持分页查询。
     * targetType 为空时查询全部类型的审核队列。</p>
     *
     * @param targetType  目标类型（可选，null则查询全部类型）
     * @param auditStatus 审核状态（可选，null则查询全部状态）
     * @param pageQuery   分页参数（pageNum、pageSize）
     * @return 审核队列分页结果，包含内容基本信息、审核状态、风险等级等
     */
    @Override
    public PageResult<DocAuditQueueVO> getAuditQueue(Integer targetType, Integer auditStatus, PageQuery pageQuery) {
        log.info("获取审核队列, targetType={}, auditStatus={}, pageNum={}, pageSize={}",
                targetType, auditStatus, pageQuery.getPageNum(), pageQuery.getPageSize());

        long offset = calculateOffset(pageQuery);

        List<DocAudit> auditList = this.lambdaQuery()
                .eq(targetType != null, DocAudit::getTargetType, targetType)
                .eq(auditStatus != null, DocAudit::getAuditStatus, auditStatus)
                .orderByDesc(DocAudit::getCreateTime)
                .last("LIMIT " + offset + "," + pageQuery.getPageSize())
                .list();

        List<DocAuditQueueVO> voList = auditList.stream()
                .map(audit -> {
                    AuditTargetType type = AuditTargetType.getByCode(audit.getTargetType());
                    if (type == null) {
                        return null;
                    }
                    AuditTargetStrategy strategy = getStrategy(type);
                    DocAuditQueueVO vo = strategy.buildQueueItem(audit.getTargetId());
                    if (vo != null) {
                        vo.setId(audit.getId());
                        vo.setRiskLevel(audit.getRiskLevel());
                        vo.setIsAiChecked("ai".equals(audit.getAuditType()));
                    }
                    return vo;
                })
                .filter(vo -> vo != null)
                .toList();

        long total = this.lambdaQuery()
                .eq(targetType != null, DocAudit::getTargetType, targetType)
                .eq(auditStatus != null, DocAudit::getAuditStatus, auditStatus)
                .count();

        log.info("获取审核队列完成, 共{}条记录", total);
        return PageResult.of(voList, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 获取审核详情
     *
     * <p>根据目标类型和目标ID查询最新的审核记录，按创建时间倒序取第一条。
     * 同时通过策略获取内容标题补充到详情中。</p>
     *
     * @param targetType 目标类型（必填）
     * @param targetId   目标ID（必填）
     * @return 最新的审核详情，若记录不存在则返回null
     * @throws BusinessException 当targetId无效或targetType不合法时抛出
     */
    @Override
    public DocAuditDetailVO getAuditDetail(Integer targetType, Long targetId) {
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }

        AuditTargetType type = validateAndGetTargetType(targetType);
        log.info("获取审核详情, targetType={}, targetId={}", type.getDesc(), targetId);

        DocAudit audit = this.lambdaQuery()
                .eq(DocAudit::getTargetType, type.getCode())
                .eq(DocAudit::getTargetId, targetId)
                .orderByDesc(DocAudit::getCreateTime)
                .last("LIMIT 1")
                .one();

        if (audit == null) {
            log.warn("审核详情不存在, targetType={}, targetId={}", type.getDesc(), targetId);
            return null;
        }

        AuditTargetStrategy strategy = getStrategy(type);
        String targetTitle = strategy.getTargetTitle(targetId);

        DocAuditDetailVO vo = new DocAuditDetailVO();
        vo.setId(audit.getId());
        vo.setTargetType(audit.getTargetType());
        vo.setTargetId(audit.getTargetId());
        vo.setTargetTitle(targetTitle);
        vo.setAuditType(audit.getAuditType());
        vo.setAuditStatus(audit.getAuditStatus());
        vo.setAuditResult(audit.getAuditResult());
        vo.setRiskLevel(audit.getRiskLevel());
        vo.setAuditMind(audit.getAuditMind());
        vo.setViolationIds(audit.getViolationIds());
        vo.setAuditorId(audit.getAuditorId());
        vo.setAuditorName(audit.getAuditorName());
        vo.setAuditTime(audit.getAuditTime());
        vo.setCreateTime(audit.getCreateTime());
        return vo;
    }

    /**
     * 提交审核结果
     *
     * <p>单条内容审核结果提交，包含以下逻辑：</p>
     * <ol>
     *   <li>校验请求参数（targetType、targetId、auditStatus）</li>
     *   <li>获取对应策略，校验目标内容是否存在</li>
     *   <li>查找最新的待审核记录，存在则更新；不存在则创建新记录（重新审核场景）</li>
     *   <li>通过策略回写主表的 audit_status 字段</li>
     * </ol>
     *
     * <p>重新审核支持：如果内容已被审核过（audit_status=1或2），再次提交审核时会
     * 将审核状态重置为待审核（0），审核轮次+1，等待新的审核结果。</p>
     *
     * @param request 审核提交请求（targetType、targetId、auditStatus必填）
     * @throws BusinessException 当目标内容不存在或参数无效时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(DocAuditSubmitRequest request) {
        validateSubmitRequest(request);

        AuditTargetType type = AuditTargetType.getByCode(request.getTargetType());
        AuditTargetStrategy strategy = getStrategy(type);

        Long targetId = request.getTargetId();
        Integer auditStatus = request.getAuditStatus();

        log.info("提交审核结果, targetType={}, targetId={}, auditStatus={}, auditorId={}",
                type.getDesc(), targetId, auditStatus, SecurityUtils.getUserId());

        String targetTitle = strategy.getTargetTitle(targetId);
        if (targetTitle == null) {
            log.error("提交审核结果失败, 目标内容不存在, targetType={}, targetId={}", type.getDesc(), targetId);
            throw new BusinessException(type.getDesc() + "不存在");
        }

        // 查找最新的审核记录
        DocAudit latestAudit = this.lambdaQuery()
                .eq(DocAudit::getTargetType, type.getCode())
                .eq(DocAudit::getTargetId, targetId)
                .orderByDesc(DocAudit::getAuditRound)
                .orderByDesc(DocAudit::getCreateTime)
                .last("LIMIT 1")
                .one();

        DocAudit audit;
        if (latestAudit != null && latestAudit.getAuditStatus() != null && latestAudit.getAuditStatus() == AuditStatus.PENDING.getCode()) {
            // 有待审核记录，直接更新
            audit = latestAudit;
            audit.setAuditStatus(auditStatus);
            audit.setAuditMind(request.getAuditMind());
            audit.setViolationIds(CollectionUtils.isEmpty(request.getViolationIds())
                    ? null : String.join(",", request.getViolationIds()));
            audit.setAuditTime(LocalDateTime.now());
            audit.setAuditorId(SecurityUtils.getUserId());
            audit.setAuditorName(SecurityUtils.getUserName());
            this.updateById(audit);
            log.info("更新待审核记录, auditId={}, auditRound={}", audit.getId(), audit.getAuditRound());
        } else if (latestAudit != null) {
            // 已有审核结果，重新审核：创建新记录，轮次+1
            audit = new DocAudit();
            audit.setTargetType(type.getCode());
            audit.setTargetId(targetId);
            audit.setAuditType(AUDIT_TYPE_MANUAL);
            audit.setAuditStatus(auditStatus);
            audit.setAuditMind(request.getAuditMind());
            audit.setRiskLevel(RISK_LEVEL_LOW);
            audit.setViolationIds(CollectionUtils.isEmpty(request.getViolationIds())
                    ? null : String.join(",", request.getViolationIds()));
            audit.setAuditTime(LocalDateTime.now());
            audit.setAuditorId(SecurityUtils.getUserId());
            audit.setAuditorName(SecurityUtils.getUserName());
            audit.setAuditRound(latestAudit.getAuditRound() + 1);
            this.save(audit);
            log.info("重新审核，创建新记录, auditId={}, auditRound={}", audit.getId(), audit.getAuditRound());
        } else {
            // 首次审核
            audit = new DocAudit();
            audit.setTargetType(type.getCode());
            audit.setTargetId(targetId);
            audit.setAuditType(AUDIT_TYPE_MANUAL);
            audit.setAuditStatus(auditStatus);
            audit.setAuditMind(request.getAuditMind());
            audit.setRiskLevel(RISK_LEVEL_LOW);
            audit.setViolationIds(CollectionUtils.isEmpty(request.getViolationIds())
                    ? null : String.join(",", request.getViolationIds()));
            audit.setAuditTime(LocalDateTime.now());
            audit.setAuditorId(SecurityUtils.getUserId());
            audit.setAuditorName(SecurityUtils.getUserName());
            audit.setAuditRound(1);
            this.save(audit);
            log.info("首次审核，创建记录, auditId={}, auditRound={}", audit.getId(), audit.getAuditRound());
        }

        strategy.updateAuditStatus(targetId, auditStatus, request.getAuditMind());

        log.info("提交审核结果完成, targetType={}, targetId={}, auditId={}, auditRound={}",
                type.getDesc(), targetId, audit.getId(), audit.getAuditRound());
    }

    /**
     * 批量提交审核结果
     *
     * <p>对同一类型的多个内容执行相同的审核结果批量提交。采用逐条处理策略，每条独立异常捕获，
     * 避免单条失败导致全部回滚。如需原子性批量操作，请调用方自行保证业务一致性。</p>
     *
     * @param request 批量审核提交请求（targetType、targetIds、auditStatus必填）
     * @throws BusinessException 当请求参数无效或全部失败时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuditBatch(DocAuditBatchSubmitRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getTargetIds())) {
            throw new BusinessException("目标ID列表不能为空");
        }
        if (request.getAuditStatus() == null) {
            throw new BusinessException("审核状态不能为空");
        }

        AuditTargetType type = validateAndGetTargetType(request.getTargetType());
        List<Long> targetIds = request.getTargetIds();

        log.info("批量提交审核结果, targetType={}, targetIds={}, auditStatus={}, count={}",
                type.getDesc(), targetIds, request.getAuditStatus(), targetIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long targetId : targetIds) {
            try {
                DocAuditSubmitRequest submitRequest = new DocAuditSubmitRequest();
                submitRequest.setTargetType(request.getTargetType());
                submitRequest.setTargetId(targetId);
                submitRequest.setAuditStatus(request.getAuditStatus());
                submitRequest.setAuditMind(request.getAuditMind());
                submitRequest.setViolationIds(request.getViolationIds());
                submitAudit(submitRequest);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("批量审核单条处理失败, targetType={}, targetId={}, error={}",
                        type.getDesc(), targetId, e.getMessage(), e);
            }
        }

        log.info("批量提交审核结果完成, 成功{}条, 失败{}条", successCount, failCount);

        if (failCount == targetIds.size()) {
            throw new BusinessException("批量审核全部失败，请检查内容状态");
        }
    }

    /**
     * 获取审核日志
     *
     * <p>查询所有审核操作日志，支持按目标类型筛选，按审核时间倒序排列，支持分页。
     * 日志中的内容标题通过策略动态补充。</p>
     *
     * @param targetType 目标类型（可选，null则查询全部类型）
     * @param pageQuery  分页参数（pageNum、pageSize）
     * @return 审核日志分页结果，包含内容标题、审核人、审核时间、审核结果等
     */
    @Override
    public PageResult<DocAuditLogVO> getAuditLogs(Integer targetType, PageQuery pageQuery) {
        log.info("获取审核日志, targetType={}, pageNum={}, pageSize={}",
                targetType, pageQuery.getPageNum(), pageQuery.getPageSize());

        long offset = calculateOffset(pageQuery);

        List<DocAudit> audits = this.lambdaQuery()
                .eq(targetType != null, DocAudit::getTargetType, targetType)
                .in(DocAudit::getAuditStatus, 1, 2, 3)
                .orderByDesc(DocAudit::getAuditTime)
                .last("LIMIT " + offset + "," + pageQuery.getPageSize())
                .list();

        List<DocAuditLogVO> list = audits.stream()
                .map(audit -> {
                    DocAuditLogVO vo = new DocAuditLogVO();
                    vo.setId(audit.getId());
                    vo.setTargetType(audit.getTargetType());
                    vo.setTargetId(audit.getTargetId());
                    vo.setAuditRound(audit.getAuditRound());
                    vo.setAuditorName(audit.getAuditorName());
                    vo.setAuditTime(audit.getAuditTime() != null ? audit.getAuditTime().toString() : null);
                    vo.setResult(audit.getAuditStatus());
                    vo.setAuditMind(audit.getAuditMind());
                    vo.setRiskLevel(audit.getRiskLevel());
                    return vo;
                })
                .toList();

        if (!list.isEmpty()) {
            for (DocAuditLogVO logVO : list) {
                AuditTargetType type = AuditTargetType.getByCode(logVO.getTargetType());
                if (type != null) {
                    AuditTargetStrategy strategy = getStrategy(type);
                    String title = strategy.getTargetTitle(logVO.getTargetId());
                    if (title != null) {
                        logVO.setTargetTitle(title);
                    }
                }
            }
        }

        long total = this.lambdaQuery()
                .eq(targetType != null, DocAudit::getTargetType, targetType)
                .in(DocAudit::getAuditStatus, 1, 2, 3)
                .count();

        log.info("获取审核日志完成, 共{}条记录", total);
        return PageResult.of(list, total, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 获取违规原因列表
     *
     * <p>根据目标类型从远程字典服务获取对应的违规原因字典数据，转换为前端需要的VO格式。
     * 不同内容类型对应不同的违规原因字典（文档/视频）。</p>
     *
     * @param targetType 目标类型（必填）
     * @return 违规原因列表（id-label结构），若字典服务异常则返回空列表
     */
    @Override
    public List<DocViolationReasonVO> getViolationReasons(Integer targetType) {
        AuditTargetType type = validateAndGetTargetType(targetType);
        AuditTargetStrategy strategy = getStrategy(type);
        String dictType = strategy.getViolationDictType();

        log.info("获取违规原因列表, targetType={}, dictType={}", type.getDesc(), dictType);

        try {
            R<List<SysDictDataApi>> result = remoteDictService.getDictDataByType(dictType);

            if (result == null || result.getCode() != 200 || result.getData() == null) {
                log.warn("获取违规原因列表失败, 响应异常, code={}", result != null ? result.getCode() : "null");
                return Collections.emptyList();
            }

            List<DocViolationReasonVO> list = result.getData().stream()
                    .map(this::convertToViolationReasonVO)
                    .toList();

            log.info("获取违规原因列表完成, 共{}条", list.size());
            return list;

        } catch (Exception e) {
            log.error("从字典服务获取违规原因失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 提交内容到审核队列
     *
     * <p>当内容（文档/视频/评论）发布时调用此方法，在 document_audit 表中
     * 创建一条 audit_status=0 的待审核记录，使内容进入审核队列。
     * 如果该内容已有待审核记录，则跳过不重复创建。</p>
     *
     * @param targetType 目标类型（1-文档 2-视频 3-文档评论 4-视频评论）
     * @param targetId   目标ID
     */
    @Override
    public void submitToAudit(Integer targetType, Long targetId) {
        AuditTargetType type = validateAndGetTargetType(targetType);

        log.info("提交内容到审核队列, targetType={}, targetId={}", type.getDesc(), targetId);

        long existingCount = this.lambdaQuery()
                .eq(DocAudit::getTargetType, type.getCode())
                .eq(DocAudit::getTargetId, targetId)
                .eq(DocAudit::getAuditStatus, AuditStatus.PENDING.getCode())
                .count();

        if (existingCount > 0) {
            log.info("内容已有待审核记录，跳过创建, targetType={}, targetId={}", type.getDesc(), targetId);
            return;
        }

        // 查询最大审核轮次
        Integer maxRound = this.lambdaQuery()
                .eq(DocAudit::getTargetType, type.getCode())
                .eq(DocAudit::getTargetId, targetId)
                .select(DocAudit::getAuditRound)
                .orderByDesc(DocAudit::getAuditRound)
                .last("LIMIT 1")
                .oneOpt()
                .map(DocAudit::getAuditRound)
                .orElse(0);

        DocAudit audit = new DocAudit();
        audit.setTargetType(type.getCode());
        audit.setTargetId(targetId);
        audit.setAuditType(AUDIT_TYPE_MANUAL);
        audit.setAuditStatus(AuditStatus.PENDING.getCode());
        audit.setRiskLevel(RISK_LEVEL_LOW);
        audit.setAuditRound(maxRound + 1);
        this.save(audit);

        log.info("内容已提交到审核队列, targetType={}, targetId={}, auditId={}, auditRound={}",
                type.getDesc(), targetId, audit.getId(), audit.getAuditRound());
    }

    /**
     * 根据目标类型获取对应的审核策略
     *
     * @param type 目标类型枚举
     * @return 审核策略
     * @throws BusinessException 不支持的类型时抛出
     */
    private AuditTargetStrategy getStrategy(AuditTargetType type) {
        AuditTargetStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new BusinessException("不支持的审核目标类型: " + type.getDesc());
        }
        return strategy;
    }

    /**
     * 校验并转换目标类型编码为枚举
     *
     * @param targetType 目标类型编码
     * @return 目标类型枚举
     * @throws BusinessException 类型无效时抛出
     */
    private AuditTargetType validateAndGetTargetType(Integer targetType) {
        if (targetType == null) {
            throw new BusinessException("目标类型不能为空");
        }
        AuditTargetType type = AuditTargetType.getByCode(targetType);
        if (type == null) {
            throw new BusinessException("无效的目标类型: " + targetType);
        }
        return type;
    }

    /**
     * 计算分页偏移量
     *
     * @param pageQuery 分页参数
     * @return 偏移量（offset）
     */
    private long calculateOffset(PageQuery pageQuery) {
        return (pageQuery.getPageNum() - 1L) * pageQuery.getPageSize();
    }

    /**
     * 校验审核提交请求参数
     *
     * @param request 审核提交请求
     * @throws BusinessException 参数无效时抛出
     */
    private void validateSubmitRequest(DocAuditSubmitRequest request) {
        if (request == null) {
            throw new BusinessException("审核请求不能为空");
        }
        if (request.getTargetType() == null) {
            throw new BusinessException("目标类型不能为空");
        }
        if (request.getTargetId() == null || request.getTargetId() <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (request.getAuditStatus() == null) {
            throw new BusinessException("审核状态不能为空");
        }
        if (request.getAuditStatus() < 1 || request.getAuditStatus() > 2) {
            throw new BusinessException("审核状态值不合法，仅支持1-通过 2-驳回");
        }
    }

    /**
     * 将字典数据转换为违规原因VO
     *
     * @param dict 字典数据
     * @return 违规原因VO
     */
    private DocViolationReasonVO convertToViolationReasonVO(SysDictDataApi dict) {
        DocViolationReasonVO vo = new DocViolationReasonVO();
        vo.setId(dict.getDictValue());
        vo.setLabel(dict.getDictLabel());
        return vo;
    }
}
