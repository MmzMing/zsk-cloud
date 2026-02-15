-- ----------------------------
-- 1. 用户管理表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL  COMMENT '用户ID',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` int(4) DEFAULT 0 COMMENT '用户类型（0系统用户）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` int(4) DEFAULT 0 COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `status` int(4) DEFAULT 0 COMMENT '帐号状态（0正常 1停用）',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';

-- ----------------------------
-- 2. 角色管理表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL  COMMENT '角色ID',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` int(4) DEFAULT 1 COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` int(4) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色信息表';

-- ----------------------------
-- 3. 用户和角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户和角色关联表';

-- ----------------------------
-- 4. 菜单管理表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL  COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `is_frame` tinyint(1) DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` tinyint(1) DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` varchar(10) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` int(4) DEFAULT 0 COMMENT '菜单显示状态（0显示 1隐藏）',
  `status` int(4) DEFAULT 0 COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-- ----------------------------
-- 5. 角色和菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和菜单关联表';

-- ----------------------------
-- 6. 字典管理表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` bigint(20) NOT NULL  COMMENT '字典主键',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` int(4) DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` bigint(20) NOT NULL  COMMENT '字典编码',
  `dict_sort` int(4) DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认（0否 1是）',
  `status` int(4) DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

-- ----------------------------
-- 7. 参数管理表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint(20) NOT NULL  COMMENT '参数主键',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` int(4) DEFAULT 0 COMMENT '系统内置（0否 1是）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数配置表';

-- ----------------------------
-- 8. 通知公告表
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint(20) NOT NULL  COMMENT '公告ID',
  `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
  `notice_type` int(4) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` int(4) DEFAULT 0 COMMENT '公告状态（0正常 1关闭）',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';

-- ----------------------------
-- 9. 系统令牌表
-- ----------------------------
DROP TABLE IF EXISTS `sys_token`;
CREATE TABLE `sys_token` (
  `id` bigint(20) NOT NULL COMMENT '主键ID（雪花算法）',
  `token_name` varchar(100) NOT NULL COMMENT '令牌名称',
  `token_value` varchar(500) NOT NULL COMMENT '令牌值',
  `token_type` varchar(20) NOT NULL DEFAULT 'api' COMMENT '令牌类型（api-接口令牌 personal-个人令牌 internal-内部令牌）',
  `bound_user_id` bigint(20) DEFAULT NULL COMMENT '绑定用户ID',
  `bound_user_name` varchar(50) DEFAULT NULL COMMENT '绑定用户名',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态（active-有效 expired-已过期 revoked-已吊销）',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_token_value` (`token_value`(100)),
  INDEX `idx_bound_user` (`bound_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统令牌表';

-- ----------------------------
-- 10. 字典数据 - 视频分类
-- ----------------------------
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(1001, '视频分类', 'video_category', 0, 'admin', NOW(), 'admin', NOW(), '视频内容分类'),
(1002, '视频标签', 'video_tag', 0, 'admin', NOW(), 'admin', NOW(), '视频内容标签'),
(1003, '视频违规原因', 'video_violation_reason', 0, 'admin', NOW(), 'admin', NOW(), '视频审核违规原因');

INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10001, 1, '前端开发', '1', 'video_category', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10002, 2, '后端开发', '2', 'video_category', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10003, 3, '计算机基础', '3', 'video_category', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10004, 4, '人工智能', '4', 'video_category', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10005, 5, '职场技能', '5', 'video_category', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10101, 1, 'Java', 'java', 'video_tag', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10102, 2, 'Python', 'python', 'video_tag', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10103, 3, '前端', 'frontend', 'video_tag', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10104, 4, '后端', 'backend', 'video_tag', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10105, 5, '数据库', 'database', 'video_tag', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10106, 6, '微服务', 'microservice', 'video_tag', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10107, 7, 'Docker', 'docker', 'video_tag', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10108, 8, 'Kubernetes', 'k8s', 'video_tag', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10109, 9, 'AI', 'ai', 'video_tag', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10110, 10, '大数据', 'bigdata', 'video_tag', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10201, 1, '涉黄内容', '1', 'video_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10202, 2, '涉政内容', '2', 'video_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10203, 3, '涉暴内容', '3', 'video_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10204, 4, '侵权内容', '4', 'video_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10205, 5, '虚假信息', '5', 'video_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10206, 6, '低俗内容', '6', 'video_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10207, 7, '广告推广', '7', 'video_violation_reason', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10208, 8, '其他违规', '8', 'video_violation_reason', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
