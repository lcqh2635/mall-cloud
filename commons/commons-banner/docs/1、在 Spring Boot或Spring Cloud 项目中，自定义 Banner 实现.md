是的，完全可以！在 Spring Boot/Spring Cloud 项目中，你可以通过自定义 `Banner` 实现：在服务启动时，根据当前业务模块的信息（如服务名、版本、环境、端口等）动态打印横幅信息到控制台。

Spring Boot 原生支持自定义 Banner，你可以通过实现 `org.springframework.boot.Banner` 接口或使用 `banner.txt` + 占位符的方式。但为了实现“**根据当前业务模块信息动态生成**”，推荐使用 **编程式 Banner 实现**。

---

## ✅ 推荐方案：自定义 Banner 类 + 配置注入

### 步骤 1：创建自定义 Banner 类

```java
// src/main/java/com/yourcompany/config/CustomBanner.java
package com.yourcompany.config;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBanner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        // 获取服务相关信息
        String appName = environment.getProperty("spring.application.name", "Unknown-Service");
        String serverPort = environment.getProperty("server.port", "8080");
        String profiles = String.join(",", environment.getActiveProfiles());
        String version = environment.getProperty("info.app.version", "1.0.0");
        String springBootVersion = SpringBootVersion.getVersion();
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 构建横幅内容
        String bannerText = "\n" +
                "===============================================\n" +
                "  🚀 " + appName.toUpperCase() + " 启动成功！\n" +
                "===============================================\n" +
                "  服务名称    : " + appName + "\n" +
                "  服务端口    : " + serverPort + "\n" +
                "  激活环境    : " + (profiles.isEmpty() ? "default" : profiles) + "\n" +
                "  服务版本    : " + version + "\n" +
                "  Spring Boot : " + springBootVersion + "\n" +
                "  启动时间    : " + startTime + "\n" +
                "===============================================\n";

        // 输出到控制台
        out.println(bannerText);
    }
}
```

---

### 步骤 2：在主启动类中设置自定义 Banner

```java
// src/main/java/com/yourcompany/YourApplication.java
package com.yourcompany;

import com.yourcompany.config.CustomBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YourApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(YourApplication.class);
        app.setBanner(new CustomBanner()); // 设置自定义 Banner
        app.run(args);
    }
}
```

---

## ✅ 进阶：封装成 Starter 模块（可选）

如果你希望在多个微服务中复用这个功能，可以封装成一个 `starter`：

1. 新建一个 Maven 模块，比如 `common-banner-starter`
2. 添加依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```
3. 在 `resources/META-INF/spring.factories` 中自动配置：
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.yourcompany.banner.autoconfigure.BannerAutoConfiguration
```

4. 创建自动配置类：

```java
// BannerAutoConfiguration.java
package com.yourcompany.banner.autoconfigure;

import com.yourcompany.config.CustomBanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "custom.banner", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BannerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CustomBanner customBanner() {
        return new CustomBanner();
    }
}
```

5. 在业务模块中引入 starter：

```xml
<dependency>
    <groupId>com.yourcompany</groupId>
    <artifactId>common-banner-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

6. 在业务模块的 `application.yml` 中启用（可选）：

```yaml
custom:
  banner:
    enabled: true
```

7. 主类中无需手动设置，自动生效：

```java
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args); // 自动使用 CustomBanner
    }
}
```

---

## 🎯 输出效果示例

```
===============================================
  🚀 ORDER-SERVICE 启动成功！
===============================================
  服务名称    : order-service
  服务端口    : 8081
  激活环境    : dev
  服务版本    : 2.1.0
  Spring Boot : 3.2.5
  启动时间    : 2025-04-05 10:30:45
===============================================
```

---

## 💡 小贴士

- 你可以加入公司 Logo、ASCII Art、健康检查地址、Swagger 地址等。
- 可读取 `git.properties`（通过 `git-commit-id-plugin` 插件）显示 Git Commit 信息。
- 可结合 `@Value` 或 `@ConfigurationProperties` 注入更复杂的配置。
- 支持彩色输出（使用 ANSI 转义码），例如：

```java
String red = "\u001B[31m";
String reset = "\u001B[0m";
out.println(red + "错误信息" + reset);
```

---

## ✅ 总结

你完全可以封装一个通用模块，在 Spring Cloud 微服务启动时根据当前服务信息打印横幅。推荐使用 **自定义 Banner 类 + Starter 封装** 的方式，便于统一管理和复用。这样每个微服务启动时都会自动打印专属“名片”，提升运维体验和可读性！

如需更酷炫的 ASCII Banner，也可以结合 [http://patorjk.com/software/taag/](http://patorjk.com/software/taag/) 生成艺术字。

需要我帮你生成带 ASCII Logo 的版本吗？ 😊