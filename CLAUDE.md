# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ZSK-Cloud — enterprise microservice scaffold built on **JDK 21**, **Spring Boot 3.5.0**, **Spring Cloud 2025.0.0**, **Spring Cloud Alibaba 2025.0.0.0**, with **Nacos 3.1.1** for registry/config, **Sentinel** for flow control, **Spring Cloud Gateway** as the edge, **MyBatis-Plus 3.5.7 + Druid + Dynamic-Datasource** for persistence, and **Redis/Redisson** for cache and distributed locks.

GroupId: `com.zsk.cloud`. All internal versions are pinned via the root `pom.xml` (`zsk-cloud.version = 1.0.0-SNAPSHOT`) and managed in `<dependencyManagement>` — child modules **must not** declare versions for `zsk-*`, Spring, or Alibaba dependencies.

## Common Commands

Run from the repo root unless noted. The Maven wrapper is **not** committed; use a local `mvn` (3.9+).

```bash
# Full build (all modules), skip tests
mvn -T 1C clean install -DskipTests

# Build a single module (with its dependencies)
mvn -pl zsk-module/zsk-module-system -am clean install -DskipTests

# Run tests for one module
mvn -pl zsk-common/zsk-common-core test

# Run a single test class / method
mvn -pl zsk-module/zsk-module-system test -Dtest=ZskSystemApplicationTests
mvn -pl zsk-module/zsk-module-system test -Dtest=UserServiceTest#testCreateUser

# Compile-only check (the project's own RULE-CORE-003 gate)
mvn clean compile

# Run a service locally (after install)
mvn -pl zsk-gateway spring-boot:run
mvn -pl zsk-auth   spring-boot:run
mvn -pl zsk-module/zsk-module-system   spring-boot:run
mvn -pl zsk-module/zsk-module-document spring-boot:run
mvn -pl zsk-visual/zsk-visual-monitor  spring-boot:run
```

Local startup order: **Nacos → Redis → MySQL → gateway → auth → business modules**. Import `init/nacos/**` configs into Nacos and run scripts under `init/sql/` before first launch.

## Architecture (big picture)

The repo is a flat Maven multi-module reactor. The aggregator `pom.xml` lists six top-level modules; each contains sub-modules.

```
zsk-cloud (parent BOM, dependencyManagement only)
├── zsk-common         # shared starters — added to business modules as deps
│   ├── zsk-common-core         # R<T>, BusinessException, GlobalExceptionHandler, ResultCode, SecurityContext
│   ├── zsk-common-security     # Spring Security, SecurityUtils, HeaderContextFilter, @PreAuthorize, @RepeatSubmit
│   ├── zsk-common-redis        # Redisson + RedisTemplate auto-config
│   ├── zsk-common-datasource   # dynamic-datasource (multi-DS switching)
│   ├── zsk-common-oss          # MinIO + Aliyun OSS unified façade
│   ├── zsk-common-log          # operation log collection
│   ├── zsk-common-swagger      # Knife4j / SpringDoc auto-config
│   ├── zsk-common-sentinel     # SentinelBlockHandler, @CircuitBreaker
│   └── zsk-common-xxljob       # XXL-Job (placeholder, not implemented)
├── zsk-api            # Feign client interfaces, consumed by other services
│   ├── zsk-api-system
│   └── zsk-api-document
├── zsk-auth           # ZskAuthApplication — login, JWT issuance, OAuth2 (QQ/WeChat/GitHub), captcha, magic-link
├── zsk-gateway        # ZskGatewayApplication — Spring Cloud Gateway: auth filter, blacklist, XSS, Sentinel rate-limit
├── zsk-module         # business services
│   ├── zsk-module-system    # ZskSystemApplication — RBAC (user/role/menu), dict, params, notices, monitor, op-log
│   └── zsk-module-document  # ZskDocumentApplication — notes, files, videos, comments, OSS tasks, document/video review
└── zsk-visual
    └── zsk-visual-monitor   # ZskMonitorApplication — monitor center (placeholder, not fully implemented)
```

Each `zsk-common-*` starter ships a `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file — adding the dependency is enough; **no manual `@Import` or `@ComponentScan` adjustments**. Business modules typically depend on `zsk-common-core`, `-security`, `-redis`, `-datasource`, `-swagger`, plus the `zsk-api-*` clients they consume.

### Request flow

```
Client → zsk-gateway (auth filter validates JWT, sets headers) →
business module Controller → Service → (Manager) → Mapper → MySQL/Redis
```

`zsk-gateway` extracts the user from the JWT and propagates it via request headers. Downstream services read user context through **`SecurityUtils`** (`com.zsk.common.security.utils.SecurityUtils`) which wraps **`SecurityContext`** (`com.zsk.common.core.context.SecurityContext`). The `HeaderContextFilter` in `zsk-common-security` reads the gateway-propagated headers and populates `SecurityContext` (ThreadLocal via `TransmittableThreadLocal`) + Spring Security's `SecurityContextHolder`. **Do not parse `HttpServletRequest` in Service code** — use `SecurityUtils.getUserId()`, `SecurityUtils.getUserName()`, etc.

Cross-module calls go through `zsk-api-*` Feign clients (not direct HTTP).

### Port allocation

| Service | Port |
| :--- | :--- |
| gateway | 8080 |
| auth | 10010 |
| system | 20010 |
| document | (check Nacos config) |

### JWT key model (important)

`zsk-auth` follows **"private key signs, public key verifies"**:
- Only `zsk-auth` holds the RS256 private key and issues tokens.
- Other services hold only the public key and verify via JJWT.
- A `keyLocator` in the verification path inspects the token's `alg` header: `RS256` → public key; `HS256` → fall back to shared `secret`. This allows symmetric ↔ asymmetric migration without breaking live tokens.

When changing JWT logic, update **both** the signing side (`zsk-auth`) and the verification side (`zsk-common-security`/gateway), and keep `keyLocator` semantics intact.

### Unified response class

The project uses **`R<T>`** (`com.zsk.common.core.domain.R`), not `Result<T>`. Controllers return `R<T>`; errors throw `BusinessException` and `GlobalExceptionHandler` translates to `R<?>`. Key static methods: `R.ok()`, `R.ok(data)`, `R.fail()`, `R.fail(msg)`, `R.fail(code, msg)`, `R.fail(ResultCode)`, `R.paramError(msg)`, `R.bizError(msg)`, `R.unauthorized(msg)`, `R.forbidden(msg)`.

`ResultCode` (`com.zsk.common.core.enums.ResultCode`) defines 40+ error codes: SUCCESS(200), PARAM_ERROR(10001), BIZ_ERROR(10100), UNAUTHORIZED(10301), FORBIDDEN(10302), SYSTEM_ERROR(500), etc.

## Mandatory project rules

This codebase enforces a strict ruleset. Both `.trae/rules/rule-java-development.md` and the global `~/.claude/rules/rule-java-development.md` apply. Highlights — **violations are blocking (P0)**:

- **Layering** (`RULE-ARCH-*`): `Controller → Service → (Manager) → Mapper`. Controllers contain no business logic and never touch Mapper directly. Services never parse `HttpServletRequest`.
- **Responses**: every Controller returns **`R<T>`** (not `Result<T>`). Errors throw `BusinessException`; let `GlobalExceptionHandler` translate.
- **User context**: use `SecurityUtils` (not `GatewayContextHolder` — that class doesn't exist). Never parse `HttpServletRequest` in Service.
- **DI** (`RULE-DEV-008`): constructor injection via Lombok `@RequiredArgsConstructor` on `final` fields. **No `@Autowired` field injection.**
- **Naming** (`RULE-NAME-006`): `XxxDO` (entity), `XxxDTO` (input), `XxxVO` (output), `XxxQuery` (query). Don't reuse a DO across layers.
- **SQL** (`RULE-DEV-002`, `RULE-SEC-001`): never `SELECT *`, never `${}` concatenation — use MyBatis-Plus or `#{}`. Complex SQL goes in XML, not Java strings.
- **IDs** (`RULE-DEV-003`): Snowflake / UUID. Do not rely on DB auto-increment.
- **Money** (`RULE-DEV-004`): `BigDecimal` or `Long` (cents). `float`/`double` are forbidden — refuse the request and rewrite.
- **Concurrency** (`RULE-PERF-001`): `ThreadPoolExecutor` only — no `Executors.newFixedThreadPool` and no raw `new Thread()`. For distributed locks use Redisson.
- **Transactions** (`RULE-PERF-003`): `@Transactional` on public methods only; never wrap RPC/HTTP calls or slow SQL inside a transaction.
- **Logging** (`RULE-DEV-006`): Lombok `@Slf4j` with `{}` placeholders. No `System.out.println`, no `e.printStackTrace()`.
- **Utilities** (`RULE-DEV-007`): use Hutool (`StrUtil`, `CollUtil`, `DateUtil`) and MapStruct. **Do not use `BeanUtil.copyProperties`.**
- **Mapper** (`RULE-ARCH-004`): extend `BaseMapper<T>`, annotated `@Mapper`. Never use `Map` as parameter or return type — always DTO/DO.

When generating a Controller, auto-add `@RestController`, `@RequestMapping`, Knife4j `@Tag`/`@Operation`, and constructor-inject the Service. When generating a Mapper, extend `BaseMapper<T>` and put complex SQL in the matching XML file.

## Documentation map

- `README.md` — tech stack table, module overview, OAuth2 flow.
- `PROJECT_SUMMARY.md` — what's done vs. planned (RocketMQ, Elasticsearch, XXL-Job, Flowable are placeholders only).
- `docs/开发文档.md` — development conventions in depth, port allocation, request chain detail.
- `docs/认证流程说明.md`, `docs/魔法链接登录设计文档.md`, `docs/Token存储逻辑变更说明.md` — auth design.
- `docs/api/**` — per-module API specs (system, document, auth) generated from the Apifox flow.
- `.trae/rules/rule-java-development.md` — Java enterprise development rules (the comprehensive rule set with RULE-* numbering).
- `init/nacos/dev/` — Nacos config files for dev environment (shared `application.yml` + per-service configs + Sentinel rules).
- `init/nacos/prod/` — Nacos config for prod environment (currently only system module).
- `init/sql/` — DB bootstrap scripts (`zsk_system.sql`, `zsk_document.sql`).