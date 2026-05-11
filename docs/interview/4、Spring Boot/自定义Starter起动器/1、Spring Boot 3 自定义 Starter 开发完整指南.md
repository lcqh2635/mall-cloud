当然可以！下面是一个**完整、详细、具有实际开发参考意义的Spring Boot 3自定义Starter开发示例**，适用于你当前的环境（Fedora + JDK21 + Spring Boot 3）。我们将创建一个名为 `my-spring-boot-starter-greeting` 的 Starter，它提供一个可配置的“问候语”服务，行为完全通过 `application.yaml` 控制。

---

# 🧩 Spring Boot 3 自定义 Starter 开发完整指南

## 📌 目标功能

- 提供一个 `GreetingService`，调用其 `sayHello(String name)` 方法可返回自定义问候语。
- 支持通过 `application.yaml` 配置前缀、后缀、是否启用、默认名称等。
- 自动装配：只要引入 Starter，无需手动配置，即可注入 `GreetingService`。
- 支持 `@ConfigurationProperties` 绑定配置。
- 支持 `@ConditionalOnProperty` 条件化启用。

---

## 🗂️ 项目结构规划

我们将创建一个 Maven 项目，结构如下：

```
my-spring-boot-starter-greeting/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── starter/
        │               ├── GreetingProperties.java        // 配置属性类
        │               ├── GreetingService.java          // 核心服务接口
        │               ├── GreetingServiceImpl.java      // 服务实现
        │               └── GreetingAutoConfiguration.java // 自动配置类
        └── resources/
            └── META-INF/
                └── spring/
                    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

> 💡 注意：Spring Boot 3 使用 `AutoConfiguration.imports` 文件替代了旧版的 `spring.factories`。

---

## 🧱 第一步：创建 Maven 项目

在终端中创建项目目录并初始化：

```bash
mkdir -p my-spring-boot-starter-greeting
cd my-spring-boot-starter-greeting
```

创建 `pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Starter 通常不继承 spring-boot-starter-parent -->
    <groupId>com.example</groupId>
    <artifactId>my-spring-boot-starter-greeting</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>My Spring Boot Starter Greeting</name>
    <description>自定义 Spring Boot Starter：可配置的问候语服务</description>

    <properties>
        <java.version>21</java.version>
        <spring-boot.version>3.3.0</spring-boot.version> <!-- 使用最新稳定版 -->
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- 必须依赖：Spring Boot 自动配置处理器（用于@ConfigurationProperties） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>

        <!-- 可选：如果你需要日志、条件装配等功能，可添加 spring-boot-starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring-boot.version}</version>
            <optional>true</optional>
        </dependency>

        <!-- 用于 @ConfigurationProperties 注解处理器（编译时生成元数据） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <version>${spring-boot.version}</version>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <!-- 指定 Spring Boot 版本管理（非继承 parent 时推荐） -->
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

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🧑‍💻 第二步：编写核心代码

### 1️⃣ GreetingService.java —— 服务接口

```java
package com.example.starter;

/**
 * 问候语服务接口
 * 用户可通过此接口获取自定义格式的问候语
 */
public interface GreetingService {

    /**
     * 根据传入的名称，返回格式化后的问候语
     * @param name 被问候的人名
     * @return 格式化后的问候字符串
     */
    String sayHello(String name);
}
```

---

### 2️⃣ GreetingServiceImpl.java —— 服务实现类

```java
package com.example.starter;

import org.springframework.stereotype.Component;

/**
 * GreetingService 的默认实现类
 * 使用 GreetingProperties 中的配置来定制问候语格式
 */
@Component
public class GreetingServiceImpl implements GreetingService {

    private final GreetingProperties properties;

    /**
     * 构造器注入配置属性
     */
    public GreetingServiceImpl(GreetingProperties properties) {
        this.properties = properties;
    }

    /**
     * 实现 sayHello 方法：
     * 根据配置拼接前缀 + 名字 + 后缀
     * 如果传入 name 为空，则使用默认名称
     */
    @Override
    public String sayHello(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = properties.getDefaultName();
        }
        return properties.getPrefix() + name + properties.getSuffix();
    }
}
```

---

### 3️⃣ GreetingProperties.java —— 配置属性绑定类

```java
package com.example.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用于绑定 application.yaml 中 my.greeting.* 配置项的属性类
 * 所有配置以 "my.greeting" 为前缀
 */
@ConfigurationProperties(prefix = "my.greeting")
public class GreetingProperties {

    /**
     * 是否启用此服务，默认为 true
     */
    private boolean enabled = true;

    /**
     * 问候语前缀，默认为 "Hello, "
     */
    private String prefix = "Hello, ";

    /**
     * 问候语后缀，默认为 "!"
     */
    private String suffix = "!";

    /**
     * 默认名称，当传入名称为空时使用，默认为 "Guest"
     */
    private String defaultName = "Guest";

    // ========== Getter & Setter ==========

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }
}
```

> ✅ `@ConfigurationProperties` 会自动将 `application.yaml` 中 `my.greeting.xxx` 映射到此类字段。

---

### 4️⃣ GreetingAutoConfiguration.java —— 自动配置类

```java
package com.example.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类 —— 核心！Spring Boot 启动时会加载此类
 * 条件：
 *   - 当配置 my.greeting.enabled=true（默认）时才生效
 *   - 如果用户未手动定义 GreetingService Bean，则自动创建默认实现
 */
@Configuration
@ConditionalOnProperty(
    prefix = "my.greeting",
    name = "enabled",
    havingValue = "true", // 只有当 my.greeting.enabled=true 时才装配
    matchIfMissing = true // 如果未配置，默认视为 true
)
@EnableConfigurationProperties(GreetingProperties.class) // 启用属性绑定
public class GreetingAutoConfiguration {

    /**
     * 自动创建 GreetingService Bean
     * @ConditionalOnMissingBean：如果容器中不存在此类型的 Bean，则创建
     */
    @Bean
    @ConditionalOnMissingBean
    public GreetingService greetingService(GreetingProperties properties) {
        return new GreetingServiceImpl(properties);
    }
}
```

---

## 📄 第三步：注册自动配置（Spring Boot 3 方式）

在 `src/main/resources/META-INF/spring/` 目录下创建文件：

```
src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

文件内容（只有一行）：

```
com.example.starter.GreetingAutoConfiguration
```

> ✅ 这是 Spring Boot 3 推荐的方式，替代了旧版 `spring.factories`。

---

## 📦 第四步：编译并安装到本地仓库

在项目根目录执行：

```bash
mvn clean install
```

成功后，你将在本地 Maven 仓库看到：

```
~/.m2/repository/com/example/my-spring-boot-starter-greeting/1.0.0/
```

---

## 🚀 第五步：创建测试项目验证 Starter

新建一个 Spring Boot 3 项目来测试你的 Starter：

### 创建测试项目：

```bash
mkdir test-greeting-app
cd test-greeting-app
```

### pom.xml：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>test-greeting-app</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 引入我们自定义的 Starter -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>my-spring-boot-starter-greeting</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 创建控制器测试：

`src/main/java/com/example/testgreetingapp/TestController.java`

```java
package com.example.testgreetingapp;

import com.example.starter.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final GreetingService greetingService;

    public TestController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(required = false) String name) {
        return greetingService.sayHello(name);
    }
}
```

### 配置 application.yaml：

`src/main/resources/application.yaml`

```yaml
# 自定义 Starter 配置
my:
  greeting:
    enabled: true
    prefix: "🎉 欢迎, "
    suffix: " 👋"
    default-name: "神秘访客"

# 可选：配置端口
server:
  port: 8081
```

---

## ▶️ 第六步：运行测试

在 `test-greeting-app` 目录下运行：

```bash
mvn spring-boot:run
```

访问：

```
http://localhost:8081/greet?name=张三
```

👉 输出：

```
🎉 欢迎, 张三 👋
```

访问：

```
http://localhost:8081/greet
```

👉 输出：

```
🎉 欢迎, 神秘访客 👋
```

---

## ✅ 功能验证清单

| 功能 | 是否实现 | 说明 |
|------|----------|------|
| 通过 YAML 配置前缀/后缀/默认名 | ✅ | `application.yaml` 控制 |
| 条件化启用/禁用服务 | ✅ | `my.greeting.enabled=false` 可关闭 |
| 自动装配无需手动配置 | ✅ | 引入依赖即自动注入 `GreetingService` |
| 支持用户自定义实现替换 | ✅ | 定义自己的 `GreetingService` Bean 即可覆盖 |
| 配置元数据支持（IDE提示） | ✅ | 因为引入了 `spring-boot-configuration-processor`，IDE 会有提示 |

---

## 🧩 高级技巧（可选）

### 1. 添加配置元数据提示（IDE友好）

编译后，在 `target/classes/META-INF/spring-configuration-metadata.json` 会自动生成配置提示。用户在 `application.yaml` 输入 `my.greeting.` 时，IDEA 或 VSCode 会有自动补全和文档提示。

### 2. 添加 Starter 文档注释

可在 `GreetingProperties` 字段上添加 `@Description`：

```java
@Description("问候语前缀，例如 'Hello, '")
private String prefix = "Hello, ";
```

需添加依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 📚 总结

你现在已经成功创建了一个：

- ✅ **符合 Spring Boot 3 规范** 的自定义 Starter
- ✅ **完全通过 `application.yaml` 配置驱动**
- ✅ **支持条件装配、属性绑定、自动注入**
- ✅ **可直接发布到 Maven 私服或中央仓库供团队复用**

这个 Starter 虽小，但五脏俱全，是学习和实际开发中非常标准的模板。

---

## 🧭 后续扩展建议

- 添加健康检查 `HealthIndicator`
- 添加指标监控 `MeterBinder`
- 支持多语言 i18n 问候语
- 添加 `@ConfigurationProperties` 校验（如 `@NotBlank`）
- 编写单元测试（使用 `@SpringBootTest` + `@TestConfiguration`）

---

如果你希望我帮你扩展上述任意功能（比如添加 Actuator 健康检查），欢迎继续提问！

祝你开发愉快！🚀  
—— 来自一位 Spring Boot 资深开发者