在 Spring 生态中，**“HTTP 客户端接口”**通常指的是 **声明式 HTTP 客户端（Declarative HTTP Client）**。它的核心思想是：*
*你只需定义一个 Java 接口，用注解描述请求路径、方法、参数和返回值，框架会自动生成代理实现，无需手写模板代码。**

下面我按现代 Spring 技术栈（Spring Framework 6.1+ / Spring Boot 3.2~4.x）为你系统梳理：

---

### 📦 一、Spring 官方推荐方案：`@HttpExchange` 注解族

这是 Spring 6.0 引入、Boot 3.2+ 深度集成的原生声明式 HTTP 客户端，定位上替代了 OpenFeign / Retrofit 的部分场景，底层可插拔
`RestClient`（同步）或 `WebClient`（响应式）。

#### 1. 定义接口

```java
import org.springframework.web.service.annotation.*;
import org.springframework.web.bind.annotation.*;

@HttpExchange(url = "/api/v1/users")
public interface UserHttpClient {

    @GetExchange("/{id}")
    UserDto getUser(@PathVariable String id);

    @PostExchange(consumes = "application/json")
    UserDto createUser(@RequestBody UserDto user);

    @PutExchange("/{id}")
    ResponseEntity<Void> updateUser(@PathVariable String id, @RequestBody UserDto user);

    @DeleteExchange("/{id}")
    void deleteUser(@PathVariable String id);

    // 支持自定义 Header、超时、响应类型映射
    @GetExchange("/search")
    List<UserDto> search(
            @RequestParam("name") String name,
            @RequestHeader("X-Trace-Id") String traceId
    );
}
```

#### 2. 注册为 Spring Bean（Boot 3.3+ 已支持自动扫描，也可手动配置）

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.invoker.RestClientAdapter;

@Configuration
public class HttpClientConfig {

    @Bean
    public UserHttpClient userHttpClient() {
        // 底层使用 RestClient（同步阻塞，适合 90% 业务场景）
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.example.com")
                .requestFactory(new JdkClientHttpRequestFactory()) // 支持 HTTP/2 & 虚拟线程
                .defaultHeader("Accept", "application/json")
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(UserHttpClient.class);
    }
}
```

#### 3. 直接注入使用

```java

@Service
public class UserService {
    private final UserHttpClient userClient;

    public UserService(UserHttpClient userClient) {
        this.userClient = userClient;
    }

    public UserDto fetchUser(String id) {
        return userClient.getUser(id); // 像调用本地方法一样
    }
}
```

---

### 🔍 二、底层执行器选型对比

`@HttpExchange` 本身是“协议层”，具体网络请求由底层 Adapter 决定：

| 底层实现                | 适用场景           | 特点                                                           |
|---------------------|----------------|--------------------------------------------------------------|
| `RestClientAdapter` | 常规同步调用、CRUD 服务 | 阻塞式、API 简洁、默认连接池、Boot 4 默认推荐                                 |
| `WebClientAdapter`  | 高并发、流式响应、事件驱动  | 非阻塞响应式、支持背压、学习曲线略陡、适合 WebFlux 项目                             |
| `JDK HttpClient`    | 需要 HTTP/2 原生特性 | Java 11+ 内置，Spring 6.1+ 已提供 `JdkClientHttpRequestFactory` 支持 |

> 💡 **Spring Boot 4 趋势**：官方将 `RestClient + @HttpExchange` 作为同步场景的**事实标准**。虚拟线程（Virtual
> Threads）普及后，同步代码的并发瓶颈被打破，无需盲目转向响应式。

---

### 🛠 三、企业级最佳实践（避坑指南）

1. **超时配置必须显式声明**
   ```java
   RestClient restClient = RestClient.builder()
       .requestFactory(new JdkClientHttpRequestFactory(java.time.Duration.ofSeconds(5)))
       .build();
   ```
2. **统一拦截器（日志/鉴权/链路追踪）**
   ```java
   .requestInterceptor(new ExchangeFunction() {
       @Override
       public ClientResponse exchange(ClientRequest request) {
           ClientRequest traced = ClientRequest.from(request)
               .header("X-Trace-Id", TraceContext.currentId())
               .build();
           return next.exchange(traced);
       }
   })
   ```
3. **错误处理全局化**
   ```java
   .defaultStatusHandler(HttpStatusCode::is4xxClientError, 
       (req, res) -> { throw new BusinessException("客户端异常: " + res.getStatusCode()); })
   ```
4. **避免在接口方法中暴露底层类型**（如 `ResponseEntity`），优先使用 DTO 或 `Result<T>` 包装，便于后续切换 AOT/原生镜像。

---

### 🔄 四、与历史方案对比

| 方案              | 状态      | 适用性                                 |
|-----------------|---------|-------------------------------------|
| `RestTemplate`  | ⚠️ 维护模式 | 老项目兼容，新项目不推荐                        |
| OpenFeign       | 📦 第三方  | Spring Cloud 生态仍可用，但 Spring 原生方案更轻量 |
| `@HttpExchange` | ✅ 官方主力  | 新项目首选，Boot 4 深度优化                   |

---

### 📌 下一步建议

为了给你最精准的代码模板，请告诉我：

1. 你的调用是 **同步阻塞** 还是 **异步/流式**？
2. 是否需要对接 **OAuth2 / JWT / 签名鉴权**？
3. 项目是否已启用 **虚拟线程（Spring Boot 3.2+）** 或计划升级到 **Boot 4**？
4. 目标 API 是 RESTful 标准接口，还是包含文件上传/下载、SSE 等特殊场景？

提供场景后，我可直接输出带 **重试策略、熔断集成、OpenTelemetry 追踪、AOT 兼容** 的生产级完整示例。