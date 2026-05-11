在 Spring Boot + Spring Cloud 微服务项目开发中，**Maven Profile** 是一套用于实现**环境差异化构建**的核心机制。它不是运行时配置（如
application.yml），而是**构建时**的环境隔离手段，用于控制编译、打包阶段引入的资源、依赖、插件行为，是保障“一次构建，多环境部署”可重复性与安全性的基石。

---

### 一、Maven Profile 是什么？

**Maven Profile** 是 Maven 提供的一种**构建配置集**。它允许你为不同环境（如 dev、test、prod）定义一组特定的属性、依赖、资源文件、插件配置等。当激活某个
Profile 时，Maven 会将该 Profile 中的配置合并到主 POM 中，覆盖或补充默认设置，从而生成适配该环境的构建产物（如 JAR/WAR）。

> ✅ 简单理解：**Profile 是“构建参数模板”，不同环境用不同模板打包，避免手动改配置导致的线上事故。**

---

### 二、Maven Profile 的核心作用

| 作用          | 说明                                                      |
|-------------|---------------------------------------------------------|
| **环境隔离**    | 避免将开发配置（如本地数据库）打包进生产镜像                                  |
| **资源替换**    | 自动替换 `application.yml` 中的占位符（如 `${db.url}`）为对应环境值       |
| **依赖控制**    | 某些环境禁用调试依赖（如 devtools），或引入监控依赖（如 actuator + prometheus） |
| **插件行为定制**  | 如：测试环境跳过单元测试，生产环境启用代码检查（Checkstyle、SpotBugs）            |
| **构建产物差异化** | 打包时生成不同名称或包含不同资源的 JAR（如 `myapp-prod.jar`）               |

> 🚫 **重要澄清**：Profile **不替代** Spring Boot 的 `application-{profile}.yml`，而是**为其提供构建时的上下文**
> 。两者配合使用，才是最佳实践。

---

### 三、典型使用场景（结合你的实际项目）

你正在开发**人身保险系统**，涉及老系统迁移、Feign 网关化、PowerJob 定时任务、Docker 日志监控等，以下场景高度相关：

| 场景              | 实际应用                                                     |
|-----------------|----------------------------------------------------------|
| **多环境部署**       | 开发（dev）、测试（test）、预发布（staging）、生产（prod）各自数据库、Redis、网关地址不同 |
| **日志级别控制**      | dev 环境输出 DEBUG，prod 环境仅输出 INFO/WARN，避免日志爆炸               |
| **外部配置隔离**      | 生产环境使用 Vault 或 Nacos 配置中心，开发环境使用本地文件                     |
| **第三方服务开关**     | 测试环境启用 Mock 服务（如 WireMock），生产环境调用真实 AI 外呼接口              |
| **构建优化**        | 生产构建时启用 ProGuard 混淆（Java 项目较少用，但可做资源压缩）、跳过测试             |
| **Docker 镜像构建** | 使用不同 Profile 打包不同 Dockerfile（如 dev 使用轻量镜像，prod 使用多阶段构建）  |

> 💡 你的 PowerJob 定时任务：**建议为每个环境配置独立的 Job 执行器地址和消息队列**，避免测试任务误触发生产外呼。

---

### 四、深度使用知识点与技巧（实战指南）

#### ✅ 1. Profile 定义位置（3种方式）

| 位置                        | 适用场景          | 推荐度   |
|---------------------------|---------------|-------|
| `pom.xml` 内部 `<profiles>` | 项目级配置，团队共享    | ⭐⭐⭐⭐⭐ |
| `~/.m2/settings.xml`      | 用户级配置（如私有镜像源） | ⭐⭐⭐⭐  |
| 外部 profile 文件（不推荐）        | 极少用，易出错       | ⭐     |

> ✅ **推荐做法**：所有环境配置写在 `pom.xml` 中，由 CI/CD 系统传参激活，确保构建可重现。

#### ✅ 2. 标准 Profile 结构示例（推荐结构）

```xml
<!-- pom.xml -->
<profiles>
    <!-- 开发环境 -->
    <profile>
        <id>dev</id>
        <properties>
            <spring.profiles.active>dev</spring.profiles.active>
            <db.url>jdbc:postgresql://localhost:5432/insurance_dev</db.url>
            <redis.host>127.0.0.1</redis.host>
            <gateway.url>http://localhost:8080/api</gateway.url>
            <log.level>DEBUG</log.level>
        </properties>
        <activation>
            <activeByDefault>true</activeByDefault> <!-- 默认激活 -->
        </activation>
        <build>
            <resources>
                <resource>
                    <directory>src/main/resources</directory>
                    <filtering>true</filtering> <!-- 启用占位符替换 -->
                </resource>
            </resources>
        </build>
    </profile>

    <!-- 生产环境 -->
    <profile>
        <id>prod</id>
        <properties>
            <spring.profiles.active>prod</spring.profiles.active>
            <db.url>jdbc:postgresql://pg-prod.company.com:5432/insurance_prod</db.url>
            <redis.host>redis-prod.company.com</redis.host>
            <gateway.url>https://api.company.com/gateway</gateway.url>
            <log.level>INFO</log.level>
            <skipTests>true</skipTests> <!-- 生产构建跳过测试 -->
        </properties>
        <build>
            <resources>
                <resource>
                    <directory>src/main/resources</directory>
                    <filtering>true</filtering>
                </resource>
            </resources>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-jar-plugin</artifactId>
                    <configuration>
                        <archive>
                            <manifestEntries>
                                <Build-Profile>prod</Build-Profile>
                            </manifestEntries>
                        </archive>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>

    <!-- 测试环境 -->
    <profile>
        <id>test</id>
        <properties>
            <spring.profiles.active>test</spring.profiles.active>
            <db.url>jdbc:postgresql://pg-test.company.com:5432/insurance_test</db.url>
            <redis.host>redis-test.company.com</redis.host>
            <gateway.url>https://api-test.company.com/gateway</gateway.url>
            <log.level>INFO</log.level>
            <mock.enabled>true</mock.enabled> <!-- 用于控制是否启用Mock -->
        </properties>
        <build>
            <resources>
                <resource>
                    <directory>src/main/resources</directory>
                    <filtering>true</filtering>
                </resource>
            </resources>
        </build>
    </profile>
</profiles>
```

#### ✅ 3. 资源过滤（Resource Filtering）——关键技巧

在 `src/main/resources` 下创建 `application.yml`：

```yaml
spring:
  datasource:
    url: ${db.url}
    username: ${db.username}
    password: ${db.password}
  redis:
    host: ${redis.host}
    port: 6379

logging:
  level:
    com.yourcompany: ${log.level}

powerjob:
  server-address: ${gateway.url}/powerjob
```

> ✅ **必须在 Profile 的 `<build><resources>` 中设置 `<filtering>true>`**，否则 `${}` 不会被替换！

#### ✅ 4. 激活方式（4种）

| 方式       | 命令/配置                                             | 适用场景                          |
|----------|---------------------------------------------------|-------------------------------|
| **命令行**  | `mvn clean package -Pprod`                        | CI/CD、手动构建                    |
| **IDEA** | Maven 工具栏 → Profiles → 选中 prod                    | 开发调试                          |
| **默认激活** | `<activeByDefault>true</activeByDefault>`         | 开发者本地默认环境                     |
| **系统属性** | `mvn clean package -Dspring.profiles.active=prod` | 与 Profile 配合使用，但不推荐替代 Profile |

> ⚠️ **注意**：`-Dspring.profiles.active=prod` 是 Spring Boot 启动参数，**不是 Maven Profile 激活方式**。两者必须**同时生效
**才完整：
> - Maven Profile → 替换配置 → 生成 `application-prod.yml`
> - Spring Boot 启动 → 读取 `spring.profiles.active=prod` → 加载该文件

#### ✅ 5. 最佳实践：Profile 命名规范（强烈推荐）

| 环境   | Profile ID | 说明                         |
|------|------------|----------------------------|
| 开发   | `dev`      | 本地开发，使用本地数据库、Mock 服务       |
| 测试   | `test`     | 测试环境，使用隔离数据库，启用 Mock       |
| 预发布  | `staging`  | 准生产环境，配置与 prod 尽量一致        |
| 生产   | `prod`     | 生产环境，禁止 DEBUG，禁用测试，启用监控    |
| 本地调试 | `local`    | 个人本地，不提交 Git，可配置 IDEA 启动参数 |

> ✅ **禁止使用**：`production`、`uat`、`env1` 等模糊名称。统一用 `dev/test/prod/staging`，团队无歧义。

#### ✅ 6. 安全规范：敏感信息绝不写入 POM

❌ 错误做法：

```xml

<properties>
    <db.password>123456</db.password> <!-- 绝对禁止！ -->
</properties>
```

✅ 正确做法：

- **数据库密码、API Key**：使用 **外部配置**（如 Kubernetes Secret、Vault、Nacos）
- **Maven 中只放地址、端口、开关**，不放密钥
- 在 CI/CD Pipeline 中通过 `-Ddb.password=${SECRET_DB_PASS}` 传入

#### ✅ 7. 与 Spring Boot 的协同最佳实践

| 目标             | 实现方式                                                                                 |
|----------------|--------------------------------------------------------------------------------------|
| **自动加载对应配置文件** | 在 `application.yml` 中设 `spring.profiles.active=@spring.profiles.active@`，Maven 替换后生效 |
| **多配置文件共存**    | `application-dev.yml`、`application-prod.yml` 与 Profile 一一对应                          |
| **避免配置遗漏**     | 在 `application.yml` 中添加默认值，防止未激活 Profile 时启动失败                                       |
| **配置校验**       | 使用 `@ConfigurationProperties` + `@Validated`，确保环境变量必填                                |

#### ✅ 8. 高级技巧：Profile 与 Docker 多阶段构建联动

在 `Dockerfile` 中：

```dockerfile
# 构建阶段
FROM maven:3.8.6-openjdk-25 AS builder
COPY ../.. /app
WORKDIR /app
RUN mvn clean package -Pprod -DskipTests

# 运行阶段
FROM eclipse-temurin:25-jre
COPY --from=builder /app/target/myapp.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

> ✅ CI/CD 中执行：`mvn clean package -P${ENV}`，然后构建对应镜像，实现**构建即部署**。

---

### 五、推荐遵循的规范（总结为 Checklist）

| 类别           | 规范                                                |
|--------------|---------------------------------------------------|
| **命名**       | 使用 `dev/test/staging/prod`，统一、简洁、无歧义              |
| **敏感信息**     | 绝不写密码、密钥到 `pom.xml`，使用外部 Secret 管理                |
| **资源过滤**     | 所有 `.yml/.properties` 必须配置 `<filtering>true>`     |
| **默认激活**     | `dev` 作为默认 Profile，确保本地开箱即用                       |
| **测试控制**     | `prod` Profile 中设置 `<skipTests>true>`，防止误打包       |
| **版本控制**     | `application-*.yml` 必须提交 Git，但 `settings.xml` 不提交 |
| **CI/CD 集成** | Jenkins/GitLab CI 中使用 `-P${CI_ENV}`，确保环境由系统控制     |
| **日志级别**     | `prod` 环境统一为 `INFO`，避免 DEBUG 泄露敏感字段               |
| **文档化**      | 在项目 `README.md` 中写明：`如何激活 Profile`、`各环境配置说明`      |

---

### 六、真实项目参考示例（你可用的模板）

#### ✅ `pom.xml` 片段（可直接复用）

```xml

<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <spring-boot.version>3.3.0</spring-boot.version>
    <spring.profiles.active>dev</spring.profiles.active> <!-- 默认值，会被 Profile 覆盖 -->
</properties>

<profiles>
<profile>
    <id>dev</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <spring.profiles.active>dev</spring.profiles.active>
        <db.url>jdbc:postgresql://localhost:5432/insurance_dev</db.url>
        <redis.host>localhost</redis.host>
        <gateway.url>http://localhost:8080/api</gateway.url>
        <log.level>DEBUG</log.level>
        <mock.enabled>false</mock.enabled>
    </properties>
    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
    </build>
</profile>

<profile>
    <id>prod</id>
    <properties>
        <spring.profiles.active>prod</spring.profiles.active>
        <db.url>jdbc:postgresql://pg-prod.company.com:5432/insurance_prod</db.url>
        <redis.host>redis-prod.company.com</redis.host>
        <gateway.url>https://api.company.com/gateway</gateway.url>
        <log.level>INFO</log.level>
        <skipTests>true</skipTests>
        <mock.enabled>false</mock.enabled>
    </properties>
    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Build-Profile>prod</Build-Profile>
                            <Build-Timestamp>${maven.build.timestamp}</Build-Timestamp>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
</profiles>
```

#### ✅ `application.yml` 示例（配合过滤）

```yaml
spring:
  datasource:
    url: ${db.url}
    username: ${db.username:insurance_user}
    password: ${db.password:secret}
  redis:
    host: ${redis.host}
    port: 6379
  application:
    name: insurance-core-service

logging:
  level:
    com.yourcompany: ${log.level}
    org.springframework: INFO
    org.hibernate: WARN

powerjob:
  server-address: ${gateway.url}/powerjob
  enabled: true
  mock-mode: ${mock.enabled}
```

#### ✅ CI/CD 示例（GitLab CI）

```yaml
deploy-prod:
  stage: deploy
  script:
    - mvn clean package -Pprod -DskipTests
    - docker build -t registry.company.com/insurance-core:latest .
    - docker push registry.company.com/insurance-core:latest
  only:
    - main
```

---

### 七、常见陷阱与避坑指南

| 陷阱                               | 解法                                             |
|----------------------------------|------------------------------------------------|
| ❌ `application.yml` 中 `${}` 没被替换 | 检查 `<filtering>true>` 是否配置                     |
| ❌ 打包后还是本地数据库地址                   | 检查是否误用 `-Dspring.profiles.active` 而非 `-Pprod`  |
| ❌ 生产环境启用了 devtools               | 在 `prod` Profile 中排除 `spring-boot-devtools` 依赖 |
| ❌ 多人本地 Profile 混乱                | 团队统一使用 `dev`，禁止自定义 Profile 名称                  |
| ❌ 用 Profile 管理数据库密码              | 改用 Vault/Nacos，Maven 只传地址                      |

---

### 八、总结：Maven Profile 的核心价值

> ✅ **Maven Profile = 构建时的“环境开关”**  
> ✅ **Spring Boot Profile = 运行时的“配置加载器”**  
> ✅ **二者配合 = 安全、可追溯、自动化、零人工干预的微服务发布流水线**

在你负责的**银行保险系统**中，使用规范的 Maven Profile，能极大降低因配置错配导致的核保失败、外呼重复、日志泄露等生产事故风险。

---

### 🔚 建议下一步行动

1. **在你的项目中**，将 `application.yml` 改为占位符形式
2. **新增 `dev` 和 `prod` Profile**，按上述模板配置
3. **在 IDEA 中**，打开 Maven 面板 → Profiles → 选 `prod` → 打包 → 查看 `target/classes/application.yml` 是否被正确替换
4. **在 CI/CD 中**，添加 `-Pprod` 参数，确保部署包与本地一致

> 📌 **你正在使用 IDEA 2025.2 + JDK 25 + Fedora + Podman**，这些环境完全支持该方案。建议将 Profile 配置纳入团队 Git
> 仓库，作为标准开发规范。

如需我为你生成一个**可直接复制的完整 `pom.xml` 示例模板**（含 Spring Boot + PostgreSQL + PowerJob 配置），请告诉我，我可立即为你定制。

# 自定义 Maven 资源过滤占位符

当然有。在实际企业级开发中，Maven Profile 的默认占位符 `${}` 虽然通用，但在某些场景下存在**语义冲突**或**安全风险**，例如：

- 项目中大量使用 Spring EL 表达式（如 `#{systemProperties['user.name']}`）或 Thymeleaf 模板语法（如 `[[${...}]]`），与 Maven
  的 `${}` 冲突；
- 配置文件中需要保留部分 `${}` 不被替换（如 Dockerfile 中的环境变量）；
- 安全审计要求避免使用标准符号，防止自动化脚本误解析敏感内容；
- 团队希望统一使用更清晰、更“醒目”的自定义符号，提升可读性与维护性。

---

### ✅ 解决方案：自定义 Maven 资源过滤占位符

Maven 的 `maven-resources-plugin` 支持通过 `<delimiters>` 配置**自定义占位符符号**，你可以将默认的 `${}` 替换为 `@...@`、
`#{...}#`、`%%...%%` 等任意符号，彻底避免与 Spring/Thymeleaf/Shell 等语法冲突。

---

## 📄 详细实战示例：自定义占位符的完整 `pom.xml`（含中文注释）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.yourbank.insurance</groupId>
    <artifactId>insurance-core-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>保险核心服务 - 人工核保系统</name>
    <description>银行保险系统中的人工核保服务，集成网关、PowerJob、PostgreSQL，支持多环境部署</description>

    <properties>
        <!-- ==================== 通用属性 ==================== -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <spring-boot.version>3.3.0</spring-boot.version>

        <!-- ==================== 自定义占位符配置 ==================== -->
        <!-- 使用 @...@ 作为 Maven 资源过滤的占位符，避免与 Spring EL #{...}、Thymeleaf [[${...}]] 冲突 -->
        <!-- 推荐：@db.url@、@redis.host@ 等，视觉清晰，不易误读 -->
        <maven.resources.delimiter>@</maven.resources.delimiter>

        <!-- ==================== 环境默认值（会被 Profile 覆盖）==================== -->
        <!-- 注意：此处为默认值，仅用于本地开发，生产环境由 CI/CD 注入 -->
        <spring.profiles.active>dev</spring.profiles.active>
        <db.url>@db.url@</db.url>
        <db.username>@db.username@</db.username>
        <db.password>@db.password@</db.password>
        <redis.host>@redis.host@</redis.host>
        <gateway.url>@gateway.url@</gateway.url>
        <log.level>@log.level@</log.level>
        <mock.enabled>@mock.enabled@</mock.enabled>
        <powerjob.server>@powerjob.server@</powerjob.server>
        <app.version>@project.version@</app.version> <!-- 使用 Maven 内置变量，仍用 @ 符号 -->
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.kagkarlsson</groupId>
            <artifactId>db-scheduler-spring-boot-starter</artifactId>
            <version>10.0.0</version>
        </dependency>
        <!-- PowerJob 依赖（定时任务） -->
        <dependency>
            <groupId>com.github.kaiser1990</groupId>
            <artifactId>powerjob-worker-spring-boot-starter</artifactId>
            <version>4.4.0</version>
        </dependency>
        <!-- 开发工具（仅开发环境启用） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <resources>
            <!-- ==================== 资源过滤配置（核心！）==================== -->
            <!-- 1. 为所有资源目录启用过滤，让 Maven 替换 @...@ 占位符 -->
            <!-- 2. 使用自定义分隔符 @，避免与 Spring EL #{...} 冲突 -->
            <!-- 3. filtering=true 表示启用变量替换 -->
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
                <!-- 自定义占位符符号：@ 开头，@ 结尾 -->
                <!-- 重要：必须与 properties 中的 maven.resources.delimiter 一致 -->
                <includes>
                    <include>**/*.yml</include>
                    <include>**/*.properties</include>
                    <include>**/*.json</include>
                    <include>**/*.conf</include>
                </includes>
            </resource>

            <!-- 2. 非过滤资源：保留原始内容，不替换任何符号 -->
            <!-- 例如：Dockerfile、K8s YAML、模板文件中包含的 ${} 需要保留 -->
            <resource>
                <directory>src/main/resources</directory>
                <filtering>false</filtering>
                <!-- 排除所有 .yml/.properties，只保留其他文件 -->
                <excludes>
                    <exclude>**/*.yml</exclude>
                    <exclude>**/*.properties</exclude>
                    <exclude>**/*.json</exclude>
                    <exclude>**/*.conf</exclude>
                </excludes>
            </resource>
        </resources>

        <!-- ==================== 插件配置：明确指定资源过滤器 ==================== -->
        <!-- 虽然默认插件会读取 maven.resources.delimiter，但显式声明更安全 -->
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <!-- 为构建的 JAR 添加构建信息，便于排查 -->
                    <addResources>true</addResources>
                </configuration>
            </plugin>

            <!-- ==================== 自定义资源插件（可选，增强控制）==================== -->
            <!-- Maven 默认资源插件已足够，但若需更复杂逻辑（如动态生成文件），可扩展 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.3.1</version>
                <configuration>
                    <!-- 关键：指定自定义分隔符，必须与 properties 中一致 -->
                    <delimiters>
                        <delimiter>@</delimiter>
                    </delimiters>
                    <!-- 是否使用默认分隔符 ${}，设为 false 避免冲突 -->
                    <useDefaultDelimiters>false</useDefaultDelimiters>
                    <!-- 编码 -->
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <!-- ==================== Maven Profiles：多环境配置 ==================== -->
    <profiles>
        <!-- 开发环境：默认激活 -->
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <!-- 使用 @...@ 格式，与资源过滤器一致 -->
                <spring.profiles.active>dev</spring.profiles.active>
                <db.url>jdbc:postgresql://localhost:5432/insurance_dev</db.url>
                <db.username>insurance_dev_user</db.username>
                <db.password>dev_password_123</db.password>
                <redis.host>localhost</redis.host>
                <gateway.url>http://localhost:8080/api</gateway.url>
                <log.level>DEBUG</log.level>
                <mock.enabled>true</mock.enabled>
                <powerjob.server>http://localhost:7700</powerjob.server>
                <!-- 保留 Maven 内置变量，仍用 @ 符号 -->
                <app.version>${project.version}</app.version>
            </properties>
        </profile>

        <!-- 测试环境 -->
        <profile>
            <id>test</id>
            <properties>
                <spring.profiles.active>test</spring.profiles.active>
                <db.url>jdbc:postgresql://pg-test.company.com:5432/insurance_test</db.url>
                <db.username>test_user</db.username>
                <db.password>test_pass</db.password>
                <redis.host>redis-test.company.com</redis.host>
                <gateway.url>https://api-test.company.com/gateway</gateway.url>
                <log.level>INFO</log.level>
                <mock.enabled>true</mock.enabled>
                <powerjob.server>https://powerjob-test.company.com</powerjob.server>
                <app.version>${project.version}</app.version>
            </properties>
        </profile>

        <!-- 生产环境 -->
        <profile>
            <id>prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
                <db.url>jdbc:postgresql://pg-prod.company.com:5432/insurance_prod</db.url>
                <db.username>prod_user</db.username>
                <!-- 生产密码不写入 pom.xml！此处仅为占位，实际由 CI/CD 注入 -->
                <db.password>@db.password@</db.password>
                <redis.host>redis-prod.company.com</redis.host>
                <gateway.url>https://api.company.com/gateway</gateway.url>
                <log.level>INFO</log.level>
                <mock.enabled>false</mock.enabled>
                <powerjob.server>https://powerjob-prod.company.com</powerjob.server>
                <app.version>${project.version}</app.version>
                <!-- 生产构建跳过测试 -->
                <skipTests>true</skipTests>
            </properties>
            <build>
                <plugins>
                    <!-- 生产构建时添加构建信息到 MANIFEST.MF -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-jar-plugin</artifactId>
                        <configuration>
                            <archive>
                                <manifestEntries>
                                    <Build-Profile>prod</Build-Profile>
                                    <Build-Timestamp>${maven.build.timestamp}</Build-Timestamp>
                                    <Build-Commit>${scm.revision}</Build-Commit>
                                </manifestEntries>
                            </archive>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

    <!-- ==================== SCM 信息（用于记录构建版本）==================== -->
    <scm>
        <connection>scm:git:https://github.com/yourcompany/insurance-core-service.git</connection>
        <developerConnection>scm:git:https://github.com/yourcompany/insurance-core-service.git</developerConnection>
        <url>https://github.com/yourcompany/insurance-core-service</url>
    </scm>

</project>
```

---

## 📚 详细文档说明：自定义占位符的原理与优势

### ✅ 1. 为什么使用 `@...@` 而不是 `${}`？

| 问题                           | 说明                                                                                 |
|------------------------------|------------------------------------------------------------------------------------|
| **Spring EL 冲突**             | `#{systemProperties['user.name']}` 是 Spring 的表达式，若 Maven 也用 `${}`，可能误解析为占位符，导致启动失败 |
| **Thymeleaf 模板冲突**           | `[[${user.name}]]` 是前端模板语法，若被 Maven 替换，页面渲染错误                                      |
| **Dockerfile / K8s YAML 冲突** | `ENV DB_URL=${DB_URL}` 是 Shell 环境变量语法，若被 Maven 替换，容器启动失败                           |
| **安全审计要求**                   | 某些金融系统要求“所有配置占位符必须非标准”，避免自动化脚本误读                                                   |
| **可读性提升**                    | `@db.url@` 更醒目，一眼识别为“需注入的配置项”，而非普通字符串                                              |

> ✅ **结论**：`@...@` 是 Java 企业级项目中最广泛采用的替代方案，已被 Spring Cloud、Netflix、阿里云等大量使用。

---

### ✅ 2. 如何确保自定义符号生效？

| 配置项                                                        | 作用                               | 必须配置？       |
|------------------------------------------------------------|----------------------------------|-------------|
| `<maven.resources.delimiter>@</maven.resources.delimiter>` | 设置全局占位符符号                        | ✅ 是         |
| `<delimiters><delimiter>@</delimiter></delimiters>`        | 在 `maven-resources-plugin` 中显式声明 | ✅ 强烈建议      |
| `<useDefaultDelimiters>false</useDefaultDelimiters>`       | 禁用默认的 `${}`                      | ✅ 必须，避免双重替换 |
| `<filtering>true</filtering>`                              | 启用资源过滤                           | ✅ 是         |

> ⚠️ 若未设置 `<useDefaultDelimiters>false>`，Maven 会同时处理 `${}` 和 `@...@`，可能导致部分配置被意外替换！

---

### ✅ 3. 实际开发中如何使用？

#### 📁 文件结构示例：

```
src/
└── main/
    └── resources/
        ├── application.yml             ← 被过滤，@db.url@ 会被替换
        ├── application-prod.yml        ← 被过滤，但生产密码留空
        ├── docker/Dockerfile           ← 不被过滤，保留 ${DB_URL}
        ├── k8s/deployment.yaml         ← 不被过滤，保留 {{ .Values.db.url }}
        └── templates/email-template.html ← 不被过滤，保留 {{user.name}}
```

#### 📄 `application.yml` 内容（过滤前）：

```yaml
spring:
  datasource:
    url: @db.url@
    username: @db.username@
    password: @db.password@
  redis:
    host: @redis.host@
    port: 6379

logging:
  level:
    com.yourbank: @log.level@

powerjob:
  server-address: @powerjob.server@
```

#### ✅ 执行命令后（`mvn clean package -Pprod`）：

生成的 `target/classes/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://pg-prod.company.com:5432/insurance_prod
    username: prod_user
    password: @db.password@          <!-- 保留原样！安全！ -->
  redis:
    host: redis-prod.company.com
    port: 6379

logging:
  level:
    com.yourbank: INFO

powerjob:
  server-address: https://powerjob-prod.company.com
```

> ✅ **关键点**：生产密码 `@db.password@` 未被替换 → **安全！**  
> ✅ 所有其他配置被正确注入 → **功能正常！**

---

### ✅ 4. CI/CD 中如何注入敏感信息？

在 GitLab CI / Jenkins 中：

```yaml
deploy-prod:
  script:
    - mvn clean package -Pprod -Ddb.password=$DB_PROD_PASSWORD
```

此时，Maven 会将 `$DB_PROD_PASSWORD` 的值注入到 `@db.password@`，即使你 POM 中写的是 `@db.password@`，也能动态填充。

> ✅ **优势**：敏感信息完全由 CI/CD 管理，**永不进入 Git 仓库**。

---

### ✅ 5. 与 Spring Boot 的协同建议

| 场景                      | 建议                                                                                       |
|-------------------------|------------------------------------------------------------------------------------------|
| **Spring 配置占位符**        | 使用 `#{systemProperties['xxx']}` 或 `@Value("#{systemProperties['xxx']}")`，**不与 @...@ 冲突** |
| **Thymeleaf 模板**        | 使用 `[[${...}]]`，Maven 不处理，安全                                                             |
| **Dockerfile 中的环境变量**   | 使用 `${VAR}`，但将 Dockerfile 放入 `src/main/docker/` 并设置 `<filtering>false>`                  |
| **K8s YAML 中的 Helm 模板** | 使用 `{{ .Values.xxx }}`，同样不启用过滤                                                           |

---

## ✅ 推荐遵循的规范（企业级标准）

| 类别         | 规范                                                                               |
|------------|----------------------------------------------------------------------------------|
| **占位符符号**  | 统一使用 `@...@`，禁止使用 `${}` 作为配置占位符                                                  |
| **敏感信息**   | 所有密码、密钥、Token 必须保留为 `@xxx@`，由 CI/CD 注入，**绝不写入代码库**                               |
| **资源过滤范围** | 仅对 `.yml`、`.properties`、`.json` 启用过滤，**其他文件一律关闭**                                |
| **IDE 配置** | 在 IDEA 中启用 “Maven → Importing → Use Maven wrapper” 和 “Enable auto-import”，确保同步配置 |
| **团队文档**   | 在 `CONTRIBUTING.md` 中注明：**“所有配置项必须使用 @...@ 格式，否则无法被构建系统识别”**                     |
| **构建验证**   | 在 CI/CD 中添加 `grep -r "@.*@" target/classes/`，确保生产包中无未替换占位符（安全审计）                 |
| **版本控制**   | `application-*.yml` 必须提交，**但 `settings.xml`、`~/.m2/` 不提交**                       |

---

## 🚀 最佳实践总结：你该怎么做？

| 步骤  | 操作                                                                                                                                        |
|-----|-------------------------------------------------------------------------------------------------------------------------------------------|
| 1️⃣ | 将你当前项目中所有 `application.yml` 中的 `${}` 替换为 `@...@`                                                                                          |
| 2️⃣ | 在 `pom.xml` 中添加 `<maven.resources.delimiter>@</maven.resources.delimiter>`                                                                |
| 3️⃣ | 在 `maven-resources-plugin` 中设置 `<delimiters><delimiter>@</delimiter></delimiters>` 和 `<useDefaultDelimiters>false</useDefaultDelimiters>` |
| 4️⃣ | 将 `src/main/resources` 中的 Dockerfile、K8s YAML、模板文件等，**设置为 `<filtering>false>`**                                                           |
| 5️⃣ | 在 CI/CD 中，用 `-Ddb.password=$SECRET_PASSWORD` 动态注入敏感值                                                                                      |
| 6️⃣ | 在团队 Wiki 中发布《Maven 资源过滤规范》，强制执行                                                                                                           |

---

## ✅ 附：验证是否生效的命令（开发时调试）

```bash
# 打包并查看替换结果
mvn clean package -Pdev
cat target/classes/application.yml

# 检查是否有残留 @ 符号（生产环境必须无）
grep -r "@.*@" target/classes/
# 若输出为空 → 成功！
# 若有输出 → 检查是否漏了过滤或未注入
```

---

> 💡 **你的项目背景（银行保险系统 + PowerJob + Docker + Podman）**：  
> 这种自定义占位符方案，**完美适配你的场景**：
> - 避免 AI 外呼接口地址被错误替换
> - 防止 Dockerfile 中的环境变量被 Maven 破坏
> - 满足金融系统安全审计对“配置隔离”和“非标准符号”的要求

**这是你团队迈向“生产级构建规范”的关键一步。**  
如需我为你生成一个**完整的 Spring Boot + PowerJob + PostgreSQL + Docker + CI/CD 的完整项目模板**（含所有文件），欢迎告诉我，我可立即为你定制。