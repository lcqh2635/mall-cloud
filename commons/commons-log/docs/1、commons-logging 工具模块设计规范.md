非常好的问题！你正在构建一个**企业级微服务架构**，而 `commons-logging` 模块是其中**极易被忽视、但至关重要的一环**。

> ✅ 你的直觉完全正确：**不应该每个服务都自己写日志配置、重复定义格式、手动注入 MDC（Mapped Diagnostic Context）** —— 这是典型的“重复造轮子”，严重降低工程效率和一致性。

---

# 📜《urbane-commerce commons-logging 工具模块设计规范》
> **版本：19.0 | 最后更新：2025年4月 | 技术栈：Logback + SLF4J + Spring Boot 3.x + MDC + OpenTelemetry**

---

## ✅ 一、`commons-logging` 模块的核心作用（为什么需要它？）

| 问题 | 传统做法（无统一日志模块） | 使用 `commons-logging` 后 |
|------|---------------------------|----------------------------|
| 日志格式不一致 | A服务用 `[%d] [%t] %msg`，B服务用 `{timestamp} {level}` | ✅ 所有服务统一格式：`[traceId=xxx, userId=123] [INFO] ...` |
| 缺乏上下文追踪 | 无法通过 TraceID 关联跨服务调用链 | ✅ 自动注入 `X-Trace-ID` 和 `X-User-ID` 到日志 |
| 网关与服务日志断层 | 网关有 traceId，服务没有 → 链路断裂 | ✅ 网关注入 → 服务自动继承，全链路打通 |
| 配置冗余 | 每个服务都要复制粘贴 `logback-spring.xml` | ✅ 一个模板，所有服务复用 |
| 新人上手难 | 要学 Logback、MDC、SLF4J、Filter、Appender | ✅ 一行依赖，开箱即用 |
| 不支持 OpenTelemetry | 无法对接 Jaeger / Zipkin / Prometheus | ✅ 内置 OpenTelemetry 支持，一键接入 |

> 💡 **一句话总结**：  
> **`commons-logging` 是整个系统的“日志中枢”——它不是“打印日志的工具”，而是实现“全链路可观测性”的基础设施。**

---

## ✅ 二、推荐目录结构（完整企业级标准）

```
commons/
├── commons-dto/
├── commons-security/
├── commons-openapi/
├── commons-logging/                  ← 👉 你的核心模块
│   ├── pom.xml                       ← 依赖管理
│   └── src/
│       └── main/
│           ├── java/
│           │   └── io/urbane/commons/logging/
│           │       ├── MDCLoggingFilter.java      # 👉 核心：从 Header 注入 traceId/userId 到 MDC
│           │       ├── LogbackConfigurer.java     # 👉 核心：加载统一 logback-spring.xml
│           │       └── OpenTelemetryConfig.java   # 👉 可选：集成 OpenTelemetry 上下文传播
│           │
│           └── resources/
│               └── logback-spring.xml             # 👉 核心：统一日志格式、输出、滚动策略
│
└── ...
```

---

## ✅ 三、详细文件实现（带中文注释）

### ✅ 1️⃣ `pom.xml` —— 依赖管理（轻量、无干扰）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.urbane</groupId>
    <artifactId>commons-logging</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>urbane-commons-logging</name>
    <description>统一日志配置模块：提供标准 Logback 配置、MDC 上下文注入、OpenTelemetry 支持</description>

    <!-- 引入 Spring Boot 日志 Starter（默认使用 Logback） -->
    <dependencies>
        <!-- Spring Boot 默认日志框架 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-log4j2</artifactId>
            <!-- 注意：这里我们用 Log4j2 替代默认 Logback，性能更好，生产推荐 -->
            <!-- 如果坚持用 Logback，请改用 spring-boot-starter-logging -->
        </dependency>

        <!-- 若你决定使用 Logback（更常见），请用下面这行替代上面的 -->
        <!--
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </dependency>
        -->

        <!-- OpenTelemetry SDK（可选，用于分布式追踪） -->
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-sdk-extension-autoconfigure</artifactId>
            <version>1.37.0</version>
            <scope>runtime</scope>
        </dependency>

        <!-- 提供对 Spring Cloud Sleuth 的兼容支持（如需） -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
            <version>4.1.0</version>
            <scope>provided</scope> <!-- 由业务模块引入 -->
        </dependency>

        <!-- Lombok 简化代码（可选） -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <!-- 构建插件：确保打包时包含资源文件 -->
    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>false</filtering>
            </resource>
        </resources>

        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Implementation-Version>${project.version}</Implementation-Version>
                            <Built-By>${user.name}</Built-By>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

> ✅ **关键说明**：
> - 我们**不引入任何 Web 框架**，只封装日志能力
> - 使用 `spring-boot-starter-log4j2` 更稳定、高性能（生产推荐）
> - OpenTelemetry 为可选，避免强制依赖

---

### ✅ 2️⃣ `src/main/java/io/urbane/commons/logging/MDCLoggingFilter.java` —— 核心：注入上下文（最重要！）

```java
package io.urbane.commons.logging;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * MDC 日志上下文过滤器
 * 功能：
 *   - 在每个 HTTP 请求开始时，从请求头中提取 traceId 和 userId
 *   - 将它们注入到 SLF4J 的 MDC（Mapped Diagnostic Context）中
 *   - 确保所有日志输出自动携带这些字段
 *   - 请求结束时清理 MDC，防止线程池污染（重要！）
 *
 * 注意：
 *   - 必须在网关层或所有服务中注册此过滤器
 *   - 本类会被 commons-logging 模块自动加载，无需业务模块手动配置
 *   - 使用 OncePerRequestFilter 确保每个请求只执行一次
 */
@Component
public class MDCLoggingFilter extends OncePerRequestFilter implements Ordered {

    /**
     * 从请求头中获取 Trace ID
     * 优先级：X-Trace-ID > x-trace-id > trace-id > 自动生成
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-ID");
        if (traceId != null && !traceId.trim().isEmpty()) {
            return traceId.trim();
        }
        // 若无，则生成唯一 ID（适用于非网关直接访问）
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 从请求头中获取用户 ID
     * 通常由网关在 JWT 认证后注入 X-User-ID
     */
    private String getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        return userId != null ? userId : "unknown";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头提取上下文信息
        String traceId = getTraceId(request);
        String userId = getUserId(request);

        try {
            // 2. 设置到 MDC（Spring Boot 会自动在日志格式中使用）
            MDC.put("traceId", traceId);
            MDC.put("userId", userId);

            // 3. 继续执行后续过滤器和业务逻辑
            filterChain.doFilter(request, response);

        } finally {
            // 4. 清理 MDC，避免线程池复用导致内存泄漏或数据错乱（极其重要！）
            MDC.clear();
        }
    }

    /**
     * 设置过滤器优先级，确保在其他过滤器之前执行
     * 保证 MDC 在 Controller、Service、Repository 中都能拿到值
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级
    }
}
```

> ✅ **为什么必须 `MDC.clear()`？**
> - Tomcat/Jetty 使用线程池复用线程
> - 如果不清除，下一个请求可能看到上一个用户的 `userId`
> - 这会导致**严重的安全漏洞和审计混乱**

---

### ✅ 3️⃣ `src/main/resources/logback-spring.xml` —— 统一日志格式（核心！）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">

    <!-- ==================== 控制台输出 ==================== -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- 定义统一日志格式 -->
            <pattern>
                [%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level %logger{36} - [traceId=%X{traceId}, userId=%X{userId}] %msg%n
            </pattern>
            <!-- 输出编码 -->
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- ==================== 文件输出（按天滚动） ==================== -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 日志文件路径 -->
        <file>logs/urbane-commerce.log</file>

        <!-- 滚动策略：每天一个文件 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 文件名模式：按日期分文件 -->
            <fileNamePattern>logs/urbane-commerce.%d{yyyy-MM-dd}.log</fileNamePattern>
            <!-- 保留最近 30 天的日志 -->
            <maxHistory>30</maxHistory>
            <!-- 单个文件最大 500MB -->
            <totalSizeCap>50GB</totalSizeCap>
        </rollingPolicy>

        <!-- 编码器 -->
        <encoder>
            <pattern>
                [%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level %logger{36} - [traceId=%X{traceId}, userId=%X{userId}] %msg%n
            </pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- ==================== JSON 格式输出（用于 ELK）==================== -->
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app":"urbane-commerce","env":"${LOG_ENV:dev}"}</customFields>
            <includeCallerData>false</includeCallerData>
        </encoder>
    </appender>

    <!-- ==================== 日志级别控制 ==================== -->
    <!-- 生产环境建议设为 INFO -->
    <root level="INFO">
        <!-- 开发环境：控制台 + 文件 -->
        <springProfile name="dev">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </springProfile>

        <!-- 生产环境：JSON 格式 + 文件（便于 ELK 收集） -->
        <springProfile name="prod">
            <appender-ref ref="JSON_CONSOLE"/>
            <appender-ref ref="FILE"/>
        </springProfile>

        <!-- 测试环境：仅控制台 -->
        <springProfile name="test">
            <appender-ref ref="CONSOLE"/>
        </springProfile>
    </root>

    <!-- ==================== 特定包日志级别 ==================== -->
    <!-- Spring Boot 内部组件 -->
    <logger name="org.springframework" level="WARN"/>
    <logger name="org.hibernate" level="WARN"/>
    <logger name="com.zaxxer.hikari" level="WARN"/>

    <!-- 公共工具包 -->
    <logger name="io.urbane" level="INFO"/>

    <!-- 数据库 SQL 日志（开发调试用） -->
    <logger name="org.springframework.jdbc.core.JdbcTemplate" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>

</configuration>
```

> ✅ **关键特性说明**：

| 功能 | 说明 |
|------|------|
| ✅ **MDC 占位符 `%X{traceId}`** | 自动读取 `MDC.put("traceId", ...)` 的值 |
| ✅ **多环境支持** | 使用 `<springProfile>` 区分 dev/prod/test |
| ✅ **JSON 格式输出** | 生产环境使用 `LogstashEncoder`，直接对接 ELK/Kibana |
| ✅ **文件滚动** | 按天分割，自动归档，防磁盘爆满 |
| ✅ **线程安全** | MDC 是 ThreadLocal 实现，不会冲突 |
| ✅ **性能优化** | 不记录方法名、行号（`%M`, `%L`），减少开销 |

> ✅ **示例日志输出**：
> ```
> [2025-04-05 10:30:00.123] [http-nio-8080-exec-1] INFO  io.urbane.order.controller.OrderController - [traceId=a1b2c3d4, userId=123] 创建订单成功，订单号：ORD20250405123456
> ```

---

### ✅ 4️⃣ `src/main/java/io/urbane/commons/logging/OpenTelemetryConfig.java` —— 高级：集成 OpenTelemetry（可选）

如果你希望将日志与分布式追踪（Jaeger/Zipkin）深度集成，可启用此配置。

```java
package io.urbane.commons.logging;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.service.name:urbane-commerce}")
    private String serviceName;

    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    /**
     * 创建 OpenTelemetry SDK 实例，用于全局上下文传播
     * 功能：
     *   - 自动注入 SpanContext 到 MDC（使日志与 TraceID 对齐）
     *   - 支持跨进程、跨服务追踪
     *   - 与 Spring Cloud Sleuth 兼容
     */
    @Bean
    @Primary
    public OpenTelemetry openTelemetry() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.newBuilder(OtlpGrpcSpanExporter.builder()
                        .setEndpoint(otlpEndpoint)
                        .build())
                        .build())
                .setResource(Resource.getDefault()
                        .merge(Resource.create(
                                ResourceAttributes.SERVICE_NAME, serviceName
                        )))
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(io.opentelemetry.baggage.propagation.BaggagePropagator.getInstance()))
                .buildAndRegisterGlobal();
    }
}
```

> ✅ 使用方式：
> - 在 `application-prod.yml` 中配置：
    >   ```yaml
>   otel:
>     service.name: urbane-commerce-order
>     exporter.otlp.endpoint: http://jaeger-collector:4317
>   ```
> - 启动容器时挂载 Jaeger/Tempo 服务
> - 日志中自动包含 `trace_id`、`span_id`

> ⚠️ **注意**：这是**进阶功能**，小型项目可不启用。  
> 但一旦你上云、上 Kubernetes、做链路监控，它就是**必备项**。

---

## ✅ 四、如何让业务模块“零配置”使用？

### ✅ 步骤 1：在 `commons-logging/pom.xml` 中添加自动配置（关键！）

创建文件：

```
src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

内容：

```properties
io.urbane.commons.logging.MDCLoggingFilter
io.urbane.commons.logging.LogbackConfigurer
io.urbane.commons.logging.OpenTelemetryConfig
```

> ✅ 这样，**只要业务模块引入 `commons-logging`，就自动加载过滤器和日志配置，无需任何额外代码！**

---

### ✅ 步骤 2：业务模块只需引入依赖

```xml
<!-- order-service/pom.xml -->
<dependency>
    <groupId>io.urbane</groupId>
    <artifactId>commons-logging</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

✅ **然后呢？什么也不用干了！**

- 所有日志自动带 `traceId` 和 `userId`
- 所有服务使用统一格式
- 开发环境输出彩色控制台，生产环境输出 JSON 到 ELK
- 你甚至不需要写一行 `@Slf4j` —— Lombok 会帮你处理！

---

## ✅ 五、最终效果对比

| 场景 | 传统做法 | 使用 `commons-logging` |
|------|----------|------------------------|
| 新建一个微服务 | 复制 200 行 `logback-spring.xml`，写过滤器，配 MDC | 一行依赖，自动生效 |
| 查看订单日志 | 手动找 traceId，拼接多个服务日志 | 直接搜索 `traceId=a1b2c3`，全链路展示 |
| 排查线上故障 | 一堆无上下文的日志，无法定位 | 一眼看到 `userId=123, traceId=xxx`，秒级定位 |
| 前端反馈“我看不到日志” | “你们服务没打日志？” | “你搜一下 traceId，全链路都在” |
| 审计合规 | 无法证明某次操作是谁做的 | 每条日志都有 `userId`，满足 GDPR |

---

## ✅ 六、总结：为什么 `commons-logging` 是企业级标配？

| 维度 | 价值 |
|------|------|
| ✅ **标准化** | 全公司日志格式统一，杜绝混乱 |
| ✅ **可观测性** | 实现全链路追踪，告别“日志孤岛” |
| ✅ **效率提升** | 新人 5 分钟上线，不再花 2 天配