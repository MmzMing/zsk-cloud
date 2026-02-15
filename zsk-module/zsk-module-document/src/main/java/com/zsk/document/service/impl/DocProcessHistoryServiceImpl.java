package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocProcessHistory;
import com.zsk.document.mapper.DocProcessHistoryMapper;
import com.zsk.document.service.IDocProcessHistoryService;
import org.springframework.stereotype.Service;

/**
 * 文档处理历史Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Service
public class DocProcessHistoryServiceImpl extends ServiceImpl<DocProcessHistoryMapper, DocProcessHistory> implements IDocProcessHistoryService {
}
