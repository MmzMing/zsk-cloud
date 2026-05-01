# 字典值转标签接口变更文档

## 一、变更概述

### 1.1 变更目的
将原有返回字典原始值（如 `status: 1`）的接口，改为后端通过 Feign 调用字典服务转换后返回中文标签（如 `statusLabel: "正常"`），前端无需再进行字典转换。

### 1.2 变更范围
- **后端变更**: 新增字典标签查询接口，业务模块通过 Feign 调用字典服务进行转换
- **前端变更**: 所有使用字典值的页面需要改为直接使用后端返回的标签字段

### 1.3 变更时间
2026-05-01

---

## 二、后端变更

### 2.1 新增接口

#### 2.1.1 字典标签查询接口（内部调用）

**接口路径**: `GET /dict/data/label/{dictType}/{dictValue}`

**说明**: 根据字典类型和字典值查询对应的中文标签

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dictType | String | 是 | 字典类型（如 sys_user_sex） |
| dictValue | String | 是 | 字典值（如 1） |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "男"
}
```

**使用示例**:
```java
// Feign 调用
R<String> result = remoteDictService.getDictLabel("sys_user_sex", "1");
String label = result.getData(); // "男"
```

### 2.2 新增 Feign 接口

**文件**: `zsk-api-system/src/main/java/com/zsk/system/api/RemoteDictService.java`

```java
@FeignClient(contextId = "remoteDictService", value = ServiceNameConstants.SYSTEM_SERVICE, 
             fallbackFactory = RemoteDictFallbackFactory.class, url = "http://127.0.0.1:20010")
public interface RemoteDictService {

    /**
     * 根据字典类型和字典值查询字典标签
     */
    @GetMapping("/dict/data/label/{dictType}/{dictValue}")
    R<String> getDictLabel(@PathVariable("dictType") String dictType, 
                           @PathVariable("dictValue") String dictValue);
}
```

### 2.3 Service 层新增方法

**文件**: `zsk-module-system/src/main/java/com/zsk/system/service/ISysDictDataService.java`

```java
/**
 * 根据字典类型和字典值查询字典标签
 */
String selectDictLabel(String dictType, String dictValue);
```

---

## 三、需要转换的字典字段清单

### 3.1 System 模块

#### 3.1.1 用户管理 (SysUser)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| user_type | sys_user_type | 0 | 系统用户 | user_type_label |
| sex | sys_user_sex | 0/1/2 | 男/女/未知 | sex_label |
| status | sys_common_status | 0/1 | 正常/停用 | status_label |

**需要修改的接口**:
- `GET /user/list` - 用户列表
- `GET /user/page` - 用户分页列表
- `GET /user/{id}` - 用户详情
- `GET /user/profile` - 用户个人信息

#### 3.1.2 角色管理 (SysRole)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| status | sys_common_status | 0/1 | 正常/停用 | status_label |
| data_scope | sys_data_scope | 1/2/3/4 | 全部数据权限/自定义数据权限/... | data_scope_label |

**需要修改的接口**:
- `GET /role/list` - 角色列表
- `GET /role/page` - 角色分页列表
- `GET /role/{id}` - 角色详情

#### 3.1.3 菜单管理 (SysMenu)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| menu_type | sys_menu_type | M/C/F | 目录/菜单/按钮 | menu_type_label |
| visible | sys_menu_visible | 0/1 | 显示/隐藏 | visible_label |
| status | sys_common_status | 0/1 | 正常/停用 | status_label |

**需要修改的接口**:
- `GET /menu/tree` - 菜单树
- `GET /menu/list` - 菜单列表
- `GET /menu/{id}` - 菜单详情

#### 3.1.4 公告管理 (SysNotice)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| notice_type | sys_notice_type | 1/2 | 通知/公告 | notice_type_label |
| status | sys_notice_status | 0/1 | 正常/关闭 | status_label |

**需要修改的接口**:
- `GET /notice/list` - 公告列表
- `GET /notice/page` - 公告分页列表
- `GET /notice/{id}` - 公告详情

#### 3.1.5 任务管理 (SysTask)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| type | sys_task_type | task/project/milestone | 普通任务/项目/里程碑 | type_label |
| open_flag | sys_yes_no | 0/1 | 否/是 | open_flag_label |

**需要修改的接口**:
- `GET /task/list` - 任务列表
- `GET /task/page` - 任务分页列表
- `GET /task/{id}` - 任务详情

#### 3.1.6 字典数据 (SysDictData)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| status | sys_common_status | 0/1 | 正常/停用 | status_label |

**需要修改的接口**:
- `GET /dict/data/list` - 字典数据列表
- `GET /dict/data/page` - 字典数据分页列表

---

### 3.2 Document 模块

#### 3.2.1 笔记管理 (DocNote)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| note_grade | doc_note_grade | 1/2/3/4 | 入门/进阶/高级/专家 | note_grade_label |
| note_mode | doc_note_mode | 1/2/3/4 | 公开/仅自己可见/指定租户可见/付费可见 | note_mode_label |
| audit_status | doc_audit_status | 0/1/2/3 | 待审核/审核通过/审核驳回/已撤回 | audit_status_label |
| status | doc_note_status | 1/2/3/4 | 正常/下架/草稿/过期 | status_label |

**需要修改的接口**:
- `GET /note/list` - 笔记列表
- `GET /note/page` - 笔记分页列表
- `GET /note/{id}` - 笔记详情
- `GET /note/home/list` - 首页笔记列表
- `GET /note/dtl/{id}` - 笔记全量详情

#### 3.2.2 视频管理 (DocVideo)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| audit_status | doc_audit_status | 0/1/2/3 | 待审核/审核通过/审核驳回/已撤回 | audit_status_label |
| status | doc_video_status | 1/2/3 | 正常/下架/草稿 | status_label |

**需要修改的接口**:
- `GET /video/list` - 视频列表
- `GET /video/page` - 视频分页列表
- `GET /video/{id}` - 视频详情
- `GET /video/home/list` - 首页视频列表

#### 3.2.3 笔记评论 (DocNoteComment)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| audit_status | doc_audit_status | 0/1/2/3 | 待审核/审核通过/审核驳回/已撤回 | audit_status_label |
| status | doc_comment_status | 1/2/3 | 正常/隐藏/删除 | status_label |

**需要修改的接口**:
- `GET /note/comment/list` - 评论列表
- `GET /note/comment/page` - 评论分页列表

#### 3.2.4 视频评论 (DocVideoComment)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| audit_status | doc_audit_status | 0/1/2/3 | 待审核/审核通过/审核驳回/已撤回 | audit_status_label |
| status | doc_comment_status | 1/2/3 | 正常/隐藏/删除 | status_label |

**需要修改的接口**:
- `GET /video/comment/list` - 评论列表
- `GET /video/comment/page` - 评论分页列表

#### 3.2.5 审核管理 (DocAudit)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| target_type | doc_audit_target_type | 1/2/3/4 | 文档/视频/文档评论/视频评论 | target_type_label |
| audit_type | doc_audit_type | ai/manual | AI审核/人工审核 | audit_type_label |
| audit_status | doc_audit_status | 0/1/2/3 | 待审核/审核通过/审核驳回/已撤回 | audit_status_label |
| risk_level | doc_risk_level | low/medium/high | 低/中/高 | risk_level_label |

**需要修改的接口**:
- `GET /audit/list` - 审核列表
- `GET /audit/page` - 审核分页列表
- `GET /audit/{id}` - 审核详情
- `GET /audit/queue` - 审核队列

#### 3.2.6 文件管理 (DocFiles)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| file_type | doc_file_type | image/document/video | 图片/文档/视频 | file_type_label |
| status | doc_upload_status | 0/1/2 | 未上传/上传中/已上传 | status_label |

**需要修改的接口**:
- `GET /files/list` - 文件列表
- `GET /files/page` - 文件分页列表

#### 3.2.7 处理记录 (DocProcess)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| status | doc_process_status | 1/2/3 | 未处理/处理成功/处理失败 | status_label |

**需要修改的接口**:
- `GET /process/list` - 处理记录列表
- `GET /process/page` - 处理记录分页列表

#### 3.2.8 视频合集 (DocVideoCollection)

| 字段 | 字典类型 | 值示例 | 标签示例 | VO字段 |
|------|----------|--------|----------|--------|
| status | doc_collection_status | 1/2 | 公开/私密 | status_label |

**需要修改的接口**:
- `GET /video/collection/list` - 合集列表
- `GET /video/collection/page` - 合集分页列表

---

## 四、VO 对象变更规范

### 4.1 变更规则

所有涉及字典值的 VO 对象，需在原有字段基础上**增加对应的 `_label` 后缀字段**：

**变更前**:
```java
public class DocNoteListVo {
    private Integer status;           // 1
    private Integer auditStatus;      // 0
    private Integer noteGrade;        // 2
}
```

**变更后**:
```java
public class DocNoteListVo {
    private Integer status;           // 1（保留原始值）
    private String statusLabel;       // "正常"（新增标签）
    
    private Integer auditStatus;      // 0（保留原始值）
    private String auditStatusLabel;  // "待审核"（新增标签）
    
    private Integer noteGrade;        // 2（保留原始值）
    private String noteGradeLabel;    // "进阶"（新增标签）
}
```

### 4.2 Service 层转换逻辑

在 Service 层组装 VO 时，调用 Feign 接口进行转换：

```java
@Service
@RequiredArgsConstructor
public class DocNoteServiceImpl implements IDocNoteService {
    
    private final RemoteDictService remoteDictService;
    
    public DocNoteListVo buildNoteVo(DocNote note) {
        DocNoteListVo vo = new DocNoteListVo();
        // 复制基础字段
        vo.setId(note.getId());
        vo.setNoteName(note.getNoteName());
        // ... 其他字段
        
        // 转换字典值为标签
        if (note.getStatus() != null) {
            vo.setStatus(note.getStatus());
            R<String> labelResult = remoteDictService.getDictLabel("doc_note_status", note.getStatus().toString());
            vo.setStatusLabel(labelResult.getData());
        }
        
        if (note.getAuditStatus() != null) {
            vo.setAuditStatus(note.getAuditStatus());
            R<String> labelResult = remoteDictService.getDictLabel("doc_audit_status", note.getAuditStatus().toString());
            vo.setAuditStatusLabel(labelResult.getData());
        }
        
        if (note.getNoteGrade() != null) {
            vo.setNoteGrade(note.getNoteGrade());
            R<String> labelResult = remoteDictService.getDictLabel("doc_note_grade", note.getNoteGrade().toString());
            vo.setNoteGradeLabel(labelResult.getData());
        }
        
        return vo;
    }
}
```

---

## 五、前端变更清单

### 5.1 需要修改的前端页面

#### 5.1.1 系统管理模块

| 页面 | 路径 | 需要转换的字段 | 修改内容 |
|------|------|----------------|----------|
| 用户管理 | `/system/user` | user_type, sex, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 角色管理 | `/system/role` | status, data_scope | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 菜单管理 | `/system/menu` | menu_type, visible, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 公告管理 | `/system/notice` | notice_type, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 任务管理 | `/system/task` | type, open_flag | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 字典管理 | `/system/dict` | status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |

#### 5.1.2 文档管理模块

| 页面 | 路径 | 需要转换的字段 | 修改内容 |
|------|------|----------------|----------|
| 笔记列表 | `/doc/note/list` | note_grade, note_mode, audit_status, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 笔记详情 | `/doc/note/detail` | note_grade, note_mode, audit_status, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 视频列表 | `/doc/video/list` | audit_status, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 视频详情 | `/doc/video/detail` | audit_status, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 笔记评论 | `/doc/note/comment` | audit_status, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 视频评论 | `/doc/video/comment` | audit_status, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 审核管理 | `/doc/audit` | target_type, audit_type, audit_status, risk_level | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 文件管理 | `/doc/files` | file_type, status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 处理记录 | `/doc/process` | status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |
| 视频合集 | `/doc/video/collection` | status | 移除前端字典转换，直接使用后端返回的 `xxxLabel` |

### 5.2 前端修改示例

#### 修改前 (Vue 示例):

```vue
<template>
  <el-table-column label="状态" prop="status">
    <template #default="{ row }">
      <dict-tag :options="dict.type.doc_note_status" :value="row.status" />
    </template>
  </el-table-column>
</template>

<script setup>
import { getDictData } from '@/api/system/dict/data'

const dict = reactive({
  type: {}
})

onMounted(() => {
  getDictData('doc_note_status').then(res => {
    dict.type.doc_note_status = res.data
  })
})
</script>
```

#### 修改后:

```vue
<template>
  <el-table-column label="状态" prop="statusLabel">
    <template #default="{ row }">
      <el-tag :type="getStatusTagType(row.status)">
        {{ row.statusLabel || '-' }}
      </el-tag>
    </template>
  </el-table-column>
</template>

<script setup>
// 不再需要加载字典数据，直接使用后端返回的 statusLabel
const getStatusTagType = (status) => {
  const map = {
    1: 'success',  // 正常
    2: 'danger',   // 下架
    3: 'info',     // 草稿
    4: 'warning'   // 过期
  }
  return map[status] || 'info'
}
</script>
```

### 5.3 前端字典工具类处理

如果前端有封装字典转换工具类，需要更新：

**修改前**:
```javascript
// utils/dict.js
export function getDictLabel(dictType, value) {
  const dictData = store.getDictData(dictType)
  return dictData.find(item => item.dictValue === value)?.dictLabel || value
}
```

**修改后**:
```javascript
// utils/dict.js
// 此方法不再需要，直接使用后端返回的 xxxLabel 字段
// 保留此方法仅用于兼容旧代码或表单下拉框
export function getDictLabel(dictType, value) {
  console.warn('[Deprecated] getDictLabel is deprecated, use xxxLabel from backend instead')
  const dictData = store.getDictData(dictType)
  return dictData.find(item => item.dictValue === value)?.dictLabel || value
}
```

---

## 六、字典类型对照表

### 6.1 系统模块字典

| 字典类型 | 字典名称 | 值域 | 说明 |
|----------|----------|------|------|
| sys_user_type | 用户类型 | 0: 系统用户 | 系统用户分类 |
| sys_user_sex | 用户性别 | 0: 男, 1: 女, 2: 未知 | 用户性别枚举 |
| sys_common_status | 通用状态 | 0: 正常, 1: 停用 | 通用状态 |
| sys_data_scope | 数据范围 | 1: 全部数据, 2: 自定义, 3: 本部门, 4: 本部门及以下 | 角色数据权限 |
| sys_yes_no | 是否开关 | 0: 否, 1: 是 | 通用是否开关 |
| sys_menu_type | 菜单类型 | M: 目录, C: 菜单, F: 按钮 | 菜单分类 |
| sys_menu_visible | 菜单显示 | 0: 显示, 1: 隐藏 | 菜单可见性 |
| sys_notice_type | 公告类型 | 1: 通知, 2: 公告 | 公告分类 |
| sys_notice_status | 公告状态 | 0: 正常, 1: 关闭 | 公告状态 |
| sys_task_type | 任务类型 | task: 普通任务, project: 项目, milestone: 里程碑 | Gantt任务类型 |
| sys_task_link_type | 任务依赖类型 | 0: FS, 1: SS, 2: FF, 3: SF | 任务依赖关系 |

### 6.2 文档模块字典

| 字典类型 | 字典名称 | 值域 | 说明 |
|----------|----------|------|------|
| doc_note_grade | 笔记等级 | 1: 入门, 2: 进阶, 3: 高级, 4: 专家 | 笔记内容等级 |
| doc_note_mode | 笔记模式 | 1: 公开, 2: 仅自己, 3: 指定租户, 4: 付费可见 | 笔记可见范围 |
| doc_audit_status | 审核状态 | 0: 待审核, 1: 通过, 2: 驳回, 3: 已撤回 | 通用审核状态 |
| doc_note_status | 笔记状态 | 1: 正常, 2: 下架, 3: 草稿, 4: 过期 | 笔记生命周期 |
| doc_comment_status | 评论状态 | 1: 正常, 2: 隐藏, 3: 删除 | 评论可见状态 |
| doc_file_type | 文件类型 | image: 图片, document: 文档, video: 视频 | 文件分类 |
| doc_upload_status | 上传状态 | 0: 未上传, 1: 上传中, 2: 已上传 | 文件上传进度 |
| doc_process_status | 处理状态 | 1: 未处理, 2: 处理成功, 3: 处理失败 | 文件处理结果 |
| doc_video_status | 视频状态 | 1: 正常, 2: 下架, 3: 草稿 | 视频生命周期 |
| doc_collection_status | 合集状态 | 1: 公开, 2: 私密 | 视频合集可见性 |
| doc_interaction_type | 交互类型 | 1: 点赞, 2: 收藏, 3: 关注, 4: 浏览 | 用户交互行为 |
| doc_interaction_target | 交互目标类型 | 1: 文档, 2: 视频, 3: 用户, 4: 评论 | 交互对象类型 |
| doc_interaction_status | 交互状态 | 0: 取消, 1: 有效 | 交互有效性 |
| doc_audit_target_type | 审核目标类型 | 1: 文档, 2: 视频, 3: 文档评论, 4: 视频评论 | 审核对象分类 |
| doc_audit_type | 审核类型 | ai: AI审核, manual: 人工审核 | 审核方式 |
| doc_risk_level | 风险等级 | low: 低, medium: 中, high: 高 | 内容风险级别 |
| video_category | 视频分类 | 1-5 | 视频内容分类 |
| video_tag | 视频标签 | java/python/... | 视频内容标签 |
| video_violation_reason | 视频违规原因 | 1-8 | 视频审核违规原因 |
| document_category | 文档分类 | 1-5 | 文档内容分类 |
| document_tag | 文档标签 | java/python/... | 文档内容标签 |
| document_violation_reason | 文档违规原因 | 1-8 | 文档审核违规原因 |

---

## 七、实施步骤

### 7.1 后端实施

1. **Phase 1**: 完成字典标签查询接口开发（已完成）
   - [x] 新增 `ISysDictDataService.selectDictLabel()` 方法
   - [x] 新增 `SysDictDataController.getDictLabel()` 接口
   - [x] 新增 `RemoteDictService.getDictLabel()` Feign 接口
   - [x] 更新 `RemoteDictFallbackFactory` 降级处理

2. **Phase 2**: 业务模块 Service 层改造（待实施）
   - [ ] System 模块：在 Service 层转换用户、角色、菜单等 VO 的字典值
   - [ ] Document 模块：在 Service 层转换笔记、视频、评论等 VO 的字典值
   - [ ] 批量转换优化：避免循环调用 Feign，使用批量查询

3. **Phase 3**: 接口测试
   - [ ] 验证所有列表接口返回 `xxxLabel` 字段
   - [ ] 验证标签值与字典配置一致
   - [ ] 验证 Feign 降级处理正常

### 7.2 前端实施

1. **Phase 1**: 评估影响范围
   - [ ] 统计所有使用字典转换的页面和组件
   - [ ] 制定前端修改计划

2. **Phase 2**: 逐步替换
   - [ ] 移除页面中的字典数据加载逻辑
   - [ ] 将表格/表单中的 `dict-tag` 改为直接使用 `xxxLabel`
   - [ ] 更新下拉框数据源（仍需加载字典数据用于表单编辑）

3. **Phase 3**: 测试验证
   - [ ] 验证所有列表页标签显示正常
   - [ ] 验证表单编辑页下拉框数据正常
   - [ ] 验证筛选条件中的字典组件正常

---

## 八、注意事项

### 8.1 性能优化

1. **批量查询**: 避免在循环中逐个调用 Feign 接口，应批量查询后在内存中匹配
2. **缓存优化**: 字典数据变化频率低，可在业务服务层增加本地缓存
3. **异步转换**: 对于非核心字段，可异步转换避免阻塞主流程

### 8.2 兼容性

1. **保留原始值**: VO 中需同时保留原始值字段（如 `status`）和标签字段（如 `statusLabel`），确保向下兼容
2. **表单编辑**: 表单编辑场景仍需要加载完整字典数据用于下拉选择，不可移除
3. **筛选条件**: 列表筛选条件中的字典组件仍需加载字典数据

### 8.3 异常处理

1. **Feign 超时**: 字典服务不可用时，降级返回原始值或空字符串
2. **字典未配置**: 未找到对应字典项时，标签字段返回 `null` 或原始值
3. **日志记录**: 转换失败时记录 warn 日志，不影响主流程

---

## 九、测试用例

### 9.1 后端测试

```java
@Test
public void testGetDictLabel() {
    // 查询性别字典
    String label = dictDataService.selectDictLabel("sys_user_sex", "1");
    assertEquals("女", label);
    
    // 查询不存在的值
    String nullLabel = dictDataService.selectDictLabel("sys_user_sex", "999");
    assertNull(nullLabel);
    
    // 查询不存在的字典类型
    String nullLabel2 = dictDataService.selectDictLabel("not_exist", "1");
    assertNull(nullLabel2);
}
```

### 9.2 接口测试

```bash
# 测试字典标签查询接口
curl -X GET http://localhost:20010/dict/data/label/sys_user_sex/1

# 预期响应
{
  "code": 200,
  "msg": "操作成功",
  "data": "女"
}
```

### 9.3 前端测试

| 测试项 | 测试步骤 | 预期结果 |
|--------|----------|----------|
| 笔记列表页 | 打开笔记列表 | status 列显示"正常"/"下架"等中文标签 |
| 视频列表页 | 打开视频列表 | audit_status 列显示"待审核"/"审核通过"等中文标签 |
| 用户管理页 | 打开用户列表 | sex 列显示"男"/"女"等中文标签 |
| 表单编辑 | 打开笔记编辑表单 | 状态下拉框正常显示字典选项 |
| 筛选条件 | 打开列表页筛选条件 | 状态筛选下拉框正常显示字典选项 |

---

## 十、回滚方案

如果前端实施后出现问题，可快速回滚：

1. **前端回滚**: 恢复原有的字典转换逻辑
2. **后端兼容**: 后端接口同时返回原始值和标签字段，前端可自由选择使用
3. **灰度发布**: 先对部分用户开放新接口，验证无问题后再全量发布

---

## 十一、附录

### 11.1 相关文件清单

**后端文件**:
- `zsk-module-system/src/main/java/com/zsk/system/service/ISysDictDataService.java`
- `zsk-module-system/src/main/java/com/zsk/system/service/impl/SysDictDataServiceImpl.java`
- `zsk-module-system/src/main/java/com/zsk/system/controller/SysDictDataController.java`
- `zsk-api-system/src/main/java/com/zsk/system/api/RemoteDictService.java`
- `zsk-api-system/src/main/java/com/zsk/system/api/factory/RemoteDictFallbackFactory.java`

**前端文件** (需根据实际情况调整):
- `src/views/system/user/index.vue`
- `src/views/system/role/index.vue`
- `src/views/system/menu/index.vue`
- `src/views/system/notice/index.vue`
- `src/views/doc/note/list.vue`
- `src/views/doc/video/list.vue`
- 等等...

### 11.2 字典数据初始化

所有字典数据已在 `init/sql/dict_data_document.md` 中定义，启动时自动导入即可。
