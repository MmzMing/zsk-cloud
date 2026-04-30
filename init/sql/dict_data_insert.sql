-- ======================================================================
-- 系统字典数据完整插入脚本
-- 来源：zsk_system.sql + zsk_document.sql
-- 包含所有模块的字典类型和字典数据
-- ======================================================================

-- ======================================================================
-- 第一部分：字典类型 (sys_dict_type)
-- ======================================================================

-- 系统模块字典类型 (ID: 1-27)
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(1, '用户类型', 'sys_user_type', 0, 'admin', NOW(), 'admin', NOW(), '系统用户分类'),
(2, '用户性别', 'sys_user_sex', 0, 'admin', NOW(), 'admin', NOW(), '用户性别枚举'),
(3, '通用状态', 'sys_common_status', 0, 'admin', NOW(), 'admin', NOW(), '通用状态（正常/停用）'),
(4, '数据范围', 'sys_data_scope', 0, 'admin', NOW(), 'admin', NOW(), '角色数据权限范围'),
(5, '是否开关', 'sys_yes_no', 0, 'admin', NOW(), 'admin', NOW(), '通用是否开关'),
(6, '菜单类型', 'sys_menu_type', 0, 'admin', NOW(), 'admin', NOW(), '目录/菜单/按钮'),
(7, '菜单显示状态', 'sys_menu_visible', 0, 'admin', NOW(), 'admin', NOW(), '显示/隐藏'),
(8, '公告类型', 'sys_notice_type', 0, 'admin', NOW(), 'admin', NOW(), '通知/公告'),
(9, '公告状态', 'sys_notice_status', 0, 'admin', NOW(), 'admin', NOW(), '正常/关闭'),
(10, '任务类型', 'sys_task_type', 0, 'admin', NOW(), 'admin', NOW(), 'Gantt任务类型'),
(11, '任务依赖类型', 'sys_task_link_type', 0, 'admin', NOW(), 'admin', NOW(), '任务依赖关系'),
(12, '笔记等级', 'doc_note_grade', 0, 'admin', NOW(), 'admin', NOW(), '笔记内容等级'),
(13, '笔记模式', 'doc_note_mode', 0, 'admin', NOW(), 'admin', NOW(), '笔记可见范围'),
(14, '审核状态', 'doc_audit_status', 0, 'admin', NOW(), 'admin', NOW(), '通用审核状态'),
(15, '笔记状态', 'doc_note_status', 0, 'admin', NOW(), 'admin', NOW(), '笔记生命周期状态'),
(16, '评论状态', 'doc_comment_status', 0, 'admin', NOW(), 'admin', NOW(), '评论可见状态'),
(17, '文件类型', 'doc_file_type', 0, 'admin', NOW(), 'admin', NOW(), '文件分类'),
(18, '上传状态', 'doc_upload_status', 0, 'admin', NOW(), 'admin', NOW(), '文件上传进度'),
(19, '处理状态', 'doc_process_status', 0, 'admin', NOW(), 'admin', NOW(), '文件处理结果'),
(20, '视频状态', 'doc_video_status', 0, 'admin', NOW(), 'admin', NOW(), '视频生命周期状态'),
(21, '合集状态', 'doc_collection_status', 0, 'admin', NOW(), 'admin', NOW(), '视频合集可见性'),
(22, '交互类型', 'doc_interaction_type', 0, 'admin', NOW(), 'admin', NOW(), '用户交互行为'),
(23, '交互目标类型', 'doc_interaction_target', 0, 'admin', NOW(), 'admin', NOW(), '交互对象类型'),
(24, '交互状态', 'doc_interaction_status', 0, 'admin', NOW(), 'admin', NOW(), '交互有效性'),
(25, '审核目标类型', 'doc_audit_target_type', 0, 'admin', NOW(), 'admin', NOW(), '审核对象分类'),
(26, '审核类型', 'doc_audit_type', 0, 'admin', NOW(), 'admin', NOW(), 'AI/人工审核'),
(27, '风险等级', 'doc_risk_level', 0, 'admin', NOW(), 'admin', NOW(), '内容风险级别');

-- ======================================================================
-- 第二部分：字典数据 (sys_dict_data)
-- ======================================================================

-- 1. sys_user_type - 用户类型 (ID范围: 20001-20010)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20001, 1, '系统用户', '0', 'sys_user_type', NULL, 'default', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 2. sys_user_sex - 用户性别 (ID范围: 20011-20020)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20011, 1, '男', '0', 'sys_user_sex', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20012, 2, '女', '1', 'sys_user_sex', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20013, 3, '未知', '2', 'sys_user_sex', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 3. sys_common_status - 通用状态 (ID范围: 20021-20030)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20021, 1, '正常', '0', 'sys_common_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20022, 2, '停用', '1', 'sys_common_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 4. sys_data_scope - 数据范围 (ID范围: 20031-20040)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20031, 1, '全部数据权限', '1', 'sys_data_scope', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20032, 2, '自定义数据权限', '2', 'sys_data_scope', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20033, 3, '本部门数据权限', '3', 'sys_data_scope', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20034, 4, '本部门及以下数据权限', '4', 'sys_data_scope', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 5. sys_yes_no - 是否开关 (ID范围: 20041-20050)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20041, 1, '否', '0', 'sys_yes_no', NULL, 'info', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20042, 2, '是', '1', 'sys_yes_no', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 6. sys_menu_type - 菜单类型 (ID范围: 20051-20060)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20051, 1, '目录', 'M', 'sys_menu_type', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20052, 2, '菜单', 'C', 'sys_menu_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20053, 3, '按钮', 'F', 'sys_menu_type', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 7. sys_menu_visible - 菜单显示状态 (ID范围: 20061-20070)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20061, 1, '显示', '0', 'sys_menu_visible', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20062, 2, '隐藏', '1', 'sys_menu_visible', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 8. sys_notice_type - 公告类型 (ID范围: 20071-20080)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20071, 1, '通知', '1', 'sys_notice_type', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20072, 2, '公告', '2', 'sys_notice_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 9. sys_notice_status - 公告状态 (ID范围: 20081-20090)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20081, 1, '正常', '0', 'sys_notice_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20082, 2, '关闭', '1', 'sys_notice_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 10. sys_task_type - 任务类型 (ID范围: 20091-20100)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20091, 1, '普通任务', 'task', 'sys_task_type', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20092, 2, '项目', 'project', 'sys_task_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20093, 3, '里程碑', 'milestone', 'sys_task_type', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 11. sys_task_link_type - 任务依赖类型 (ID范围: 20101-20110)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20101, 1, '完成-开始', '0', 'sys_task_link_type', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), 'FS: 前任务完成后任务开始'),
(20102, 2, '开始-开始', '1', 'sys_task_link_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), 'SS: 前任务开始后任务开始'),
(20103, 3, '完成-完成', '2', 'sys_task_link_type', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), 'FF: 前任务完成后任务完成'),
(20104, 4, '开始-完成', '3', 'sys_task_link_type', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), 'SF: 前任务开始后任务完成');

-- 12. doc_note_grade - 笔记等级 (ID范围: 20111-20120)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20111, 1, '入门', '1', 'doc_note_grade', NULL, 'info', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20112, 2, '进阶', '2', 'doc_note_grade', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20113, 3, '高级', '3', 'doc_note_grade', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20114, 4, '专家', '4', 'doc_note_grade', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 13. doc_note_mode - 笔记模式 (ID范围: 20121-20130)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20121, 1, '公开', '1', 'doc_note_mode', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20122, 2, '仅自己可见', '2', 'doc_note_mode', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20123, 3, '指定租户可见', '3', 'doc_note_mode', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20124, 4, '付费可见', '4', 'doc_note_mode', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 14. doc_audit_status - 审核状态 (ID范围: 20131-20140)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20131, 1, '待审核', '0', 'doc_audit_status', NULL, 'warning', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20132, 2, '审核通过', '1', 'doc_audit_status', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20133, 3, '审核驳回', '2', 'doc_audit_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20134, 4, '已撤回', '3', 'doc_audit_status', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 15. doc_note_status - 笔记状态 (ID范围: 20141-20150)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20141, 1, '正常', '1', 'doc_note_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20142, 2, '下架', '2', 'doc_note_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20143, 3, '草稿', '3', 'doc_note_status', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20144, 4, '过期', '4', 'doc_note_status', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 16. doc_comment_status - 评论状态 (ID范围: 20151-20160)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20151, 1, '正常', '1', 'doc_comment_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20152, 2, '隐藏', '2', 'doc_comment_status', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20153, 3, '删除', '3', 'doc_comment_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 17. doc_file_type - 文件类型 (ID范围: 20161-20170)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20161, 1, '图片', 'image', 'doc_file_type', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20162, 2, '文档', 'document', 'doc_file_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20163, 3, '视频', 'video', 'doc_file_type', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 18. doc_upload_status - 上传状态 (ID范围: 20171-20180)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20171, 1, '未上传', '0', 'doc_upload_status', NULL, 'info', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20172, 2, '上传中', '1', 'doc_upload_status', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20173, 3, '已上传', '2', 'doc_upload_status', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 19. doc_process_status - 处理状态 (ID范围: 20181-20190)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20181, 1, '未处理', '1', 'doc_process_status', NULL, 'info', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20182, 2, '处理成功', '2', 'doc_process_status', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20183, 3, '处理失败', '3', 'doc_process_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 20. doc_video_status - 视频状态 (ID范围: 20191-20200)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20191, 1, '正常', '1', 'doc_video_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20192, 2, '下架', '2', 'doc_video_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20193, 3, '草稿', '3', 'doc_video_status', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 21. doc_collection_status - 合集状态 (ID范围: 20201-20210)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20201, 1, '公开', '1', 'doc_collection_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20202, 2, '私密', '2', 'doc_collection_status', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 22. doc_interaction_type - 交互类型 (ID范围: 20211-20220)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20211, 1, '点赞', '1', 'doc_interaction_type', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20212, 2, '收藏', '2', 'doc_interaction_type', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20213, 3, '关注', '3', 'doc_interaction_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20214, 4, '浏览', '4', 'doc_interaction_type', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 23. doc_interaction_target - 交互目标类型 (ID范围: 20221-20230)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20221, 1, '文档', '1', 'doc_interaction_target', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20222, 2, '视频', '2', 'doc_interaction_target', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20223, 3, '用户', '3', 'doc_interaction_target', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20224, 4, '评论', '4', 'doc_interaction_target', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 24. doc_interaction_status - 交互状态 (ID范围: 20231-20240)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20231, 1, '取消', '0', 'doc_interaction_status', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20232, 2, '有效', '1', 'doc_interaction_status', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 25. doc_audit_target_type - 审核目标类型 (ID范围: 20241-20250)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20241, 1, '文档', '1', 'doc_audit_target_type', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20242, 2, '视频', '2', 'doc_audit_target_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20243, 3, '文档评论', '3', 'doc_audit_target_type', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20244, 4, '视频评论', '4', 'doc_audit_target_type', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 26. doc_audit_type - 审核类型 (ID范围: 20251-20260)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20251, 1, 'AI审核', 'ai', 'doc_audit_type', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20252, 2, '人工审核', 'manual', 'doc_audit_type', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 27. doc_risk_level - 风险等级 (ID范围: 20261-20270)
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(20261, 1, '低', 'low', 'doc_risk_level', NULL, 'success', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20262, 2, '中', 'medium', 'doc_risk_level', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(20263, 3, '高', 'high', 'doc_risk_level', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- ======================================================================
-- 第三部分：视频/文档分类标签字典 (原有数据，ID: 1001-1006)
-- ======================================================================

-- 视频/文档字典类型
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(1001, '视频分类', 'video_category', 0, 'admin', NOW(), 'admin', NOW(), '视频内容分类'),
(1002, '视频标签', 'video_tag', 0, 'admin', NOW(), 'admin', NOW(), '视频内容标签'),
(1003, '视频违规原因', 'video_violation_reason', 0, 'admin', NOW(), 'admin', NOW(), '视频审核违规原因'),
(1004, '文档分类', 'document_category', 0, 'admin', NOW(), 'admin', NOW(), '文档内容分类'),
(1005, '文档标签', 'document_tag', 0, 'admin', NOW(), 'admin', NOW(), '文档内容标签'),
(1006, '文档违规原因', 'document_violation_reason', 0, 'admin', NOW(), 'admin', NOW(), '文档审核违规原因');

-- 1001. video_category - 视频分类
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10001, 1, '前端开发', '1', 'video_category', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10002, 2, '后端开发', '2', 'video_category', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10003, 3, '计算机基础', '3', 'video_category', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10004, 4, '人工智能', '4', 'video_category', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10005, 5, '职场技能', '5', 'video_category', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 1002. video_tag - 视频标签
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

-- 1003. video_violation_reason - 视频违规原因
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10201, 1, '涉黄内容', '1', 'video_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10202, 2, '涉政内容', '2', 'video_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10203, 3, '涉暴内容', '3', 'video_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10204, 4, '侵权内容', '4', 'video_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10205, 5, '虚假信息', '5', 'video_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10206, 6, '低俗内容', '6', 'video_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10207, 7, '广告推广', '7', 'video_violation_reason', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10208, 8, '其他违规', '8', 'video_violation_reason', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 1004. document_category - 文档分类
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10301, 1, '前端开发', '1', 'document_category', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10302, 2, '后端开发', '2', 'document_category', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10303, 3, '计算机基础', '3', 'document_category', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10304, 4, '人工智能', '4', 'document_category', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10305, 5, '职场技能', '5', 'document_category', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 1005. document_tag - 文档标签
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10401, 1, 'Java', 'java', 'document_tag', NULL, 'primary', 1, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10402, 2, 'Python', 'python', 'document_tag', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10403, 3, '前端', 'frontend', 'document_tag', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10404, 4, '后端', 'backend', 'document_tag', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10405, 5, '数据库', 'database', 'document_tag', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10406, 6, '微服务', 'microservice', 'document_tag', NULL, 'primary', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10407, 7, 'Docker', 'docker', 'document_tag', NULL, 'success', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10408, 8, 'Kubernetes', 'k8s', 'document_tag', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10409, 9, 'AI', 'ai', 'document_tag', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10410, 10, '大数据', 'bigdata', 'document_tag', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 1006. document_violation_reason - 文档违规原因
INSERT INTO `sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_name`, `create_time`, `update_name`, `update_time`, `remark`) VALUES
(10501, 1, '涉黄内容', '1', 'document_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10502, 2, '涉政内容', '2', 'document_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10503, 3, '涉暴内容', '3', 'document_violation_reason', NULL, 'danger', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10504, 4, '侵权内容', '4', 'document_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10505, 5, '虚假信息', '5', 'document_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10506, 6, '低俗内容', '6', 'document_violation_reason', NULL, 'warning', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10507, 7, '广告推广', '7', 'document_violation_reason', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL),
(10508, 8, '其他违规', '8', 'document_violation_reason', NULL, 'info', 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
