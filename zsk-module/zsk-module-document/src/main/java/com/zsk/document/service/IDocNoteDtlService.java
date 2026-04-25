package com.zsk.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.document.domain.DocNoteDtl;
import com.zsk.document.domain.dto.DocNoteDtlDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 笔记详情Service接口
 * <p>
 * 定义笔记详情相关的业务逻辑接口，包括：
 * 1. 基础的 CRUD 操作（继承 IService）
 * 2. 根据笔记ID查询/删除详情
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-25
 */
public interface IDocNoteDtlService extends IService<DocNoteDtl> {

    /**
     * 根据笔记ID查询笔记详情
     * <p>
     * 通过 note_id 字段查询对应的笔记内容详情。
     * 由于 note_id 有唯一索引，查询结果最多只有一条记录。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 笔记详情对象，若不存在则返回 null
     */
    DocNoteDtl getByNoteId(Long noteId);

    /**
     * 新增或更新笔记详情
     * <p>
     * 根据 noteId 判断是新增还是更新：
     * - 若该笔记ID已存在详情记录，则执行更新操作
     * - 若不存在，则执行新增操作
     * 使用乐观锁防止并发更新冲突。
     * </p>
     *
     * @param dto 笔记详情DTO
     * @return 是否成功
     */
    boolean saveOrUpdateByNoteId(DocNoteDtlDTO dto);

    /**
     * 根据笔记ID删除笔记详情
     * <p>
     * 使用逻辑删除方式，保留数据便于恢复和审计。
     * 实际执行的是 UPDATE 语句，将 deleted 字段设置为 1。
     * </p>
     *
     * @param noteId 笔记ID
     * @return 是否成功
     */
    boolean removeByNoteId(Long noteId);

    /**
     * 批量根据笔记ID删除笔记详情
     * <p>
     * 批量逻辑删除，用于批量删除笔记时同步删除内容详情。
     * </p>
     *
     * @param noteIds 笔记ID列表
     * @return 是否成功
     */
    boolean removeByNoteIds(List<Long> noteIds);

    /**
     * 上传MD文件并保存到数据库
     * <p>
     * 接收前端上传的Markdown文件，解析文件内容并保存到数据库中。
     * 支持的文件格式：.md、.markdown
     * 文件大小限制：根据配置决定
     * </p>
     *
     * @param noteId 笔记ID（关联document_note.id）
     * @param file   上传的MD文件
     * @return 是否成功
     */
    boolean uploadMdFile(Long noteId, MultipartFile file);
}
