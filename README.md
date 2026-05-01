# ZSK-Cloud 企业级微服务脚手架

## 🚀 项目概述
ZSK-Cloud 是一款基于最新技术栈构建的企业级微服务开发脚手架，旨在为开发者提供高性能、高可用、可快速扩展的底座。

- **核心架构**: 基于 Java 21、Spring Boot 3.5.0 及 Spring Cloud 2025.0.0。
- **治理中心**: 集成 Nacos 3.1.1、Sentinel 流量控制、Spring Cloud Gateway。
- **核心功能**: 包含认证授权（多模式登录）、动态数据源、多租户支持、统一对象存储（MinIO/OSS）、Redis Bitmap 交互系统、统一审核中心及 Docker 容器化部署。

---

## 🛠️ 技术栈
| 类别 | 关键技术 | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| **开发环境** | JDK / Maven | 21 / 3.9+ | 享受最新 Java 特性 |
| **核心框架** | Spring Boot | 3.5.0 | 官方推荐稳定版本 |
| | Spring Cloud | 2025.0.0 | 微服务架构 |
| | Spring Cloud Alibaba | 2025.0.0.0 | 阿里微服务生态 |
| **服务治理** | Nacos | 3.1.1 | 服务注册与配置中心 |
| | Sentinel | 3.1.1 (集成) | 流量控制与熔断降级 |
| | Spring Cloud Gateway | 4.x | API 网关 |
| **持久层** | MyBatis-Plus | 3.5.7 | 灵活高效的 ORM 框架 |
| | MySQL | 8.3.0 | 关系型数据库 |
| | MongoDB | 6.x | NoSQL 数据库（行为审计/日志） |
| | Druid | 1.2.23 | 数据库连接池 |
| | Dynamic Datasource | 4.3.1 | 动态数据源（主从切换） |
| **缓存/队列** | Redis / Lettuce | 3.4.1 / 6.3.2 | 缓存数据库 |
| | Redisson | 3.27.0 | 分布式锁与工具 |
| | Caffeine | 3.1.8 | 本地缓存 |
| | RocketMQ | 5.2.0 | 消息队列 (待集成) |
| **安全认证** | Spring Security | 6.x | 权限控制 |
| | JWT (JJWT) | 0.12.5 | 令牌认证（HS256/RS256 双模） |
| | Bouncy Castle | 1.78 | 加密算法 |
| | OAuth2 | - | 第三方登录（QQ/微信/GitHub） |
| | Cloudflare Turnstile | - | 人机验证 (代码已预留) |
| **文件存储** | MinIO | 8.5.8 | 对象存储（含分片上传） |
| | 阿里云 OSS | 3.18.1 | 云存储 |
| **搜索引擎** | Elasticsearch | 8.12.0 | 全文搜索 (待集成) |
| **工具类** | Hutool | 5.8.40 | Java 工具集 |
| | Guava | 33.0.0-jre | Google 工具库 |
| | MapStruct | 1.5.5.Final | 对象映射 |
| | Lombok | 1.18.36 | 代码简化 |
| | EasyExcel | 4.0.3 | Excel 处理 |
| | Kryo | 5.6.0 | 高性能序列化 |
| | TTL | 2.14.5 | 跨线程上下文传递 |
| | Commons-IO | 2.18.0 | IO 工具 |
| | Commons-Email | 1.5 | 邮件发送 |
| **文档/监控** | Knife4j | 4.5.0 | OpenAPI 3 文档 |
| | SpringDoc | 2.8.4 | API 文档 |
| | SkyWalking | 9.1.0 | 链路追踪 (待集成) |
| | Micrometer | 1.13.0 | 监控指标 |
| **任务调度** | XXL-JOB | 2.4.1 | 分布式调度 (代码已预留) |

---

## 📂 项目结构
```text
zsk-cloud
├── zsk-api               // 接口模块 (Feign 客户端)
│   ├── zsk-api-system    // 系统模块远程接口 (用户/字典)
│   └── zsk-api-document  // 文档模块远程接口 (内容/文件/统计)
├── zsk-auth              // 认证中心 (多模式登录、JWT签发、OAuth2)
├── zsk-common            // 通用组件 (9个 Starter)
│   ├── zsk-common-core      // R<T>统一响应、异常处理、SecurityContext、JwtUtils、敏感数据脱敏
│   ├── zsk-common-security  // 权限注解、HeaderContextFilter、防重提交、内部接口保护
│   ├── zsk-common-redis     // Redis/Redisson 分布式缓存、分布式锁(@RedisLock)、Bitmap工具
│   ├── zsk-common-oss       // 统一对象存储 (MinIO/Aliyun OSS)、动态切换、分片上传
│   ├── zsk-common-datasource// 动态数据源 (@Master/@Slave)、MyBatis-Plus 自动填充
│   ├── zsk-common-log       // @Log 操作日志采集 (AOP)
│   ├── zsk-common-swagger   // Knife4j/SpringDoc 自动配置
│   ├── zsk-common-sentinel  // @RateLimit/@CircuitBreaker 限流熔断 (SpEL)
│   └── zsk-common-xxljob    // XXL-JOB 分布式调度 (代码已预留)
├── zsk-gateway           // 网关中心 (鉴权、黑名单、XSS、日志、动态路由)
├── zsk-module            // 业务模块
│   ├── zsk-module-system   // 系统管理 (18个Controller)
│   └── zsk-module-document // 文档管理 (19个Controller)
├── zsk-visual            // 图形化监控
│   └── zsk-visual-monitor  // 监控中心 (占位)
├── init                  // 环境初始化配置
│   ├── nacos             // Nacos 配置文件 (dev/prod)
│   │   ├── dev           // 开发环境 (共享配置 + 各服务配置 + Sentinel规则)
│   │   └── prod          // 生产环境 (系统模块配置)
│   └── sql               // 数据库脚本 (4个)
├── docs                  // 项目文档
│   ├── api               // API 接口文档 (按模块分类)
│   └── 相关设计           // 设计文档
└── docker-compose.yml    // Docker 容器编排
```

---

## ✨ 已实现功能

### 基础架构

#### 网关服务 (`zsk-gateway`)
- **AuthFilter**: JWT 令牌验证、白名单校验、Redis Token 状态验证、用户信息注入请求头（userId/username/nickname/roles/permissions）、滑动过期机制
- **BlackListFilter**: IP 黑名单拦截（Redis Set 存储）
- **XssFilter**: XSS 跨站脚本攻击防护（URL 查询参数清洗，支持排除路径配置）
- **LogFilter**: 请求日志记录（RequestId/Method/Path/RemoteAddr/StatusCode/Duration）
- **动态路由**: 基于 Nacos 的服务发现自动路由（`/api/auth/**` → zsk-auth, `/api/system/**` → zsk-module-system, `/api/document/**` → zsk-module-document）

#### 认证服务 (`zsk-auth`)
- **多模式登录**: 密码登录、邮箱验证码登录、魔法链接登录、第三方 OAuth2 登录（QQ/微信/GitHub）
- **JWT 双模密钥**: "私钥签名、公钥验证"模式，`keyLocator` 自动兼容 HS256/RS256
- **RSA 密码加密**: 前端 RSA 公钥加密密码，后端私钥解密，防止明文传输
- **滑块验证码**: 登录/注册时的图形验证码生成与校验
- **邮箱验证码**: 注册/密码重置时的邮箱验证码发送与校验
- **魔法链接登录**: 发送含 Token 的邮件链接，点击即可免密登录
- **密码重置**: 通过邮箱验证码重置密码（验证码发送 → 验证 → 重置）
- **令牌刷新**: 支持访问令牌刷新机制
- **多设备登录**: 单用户最多 5 个有效 Token，Redis Set 存储
- **接口限流**: 注册 10次/分、登录 10次/分、邮箱验证码 5次/分、魔法链接 3次/分、密码重置 3次/分
- **Cloudflare Turnstile**: 人机验证集成（代码已预留，接口已注释）

#### 通用组件 (`zsk-common`)
- `core`: R\<T\> 统一响应、40+ ResultCode 错误码、BusinessException 等 10+ 异常类、GlobalExceptionHandler、SecurityContext（TransmittableThreadLocal）、JwtUtils、敏感数据序列化（@Sensitive）、IpUtils/JsonUtils/StringUtils/XssUtil
- `security`: HeaderContextFilter（网关头解析）、@RepeatSubmit 防重提交、@InnerAuth 内部接口保护、@RequiresPermissions/@RequiresRoles/@RequiresLogin 权限注解、SecurityUtils、DictUtils
- `redis`: RedisService（通用缓存操作）、LockService（分布式锁）、@RedisLock 注解 + AOP、BitmapOffsetUtil
- `oss`: MinioTemplate/AliyunTemplate/DynamicOssTemplate（动态切换）、OssPart 分片上传模型、ExcelUtil
- `datasource`: MybatisPlusConfig、PageQuery/PageResult 分页组件、@Master/@Slave 数据源切换、MybatisPlusMetaObjectHandler 自动填充
- `swagger`: Knife4j/SpringDoc 自动配置
- `sentinel`: @RateLimit 限流注解（SpEL 表达式支持）、@CircuitBreaker 熔断注解、SentinelBlockHandler 统一异常处理
- `log`: @Log 注解 + LogAspect AOP 切面、OperLog 实体、BusinessType 枚举
- `xxljob`: XxlJobAutoConfiguration/XxlJobRegister/XxlJobService/@XxlJobAutoRegister（代码已预留，未实际集成）

### 业务模块

#### 系统管理 (`zsk-module-system`) — 18 个 Controller
- **RBAC 权限模型**: 用户管理（CRUD、状态切换、密码重置、头像上传）、角色管理（CRUD、复制、菜单分配、用户分配）、菜单管理（树形结构、批量更新）
- **字典管理**: 字典类型/字典数据 CRUD、状态切换、批量状态切换
- **参数配置**: 系统参数 CRUD
- **通知公告**: 通知 CRUD、控制台公告查询
- **系统监控**: 服务器数据采集、概览、趋势、手动采集、数据清理
- **行为审计**: 用户行为查询、事件查询（MongoDB 数据源）
- **登录管理**: 在线用户分页查询、强制下线
- **缓存管理**: Redis 实例管理、键操作（查询/刷新/删除/批量操作）、统计信息、内存使用、TTL 刷新、缓存预热
- **操作日志**: 日志分页查询、批量删除
- **任务管理**: 甘特图任务 CRUD（支持 task/project/milestone 类型）、任务依赖关系管理（4种依赖类型、循环依赖检测）
- **仪表盘**: 系统概览数据
- **工具箱**: 配置驱动的在线工具（JSON 格式化、Base64 编解码、时间戳转换）
- **About 页面**: 技术栈展示、FAQ 问答
- **简历模块**: 简历详情查询、保存（配置驱动）

#### 文档管理 (`zsk-module-document`) — 19 个 Controller
- **笔记管理**: CRUD、草稿管理、状态批量更新、分类批量迁移、置顶/推荐、统计信息、元信息查询
- **笔记详情**: Markdown 内容管理、MD 文件上传、笔记聚合接口（全量创建/查询/更新/删除）
- **视频管理**: CRUD、草稿管理、视频上传、状态批量更新、置顶/推荐
- **视频合集**: 合集 CRUD、添加/移除视频、排序管理
- **文件管理**: 上传、删除、分片上传（init/upload/complete 三步）
- **评论系统**: B站式二级评论结构（根评论 + 回复）、评论点赞（Redis 缓存）、热门/最新排序
- **交互系统**: 点赞/收藏/关注（Redis Bitmap + 缓存）、浏览量计数、定时同步到数据库
- **统一审核**: 审核队列、审核详情、提交/批量提交审核、审核日志、违规原因查询（支持 AI 审核/人工审核）
- **全局搜索**: 关键字搜索、类型/分类筛选、热门/点赞排序
- **OSS 管理**: MinIO/Aliyun OSS 动态配置刷新、分片上传
- **文件处理任务**: 转码任务管理、处理历史记录
- **统计信息**: 用户统计（点赞/关注/收藏数）、内容统计（文章/视频/评论数）
- **前台聚合接口**: 区域化接口设计（元信息+详情、交互、评论、合集），前后台逻辑隔离

### Feign 远程调用 (`zsk-api`)
- **zsk-api-system**: RemoteUserService（7个方法：用户查询/批量查询/邮箱查询/第三方查询/创建/更新）、RemoteDictService（2个方法：字典数据查询/字典标签查询）
- **zsk-api-document**: RemoteDocumentContentService（详情/点赞/收藏/评论/评论点赞）、RemoteDocFilesService（上传/删除）、RemoteDocAllContentService（用户统计/内容统计）
- **服务发现**: 所有 FeignClient 基于 Nacos 服务发现自动寻址,不硬编码服务地址

### 容器化部署
- **Docker Compose**: 4 个服务编排（gateway:8080、auth:10010、system:20010、document:20020）
- **Dockerfile**: 各服务均提供多阶段构建 Dockerfile
- **JVM 参数**: G1GC + 容器感知内存分配（InitialRAMPercentage=50%, MaxRAMPercentage=75%）
- **环境变量**: 支持 Nacos 地址/命名空间/用户名密码、运行 Profile（dev/prod）动态配置
- **Profile 自动切换**: `SPRING_PROFILES_ACTIVE` 环境变量驱动,本地开发默认 dev,Docker 部署自动切换为 prod

---

## 🗄️ 数据库设计

### 系统模块 (`zsk_system`) — 11 张表
| 表名 | 说明 |
| :--- | :--- |
| `sys_user` | 用户信息表（含 tenant_id、avatar_id、age、bio 等扩展字段） |
| `sys_role` | 角色信息表（含 data_scope 数据范围） |
| `sys_user_role` | 用户角色关联表 |
| `sys_menu` | 菜单权限表（M 目录/C 菜单/F 按钮） |
| `sys_role_menu` | 角色菜单关联表 |
| `sys_dict_type` | 字典类型表 |
| `sys_dict_data` | 字典数据表 |
| `sys_config` | 参数配置表 |
| `sys_notice` | 通知公告表 |
| `sys_task` | 任务表（支持甘特图：task/project/milestone） |
| `sys_task_link` | 任务依赖关系表（4 种依赖类型） |

### 文档模块 (`zsk_document`) — 12 张表
| 表名 | 说明 |
| :--- | :--- |
| `document_note` | 笔记信息表（含 SEO 字段、审核状态、置顶/推荐、乐观锁） |
| `document_note_dtl` | 笔记详情表（Markdown 内容，note_id 唯一索引） |
| `document_note_comment` | 笔记评论表（B站式二级结构） |
| `document_files` | 文件表（支持分片上传状态追踪） |
| `document_process` | 文件处理任务表 |
| `document_process_history` | 文件处理历史表 |
| `document_video` | 视频表（含 meta_data JSON、审核字段） |
| `document_video_comment` | 视频评论表 |
| `document_video_collection` | 视频合集表 |
| `document_video_collection_item` | 合集视频关联表 |
| `document_user_interaction` | 用户交互关系表（4 种目标类型 × 4 种交互类型） |
| `document_audit` | 统一审核记录表（支持 AI/人工审核，4 种目标类型） |

### 其他数据库
| 数据库 | 说明 |
| :--- | :--- |
| `nacos` | Nacos 配置中心数据库（12 张表） |
| `xxl_job` | XXL-Job 调度中心数据库（8 张表，占位） |
| `zsk_log` (MongoDB) | 行为审计日志数据库 |

---

## 🚧 待开发与规划

### 核心组件完善
- **分布式消息队列**: 集成 RocketMQ，完成消息生产者与消费者示例。
- **搜索引擎**: 集成 Elasticsearch 8.12.0，实现全文搜索功能。
- **链路追踪**: 集成 SkyWalking 9.1.0，实现分布式链路追踪。

### 业务模块扩展
- **工作流引擎**: 集成 Flowable 或 Activiti 实现 BPMN 2.0 工作流，增强流程管理能力。
- **Feign 路径同步**: 修复 RemoteDocumentContentService 中的路径前缀与实际 Controller 不一致的问题。

### 工程化与运维
- **自动化测试**: 补充单元测试（JUnit/Mockito）与集成测试用例。
- **Jenkinsfile**: 提供 Jenkins CI/CD 流水线脚本。
- **日志采集**: 集成 ELK (Elasticsearch, Logstash, Kibana) 实现统一日志采集。
- **生产环境配置**: 完善 prod 环境 Nacos 配置（目前仅有 system 模块）。

### AI 与智能应用
- **AI-QQ-BOT**: 使用 Spring AI 集成大模型，对接 QQ 机器人，在 QQ 群中提供智能客服与文档助手功能，配合 Nacos 的 MCP 和 Skill 实现动态配置。
- **AI 审核增强**: 基于大模型的内容审核能力，自动识别违规内容并生成审核意见。

---

## 🛡️ 安全机制说明

### JWT 密钥管理
项目支持 **对称加密 (HS256)** 与 **非对称加密 (RS256)** 的动态兼容：
- **签名原则**：遵循"私钥签名、公钥验证"。私钥仅保存在认证中心（zsk-auth），用于签发令牌；公钥分发给各微服务，用于校验令牌合法性。
- **智能解析**：通过 `keyLocator` 机制，系统能根据令牌头部的算法 (`alg`) 自动匹配密钥。若 `alg` 为 `RS256` 则使用公钥验证，若为 `HS256` 则回退至 `secret` 验证，确保了配置迁移过程中的平滑过渡。

### RSA 密码加密
- 前端登录时使用 RSA 公钥加密密码，后端通过 `EncryptProperties` 配置的私钥解密，防止密码明文传输。
- 支持 `@RefreshScope` 动态刷新密钥对。

### 接口安全
- **防重提交**: `@RepeatSubmit` 注解，基于 Redis 实现接口防重
- **内部接口保护**: `@InnerAuth` 注解，仅允许内部 Feign 调用
- **权限控制**: `@RequiresPermissions`/`@RequiresRoles`/`@RequiresLogin` 细粒度权限注解
- **XSS 防护**: 网关层全局 XSS 过滤，支持路径排除配置
- **IP 黑名单**: 网关层基于 Redis 的 IP 黑名单拦截

---

## 🔐 第三方登录流程
项目遵循标准的 OAuth2 授权码模式，实现"即登即用"：

### **详细调用链路**

![oauth2](./docs/相关设计/oauth2.png)

### **核心逻辑步骤**
1. **授权引导**: 前端通过 `/third-party/url` 接口获取预构建的第三方授权地址，并在 Redis 中存储 `state` 防伪码以应对 CSRF 风险。
2. **授权回调**: 用户在第三方平台确认授权后，浏览器重定向至前端回调页，前端提取 `code` 与 `state` 并透传至后端回调接口。
3. **双重交换**:
   - **令牌交换**：后端策略类（Strategy）通过授权码向第三方服务器换取 `access_token`。
   - **信息获取**：使用令牌调用第三方用户信息 API，解析并封装为统一的 `SysUserApi` 实体。
4. **账户联动**:
   - **映射检测**：根据 `loginType` 与第三方唯一标识（如 OpenID）查询本地账户关联表。
   - **静默注册**：若为新用户，系统将基于第三方基础资料（昵称、头像）自动完成账号初始化。
5. **颁发凭证**: 认证通过后，系统签发高安全性的 JWT 访问令牌，并将登录状态同步至 Redis 缓存。

---

## 🌐 端口分配
| 服务 | 端口 | 说明 |
| :--- | :--- | :--- |
| zsk-gateway | 8080 | API 网关 |
| zsk-auth | 10010 | 认证中心 |
| zsk-module-system | 20010 | 系统管理 |
| zsk-module-document | 20020 | 文档管理 |

---

## 📖 快速上手
1. **克隆项目**: `git clone https://github.com/zsk-cloud/zsk-cloud.git`
2. **初始化环境**:
   - 启动 Nacos、MySQL、Redis、MongoDB 基础设施
   - 执行 `init/sql/` 下的数据库脚本（`zsk_system.sql`、`zsk_document.sql`、`nacos.sql`）
   - 在 Nacos 中导入 `init/nacos/dev/` 下的配置文件
3. **本地启动**（按顺序）:
   - 启动 `ZskGatewayApplication` (网关, 端口 8080)
   - 启动 `ZskAuthApplication` (认证, 端口 10010)
   - 启动 `ZskSystemApplication` (系统模块, 端口 20010)
   - 启动 `ZskDocumentApplication` (文档模块, 端口 20020)
4. **Docker 部署**: `docker-compose up -d`

---

## 📄 相关文档
- [开发规范指南](./docs/相关设计/开发文档.md)
- [认证流程说明](./docs/相关设计/认证流程说明.md)
- [魔法链接登录设计](./docs/相关设计/魔法链接登录设计文档.md)
- [Token 存储逻辑变更](./docs/相关设计/Token存储逻辑变更说明.md)
- [Redis 交互数据缓存设计](./docs/相关设计/Redis交互数据缓存设计.md)
- [文档图片与视频封面上传流程](./docs/相关设计/文档图片与视频封面上传流程设计.md)
- [数据库规范](.trae/rules/prorules.md)
