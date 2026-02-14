DROP TABLE IF EXISTS `document_note`;
CREATE TABLE `document_note`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `note_name` varchar(100) NOT NULL COMMENT '笔记名称',
  `note_tags` varchar(200) NULL DEFAULT NULL COMMENT '笔记标签（多个标签用英文逗号分隔，如：Java,MySQL,优化）',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '笔记简介/描述',
  `broad_code` varchar(20) NULL DEFAULT NULL COMMENT '大类（如：技术、生活、职场）',
  `narrow_code` varchar(20) NULL DEFAULT NULL COMMENT '小类（如：技术-Java、生活-美食）',
  `note_grade` int(4) NULL DEFAULT NULL COMMENT '笔记等级（1-入门 2-进阶 3-高级 4-专家）',
  `note_mode` int(4) NULL DEFAULT NULL COMMENT '笔记模式（1-公开 2-仅自己可见 3-指定租户可见 4-付费可见）',
  `suitable_users` varchar(255) NULL DEFAULT NULL COMMENT '适合人群（多个人群用英文逗号分隔，如：初学者,开发工程师,架构师）',
  `audit_status` int(4) NOT NULL COMMENT '审核状态（0-待审核 1-审核通过 2-审核驳回 3-已撤回）',
  `status` int(4) NOT NULL DEFAULT 1 COMMENT '笔记状态（1-正常 2-下架 3-草稿 4-过期）',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '笔记发布时间（审核通过后生效）',
  `view_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '笔记浏览量',
  `like_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '笔记点赞量',
  `version` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '乐观锁版本号（防并发更新冲突）',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注（如审核驳回原因、特殊说明）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cn_broad_code`(`broad_code` ASC) USING BTREE COMMENT '大类索引（高频分类查询）',
  INDEX `idx_cn_narrow_code`(`narrow_code` ASC) USING BTREE COMMENT '小类索引（细分分类查询）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文档管理服务_笔记信息表' ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS `document_note_comment`;
CREATE TABLE `document_note_comment`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
    `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `note_id` bigint(20) NOT NULL COMMENT '关联笔记ID（关联document_note.id）',
  `comment_user_id` varchar(64) NOT NULL COMMENT '评论人ID',
  `comment_content` varchar(1000) NOT NULL COMMENT '评论内容',
  `parent_comment_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID（用于回复：NULL为根评论，非NULL为回复某条评论）',
  `audit_status` int(4) NOT NULL COMMENT '审核状态（0-待审核 1-审核通过 2-审核驳回）',
  `status` int(4) NOT NULL DEFAULT 1 COMMENT '评论状态（1-正常 2-隐藏 3-删除）',
  `like_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '评论点赞数',
  `version` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '乐观锁版本号（防并发更新冲突）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_nc_note_id`(`note_id` ASC) USING BTREE COMMENT '笔记ID索引（核心关联，查询某笔记的所有评论）',
  INDEX `idx_nc_parent_comment_id`(`parent_comment_id` ASC) USING BTREE COMMENT '父评论ID索引（查询某条评论的回复）',
  INDEX `idx_nc_audit_status`(`audit_status` ASC) USING BTREE COMMENT '审核状态索引（审核流程查询）',
  INDEX `idx_nc_status`(`status` ASC) USING BTREE COMMENT '评论状态索引（筛选正常/隐藏评论）',
  INDEX `idx_nc_create_time`(`create_time` ASC) USING BTREE COMMENT '评论时间索引（按时间排序/筛选）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文档管理服务_笔记评论表' ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `document_note_pic`;
CREATE TABLE `document_note_pic`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
    `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `note_id` bigint(20) NOT NULL COMMENT '笔记ID',
  `pic_url` varchar(1024) NOT NULL COMMENT '图片URL',
  `sort` int(4) NULL DEFAULT 1 COMMENT '排序（1-封面图）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_np_note_id`(`note_id` ASC) USING BTREE COMMENT '笔记ID'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '内容管理服务_笔记多图关联表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `document_files`;
CREATE TABLE `document_files`  (
  `id` bigint(20) NOT NULL COMMENT '参数主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `file_type` varchar(12) NULL DEFAULT NULL COMMENT '文件类型（图片、文档，视频）',
  `bucket` varchar(128) NULL DEFAULT NULL COMMENT '存储目录',
  `file_id` varchar(32) NOT NULL COMMENT '文件id',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `file_path` varchar(512) NULL DEFAULT NULL COMMENT '存储路径',
  `url` varchar(1024) NULL DEFAULT NULL COMMENT '媒资文件访问地址',
  `remark` varchar(32) NULL DEFAULT NULL COMMENT '备注',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mf_file_id`(`file_id` ASC) USING BTREE COMMENT '文件id唯一索引 '
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '内容管理服务_文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 5. 文件处理任务表
-- ----------------------------
DROP TABLE IF EXISTS `document_process`;
CREATE TABLE `document_process`  (
  `id` bigint(20) NOT NULL COMMENT '参数主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `file_id` varchar(120) NOT NULL COMMENT '文件标识',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `bucket` varchar(128) NOT NULL COMMENT '存储桶',
  `file_path` varchar(512) NULL DEFAULT NULL COMMENT '存储路径',
  `status` varchar(12) NOT NULL COMMENT '状态,1:未处理，2：处理成功  3处理失败',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `fail_count` int NULL DEFAULT 0 COMMENT '失败次数',
  `url` varchar(1024) NULL DEFAULT NULL COMMENT '媒资文件访问地址',
  `error_msg` varchar(1024) NULL DEFAULT NULL COMMENT '失败原因',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mp_file_id`(`file_id` ASC) USING BTREE COMMENT '文件id唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '内容管理服务_文件类型转换表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 6. 文件处理历史记录表
-- ----------------------------
DROP TABLE IF EXISTS `document_process_history`;
CREATE TABLE `document_process_history`  (
  `id` bigint(20) NOT NULL COMMENT '参数主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `file_id` varchar(120) NOT NULL COMMENT '文件标识',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `bucket` varchar(128) NOT NULL COMMENT '存储源',
  `status` varchar(12) NOT NULL COMMENT '状态,1:未处理，2：处理成功  3处理失败',
  `finish_time` datetime NOT NULL COMMENT '完成时间',
  `url` varchar(1024) NOT NULL COMMENT '媒资文件访问地址',
  `fail_count` int NULL DEFAULT 0 COMMENT '失败次数',
  `file_path` varchar(512) NULL DEFAULT NULL COMMENT '文件路径',
  `error_msg` varchar(1024) NULL DEFAULT NULL COMMENT '失败原因',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mph_file_id`(`file_id` ASC) USING BTREE COMMENT '文件id唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '内容管理服务_文件处理历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 7. 视频详情表
-- ----------------------------
DROP TABLE IF EXISTS `document_video_detail`;
CREATE TABLE `document_video_detail`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `file_id` varchar(32) NOT NULL COMMENT '文件ID（关联document_files.file_id）',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `broad_code` varchar(20) NULL DEFAULT NULL COMMENT '大类（如：技术、生活、职场）',
  `narrow_code` varchar(20) NULL DEFAULT NULL COMMENT '小类（如：技术-Java、生活-美食）',
  `tags` varchar(500) NULL DEFAULT NULL COMMENT '标签（多个用英文逗号分隔）',
  `file_content` longtext NULL COMMENT '视频描述/文本内容',
  `meta_data` text NULL COMMENT '元数据（JSON格式，如分辨率、时长、编码等）',
  `view_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '点赞量',
  `comment_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '评论量',
  `collect_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '收藏量',
  `audit_status` int(4) NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核 1-审核通过 2-审核驳回）',
  `audit_mind` varchar(500) NULL DEFAULT NULL COMMENT '审核意见',
  `status` int(4) NOT NULL DEFAULT 1 COMMENT '状态（1-正常 2-下架 3-草稿）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dvd_file_id`(`file_id` ASC) USING BTREE COMMENT '文件ID唯一索引',
  INDEX `idx_dvd_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_dvd_category`(`broad_code`, `narrow_code`) USING BTREE COMMENT '分类复合索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '内容管理服务_视频详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 8. 视频详情评论表
-- ----------------------------
DROP TABLE IF EXISTS `document_video_comment`;
CREATE TABLE `document_video_comment`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `video_id` varchar(32) NOT NULL COMMENT '关联视频文件ID（关联document_files.file_id）',
  `comment_user_id` varchar(64) NOT NULL COMMENT '评论人ID',
  `comment_content` varchar(1000) NOT NULL COMMENT '评论内容',
  `parent_comment_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID（用于回复：NULL为根评论，非NULL为回复某条评论）',
  `audit_status` int(4) NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核 1-审核通过 2-审核驳回）',
  `status` int(4) NOT NULL DEFAULT 1 COMMENT '评论状态（1-正常 2-隐藏 3-删除）',
  `like_count` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '评论点赞数',
  `version` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0否1是)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_vc_video_id`(`video_id` ASC) USING BTREE COMMENT '视频ID索引',
  INDEX `idx_vc_parent_comment_id`(`parent_comment_id` ASC) USING BTREE COMMENT '父评论ID索引',
  INDEX `idx_vc_audit_status`(`audit_status` ASC) USING BTREE COMMENT '审核状态索引',
  INDEX `idx_vc_status`(`status` ASC) USING BTREE COMMENT '评论状态索引',
  INDEX `idx_vc_create_time`(`create_time` ASC) USING BTREE COMMENT '评论时间索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '内容管理服务_视频详情评论表' ROW_FORMAT = DYNAMIC;
