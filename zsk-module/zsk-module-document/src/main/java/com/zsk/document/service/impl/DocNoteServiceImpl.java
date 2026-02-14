package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocNote;
import com.zsk.document.mapper.DocNoteMapper;
import com.zsk.document.service.IDocNoteService;
import org.springframework.stereotype.Service;

/**
 * 笔记Service业务层处理
 * 
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Service
public class DocNoteServiceImpl extends ServiceImpl<DocNoteMapper, DocNote> implements IDocNoteService {
}
