# Implementation Plan: OSS Common Module & Document Microservice

## 1. zsk-common-oss Implementation
- [ ] **Dependencies**: Update `zsk-common/zsk-common-oss/pom.xml`
    - Add `minio`, `aliyun-sdk-oss`, `easyexcel`
    - Add `hutool-all`, `lombok` (provided by parent/common)
- [ ] **Configuration Properties**: Create `com.zsk.common.oss.properties.OssProperties`
    - Fields: `enabled`, `type` (minio/aliyun), `endpoint`, `accessKey`, `secretKey`, `bucketName`
- [ ] **Interface Definition**: Create `com.zsk.common.oss.core.OssTemplate`
    - Methods: `makeBucket`, `removeBucket`, `bucketExists`, `putObject`, `getObject`, `getObjectUrl`, `removeObject`
- [ ] **Implementations**:
    - Create `com.zsk.common.oss.core.MinioTemplate`
    - Create `com.zsk.common.oss.core.AliyunTemplate`
- [ ] **Auto Configuration**: Create `com.zsk.common.oss.config.OssConfig`
    - Conditional bean creation based on `zsk.oss.type`
- [ ] **Excel Utility**: Create `com.zsk.common.oss.utils.ExcelUtil`
    - Methods for easy import/export using EasyExcel

## 2. zsk-module-document Setup
- [ ] **Module Structure**: Create `zsk-module/zsk-module-document`
- [ ] **POM Configuration**: Create `pom.xml`
    - Parent: `zsk-module`
    - Dependencies: `zsk-common-core`, `zsk-common-oss`, `zsk-common-security`, `zsk-common-swagger`, `zsk-common-log`, `zsk-api-system`, `spring-boot-starter-web`, `mysql-connector-j`
- [ ] **Parent Registration**: Add module to `zsk-module/pom.xml`
- [ ] **Application Class**: Create `com.zsk.document.ZskDocumentApplication`
- [ ] **Configuration**: Create `src/main/resources/application.yml`
    - Server port (e.g., 9401)
    - Spring application name: `zsk-module-document`
    - DataSource config
    - Nacos config
    - OSS config (MinIO/Aliyun)

## 3. Document Business Logic
- [ ] **Domain Entities**: Create `com.zsk.document.domain.*`
    - `DocumentNote`, `DocumentNoteComment`, `DocumentNotePic`, `DocumentFiles`, `DocumentProcess`, `DocumentProcessHistory`
    - Use Lombok `@Data`, `@TableName`
- [ ] **Mappers**: Create `com.zsk.document.mapper.*`
    - Extend `BaseMapper<T>`
- [ ] **Services**: Create `com.zsk.document.service.*` and `impl.*`
    - Implement CRUD operations
    - `IDocumentFilesService` to handle file upload logic using `OssTemplate`
- [ ] **Controllers**: Create `com.zsk.document.controller.*`
    - `DocumentNoteController`, `DocumentFilesController`, etc.
    - Use `@RestController`, `@RequestMapping`, `R<T>` return type
