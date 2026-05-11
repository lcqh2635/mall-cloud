非常好的问题！在开发 Spring Boot 3 自定义 Starter 的过程中，**正确使用注解是实现自动装配、配置绑定、条件化加载等功能的核心**。下面我将为你系统梳理 **Spring Boot 3 Starter 开发中最常用、最关键的注解**，并为每个注解提供**详细中文注释的使用示例**，帮助你深入理解其作用和实际应用场景。

---

# 🧩 Spring Boot 3 自定义 Starter 常用注解详解（附中文注释示例）

---

## ✅ 1. `@Configuration` —— 标记自动配置类

### 📌 作用：
标记一个类为 Spring 的配置类，等价于 XML 配置文件。在 Starter 中用于定义自动配置类，Spring Boot 启动时会加载该类中的 `@Bean` 方法。

### 🧩 示例：

```java
package com.example.starter;

import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类：Spring Boot 启动时会扫描并加载此类
 * 用于注册 Starter 中的核心 Bean（如 Service、工具类等）
 */
@Configuration
public class GreetingAutoConfiguration {

    // 此类中可通过 @Bean 方法注册组件
}
```

> 💡 注意：自动配置类必须在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中注册才能被加载。

---

## ✅ 2. `@Bean` —— 声明 Spring 管理的组件

### 📌 作用：
在配置类中声明一个方法，该方法的返回值将被 Spring 容器管理为一个 Bean。常用于注册服务、工具类等。

### 🧩 示例：

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GreetingAutoConfiguration {

    /**
     * 声明一个 GreetingService Bean
     * Spring 容器会自动调用此方法，并将返回对象注册为 Bean
     * 其他组件可通过 @Autowired 注入使用
     */
    @Bean
    public GreetingService greetingService() {
        return new GreetingServiceImpl();
    }
}
```

---

## ✅ 3. `@ConditionalOnProperty` —— 根据配置属性条件化加载

### 📌 作用：
根据 `application.yaml` 中的配置属性决定是否加载当前配置类或 Bean。是 Starter 实现“可开关”功能的核心注解。

### 🧩 示例：

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 仅当配置文件中 my.greeting.enabled = true 时才加载此类
 * matchIfMissing = true 表示：如果用户没配置，默认视为 true（即默认启用）
 */
@Configuration
@ConditionalOnProperty(
    prefix = "my.greeting",     // 配置前缀
    name = "enabled",           // 属性名
    havingValue = "true",       // 期望值
    matchIfMissing = true       // 未配置时是否匹配（默认启用）
)
public class GreetingAutoConfiguration {

    // 只有满足条件时，里面的 @Bean 才会生效
}
```

✅ 对应的 `application.yaml`：

```yaml
my:
  greeting:
    enabled: false  # 设置为 false 将禁用整个 Starter 功能
```

---

## ✅ 4. `@ConditionalOnMissingBean` —— 当容器缺少某 Bean 时才创建

### 📌 作用：
避免 Starter 默认 Bean 与用户自定义 Bean 冲突。**如果用户自己定义了同类型的 Bean，则跳过自动装配**，实现“可覆盖”机制。

### 🧩 示例：

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GreetingAutoConfiguration {

    /**
     * 仅当 Spring 容器中不存在 GreetingService 类型的 Bean 时，
     * 才创建并注册这个默认实现。
     * 用户可通过自定义 @Bean 覆盖此默认行为。
     */
    @Bean
    @ConditionalOnMissingBean  // 如果用户自己写了 GreetingService，这个就不创建
    public GreetingService greetingService() {
        return new GreetingServiceImpl();
    }
}
```

✅ 用户自定义覆盖示例：

```java
@Component
public class MyCustomGreetingService implements GreetingService {
    @Override
    public String sayHello(String name) {
        return "Custom: Hi " + name + "!";
    }
}
// 此时 Starter 中的默认 GreetingServiceImpl 将不会被创建
```

---

## ✅ 5. `@ConfigurationProperties` —— 绑定 YAML 配置到 Java 对象

### 📌 作用：
将 `application.yaml` 中的配置属性自动绑定到 Java Bean 的字段上，支持类型转换、校验、IDE 提示（配合 `spring-boot-configuration-processor`）。

### 🧩 示例：

```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置属性类：用于绑定 application.yaml 中 my.greeting.* 的配置
 * prefix = "my.greeting" 表示绑定前缀
 */
@ConfigurationProperties(prefix = "my.greeting")
@Component  // 也可以不加 @Component，而在 @EnableConfigurationProperties 中注册
public class GreetingProperties {

    /**
     * 是否启用问候服务，默认 true
     */
    private boolean enabled = true;

    /**
     * 问候语前缀，默认 "Hello, "
     */
    private String prefix = "Hello, ";

    /**
     * 问候语后缀，默认 "!"
     */
    private String suffix = "!";

    // 必须提供 getter/setter，否则绑定失败
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
}
```

✅ 对应的 `application.yaml`：

```yaml
my:
  greeting:
    prefix: "🎉 欢迎, "
    suffix: " 👋"
```

---

## ✅ 6. `@EnableConfigurationProperties` —— 启用配置属性绑定

### 📌 作用：
在自动配置类上使用，**显式启用某个 `@ConfigurationProperties` 类的绑定功能**。如果不使用 `@Component`，则必须通过此注解注册。

### 🧩 示例：

```java
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GreetingProperties.class) // 启用 GreetingProperties 的绑定
public class GreetingAutoConfiguration {

    // 可在 @Bean 方法中注入 GreetingProperties 使用
    @Bean
    public GreetingService greetingService(GreetingProperties properties) {
        return new GreetingServiceImpl(properties);
    }
}
```

> ✅ 如果 `GreetingProperties` 上加了 `@Component`，则可以省略 `@EnableConfigurationProperties`，但官方推荐在自动配置类中显式声明，结构更清晰。

---

## ✅ 7. `@ConditionalOnClass` / `@ConditionalOnMissingClass` —— 根据类是否存在条件加载

### 📌 作用：
- `@ConditionalOnClass`：当类路径中存在指定类时才加载配置。
- `@ConditionalOnMissingClass`：当类路径中不存在指定类时才加载配置。

常用于 Starter 依赖可选库（如 Redis、MQ 等）时的条件装配。

### 🧩 示例：

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * 仅当项目中引入了 Redis 相关类时，才加载此配置
 * 避免在无 Redis 依赖的项目中报错
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisGreetingAutoConfiguration {

    // 只有项目中包含 spring-data-redis 时，此配置才生效
}
```

---

## ✅ 8. `@AutoConfigureBefore` / `@AutoConfigureAfter` —— 控制自动配置加载顺序

### 📌 作用：
控制当前自动配置类与其他配置类的加载顺序。例如，你的 Starter 依赖某个 Bean，必须在其之后加载。

### 🧩 示例：

```java
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 指定当前配置必须在 WebMvcAutoConfiguration 之前加载
 * 例如：你需要提前注册拦截器或视图控制器
 */
@Configuration
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class MyWebAutoConfiguration {

}
```

---

## ✅ 9. `@Import` —— 导入其他配置类

### 📌 作用：
在当前配置类中导入其他配置类，实现模块化配置组合。

### 🧩 示例：

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DatabaseConfig.class, SecurityConfig.class}) // 导入多个配置类
public class MyStarterAutoConfiguration {

}
```

---

## ✅ 10. `@Order` —— 定义配置类或 Bean 的加载/执行顺序

### 📌 作用：
定义配置类、拦截器、过滤器等组件的执行顺序，值越小优先级越高。

### 🧩 示例：

```java
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;

@Configuration
@Order(1) // 优先级最高
public class HighPriorityConfig {

}

@Configuration
@Order(2)
public class LowPriorityConfig {

}
```

---

## ✅ 11. `@DependsOn` —— 强制指定 Bean 依赖顺序

### 📌 作用：
强制指定某个 Bean 必须在另一个 Bean 初始化之后再初始化。

### 🧩 示例：

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class AppConfig {

    @Bean
    public DatabaseInitializer databaseInitializer() {
        return new DatabaseInitializer();
    }

    /**
     * 强制要求 greetingService 必须在 databaseInitializer 初始化之后再创建
     * 例如：greetingService 需要从数据库加载默认配置
     */
    @Bean
    @DependsOn("databaseInitializer")
    public GreetingService greetingService() {
        return new GreetingServiceImpl();
    }
}
```

---

## ✅ 12. `@Description`（配合 `spring-boot-configuration-processor`）—— 为配置属性添加说明

### 📌 作用：
为 `@ConfigurationProperties` 类的字段添加描述，**编译后生成元数据，IDE 可显示提示和说明**。

### 🧩 示例：

```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.Description;

@ConfigurationProperties(prefix = "my.greeting")
public class GreetingProperties {

    @Description("是否启用问候语服务。设为 false 可完全关闭此功能。")
    private boolean enabled = true;

    @Description("问候语前缀，例如 'Hello, ' 或 '您好，'")
    private String prefix = "Hello, ";

    @Description("问候语后缀，例如 '!' 或 ' 👋'")
    private String suffix = "!";

    // getter/setter 省略...
}
```

> ✅ 编译后，在 `target/classes/META-INF/spring-configuration-metadata.json` 中会包含描述信息，IDE 输入配置时会显示提示。

---

# 🧭 总结表格：Spring Boot 3 Starter 常用注解速查

| 注解 | 用途 | 是否必需 | 示例场景 |
|------|------|----------|----------|
| `@Configuration` | 声明自动配置类 | ✅ 必需 | 所有 Starter 配置类 |
| `@Bean` | 注册组件到容器 | ✅ 必需 | 注册 Service、Util 等 |
| `@ConditionalOnProperty` | 根据配置开关功能 | ⭐ 强烈推荐 | 实现 starter.enabled=false |
| `@ConditionalOnMissingBean` | 避免 Bean 冲突 | ⭐ 强烈推荐 | 允许用户自定义覆盖 |
| `@ConfigurationProperties` | 绑定 YAML 配置 | ✅ 必需（如需配置） | 自定义前缀/后缀/开关等 |
| `@NestedConfigurationProperty` | 标记为嵌套配置 | ✅ 推荐（结构清晰） | 与 @ConfigurationProperties 配合 |
| `@EnableConfigurationProperties` | 启用属性绑定 | ✅ 推荐（结构清晰） | 与 @ConfigurationProperties 配合 |
| `@ConditionalOnClass` | 按依赖条件加载 | ⭐ 按需使用 | Starter 依赖可选库时 |
| `@AutoConfigureBefore/After` | 控制加载顺序 | ⭐ 按需使用 | 依赖其他 Starter 时 |
| `@Import` | 导入其他配置 | ⭐ 按需使用 | 模块化配置拆分 |
| `@Order` | 定义执行顺序 | ⭐ 按需使用 | 过滤器、拦截器排序 |
| `@DependsOn` | 强制 Bean 依赖顺序 | ⭐ 按需使用 | 初始化顺序敏感场景 |
| `@Description` | 配置属性说明 | ✅ 推荐（提升体验） | 生成 IDE 配置提示 |

---

## 💡 开发建议：

- **始终使用 `@ConditionalOnMissingBean`** —— 让用户能轻松覆盖你的默认实现。
- **使用 `@ConditionalOnProperty` 提供开关** —— 提升 Starter 的灵活性。
- **添加 `@Description` + `spring-boot-configuration-processor`** —— 极大提升用户体验。
- **避免在 Starter 中使用 `@ComponentScan`** —— 应通过 `@Bean` 精确控制注册。

---

✅ 掌握以上注解，你就可以开发出**专业、健壮、用户友好**的 Spring Boot 3 自定义 Starter！

如需我为你扩展某个注解的实战场景（比如结合 Redis、数据库、AOP 等），欢迎继续提问！

祝你编码愉快，成为 Starter 大师！🚀