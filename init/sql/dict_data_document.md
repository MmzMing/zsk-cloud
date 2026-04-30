# 系统字典字典清单

## 概述

本文档汇总了 `zsk_system.sql` 和 `zsk_document.sql` 中所有需要字典管理的状态码，包含系统模块和文档模块的全部枚举字段。

---

## 一、字典类型总览（sys_dict_type）

| ID | 字典名称 | 字典类型 | 状态 | 备注 |
|---|---------|---------|------|------|
| 1 | 用户类型 | sys_user_type | 正常 | 系统用户分类 |
| 2 | 用户性别 | sys_user_sex | 正常 | 用户性别枚举 |
| 3 | 通用状态 | sys_common_status | 正常 | 通用状态（正常/停用） |
| 4 | 数据范围 | sys_data_scope | 正常 | 角色数据权限范围 |
| 5 | 是否开关 | sys_yes_no | 正常 | 通用是否开关 |
| 6 | 菜单类型 | sys_menu_type | 正常 | 目录/菜单/按钮 |
| 7 | 菜单显示状态 | sys_menu_visible | 正常 | 显示/隐藏 |
| 8 | 公告类型 | sys_notice_type | 正常 | 通知/公告 |
| 9 | 公告状态 | sys_notice_status | 正常 | 正常/关闭 |
| 10 | 任务类型 | sys_task_type | 正常 | Gantt任务类型 |
| 11 | 任务依赖类型 | sys_task_link_type | 正常 | 任务依赖关系 |
| 12 | 笔记等级 | doc_note_grade | 正常 | 笔记内容等级 |
| 13 | 笔记模式 | doc_note_mode | 正常 | 笔记可见范围 |
| 14 | 审核状态 | doc_audit_status | 正常 | 通用审核状态 |
| 15 | 笔记状态 | doc_note_status | 正常 | 笔记生命周期状态 |
| 16 | 评论状态 | doc_comment_status | 正常 | 评论可见状态 |
| 17 | 文件类型 | doc_file_type | 正常 | 文件分类 |
| 18 | 上传状态 | doc_upload_status | 正常 | 文件上传进度 |
| 19 | 处理状态 | doc_process_status | 正常 | 文件处理结果 |
| 20 | 视频状态 | doc_video_status | 正常 | 视频生命周期状态 |
| 21 | 合集状态 | doc_collection_status | 正常 | 视频合集可见性 |
| 22 | 交互类型 | doc_interaction_type | 正常 | 用户交互行为 |
| 23 | 交互目标类型 | doc_interaction_target | 正常 | 交互对象类型 |
| 24 | 交互状态 | doc_interaction_status | 正常 | 交互有效性 |
| 25 | 审核目标类型 | doc_audit_target_type | 正常 | 审核对象分类 |
| 26 | 审核类型 | doc_audit_type | 正常 | AI/人工审核 |
| 27 | 风险等级 | doc_risk_level | 正常 | 内容风险级别 |
| 1001 | 视频分类 | video_category | 正常 | 视频内容分类 |
| 1002 | 视频标签 | video_tag | 正常 | 视频内容标签 |
| 1003 | 视频违规原因 | video_violation_reason | 正常 | 视频审核违规原因 |
| 1004 | 文档分类 | document_category | 正常 | 文档内容分类 |
| 1005 | 文档标签 | document_tag | 正常 | 文档内容标签 |
| 1006 | 文档违规原因 | document_violation_reason | 正常 | 文档审核违规原因 |

---

## 二、字典数据明细（sys_dict_data）

### 1. sys_user_type（用户类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 系统用户 | 0 | default | 是 | 系统内置用户 |

### 2. sys_user_sex（用户性别）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 男 | 0 | primary | 是 | 男性 |
| 2 | 女 | 1 | danger | 否 | 女性 |
| 3 | 未知 | 2 | info | 否 | 未设置 |

### 3. sys_common_status（通用状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 正常 | 0 | success | 是 | 启用/正常状态 |
| 2 | 停用 | 1 | danger | 否 | 禁用/停用状态 |

### 4. sys_data_scope（数据范围）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 全部数据权限 | 1 | primary | 是 | 可查看所有数据 |
| 2 | 自定义数据权限 | 2 | warning | 否 | 按自定义范围 |
| 3 | 本部门数据权限 | 3 | info | 否 | 仅本部门 |
| 4 | 本部门及以下 | 4 | success | 否 | 本部门及下级 |

### 5. sys_yes_no（是否开关）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 否 | 0 | info | 是 | 否/关闭/非 |
| 2 | 是 | 1 | primary | 否 | 是/开启 |

### 6. sys_menu_type（菜单类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 目录 | M | primary | 否 | 一级导航目录 |
| 2 | 菜单 | C | success | 否 | 功能页面 |
| 3 | 按钮 | F | warning | 否 | 操作按钮 |

### 7. sys_menu_visible（菜单显示状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 显示 | 0 | success | 是 | 菜单可见 |
| 2 | 隐藏 | 1 | danger | 否 | 菜单不可见 |

### 8. sys_notice_type（公告类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 通知 | 1 | primary | 是 | 系统通知 |
| 2 | 公告 | 2 | success | 否 | 系统公告 |

### 9. sys_notice_status（公告状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 正常 | 0 | success | 是 | 公告有效 |
| 2 | 关闭 | 1 | danger | 否 | 公告已关闭 |

### 10. sys_task_type（任务类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 普通任务 | task | primary | 是 | 一般任务 |
| 2 | 项目 | project | success | 否 | 项目节点 |
| 3 | 里程碑 | milestone | warning | 否 | 里程碑 |

### 11. sys_task_link_type（任务依赖类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 完成-开始 | 0 | primary | 是 | FS: 前任务完成后任务开始 |
| 2 | 开始-开始 | 1 | success | 否 | SS: 前任务开始后任务开始 |
| 3 | 完成-完成 | 2 | warning | 否 | FF: 前任务完成后任务完成 |
| 4 | 开始-完成 | 3 | danger | 否 | SF: 前任务开始后任务完成 |

### 12. doc_note_grade（笔记等级）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 入门 | 1 | info | 是 | 初学者级别 |
| 2 | 进阶 | 2 | primary | 否 | 中级水平 |
| 3 | 高级 | 3 | warning | 否 | 高级水平 |
| 4 | 专家 | 4 | danger | 否 | 专家级别 |

### 13. doc_note_mode（笔记模式）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 公开 | 1 | success | 是 | 所有人可见 |
| 2 | 仅自己可见 | 2 | info | 否 | 私有笔记 |
| 3 | 指定租户可见 | 3 | warning | 否 | 租户范围 |
| 4 | 付费可见 | 4 | danger | 否 | 付费内容 |

### 14. doc_audit_status（审核状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 待审核 | 0 | warning | 是 | 等待审核 |
| 2 | 审核通过 | 1 | success | 否 | 审核通过 |
| 3 | 审核驳回 | 2 | danger | 否 | 审核不通过 |
| 4 | 已撤回 | 3 | info | 否 | 用户主动撤回 |

### 15. doc_note_status（笔记状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 正常 | 1 | success | 是 | 正常展示 |
| 2 | 下架 | 2 | danger | 否 | 已下架 |
| 3 | 草稿 | 3 | info | 否 | 草稿状态 |
| 4 | 过期 | 4 | warning | 否 | 已过期 |

### 16. doc_comment_status（评论状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 正常 | 1 | success | 是 | 正常显示 |
| 2 | 隐藏 | 2 | warning | 否 | 已隐藏 |
| 3 | 删除 | 3 | danger | 否 | 已删除 |

### 17. doc_file_type（文件类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 图片 | image | primary | 否 | 图片文件 |
| 2 | 文档 | document | success | 否 | 文档文件 |
| 3 | 视频 | video | warning | 否 | 视频文件 |

### 18. doc_upload_status（上传状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 未上传 | 0 | info | 是 | 尚未上传 |
| 2 | 上传中 | 1 | warning | 否 | 正在上传 |
| 3 | 已上传 | 2 | success | 否 | 上传完成 |

### 19. doc_process_status（处理状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 未处理 | 1 | info | 是 | 等待处理 |
| 2 | 处理成功 | 2 | success | 否 | 处理完成 |
| 3 | 处理失败 | 3 | danger | 否 | 处理失败 |

### 20. doc_video_status（视频状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 正常 | 1 | success | 是 | 正常播放 |
| 2 | 下架 | 2 | danger | 否 | 已下架 |
| 3 | 草稿 | 3 | info | 否 | 草稿状态 |

### 21. doc_collection_status（合集状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 公开 | 1 | success | 是 | 公开可见 |
| 2 | 私密 | 2 | danger | 否 | 仅创建者可见 |

### 22. doc_interaction_type（交互类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 点赞 | 1 | primary | 否 | like |
| 2 | 收藏 | 2 | warning | 否 | favorite |
| 3 | 关注 | 3 | success | 否 | follow |
| 4 | 浏览 | 4 | info | 否 | view |

### 23. doc_interaction_target（交互目标类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 文档 | 1 | primary | 否 | 文档内容 |
| 2 | 视频 | 2 | success | 否 | 视频内容 |
| 3 | 用户 | 3 | warning | 否 | 其他用户 |
| 4 | 评论 | 4 | info | 否 | 评论内容 |

### 24. doc_interaction_status（交互状态）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 取消 | 0 | info | 否 | 已取消 |
| 2 | 有效 | 1 | success | 是 | 有效状态 |

### 25. doc_audit_target_type（审核目标类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 文档 | 1 | primary | 否 | 文档审核 |
| 2 | 视频 | 2 | success | 否 | 视频审核 |
| 3 | 文档评论 | 3 | warning | 否 | 文档评论审核 |
| 4 | 视频评论 | 4 | danger | 否 | 视频评论审核 |

### 26. doc_audit_type（审核类型）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | AI审核 | ai | primary | 是 | AI自动审核 |
| 2 | 人工审核 | manual | success | 否 | 人工审核 |

### 27. doc_risk_level（风险等级）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 低 | low | success | 是 | 低风险 |
| 2 | 中 | medium | warning | 否 | 中风险 |
| 3 | 高 | high | danger | 否 | 高风险 |

### 1001. video_category（视频分类）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 前端开发 | 1 | primary | 是 | - |
| 2 | 后端开发 | 2 | success | 否 | - |
| 3 | 计算机基础 | 3 | info | 否 | - |
| 4 | 人工智能 | 4 | warning | 否 | - |
| 5 | 职场技能 | 5 | danger | 否 | - |

### 1002. video_tag（视频标签）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | Java | java | primary | 是 | - |
| 2 | Python | python | success | 否 | - |
| 3 | 前端 | frontend | info | 否 | - |
| 4 | 后端 | backend | warning | 否 | - |
| 5 | 数据库 | database | danger | 否 | - |
| 6 | 微服务 | microservice | primary | 否 | - |
| 7 | Docker | docker | success | 否 | - |
| 8 | Kubernetes | k8s | info | 否 | - |
| 9 | AI | ai | warning | 否 | - |
| 10 | 大数据 | bigdata | danger | 否 | - |

### 1003. video_violation_reason（视频违规原因）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 涉黄内容 | 1 | danger | 否 | - |
| 2 | 涉政内容 | 2 | danger | 否 | - |
| 3 | 涉暴内容 | 3 | danger | 否 | - |
| 4 | 侵权内容 | 4 | warning | 否 | - |
| 5 | 虚假信息 | 5 | warning | 否 | - |
| 6 | 低俗内容 | 6 | warning | 否 | - |
| 7 | 广告推广 | 7 | info | 否 | - |
| 8 | 其他违规 | 8 | info | 否 | - |

### 1004. document_category（文档分类）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 前端开发 | 1 | primary | 是 | - |
| 2 | 后端开发 | 2 | success | 否 | - |
| 3 | 计算机基础 | 3 | info | 否 | - |
| 4 | 人工智能 | 4 | warning | 否 | - |
| 5 | 职场技能 | 5 | danger | 否 | - |

### 1005. document_tag（文档标签）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | Java | java | primary | 是 | - |
| 2 | Python | python | success | 否 | - |
| 3 | 前端 | frontend | info | 否 | - |
| 4 | 后端 | backend | warning | 否 | - |
| 5 | 数据库 | database | danger | 否 | - |
| 6 | 微服务 | microservice | primary | 否 | - |
| 7 | Docker | docker | success | 否 | - |
| 8 | Kubernetes | k8s | info | 否 | - |
| 9 | AI | ai | warning | 否 | - |
| 10 | 大数据 | bigdata | danger | 否 | - |

### 1006. document_violation_reason（文档违规原因）

| 排序 | 标签 | 值 | 样式 | 默认 | 备注 |
|------|------|---|------|------|------|
| 1 | 涉黄内容 | 1 | danger | 否 | - |
| 2 | 涉政内容 | 2 | danger | 否 | - |
| 3 | 涉暴内容 | 3 | danger | 否 | - |
| 4 | 侵权内容 | 4 | warning | 否 | - |
| 5 | 虚假信息 | 5 | warning | 否 | - |
| 6 | 低俗内容 | 6 | warning | 否 | - |
| 7 | 广告推广 | 7 | info | 否 | - |
| 8 | 其他违规 | 8 | info | 否 | - |

---

## 三、字段来源对照表

| 字典类型 | 来源表.字段 | 说明 |
|---------|-----------|------|
| sys_user_type | sys_user.user_type | 用户类型 |
| sys_user_sex | sys_user.sex | 用户性别 |
| sys_common_status | sys_user.status, sys_role.status, sys_menu.status, sys_dict_type.status, sys_dict_data.status | 通用状态 |
| sys_data_scope | sys_role.data_scope | 数据范围 |
| sys_yes_no | sys_menu.is_frame, sys_menu.is_cache, sys_dict_data.is_default, sys_config.config_type, sys_task.open_flag | 通用是否 |
| sys_menu_type | sys_menu.menu_type | 菜单类型 |
| sys_menu_visible | sys_menu.visible | 菜单显示 |
| sys_notice_type | sys_notice.notice_type | 公告类型 |
| sys_notice_status | sys_notice.status | 公告状态 |
| sys_task_type | sys_task.type | 任务类型 |
| sys_task_link_type | sys_task_link.type | 任务依赖 |
| doc_note_grade | document_note.note_grade | 笔记等级 |
| doc_note_mode | document_note.note_mode | 笔记模式 |
| doc_audit_status | document_note.audit_status, document_note_comment.audit_status, document_video.audit_status, document_video_comment.audit_status, document_audit.audit_status | 审核状态 |
| doc_note_status | document_note.status | 笔记状态 |
| doc_comment_status | document_note_comment.status, document_video_comment.status | 评论状态 |
| doc_file_type | document_files.file_type | 文件类型 |
| doc_upload_status | document_files.status | 上传状态 |
| doc_process_status | document_process.status, document_process_history.status | 处理状态 |
| doc_video_status | document_video.status | 视频状态 |
| doc_collection_status | document_video_collection.status | 合集状态 |
| doc_interaction_type | document_user_interaction.interaction_type | 交互类型 |
| doc_interaction_target | document_user_interaction.target_type | 交互目标 |
| doc_interaction_status | document_user_interaction.status | 交互状态 |
| doc_audit_target_type | document_audit.target_type | 审核目标 |
| doc_audit_type | document_audit.audit_type | 审核类型 |
| doc_risk_level | document_audit.risk_level | 风险等级 |
