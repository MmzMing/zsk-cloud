package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocNoteComment;
import com.zsk.document.mapper.DocNoteCommentMapper;
import com.zsk.document.service.IDocNoteCommentService;
import org.springframework.stereotype.Service;

/**
 * 笔记评论Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Service
public class DocNoteCommentServiceImpl extends ServiceImpl<DocNoteCommentMapper, DocNoteComment> implements IDocNoteCommentService {
}
