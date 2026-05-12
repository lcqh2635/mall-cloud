package com.mallcloud.order.api.config;

import com.mallcloud.order.api.client.OrderRemoteClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestClient.class)
@ConditionalOnBean(LoadBalancerClient.class) // 仅当消费方引入 LB 依赖时才生效
public class OrderRemoteClientAutoConfiguration {

    // ① 声明一个被 @LoadBalanced 标记的 Builder
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean(name = "loadBalancedRestClientBuilder")
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // ② 基于该 Builder 创建声明式客户端
    @Bean
    @ConditionalOnMissingBean(OrderRemoteClient.class)
    public OrderRemoteClient userRemoteClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
            @Value("${user.client.service-id:user-service}") String serviceId) {

        RestClient restClient = builder
                .baseUrl("https://" + serviceId) // ⚠️ 必须用服务名，不能写 IP:Port
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(OrderRemoteClient.class);
    }
}

// REST 客户端（ RestClient ）、HTTP 服务客户端（ @HttpExchange ）
// https://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html
// https://docs.springjava.cn/spring-boot/reference/io/rest-client.html