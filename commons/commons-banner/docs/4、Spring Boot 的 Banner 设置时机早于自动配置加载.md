这是一个非常典型、也非常重要的 Spring Boot 自动配置加载顺序和机制问题！你遇到的现象：

> ✅ 手动 `app.setBanner(new CustomBanner())` → **生效**  
> ❌ 仅引入 `common-banner-starter` → **不生效**

根本原因是：

---

## 🚨 核心原因：**Spring Boot 的 Banner 设置时机早于自动配置加载**

---

### 🔍 详细解释：

在 Spring Boot 启动流程中：

1. **`SpringApplication` 实例创建时** → 会立即读取并设置 `Banner`（默认从 `banner.txt` 或 `Banner` Bean）。
2. **之后才执行 `ApplicationContext` 初始化** → 此时才会加载 `@Configuration` 类（包括你的 `BannerAutoConfiguration`）。
3. 所以：**自动配置类中定义的 `CustomBanner` Bean，在 Banner 阶段还没被创建！**

👉 也就是说，当 Spring Boot 要打印 Banner 时，你的 `BannerAutoConfiguration` 还没运行，`CustomBanner` Bean 尚未注册，自然不会生效！

---

## ✅ 解决方案

你有 **三种主流方案** 可选，推荐 **方案一（最优雅）**。

---

## ✅ 方案一：使用 `spring.main.banner-mode` + `Banner` Bean（推荐 ✅）

这是 Spring Boot 官方支持的“通过 Bean 注册 Banner”的方式。

### 步骤 1：确保你的 `CustomBanner` 是一个 Bean

```java
// BannerAutoConfiguration.java
@Configuration
@ConditionalOnProperty(prefix = "custom.banner", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BannerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Banner customBanner(Environment environment) { // ← 注意：返回类型是 org.springframework.boot.Banner
        return new CustomBanner(environment); // 确保 CustomBanner 实现了 Banner 接口
    }
}
```

> ✅ 关键点：**必须返回 `org.springframework.boot.Banner` 类型的 Bean！**

### 步骤 2：在业务模块的 `application.yml` 中启用 Banner Bean 模式

```yaml
spring:
  main:
    banner-mode: console # 或者 log（默认是 console）
```

> 📌 `banner-mode: console` 会强制 Spring Boot 从上下文查找 `Banner` 类型的 Bean 并使用它！

---

## ✅ 方案二：使用 `SpringApplicationBuilder` + `initializers`（适合 Starter）

在 Starter 中提供一个 `ApplicationContextInitializer`，在上下文刷新前设置 Banner。

### 创建 Initializer：

```java
// CommonBannerInitializer.java
package com.yourcompany.banner;

import com.yourcompany.config.CustomBanner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class CommonBannerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        applicationContext.setApplicationStartup(startup -> {
            // 设置 Banner（在 refresh 前）
            ((org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext) applicationContext)
                    .setBanner(new CustomBanner(env));
            return startup;
        });
    }
}
```

### 在 `spring.factories` 中注册：

```properties
# META-INF/spring.factories
org.springframework.context.ApplicationContextInitializer=\
com.yourcompany.banner.CommonBannerInitializer
```

> ⚠️ 此方案较复杂，且依赖具体 ApplicationContext 类型，不推荐作为首选。

---

## ✅ 方案三：在 Starter 中提供一个“工具类” + 主类模板（折中方案）

如果你暂时不想动自动配置机制，可以在 Starter 中提供一个工具方法：

```java
// BannerHelper.java
public class BannerHelper {
    public static void run(Class<?> primarySource, String[] args) {
        SpringApplication app = new SpringApplication(primarySource);
        app.setBanner(new CustomBanner());
        app.run(args);
    }
}
```

然后业务模块主类这样写：

```java
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        BannerHelper.run(OrderServiceApplication.class, args);
    }
}
```

> 👍 简单直接，但需要每个业务模块修改主类 → **不够自动化**。

---

## ✅ 最推荐方案：方案一（Banner Bean + banner-mode）

### 完整示例：

#### 1. `CustomBanner.java`（保持实现 `Banner` 接口）

```java
public class CustomBanner implements Banner {
    private final Environment environment;

    public CustomBanner(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void printBanner(Environment env, Class<?> sourceClass, PrintStream out) {
        // 你的动态 ASCII Art 逻辑
        String appName = environment.getProperty("spring.application.name", "APP").toUpperCase();
        String ascii = generateAsciiArt(appName);
        out.println(ascii);
    }

    private String generateAsciiArt(String text) {
        try {
            return FigletFont.convertOneLine(text, "small");
        } catch (Exception e) {
            return "🚀 " + text;
        }
    }
}
```

#### 2. `BannerAutoConfiguration.java`

```java
@Configuration
@ConditionalOnProperty(prefix = "custom.banner", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BannerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Banner customBanner(Environment environment) {
        return new CustomBanner(environment); // ← 返回类型必须是 Banner
    }
}
```

#### 3. 业务模块 `application.yml`

```yaml
spring:
  main:
    banner-mode: console # ← 关键！启用从 Bean 加载 Banner

custom:
  banner:
    enabled: true # 可选，默认 true
```

#### 4. 业务模块主类（无需任何修改！）

```java
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args); // ✅ 自动使用 CustomBanner
    }
}
```

---

## ✅ 验证是否生效

启动后，如果看到你的 ASCII Art，说明成功！

如果没看到：

1. 检查 `spring.main.banner-mode=console` 是否配置。
2. 检查 `CustomBanner` 是否被注册为 `Banner` 类型 Bean（可加日志或断点）。
3. 检查 `spring.factories` 是否正确加载自动配置类。
4. 检查 Starter 是否被正确引入（依赖 scope 是 compile）。

---

## 📌 Bonus：为什么手动 `setBanner()` 总是有效？

因为 `SpringApplication.setBanner()` 是在 Banner 解析阶段**之前**显式设置的，优先级最高，不受自动配置加载顺序影响。

---

## ✅ 总结

| 方案 | 是否推荐 | 说明 |
|------|----------|------|
| **Banner Bean + `banner-mode: console`** | ✅✅✅ 强烈推荐 | 最符合 Spring Boot 设计哲学，全自动，零侵入 |
| ApplicationContextInitializer | ⚠️ 可用但复杂 | 适合深度定制，但容易出错 |
| 工具类 + 修改主类 | 👍 简单直接 | 需要每个服务改主类，不够优雅 |

---

🎯 **请立即在你的 `common-banner-starter` 中采用方案一，只需加一行配置 `spring.main.banner-mode=console`，问题迎刃而解！**

需要我帮你检查当前 Starter 代码或生成完整可运行模板吗？欢迎贴出你的 `BannerAutoConfiguration` 和 `spring.factories` 😊