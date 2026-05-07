package com.mallcloud.commons.api.services.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

// @HttpExchange 是我们可以应用于 HTTP 接口及其 exchange 方法的根注解。如果我们将其应用于接口层，那么它就会应用于所有 exchange 方法。
// 这对于指定所有接口方法的共同属性（如 content type 或 URL 前缀）非常有用。模式类似有 @RequestMapping。
@HttpExchange(url = "/repos/{owner}/{repo}", accept = "application/vnd.github.v3+json")
public interface NotificationServiceClient {
    // 注意，所有 HTTP 方法注解都是用 @HttpExchange 元注解的。因此，@GetExchange("/books") 等同于 @HttpExchange(url = "/books"，method = "GET")。
    // 既然我们已经定义了 HTTP 服务接口，就需要创建一个代理来实现该接口并在我们调用其内的接口方法时，会自动执行 exchange。

    @GetExchange("/repos/{owner}/{repo}")
    Mono<ResponseEntity<String>> getData(@PathVariable String owner, @PathVariable String repo);

    @PostExchange("/repos/{owner}/{repo}/contributors")
    Mono<ResponseEntity<String>> addData(@PathVariable String owner, @PathVariable String repo);

    @PutExchange("/repos/{owner}/{repo}/contributors")
    Mono<ResponseEntity<String>> updateData(@PathVariable String owner, @PathVariable String repo);

    @DeleteExchange("/repos/{owner}/{repo}/contributors")
    Mono<ResponseEntity<String>> deleteData(@PathVariable String owner, @PathVariable String repo);
}

// 在 Spring 6 和 Spring Boot 3 中，我们可以使用 Java 接口来定义声明式的远程 HTTP 服务。
// 这种方法受到 Feign 等流行 HTTP 客户端库的启发，与在 Spring Data 中定义 Repository 的方法类似。

// OpenFeign 是一个基于 Spring 的声明式 HTTP 客户端，它使得编写 Java 客户端变得更加简单。
// 我们现在将 Spring Cloud OpenFeign 项目视为功能完备。我们只会添加错误修复，并可能合并一些小型社区功能 PR。
// 我们建议迁移到 http://docs.springjava.cn/spring-framework/reference/integration/rest-clients.html#rest-http-interface[Spring 接口客户端]。


// Spring 框架允许您使用带 @HttpExchange 方法的 Java 接口来定义 HTTP 服务。您可以将此类接口传递给 HttpServiceProxyFactory 以创建代理，
// 该代理通过 HTTP 客户端（如 RestClient 或 WebClient）执行请求。您也可以从 @Controller 实现接口以进行服务器请求处理。
