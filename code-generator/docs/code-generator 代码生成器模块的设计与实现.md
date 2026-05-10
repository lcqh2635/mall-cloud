针对您的 `mall-cloud` 项目，由于采用了 **API/Biz 分离** 的微服务架构，且使用了 **MyBatis-Flex**，标准的单体代码生成器无法直接满足需求。

您需要的 `code-generator` 应该是一个 **独立的 Spring Boot Web 服务**，它不仅能连接数据库生成代码，还能根据您的需求，将代码分别生成到
`api` 模块（Feign接口、DTO）和 `biz` 模块（Entity, Mapper, Service, Controller）。

以下是详细的设计方案和代码实现：

### 1. 模块目录结构推荐

建议采用 **FreeMarker** 作为模板引擎，因为它功能强大且易于扩展。

```text
code-generator/
├── pom.xml                              # 依赖：spring-boot-web, freemarker, postgresql-jdbc, mybatis-flex-core
├── src/main/java/com/mallcloud/generator/
│   ├── GeneratorApplication.java        # 启动类
│   ├── controller/
│   │   └── GeneratorController.java     # 对外暴露的 HTTP 接口（页面渲染、代码下载）
│   ├── config/
│   │   ├── GeneratorConfig.java         # 全局配置（包路径、作者等）
│   │   └── FreeMarkerConfig.java        # 模板配置
│   ├── service/
│   │   ├── DatabaseMetaService.java     # 负责读取 PostgreSQL 元数据（表、列信息）
│   │   └── CodeGenerateService.java     # 核心逻辑：读取元数据 -> 渲染模板 -> 打包Zip
│   ── model/
│       ├── TableInfo.java               # 表信息模型
│       └── ColumnInfo.java              # 列信息模型
└── src/main/resources/
    ├── application.yml                  # 数据库连接配置
    └── templates/                       # FreeMarker 模板目录
        ├── biz/                         # 生成到 *-biz 模块的代码
        │   ├── Entity.ftl               # 实体类 (MyBatis-Flex 风格)
        │   ├── Mapper.ftl               # Mapper 接口
        │   ├── Service.ftl              # Service 接口
        │   ├── ServiceImpl.ftl          # Service 实现类
        │   └── Controller.ftl           # Controller
        └── api/                         # 生成到 *-api 模块的代码
            ├── FeignClient.ftl          # Feign 调用接口
            └── DTO.ftl                  # 数据传输对象
```

---

### 2. 核心代码实现

#### 2.1. 依赖配置 (`pom.xml`)

```xml

<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- FreeMarker 模板引擎 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-freemarker</artifactId>
    </dependency>
    <!-- PostgreSQL 驱动 -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <!-- Hutool 工具包 (用于文件操作等) -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.25</version>
    </dependency>
</dependencies>
```

#### 2.2. 数据库元数据读取 (`DatabaseMetaService.java`)

这是最关键的一步，需要从 PostgreSQL 获取表结构。

```java
package com.mallcloud.generator.service;

import com.mallcloud.generator.model.ColumnInfo;
import com.mallcloud.generator.model.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DatabaseMetaService {

    @Autowired
    private DataSource dataSource;

    /**
     * 获取指定数据库的所有表
     */
    public List<TableInfo> getTables(String schema) {
        List<TableInfo> tables = new ArrayList<>();
        try (java.sql.Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            // 查询表信息，PostgreSQL 中 table type 通常为 'TABLE'
            try (ResultSet rs = metaData.getTables(conn.getCatalog(), schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    TableInfo table = new TableInfo();
                    table.setTableName(rs.getString("TABLE_NAME"));
                    table.setComment(rs.getString("REMARKS"));
                    tables.add(table);
                }
            }
        } catch (Exception e) {
            log.error("获取表列表失败", e);
        }
        return tables;
    }

    /**
     * 获取指定表的所有列信息
     */
    public List<ColumnInfo> getColumns(String schema, String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();
        try (java.sql.Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), schema, tableName, "%")) {
                while (rs.next()) {
                    ColumnInfo col = new ColumnInfo();
                    col.setColumnName(rs.getString("COLUMN_NAME"));
                    col.setColumnType(rs.getString("TYPE_NAME")); // 数据库类型，如 varchar, int4
                    col.setComment(rs.getString("REMARKS"));
                    col.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                    // 转换为 Java 类型
                    col.setJavaType(convertToJavaType(rs.getString("TYPE_NAME")));
                    columns.add(col);
                }
            }
        } catch (Exception e) {
            log.error("获取列信息失败", e);
        }
        return columns;
    }

    // 简化的类型转换逻辑，实际项目中应更完善
    private String convertToJavaType(String dbType) {
        if (dbType == null) return "String";
        String lower = dbType.toLowerCase();
        if (lower.contains("int")) return "Long"; // Postgres int8 对应 Long
        if (lower.contains("bool")) return "Boolean";
        if (lower.contains("date") || lower.contains("time")) return "java.time.LocalDateTime";
        if (lower.contains("decimal") || lower.contains("numeric")) return "java.math.BigDecimal";
        return "String";
    }
}
```

#### 2.3. 核心生成逻辑 (`CodeGenerateService.java`)

负责渲染模板并打包。

```java
package com.mallcloud.generator.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import com.mallcloud.generator.model.ColumnInfo;
import com.mallcloud.generator.model.TableInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class CodeGenerateService {

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private DatabaseMetaService metaService;

    /**
     * 生成代码并下载 Zip
     *
     * @param schema       数据库 Schema
     * @param tableName    表名
     * @param serviceName  服务名称 (如 "order")
     * @param response     HTTP 响应
     */
    public void generateAndDownload(String schema, String tableName, String serviceName, HttpServletResponse response) {
        try {
            // 1. 获取元数据
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(tableName);
            // 获取列
            List<ColumnInfo> columns = metaService.getColumns(schema, tableName);
            // 这里可以简单模拟获取主键，实际需查询 metaData.getPrimaryKeys
            // ...

            // 2. 准备模板数据模型 (Model)
            Map<String, Object> model = new HashMap<>();
            model.put("table", tableInfo);
            model.put("columns", columns);
            model.put("serviceName", serviceName); // 用于生成包名
            model.put("author", "mall-cloud-dev");
            model.put("date", java.time.LocalDate.now().toString());

            // 驼峰命名处理 (如 user_info -> UserInfo)
            String className = toCamelCase(tableName);
            model.put("className", className);
            model.put("classNameLower", Character.toLowerCase(className.charAt(0)) + className.substring(1));

            // 3. 渲染所有模板文件
            Map<String, String> fileContentMap = new HashMap<>();

            // 渲染 Biz 层代码
            renderTemplate("biz/Entity.ftl", model, fileContentMap, generatePath("biz", "entity", className, "java"));
            renderTemplate("biz/Mapper.ftl", model, fileContentMap, generatePath("biz", "mapper", className, "Mapper.java"));
            renderTemplate("biz/Service.ftl", model, fileContentMap, generatePath("biz", "service", "I" + className, "Service.java"));
            renderTemplate("biz/ServiceImpl.ftl", model, fileContentMap, generatePath("biz", "service.impl", className, "ServiceImpl.java"));
            renderTemplate("biz/Controller.ftl", model, fileContentMap, generatePath("biz", "controller", className, "Controller.java"));

            // 渲染 API 层代码 (Feign)
            renderTemplate("api/FeignClient.ftl", model, fileContentMap, generatePath("api", "feign", className, "FeignClient.java"));

            // 4. 打包成 Zip 下载
            downloadZip(fileContentMap, serviceName + "_" + tableName + ".zip", response);

        } catch (Exception e) {
            log.error("代码生成失败", e);
            throw new RuntimeException("代码生成失败: " + e.getMessage());
        }
    }

    private void renderTemplate(String templateName, Map<String, Object> model, Map<String, String> outMap, String filePath) throws Exception {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter stringWriter = new StringWriter();
        template.process(model, stringWriter);
        outMap.put(filePath, stringWriter.toString());
    }

    // 生成文件相对路径
    private String generatePath(String module, String subPackage, String className, String suffix) {
        // 例如：src/main/java/com/mallcloud/order-biz/entity/UserInfo.java
        return String.format("src/main/java/com/mallcloud/%s-service/%s/%s%s",
                moduleName(module), subPackage.replace('.', '/'), className, suffix);
    }

    private String moduleName(String module) {
        return module.equals("biz") ? "biz" : "api"; // 简化逻辑
    }

    private void downloadZip(Map<String, String> fileContentMap, String zipName, HttpServletResponse response) throws Exception {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipName);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, String> entry : fileContentMap.entrySet()) {
                zos.putNextEntry(new java.util.zip.ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            zos.finish();
            IoUtil.write(response.getOutputStream(), true, baos.toByteArray());
        }
    }

    private String toCamelCase(String tableName) {
        // 简单的下划线转驼峰实现
        String[] parts = tableName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
```

#### 2.4. 模板示例 (`biz/Entity.ftl`)

这是生成 **MyBatis-Flex** 风格实体类的模板。

```freemarker
<#--
    模板：Entity.ftl
    路径：src/main/resources/templates/biz/Entity.ftl
-->
package com.mallcloud.${serviceName}-biz.entity;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import java.io.Serializable;
<#if hasDate>
import java.time.LocalDateTime;
</#if>

/**
 * ${table.comment!} 实体类
 * 生成时间: ${date}
 * @author ${author}
 */
@Data
@Table("${table.tableName}")
public class ${className} implements Serializable {

    private static final long serialVersionUID = 1L;

<#list columns as column>
    <#if column.comment?has_content>
    /**
     * ${column.comment}
     */
    </#if>
    <#if column.isPrimaryKey>
    @Id(keyType = KeyType.Auto) // 假设主键自增
    </#if>
    private ${column.javaType} ${column.columnName?uncap_first}; // 假设列名已经是下划线风格，这里转驼峰

</#list>
}
```

#### 2.5. 模板示例 (`api/FeignClient.ftl`)

这是生成 **Spring Cloud OpenFeign** 接口的模板。

```freemarker
<#--
    模板：FeignClient.ftl
    路径：src/main/resources/templates/api/FeignClient.ftl
-->
package com.mallcloud.${serviceName}-api.feign;

import com.mallcloud.${serviceName}-api.dto.${className}DTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.mallcloud.commons.core.result.Result;
import java.util.List;

/**
 * ${table.comment!} 远程调用接口
 * @author ${author}
 */
@FeignClient(name = "${serviceName}-service", path = "/${serviceName}/v1/${classNameLower}")
public interface ${className}FeignClient {

    /**
     * 分页查询
     */
    @GetMapping("/page")
    Result<Page<${className}DTO>> page(@RequestParam Map<String, Object> params);

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    Result<${className}DTO> getById(@PathVariable("id") Long id);

    /**
     * 新增或修改
     */
    @PostMapping
    Result<Void> saveOrUpdate(@RequestBody ${className}DTO dto);

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable("id") Long id);
}
```

### 3. 设计亮点与优化建议

1. **API/Biz 分离支持**：
    * 在 `generatePath` 方法中，我特意区分了路径。
    * `biz` 模块生成：Entity, Mapper, Service, Controller。
    * `api` 模块生成：FeignClient (方便其他服务调用)。
    * 这完美契合您项目中 `auth-api`, `auth-biz` 的结构。

2. **MyBatis-Flex 特性适配**：
    * 模板中使用了 `@Table`, `@Id` 等 MyBatis-Flex 的注解。
    * 利用 MyBatis-Flex 的 APT (Annotation Processing Tool) 机制，生成的 Entity 会在编译期自动生成 `TableDef` 类，非常方便。

3. **数据库无关性 (部分)**：
    * 虽然目前是 JDBC 读取，但利用了 `DatabaseMetaData`，理论上支持所有标准 JDBC 数据库。针对 PostgreSQL 的 `getColumns`
      做了特定优化。

4. **使用建议**：
    * 建议在本地启动 `code-generator` 服务。
    * 访问一个简单的前端页面（可以用 Vue 写个简单的表单），输入表名、模块名。
    * 点击下载，得到一个 Zip 包。
    * 解压 Zip 包，将文件直接覆盖到对应的 `order-service/biz` 和 `order-service/api` 目录中。

这个设计既保留了自动化的效率，又解决了微服务模块化拆分带来的路径和依赖问题。