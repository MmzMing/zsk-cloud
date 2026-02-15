package com.zsk.document.domain.context;

/**
 * 用户交互关系常量上下文
 * 定义目标类型和交互类型常量
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public final class DocUserInteractionContext {

    private DocUserInteractionContext() {
    }

    /**
     * 目标类型：文档
     */
    public static final int TARGET_TYPE_NOTE = 1;

    /**
     * 目标类型：视频
     */
    public static final int TARGET_TYPE_VIDEO = 2;

    /**
     * 目标类型：用户
     */
    public static final int TARGET_TYPE_USER = 3;

    /**
     * 目标类型：评论
     */
    public static final int TARGET_TYPE_COMMENT = 4;

    /**
     * 交互类型：点赞
     */
    public static final int INTERACTION_TYPE_LIKE = 1;

    /**
     * 交互类型：收藏
     */
    public static final int INTERACTION_TYPE_FAVORITE = 2;

    /**
     * 交互类型：关注
     */
    public static final int INTERACTION_TYPE_FOLLOW = 3;
}
