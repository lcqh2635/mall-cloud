package com.mallcloud.commons.api.config;

import com.urbane.commons.api.filter.JwtAuthRequestFilter;
import com.urbane.commons.api.interceptor.JwtAuthRequestInterceptor;
import com.urbane.commons.api.services.cart.CartServiceClient;
import com.urbane.commons.api.services.inventory.InventoryServiceClient;
import com.urbane.commons.api.services.order.OrderServiceClient;
import com.urbane.commons.api.services.product.ProductServiceClient;
import com.urbane.commons.api.services.promotion.PromotionServiceClient;
import com.urbane.commons.api.services.user.UserServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Map;

/**
 * HTTP 声明式客户端配置类。
 * 负责创建 RestClient 或者 WebClient 实例和注册声明式客户端接口的代理 Bean。
 */
@Configuration
public class HttpInterfaceConfig {

    // 假设要调用的远程服务基础地址
    private static final String REMOTE_SERVICE_BASE_URL = "http://localhost:8080";

    // RestClient 是一个同步 HTTP 客户端，它提供流式 API 来执行请求。它作为 HTTP 库的抽象，并处理 HTTP 请求和响应内容与高级 Java 对象之间的转换。
    // RestClient 参考 https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-restclient
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                // 选择要使用的 HTTP 库，请参阅 客户端请求工厂 https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-request-factories
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                // 设置 baseUrl，一般都是域名
                .baseUrl(REMOTE_SERVICE_BASE_URL)
                // 设置默认请求头、Cookie、路径变量、API 版本
                .defaultUriVariables(Map.of("variable", "foo"))
                .defaultHeader("My-Header", "Foo")
                .defaultCookie("My-Cookie", "Bar")
                .defaultApiVersion("1.2")
                // 配置 ApiVersionInserter
                .apiVersionInserter(ApiVersionInserter.useHeader("API-Version"))
                // 注册请求拦截器
                .requestInterceptor(new JwtAuthRequestInterceptor())
                // 默认情况下，WebClient 对于 4xx 和 5xx HTTP 状态码会抛出 RestClientResponseException。
                // 要自定义此行为，请注册一个响应状态处理程序，该处理程序将应用于通过客户端执行的所有响应
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {

                })
                .build();
    }

    /**
     * 创建并配置一个 WebClient Bean。
     * WebClient 是 Spring WebFlux 提供的非阻塞、响应式HTTP客户端，是声明式客户端底层通信的基石。
     * 这里设置了所有通过该客户端发出的请求的默认基础URL。
     *
     * @return 配置好的 WebClient 实例
     */
    // WebClient 是一个非阻塞、响应式的客户端，用于执行 HTTP 请求。它在 5.0 中引入，提供了 RestTemplate 的替代方案，支持同步、异步和流式场景。
    // WebClient 参考 https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-webclient
    // https://docs.springjava.cn/spring-framework/reference/web/webflux-webclient/client-builder.html
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(REMOTE_SERVICE_BASE_URL)
                // 设置默认请求头、Cookie、路径变量、API 版本
                .defaultUriVariables(Map.of("variable", "foo"))
                .defaultHeader("My-Header", "Foo")
                .defaultCookie("My-Cookie", "Bar")
                .defaultApiVersion("1.2")
                // 配置 ApiVersionInserter
                .apiVersionInserter(ApiVersionInserter.useHeader("API-Version"))
                // 注册请求拦截器
                .filter(new JwtAuthRequestFilter())
                // 默认情况下，WebClient 对于 4xx 和 5xx HTTP 状态码会抛出 WebClientResponseException。
                // 要自定义此行为，请注册一个响应状态处理程序，该处理程序将应用于通过客户端执行的所有响应
                .defaultStatusHandler(HttpStatusCode::isError, resp -> null)
                .build();
    }

    /**
     * 创建 UserServiceClient 接口的代理实现 Bean。
     * 1. 通过 WebClientAdapter 将上一步定义的 WebClient 包装起来。
     * 2. 使用 HttpServiceProxyFactory 的 builder 模式，基于适配器创建工厂。
     * 3. 用工厂为指定的客户端接口创建代理对象，该对象会将接口方法的调用转换为实际的HTTP请求。
     *
     * @param webClient 注入上面定义的 WebClient Bean
     * @return UserServiceClient 接口的代理实例
     */
    // 现在您可以创建一个 RestClient 代理，在调用方法时执行请求。
    // 现在，我们可以将客户端代理实例注册为 Spring Bean 或组件，并用它请求 REST 服务。
    @Bean
    public UserServiceClient userServiceClient(WebClient webClient) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserServiceClient.class);
    }

    @Bean
    public CartServiceClient cartServiceClient(RestClient restClient) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(CartServiceClient.class);
    }

    @Bean
    public ProductServiceClient productServiceClient(WebClient webClient) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ProductServiceClient.class);
    }

    @Bean
    public OrderServiceClient orderServiceClient(WebClient webClient) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OrderServiceClient.class);
    }

    @Bean
    public InventoryServiceClient inventoryServiceClient(WebClient webClient) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(InventoryServiceClient.class);
    }

    @Bean
    public PromotionServiceClient promotionServiceClient(WebClient webClient) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(PromotionServiceClient.class);
    }
}

// 关于配置和使用 HTTP 接口 参考官网 https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-http-interface
// Spring 框架允许您使用带 @HttpExchange 方法的 Java 接口来定义 HTTP 服务。您可以将此类接口传递给 HttpServiceProxyFactory 以创建代理，
// 该代理通过 HTTP 客户端（如 RestClient 或 WebClient）执行请求。您也可以从 @Controller 实现接口以进行服务器请求处理。