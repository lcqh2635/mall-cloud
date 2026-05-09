### Spring 的 HttpServiceProxyFactory

Spring 框架确实提供了一个名为 `HttpServiceProxyFactory` 的工具，它允许你使用 Java 接口定义 HTTP 服务，并通过 HTTP 客户端（如 `RestClient` 或 `WebClient`）执行请求。这种方式提供了一种简洁的声明式方法来定义 HTTP 服务客户端，类似于 OpenFeign 的风格，但它是 Spring 5.3 引入的新特性。

### HttpServiceProxyFactory 介绍

`HttpServiceProxyFactory` 是 Spring 框架提供的一种机制，用于创建客户端代理，该代理可以调用远程服务，就像调用本地服务一样。它允许你定义一个接口，并使用 `@HttpExchange` 注解来描述 HTTP 请求和响应。

### 使用 HttpServiceProxyFactory

#### 步骤 1: 定义接口

首先，你需要定义一个接口，并使用 `@HttpExchange` 注解来描述 HTTP 请求和响应。

```java
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.invoker.HttpServiceProxyUtils;
import org.springframework.web.service.invoker.InvocationContext;
import org.springframework.web.service.invoker.InvocationCustomizer;
import org.springframework.web.service.invoker.ServiceDefinition;
import org.springframework.web.service.invoker.ServiceDefinitionBuilder;

import java.util.List;

public interface ExampleClient {

    @HttpExchange(url = "/greeting", method = "GET")
    String getGreeting();

    @HttpExchange(url = "/greeting", method = "POST", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    String postGreeting(String name);

    @HttpExchange(url = "/items/{id}", method = "GET")
    List<String> getItemById(@PathVariable String id);
}
```

在这个例子中：

- 我们定义了一个名为 `ExampleClient` 的接口。
- 使用 `@HttpExchange` 注解来描述 HTTP 请求，包括 URL、HTTP 方法、消耗和生产的媒体类型等。

#### 步骤 2: 创建代理

接下来，你需要创建一个 `HttpServiceProxyFactory` 实例，并使用它来创建一个代理。

```java
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class ExampleApplication {

    public static void main(String[] args) {
        WebClient webClient = WebClient.create("https://api.example.com");

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                        .build())
                .clientConnector(webClient)
                .build();

        ExampleClient client = factory.createClient(ExampleClient.class);
        String greeting = client.getGreeting();
        System.out.println(greeting);
    }
}
```

在这个例子中：

- 我们创建了一个 `WebClient` 实例，并指定了基础 URL。
- 使用 `HttpServiceProxyFactory` 创建了一个代理工厂。
- 使用工厂创建了一个 `ExampleClient` 的实例。
- 调用 `getGreeting` 方法来获取远程服务的响应。

### 与 OpenFeign 的比较

- **相似之处**：`HttpServiceProxyFactory` 和 OpenFeign 都采用了声明式的方式来定义 HTTP 服务客户端。
- **不同之处**：`HttpServiceProxyFactory` 是 Spring 框架的一部分，而 OpenFeign 是 Spring Cloud 的一部分。
- **客户端选择**：`HttpServiceProxyFactory` 默认使用 `WebClient` 作为 HTTP 客户端，而 OpenFeign 默认使用 `OkHttpClient`。

### 总结

`HttpServiceProxyFactory` 是一种简洁的方法来定义 HTTP 服务客户端，它可以与 `WebClient` 结合使用，提供了一种声明式的方式来调用远程服务。这种方式类似于 OpenFeign，但在 Spring 生态系统中更加原生。如果你的应用主要基于 Spring 框架，并且希望避免引入额外的依赖，`HttpServiceProxyFactory` 是一个不错的选择。




## 每一个 Http 接口都需要使用 HttpServiceProxyFactory 来创建一个对应的接口实现代理
当你使用 Java 接口定义 HTTP 服务，并通过 HTTP 客户端（如 RestClient 或 WebClient）执行请求时，确实需要使用 HttpServiceProxyFactory 来创建一个对应的接口实现代理。这种方式类似于 OpenFeign，但它是 Spring 框架原生支持的功能。
推荐在一个配置文件中集中使用 HttpServiceProxyFactory 创建 Http 接口的实现代理。然后将 HTTP 接口实现代理注入该工厂。这样，你就可以避免在每个 HTTP 服务接口中重复创建工厂，从而提高代码的可维护性和可读性。
```java
@Configuration
public class HttpConfig {

    // 定制化 RestClient 供所有 Java 服务接口使用
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com/")
                .build();
    }

    // 现在您可以创建一个 RestClient 代理，在调用方法时执行请求。
    // 现在，我们可以将客户端代理实例注册为 Spring Bean 或组件，并用它请求 REST 服务。
    @Bean
    public HttpService httpService(RestClient restClient) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(HttpService.class);
    }

    // 如果还有其他 HTTP 接口，您可以重复注册 HttpService 接口代理实现此过程。
}

// 关于配置和使用 HTTP 接口 参考官网 https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-http-interface
// Spring 框架允许您使用带 @HttpExchange 方法的 Java 接口来定义 HTTP 服务。您可以将此类接口传递给 HttpServiceProxyFactory 以创建代理，
// 该代理通过 HTTP 客户端（如 RestClient 或 WebClient）执行请求。您也可以从 @Controller 实现接口以进行服务器请求处理。
```