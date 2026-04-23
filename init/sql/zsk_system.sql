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
  `avatar_id` bigint(20) DEFAULT NULL COMMENT '头像图片ID',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
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
  `status` int(4) NOT NULL DEFAULT 0 COMMENT '角色状态（0正常 1停用）',
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
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-
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
  `id` bigint(20) NOT NULL COMMENT '字典主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` int(4) DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
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
  `id` bigint(20) NOT NULL COMMENT '字典编码',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `dict_sort` int(4) DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认（0否 1是）',
  `status` int(4) DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
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
  `id` bigint(20) NOT NULL COMMENT '参数主键',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` int(4) DEFAULT 0 COMMENT '系统内置（0否 1是）',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
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
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';


-- ----------------------------
-- 9. 任务管理表（Gantt）
-- ----------------------------
DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task` (
  `id` bigint(20) NOT NULL COMMENT '任务ID',
  `tenant_id` bigint(20) DEFAULT 0 COMMENT '租户ID',
  `text` varchar(200) NOT NULL COMMENT '任务名称',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `duration` int(11) DEFAULT 0 COMMENT '持续天数',
  `progress` int(11) DEFAULT 0 COMMENT '进度 0-100',
  `type` varchar(20) DEFAULT 'task' COMMENT '任务类型：task-普通任务 project-项目 milestone-里程碑',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父任务ID，顶级为 0',
  `open_flag` tinyint(1) DEFAULT 1 COMMENT '是否展开',
  `details` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务表';

-- ----------------------------
-- 10. 任务依赖关系表
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_link`;
CREATE TABLE `sys_task_link` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `source_id` bigint(20) NOT NULL COMMENT '源任务ID',
  `target_id` bigint(20) NOT NULL COMMENT '目标任务ID',
  `type` varchar(10) NOT NULL DEFAULT '0' COMMENT '依赖类型（dhtmlx原生格式）：0-完成开始 1-开始开始 2-完成完成 3-开始完成',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '是否已删除(0否 1是)',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(100) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务依赖表';


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
- ----------------------------
-- 4.1 菜单数据
-- ----------------------------
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2001, '仪表盘', 0, 1, '/admin/dashboard', 'dashboard/index', '', 1, 0, 'C', 0, 0, 'dashboard:view', 'home', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 15:35:52', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2002, '机器人平台', 0, 2, '/admin/robot', '', '', 1, 0, 'M', 0, 0, 'robot:view', 'bot', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 15:35:52', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2003, '人员管理', 0, 5, '/admin/personnel', '', '', 1, 0, 'M', 0, 0, 'personnel:view', 'users', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 18:38:45', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2004, '视频管理', 0, 4, '/admin/video', '', '', 1, 0, 'M', 0, 0, 'video:view', 'video', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 15:35:52', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2005, '文档管理', 0, 3, '/admin/document', '', '', 1, 0, 'M', 0, 0, 'document:view', 'file-text', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 18:39:05', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2006, '系统管理', 0, 6, '/admin/system', '', '', 1, 0, 'M', 0, 0, 'system:view', 'settings', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 15:35:52', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2007, '系统运维', 0, 7, '/admin/monitor', '', '', 1, 0, 'M', 0, 0, 'monitor:view', 'monitor', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 21:10:39', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2101, '钉钉机器人', 2002, 1, '/admin/robot/dingding', 'robot/dingding', '', 1, 0, 'C', 0, 0, 'robot:dingding:view', 'bot', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2102, '微信机器人', 2002, 2, '/admin/robot/wechat', 'robot/wechat', '', 1, 0, 'C', 0, 0, 'robot:wechat:view', 'message-square', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2103, 'NapCat机器人', 2002, 3, '/admin/robot/napcat', 'robot/napcat', '', 1, 0, 'C', 0, 0, 'robot:napcat:view', 'bot', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2104, 'QQ机器人', 2002, 4, '/admin/robot/qq', 'robot/qq', '', 1, 0, 'C', 0, 0, 'robot:qq:view', 'message-circle', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2201, '菜单管理', 2003, 3, '/admin/personnel/menu', 'personnel/menu', '', 1, 0, 'C', 0, 0, 'personnel:menu:view', 'list', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 15:40:56', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2202, '角色管理', 2003, 1, '/admin/personnel/role', 'personnel/role', '', 1, 0, 'C', 0, 0, 'personnel:role:view', 'shield-check', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 14:26:45', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2203, '用户管理', 2003, 2, '/admin/personnel/user', 'personnel/user', '', 1, 0, 'C', 0, 0, 'personnel:user:view', 'users', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 14:26:20', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2301, '视频列表', 2004, 1, '/admin/video/list', 'video/list', '', 1, 0, 'C', 0, 0, 'video:list:view', 'folder-open', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2302, '视频上传', 2004, 2, '/admin/video/upload', 'video/upload', '', 1, 0, 'C', 0, 0, 'video:upload:view', 'upload', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2303, '视频审核', 2004, 3, '/admin/video/audit', 'video/audit', '', 1, 0, 'C', 0, 0, 'video:audit:view', 'check-circle', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2401, '文档编辑', 2005, 1, '/admin/document/edit', 'document/edit', '', 1, 0, 'C', 0, 0, 'document:edit:view', 'edit', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2402, '文档列表', 2005, 2, '/admin/document/list', 'document/list', '', 1, 0, 'C', 0, 0, 'document:list:view', 'file-text', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2403, '文档上传', 2005, 3, '/admin/document/upload', 'document/upload', '', 1, 0, 'C', 0, 0, 'document:upload:view', 'upload', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2404, '文档审核', 2005, 4, '/admin/document/audit', 'document/audit', '', 1, 0, 'C', 0, 0, 'document:audit:view', 'check-circle', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2501, '参数配置', 2006, 1, '/admin/system/config', 'system/config', '', 1, 0, 'C', 0, 0, 'system:config:view', 'cog', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2502, '字典管理', 2006, 2, '/admin/system/dictionary', 'system/dictionary', '', 1, 0, 'C', 0, 0, 'system:dictionary:view', 'tag', 0, 'admin', '2026-04-20 15:17:27', 'admin', '2026-04-20 15:17:27', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2601, '服务监控', 2007, 1, '/admin/monitor/monitor', 'monitor/monitor', '', 1, 0, 'C', 0, 0, 'monitor:monitor:view', 'activity', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 21:10:11', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2602, '缓存列表', 2007, 2, '/admin/monitor/cache', 'monitor/cache', '', 1, 0, 'C', 0, 0, 'monitor:cache:view', 'database', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 21:10:15', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2603, '系统日志', 2007, 3, '/admin/monitor/syslog', 'monitor/syslog', '', 1, 0, 'C', 0, 0, 'monitor:syslog:view', 'calendar', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-22 04:10:14', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2604, '系统监控', 2007, 4, '/admin/monitor/system', 'monitor/system', '', 1, 0, 'C', 0, 0, 'monitor:system:view', 'monitor', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-21 21:10:21', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2605, '用户行为', 2007, 5, '/admin/monitor/behavior', 'monitor/behavior', '', 1, 0, 'C', 0, 0, 'monitor:behavior:view', 'users', 0, 'admin', '2026-04-20 15:17:27', '771220492@qq.com', '2026-04-22 03:12:01', null);
INSERT INTO zsk_system.sys_menu (id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, deleted, create_name, create_time, update_name, update_time, remark) VALUES (2046493034100137985, '测试2', 0, 8, '', null, null, 1, 0, 'C', 0, 0, null, 'log-out', 0, '771220492@qq.com', '2026-04-21 15:35:36', '771220492@qq.com', '2026-04-21 18:37:53', null);
