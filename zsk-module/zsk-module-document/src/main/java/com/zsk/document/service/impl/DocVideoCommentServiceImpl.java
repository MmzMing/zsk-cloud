package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocVideoComment;
import com.zsk.document.mapper.DocVideoCommentMapper;
import com.zsk.document.service.IDocVideoCommentService;
import org.springframework.stereotype.Service;

/**
 * 视频详情评论Service业务层处理
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@Service
public class DocVideoCommentServiceImpl extends ServiceImpl<DocVideoCommentMapper, DocVideoComment> implements IDocVideoCommentService {
}
