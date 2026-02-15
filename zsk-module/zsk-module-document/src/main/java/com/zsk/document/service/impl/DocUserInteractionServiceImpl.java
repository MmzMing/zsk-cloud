package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocUserInteraction;
import com.zsk.document.mapper.DocUserInteractionMapper;
import com.zsk.document.service.IDocUserInteractionService;
import org.springframework.stereotype.Service;

/**
 * 用户交互关系 服务实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Service
public class DocUserInteractionServiceImpl extends ServiceImpl<DocUserInteractionMapper, DocUserInteraction> implements IDocUserInteractionService {
}
