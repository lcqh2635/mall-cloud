在你这种 `user-api` + `user-biz` 的分层架构中，声明式 HTTP 客户端（`@HttpExchange` 接口）的落位有非常明确的业界最佳实践。核心原则是：
**契约与调用配置归 `api`，实现与运行环境归 `biz`，消费方按需激活**。

下面按模块职责、代码落位、注册方式、架构 rationale 为你完整拆解：

---

### 📐 一、模块职责划分（标准做法）

| 模块         | 职责定位               | 是否放 `@HttpExchange` 接口 | 是否放客户端注册配置            | 是否放 `@RestController` 实现 |
|------------|--------------------|------------------------|-----------------------|--------------------------|
| `user-api` | 对外契约、DTO、客户端代理定义   | ✅ 放                    | ✅ 放（或提供自动装配）          | ❌ 不放                     |
| `user-biz` | 业务逻辑、控制器、启动类       | ❌ 不放                   | ❌ 不放                  | ✅ 放（实现 `user-api` 暴露的接口） |
| 调用方服务      | 消费 `user-api` 发起请求 | ❌ 不重复定义                | ✅ 激活配置并注入 baseUrl/认证等 | ❌ 不放                     |

> 💡 **关键认知**：`user-biz` 是 **服务提供方（Provider）**，它实现的是 HTTP 接口（Controller），而不是调用自己的客户端。声明式客户端是
**服务消费方（Consumer）** 用来调用 `user-service` 的代理工具。

---

### 📦 二、具体代码落位示例

#### 1. `user-api` 模块（轻量级，通常只依赖 `spring-web`）

```text
user-api/
├── src/main/java/com/example/user/api/
│   ├── dto/
│   │   └── UserDto.java
│   ├── client/
│   │   └── UserHttpClient.java          // @HttpExchange 接口
│   └── config/
│       └── UserHttpClientAutoConfiguration.java // 注册配置（推荐）
```

**`UserHttpClient.java`**

```java

@HttpExchange("/api/v1/users")
public interface UserHttpClient {
    @GetExchange("/{id}")
    UserDto getUser(@PathVariable String id);

    @PostExchange(consumes = "application/json")
    UserDto createUser(@RequestBody UserDto user);
}
```

**`UserHttpClientAutoConfiguration.java`**（提供开箱即用的 Bean 注册）

```java

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "user.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserHttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserHttpClient userHttpClient(@Value("${user.client.base-url}") String baseUrl) {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(UserHttpClient.class);
    }
}
```

> 📌 注册 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（Spring Boot 3.2+
> 路径）实现自动装配，调用方只需引入依赖即可生效。

#### 2. `user-biz` 模块（完整 Spring Boot 应用）

```text
user-biz/
├── src/main/java/com/example/user/
│   ├── UserApplication.java
│   ├── controller/
│   │   └── UserController.java          // 实现 /api/v1/users 路由
│   └── service/
│       └── UserService.java
└── pom.xml (依赖 user-api)
```

`user-biz` **不注册任何 HTTP 客户端**，只负责用 `@RestController` 暴露接口，实现 `user-api` 中定义的契约。

#### 3. 调用方服务（如 `order-service`）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>user-api</artifactId>
</dependency>
```

```yaml
# application.yml
user:
  client:
    base-url: http://user-service:8080
```

直接注入使用：

```java

@Service
public class OrderService {
    private final UserHttpClient userClient;

    public OrderService(UserHttpClient userClient) {
        this.userClient = userClient;
    }
    // userClient.getUser("123") 即可调用
}
```

---

### 🧠 三、为什么这样设计？（架构原则）

1. **依赖方向清晰**：`user-biz` → `user-api`（实现依赖契约），`order-service` → `user-api`（调用依赖契约）。无循环依赖。
2. **轻量级复用**：其他微服务只需引入 `user-api`（通常 < 1MB），无需拉起整个 `spring-boot-starter-web` 或 `biz` 逻辑。
3. **配置解耦**：`baseUrl`、超时、鉴权等运行时参数由消费方环境决定，Provider 不感知调用方网络拓扑。
4. **AOT/原生友好**：`user-api` 仅含接口+DTO，无运行时反射负担，完美适配 Spring Boot 4 的 GraalVM 编译。

---

### 🛡 四、生产环境避坑指南

| 场景               | 推荐做法                                                              |
|------------------|-------------------------------------------------------------------|
| 多个服务调用同一 API     | 在 `user-api` 中提供 `@ConfigurationProperties` 统一封装超时/重试/拦截器         |
| 需要熔断/限流          | 客户端代理不绑定 Resilience4j，建议在消费方用 `@Service` 包装或切面统一拦截                |
| 灰度/多环境路由         | 通过 `RestClient.Builder` 动态替换 `baseUrl`，或使用服务发现（Consul/Nacos）集成    |
| 避免 Bean 冲突       | 注册类加 `@ConditionalOnMissingBean`，允许调用方自定义覆盖                       |
| OpenTelemetry 追踪 | 在 `user-api` 配置类中注入 `MicrometerTracing` 或 `OpenTelemetry` 拦截器统一注入 |

---

### 📌 总结：你的架构落位清单

```
user-api/
  ├── @HttpExchange 接口 ✅
  ├── DTO / 枚举 ✅
  └── 客户端自动配置类（注册 HttpServiceProxyFactory）✅

user-biz/
  ├── @RestController 实现 ✅
  ├── @Service / @Repository ✅
  └── Spring Boot 启动类 ✅
  ❌ 不放置任何声明式 HTTP 客户端注册代码

调用方服务/
  ├── 依赖 user-api ✅
  ├── 配置 user.client.base-url ✅
  └── @Autowired 注入使用 ✅
```

如果你希望我帮你生成一套 **完整的 `user-api` Starter 模板**（含 `AutoConfiguration`、`@ConfigurationProperties`
、统一拦截器、重试策略、OpenTelemetry 集成），或需要适配 **Nacos/Sentinel/虚拟线程** 的具体配置，告诉我你的技术栈偏好，我可直接输出可复制的工程结构。