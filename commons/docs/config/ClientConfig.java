package com.mallcloud.commons.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.time.Duration;

// https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-http-service-client-group-config
@Configuration
@ImportHttpServices(group = "services", basePackages = {"com.urbane.commons.api.services"})
public class ClientConfig {

    @Value("${services.user.base-url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.order.base-url:http://localhost:8082}")
    private String orderServiceUrl;

    @Value("${services.payment.base-url:http://localhost:8083}")
    private String paymentServiceUrl;

    @Value("${services.auth.token:default-token}")
    private String authToken;

    @Value("${services.timeout.connect:5s}")
    private Duration connectTimeout;

    @Value("${services.timeout.read:30s}")
    private Duration readTimeout;

    // 声明 HTTP 服务组后，可以添加 HttpServiceGroupConfigurer bean 以自定义每个组的客户端。例如
    // 参考 https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-http-service-client-group-config
    @Bean
    public RestClientHttpServiceGroupConfigurer serviceGroupConfigurer() {
        return groups -> {
            // 配置客户端为组 “services”
            // ========== 配置用户服务组 ==========
            groups.filterByName("services").forEachClient((group, clientBuilder) -> {
                System.out.println("配置用户服务组: " + group.name());
                // 1. 设置基础URL
                clientBuilder.baseUrl(userServiceUrl);

                // 2. 添加认证拦截器
                clientBuilder.requestInterceptor(authInterceptor);

                // 3. 添加日志拦截器
                clientBuilder.requestInterceptor(loggingInterceptor);

                // 4. 配置连接池和超时
                clientBuilder.requestFactory(createRequestFactory(
                        connectTimeout,
                        readTimeout,
                        50,  // 最大连接数
                        20   // 每个路由的最大连接数
                ));

                // 5. 配置默认请求头
                clientBuilder.defaultHeader("X-Service-Name", "user-service-client");
                clientBuilder.defaultHeader("X-Request-ID", () -> java.util.UUID.randomUUID().toString());
            });

            // 为所有组配置客户端
            // ========== 配置所有服务组的通用设置 ==========
            groups.forEachClient((group, clientBuilder) -> {
                // 1. 所有服务组都添加监控拦截器
                clientBuilder.requestInterceptor(createMetricsInterceptor(group.name()));

                // 2. 统一的内容协商配置
                clientBuilder.defaultHeader("Accept", "application/json");
                clientBuilder.defaultHeader("Content-Type", "application/json");

                // 3. 统一的用户代理
                clientBuilder.defaultHeader("User-Agent", "Microservice-Client/1.0");

                // 4. 统一的错误处理
                clientBuilder.defaultStatusHandler(
                        statusCode -> statusCode.is5xxServerError(),
                        (request, response) -> {
                            throw new ServiceException("服务端错误: " + response.getStatusCode());
                        }
                );
            });

            // 为每个组配置客户端和代理工厂
            // ========== 配置每个组的代理工厂 ==========
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                System.out.println("配置代理工厂 for group: " + group.name());

                // 1. 配置消息转换器
                factoryBuilder.messageConverters(converters -> {
                    // 添加自定义的消息转换器
                    converters.add(new CustomJsonMessageConverter());
                });

                // 2. 配置参数解析器
                factoryBuilder.argumentResolverConfigurer(configurer -> {
                    // 添加自定义的参数解析器
                    configurer.addCustomResolver(new CustomArgumentResolver());
                });

                // 3. 配置异常处理器
                if ("payment-service".equals(group.name())) {
                    // 支付服务需要特殊的异常处理
                    factoryBuilder.exceptionHandler(new PaymentServiceExceptionHandler());
                }
            });
        };
    }
}
