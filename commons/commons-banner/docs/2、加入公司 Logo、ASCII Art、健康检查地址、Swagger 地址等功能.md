非常好！在你已有自定义 `Banner` 的基础上，我们可以进一步增强横幅信息，加入：

✅ 公司 Logo（ASCII Art）  
✅ 健康检查地址（`/actuator/health`）  
✅ Swagger 文档地址（如 `/swagger-ui.html` 或 `/swagger-ui/index.html`）  
✅ 支持彩色输出（ANSI 颜色）  
✅ 自动识别当前服务 IP（可选）

---

## ✅ 最终效果预览（带颜色 + Logo + 地址）

```
===============================================
  🚀 ORDER-SERVICE 启动成功！
===============================================
   ____              _    _ _     _       _   
  / ___| _   _ _ __ | | _(_) |__ | | ___ | |_ 
  \___ \| | | | '_ \| |/ / | '_ \| |/ _ \| __|
   ___) | |_| | | | |   <| | |_) | | (_) | |_ 
  |____/ \__,_|_| |_|_|\_\_|_.__/|_|\___/ \__|
  【XX科技有限公司】微服务启动成功！
===============================================
  服务名称    : 💼 order-service
  服务端口    : 🌐 8081
  服务IP      : 📍 192.168.1.100
  激活环境    : 🧪 dev
  服务版本    : 🏷️  2.1.0
  Git提交     : 📦 a1b2c3d
  Spring Boot : 🌱 3.2.5
  启动时间    : 🕙 2025-04-05 10:30:45
  健康检查    : 🩺 http://192.168.1.100:8081/actuator/health
  Swagger文档 : 📚 http://192.168.1.100:8081/swagger-ui.html
===============================================
```

---

## ✅ 步骤 1：增强 CustomBanner 类（支持 Logo + 地址 + 颜色）

```java
// src/main/java/com/yourcompany/config/CustomBanner.java
package com.yourcompany.config;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.util.SocketUtils;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBanner implements Banner {

    // ANSI 颜色代码（可选，控制台支持才有效）
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";

    // ASCII Logo（替换成你公司的）
    private static final String LOGO = """
             ____              _    _ _     _       _   
            / ___| _   _ _ __ | | _(_) |__ | | ___ | |_ 
            \\___ \\| | | | '_ \\| |/ / | '_ \\| |/ _ \\| __|
             ___) | |_| | | | |   <| | |_) | | (_) | |_ 
            |____/ \\__,_|_| |_|_|\\_\\_|_.__/|_|\\___/ \\__|
            """;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        try {
            // 获取服务信息
            String appName = environment.getProperty("spring.application.name", "Unknown-Service");
            String serverPort = environment.getProperty("server.port", "8080");
            String profiles = String.join(",", environment.getActiveProfiles());
            String version = environment.getProperty("info.app.version", "1.0.0");
            String gitCommit = environment.getProperty("git.commit.id.abbrev", "unknown");
            String springBootVersion = SpringBootVersion.getVersion();
            String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 获取本机 IP（可选，避免 localhost）
            String hostAddress = getHostAddress();
            String baseUrl = "http://" + hostAddress + ":" + serverPort;

            // 构建健康检查和 Swagger 地址
            String healthUrl = baseUrl + "/actuator/health";
            String swaggerUrl = baseUrl + (isSwaggerV3(environment) ? "/swagger-ui/index.html" : "/swagger-ui.html");

            // 打印彩色横幅（如果控制台不支持颜色，可去掉 ANSI 代码）
            out.println(GREEN + "===============================================" + RESET);
            out.println(CYAN + "  🚀 " + appName.toUpperCase() + " 启动成功！" + RESET);
            out.println(GREEN + "===============================================" + RESET);
            out.println(YELLOW + LOGO + RESET);
            out.println(BLUE + "  【XX科技有限公司】微服务启动成功！" + RESET);
            out.println(GREEN + "===============================================" + RESET);
            out.println("  服务名称    : " + MAGENTA + "💼 " + appName + RESET);
            out.println("  服务端口    : " + CYAN + "🌐 " + serverPort + RESET);
            out.println("  服务IP      : " + YELLOW + "📍 " + hostAddress + RESET);
            out.println("  激活环境    : " + BLUE + "🧪 " + (profiles.isEmpty() ? "default" : profiles) + RESET);
            out.println("  服务版本    : " + GREEN + "🏷️  " + version + RESET);
            out.println("  Git提交     : " + MAGENTA + "📦 " + gitCommit + RESET);
            out.println("  Spring Boot : " + CYAN + "🌱 " + springBootVersion + RESET);
            out.println("  启动时间    : " + YELLOW + "🕙 " + startTime + RESET);
            out.println("  健康检查    : " + BLUE + "🩺 " + healthUrl + RESET);
            out.println("  Swagger文档 : " + GREEN + "📚 " + swaggerUrl + RESET);
            out.println(GREEN + "===============================================" + RESET);

        } catch (Exception e) {
            out.println("⚠️  横幅打印异常: " + e.getMessage());
        }
    }

    // 获取本机 IP（排除回环地址）
    private String getHostAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            if (address.isLoopbackAddress()) {
                // 尝试获取非回环地址
                return InetAddress.getByName(SocketUtils.findFirstNonLoopbackAddress().getHostAddress()).getHostAddress();
            }
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    // 判断是否是 Swagger V3（SpringDoc）
    private boolean isSwaggerV3(Environment environment) {
        // 可根据是否引入 springdoc 来判断，或配置开关
        return environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false) ||
               environment.containsProperty("springdoc.swagger-ui.path");
    }
}
```

---

## ✅ 步骤 2：确保项目支持 Git 信息（可选）

在 `pom.xml` 中添加插件，以便在 Banner 中显示 Git Commit：

```xml
<plugin>
    <groupId>io.github.git-commit-id</groupId>
    <artifactId>git-commit-id-maven-plugin</artifactId>
    <version>7.0.0</version>
    <executions>
        <execution>
            <id>get-the-git-infos</id>
            <goals>
                <goal>revision</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <generateGitPropertiesFile>true</generateGitPropertiesFile>
        <generateGitPropertiesFilename>${project.build.outputDirectory}/git.properties</generateGitPropertiesFilename>
        <format>properties</format>
    </configuration>
</plugin>
```

构建后会生成 `target/classes/git.properties`，Spring Boot 会自动加载。

---

## ✅ 步骤 3：确保 Actuator 和 Swagger 已配置

### Actuator（健康检查）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

### Swagger（任选其一）

#### ➤ SpringFox（旧版，不推荐新项目）

```yaml
springfox:
  documentation:
    swagger-ui:
      enabled: true
```

#### ➤ SpringDoc OpenAPI（推荐）

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
```

---

## ✅ 步骤 4（可选）：封装成 Starter 时自动识别 Swagger 类型

在 `BannerAutoConfiguration` 中注入 `Environment`，传入 `CustomBanner`：

```java
@Bean
@ConditionalOnMissingBean
public CustomBanner customBanner(Environment environment) {
    return new CustomBanner(environment); // 修改构造器接收 Environment
}
```

然后在 `CustomBanner` 中添加字段：

```java
private final Environment environment;

public CustomBanner(Environment environment) {
    this.environment = environment;
}

// 在 printBanner 中使用 this.environment 替代参数 environment
@Override
public void printBanner(Environment env, Class<?> sourceClass, PrintStream out) {
    // 使用 this.environment
    ...
}
```

---

## ✅ 效果增强建议

- 使用 [patorjk.com](http://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=MYAPP) 生成更炫酷的 ASCII Logo。
- 加入公司 Slogan、支持电话、文档链接等。
- 根据环境（prod/dev）显示不同颜色或警告信息。
- 在 Kubernetes 环境中，可读取 Pod IP 替代本地 IP。

---

## ✅ 彩色输出兼容性说明

- IDEA 控制台、Linux/macOS Terminal、Windows Terminal 均支持 ANSI 颜色。
- 旧版 CMD 可能不支持，可通过设置 `spring.output.ansi.enabled=ALWAYS` 强制开启：

```yaml
spring:
  output:
    ansi:
      enabled: ALWAYS
```

---

## ✅ 最终目录结构（Starter 模式）

```
common-banner-starter
├── src/main/java
│   └── com/yourcompany/banner
│       ├── config/CustomBanner.java
│       └── autoconfigure/BannerAutoConfiguration.java
├── src/main/resources
│   └── META-INF/spring.factories
└── pom.xml
```

---

## ✅ 总结

你现在拥有了一个功能完整、企业级的 Spring Boot 启动横幅系统，支持：

- 动态服务信息（名称、端口、环境、版本）
- 公司 ASCII Logo
- 健康检查 & Swagger 地址自动拼接
- 彩色输出（提升视觉体验）
- Git 提交信息（便于追踪）
- 自动获取 IP（便于外部访问）

🌟 **运维人员和开发人员都会爱上这个功能！**

---

需要我帮你生成一个你公司专属的 ASCII Logo 吗？只需告诉我公司英文名或缩写 😊  
例如：输入 “ACME Corp” → 输出艺术字 Logo + 集成代码。