package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.exception.BusinessException;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.dto.DocNoteDtlDTO;
import com.zsk.document.mapper.DocNoteDtlMapper;
import com.zsk.document.service.IDocNoteDtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
     * 笔记详情Service业务层处理
     * <p>
     * 实现笔记详情的 CRUD 操作。
     * 遵循 Service 层约束：负责核心业务逻辑、事务控制，方法粒度适中，单一职责。
     * </p>
     *
     * @author wuhuaming
     * @version 1.0
     * @date 2026-04-25
     */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocNoteDtlServiceImpl extends ServiceImpl<DocNoteDtlMapper, DocNoteDtl> implements IDocNoteDtlService {

    /**
     * 笔记详情Mapper
     * <p>
     * 通过构造器注入，符合依赖注入规范。
     * 用于执行数据库的 CRUD 操作。
     * </p>
     */
    private final DocNoteDtlMapper docNoteDtlMapper;

    /**
     * 根据笔记ID查询笔记详情
     * <p>
     * 通过 note_id 字段查询对应的笔记内容详情。
     * 由于 note_id 有唯一索引，查询结果最多只有一条记录。
     * 查询条件包含 deleted = 0，排除已逻辑删除的数据。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记详情对象，若不存在则返回 null
     */
    @Override
    public DocNoteDtl getByNoteId(Long noteId) {
        log.info("根据笔记ID查询笔记详情, noteId={}", noteId);

        // 参数校验
        if (noteId == null) {
            log.warn("查询笔记详情失败, 笔记ID为空");
            return null;
        }

        // 构建查询条件：note_id 匹配且未删除
        LambdaQueryWrapper<DocNoteDtl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocNoteDtl::getNoteId, noteId);
        wrapper.eq(DocNoteDtl::getDeleted, 0);

        // 执行查询
        DocNoteDtl result = docNoteDtlMapper.selectOne(wrapper);

        if (result == null) {
            log.info("笔记详情不存在, noteId={}", noteId);
        } else {
            log.info("查询笔记详情成功, noteId={}, contentLength={}", noteId,
                    result.getContent() != null ? result.getContent().length() : 0);
        }

        return result;
    }

    /**
     * 新增或更新笔记详情
     * <p>
     * 根据 noteId 判断是新增还是更新：
     * - 若该笔记ID已存在详情记录，则执行更新操作（乐观锁校验）
     * - 若不存在，则执行新增操作
     * 使用 LambdaUpdateWrapper 构建更新条件，避免 SQL 注入风险。
     * </p>
     *
     * @param dto 笔记详情DTO
     * @return 是否成功
     * @throws BusinessException 参数校验失败时抛出
     */
    @Override
    public boolean saveOrUpdateByNoteId(DocNoteDtlDTO dto) {
        log.info("新增或更新笔记详情, noteId={}", dto.getNoteId());

        // 参数校验
        if (dto == null || dto.getNoteId() == null) {
            log.error("新增或更新笔记详情失败, 参数为空");
            throw new BusinessException("参数错误：笔记ID不能为空");
        }

        if (!StringUtils.hasText(dto.getContent())) {
            log.error("新增或更新笔记详情失败, 内容为空, noteId={}", dto.getNoteId());
            throw new BusinessException("参数错误：笔记内容不能为空");
        }

        // 查询是否已存在该笔记的详情记录
        DocNoteDtl existDtl = getByNoteId(dto.getNoteId());

        if (existDtl != null) {
            // 已存在，执行更新操作
            log.info("笔记详情已存在，执行更新操作, id={}, noteId={}", existDtl.getId(), dto.getNoteId());

            LambdaUpdateWrapper<DocNoteDtl> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(DocNoteDtl::getId, existDtl.getId());
            // 乐观锁：通过 version 字段防止并发更新冲突
            updateWrapper.eq(DocNoteDtl::getVersion, existDtl.getVersion());
            updateWrapper.set(DocNoteDtl::getContent, dto.getContent());

            boolean result = docNoteDtlMapper.update(updateWrapper) > 0;
            if (result) {
                log.info("更新笔记详情成功, noteId={}", dto.getNoteId());
            } else {
                log.warn("更新笔记详情失败, 可能是版本冲突, noteId={}", dto.getNoteId());
                throw new BusinessException("更新失败：数据已被其他用户修改，请刷新后重试");
            }
            return result;
        } else {
            // 不存在，执行新增操作
            log.info("笔记详情不存在，执行新增操作, noteId={}", dto.getNoteId());

            DocNoteDtl newDtl = new DocNoteDtl();
            newDtl.setNoteId(dto.getNoteId());
            newDtl.setContent(dto.getContent());
            newDtl.setVersion(0L);

            boolean result = docNoteDtlMapper.insert(newDtl) > 0;
            if (result) {
                log.info("新增笔记详情成功, noteId={}, id={}", dto.getNoteId(), newDtl.getId());
            } else {
                log.error("新增笔记详情失败, noteId={}", dto.getNoteId());
            }
            return result;
        }
    }

    /**
     * 根据笔记ID删除笔记详情
     * <p>
     * 使用逻辑删除方式，保留数据便于恢复和审计。
     * 实际执行的是 UPDATE 语句，将 deleted 字段设置为 1。
     * 删除前校验记录是否存在。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 是否成功
     */
    @Override
    public boolean removeByNoteId(Long noteId) {
        log.info("根据笔记ID删除笔记详情, noteId={}", noteId);

        // 参数校验
        if (noteId == null) {
            log.warn("删除笔记详情失败, 笔记ID为空");
            return false;
        }

        // 查询记录是否存在
        DocNoteDtl existDtl = getByNoteId(noteId);
        if (existDtl == null) {
            log.warn("删除笔记详情失败, 记录不存在, noteId={}", noteId);
            return false;
        }

        // 执行逻辑删除
        LambdaUpdateWrapper<DocNoteDtl> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocNoteDtl::getNoteId, noteId);
        updateWrapper.set(DocNoteDtl::getDeleted, 1);

        boolean result = docNoteDtlMapper.update(updateWrapper) > 0;
        if (result) {
            log.info("删除笔记详情成功, noteId={}", noteId);
        } else {
            log.error("删除笔记详情失败, noteId={}", noteId);
        }

        return result;
    }

    /**
     * 批量根据笔记ID删除笔记详情
     * <p>
     * 批量逻辑删除，用于批量删除笔记时同步删除内容详情。
     * 使用 IN 语句批量更新，避免循环单条更新（N+1 问题）。
     * </p>
     *
     * @param noteIds 笔记ID列表
     * @return 是否成功
     */
    @Override
    public boolean removeByNoteIds(List<Long> noteIds) {
        log.info("批量删除笔记详情, noteIds数量={}", noteIds != null ? noteIds.size() : 0);

        // 参数校验
        if (noteIds == null || noteIds.isEmpty()) {
            log.warn("批量删除笔记详情失败, ID列表为空");
            return false;
        }

        // 执行批量逻辑删除
        LambdaUpdateWrapper<DocNoteDtl> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocNoteDtl::getNoteId, noteIds);
        updateWrapper.set(DocNoteDtl::getDeleted, 1);

        int affectedRows = docNoteDtlMapper.update(updateWrapper);
        log.info("批量删除笔记详情完成, 影响{}条记录", affectedRows);

        return affectedRows > 0;
    }

    /**
     * 上传MD文件并保存到数据库
     * <p>
     * 接收前端上传的Markdown文件，解析文件内容并保存到数据库中。
     * 处理流程：
     * 1. 参数校验（noteId非空、文件非空）
     * 2. 文件格式校验（仅允许.md和.markdown后缀）
     * 3. 读取文件内容（使用UTF-8编码）
     * 4. 调用saveOrUpdateByNoteId保存或更新笔记详情
     * </p>
     *
     * @param noteId 笔记ID（关联document_note.id）
     * @param file   上传的MD文件
     * @return 是否成功
     * @throws BusinessException 参数校验失败或文件处理异常时抛出
     */
    @Override
    public boolean uploadMdFile(Long noteId, MultipartFile file) {
        log.info("上传MD文件并保存到数据库, noteId={}, fileName={}", noteId, 
                file != null ? file.getOriginalFilename() : null);

        // 参数校验：文件不能为空
        if (file == null || file.isEmpty()) {
            log.error("上传MD文件失败, 文件为空, noteId={}", noteId);
            throw new BusinessException("参数错误：文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            log.error("上传MD文件失败, 文件名为空, noteId={}", noteId);
            throw new BusinessException("参数错误：文件名不能为空");
        }

        // 文件格式校验：仅允许.md和.markdown后缀
        String lowerFilename = originalFilename.toLowerCase();
        if (!lowerFilename.endsWith(".md") && !lowerFilename.endsWith(".markdown")) {
            log.error("上传MD文件失败, 文件格式不支持, fileName={}, noteId={}", originalFilename, noteId);
            throw new BusinessException("文件格式不支持：仅支持 .md 和 .markdown 格式的文件");
        }

        // 读取文件内容
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
            log.info("读取MD文件内容成功, noteId={}, contentLength={}", noteId, content.length());
        } catch (IOException e) {
            log.error("读取MD文件内容失败, noteId={}, fileName={}", noteId, originalFilename, e);
            throw new BusinessException("文件读取失败：" + e.getMessage());
        }

        // 校验文件内容是否为空
        if (!StringUtils.hasText(content)) {
            log.error("上传MD文件失败, 文件内容为空, noteId={}, fileName={}", noteId, originalFilename);
            throw new BusinessException("文件内容不能为空");
        }

        // 构建DTO并保存到数据库
        DocNoteDtlDTO dto = new DocNoteDtlDTO();
        dto.setNoteId(noteId);
        dto.setContent(content);

        boolean result = saveOrUpdateByNoteId(dto);
        if (result) {
            log.info("MD文件上传并保存成功, noteId={}, fileName={}", noteId, originalFilename);
        } else {
            log.error("MD文件上传保存失败, noteId={}, fileName={}", noteId, originalFilename);
        }

        return result;
    }
}
