package com.mallcloud.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 全局异常处理器（Spring Cloud Gateway 专用）
 * 功能：
 *   - 捕获所有未处理的异常（包括 404、500、JWT 失效、服务不可用等）
 *   - 返回统一格式的 JSON 错误响应
 *   - 避免向客户端暴露堆栈信息、敏感错误细节
 * <p>
 * 注意：必须实现 ErrorWebExceptionHandler 接口，并标注 @Component
 *       且需要实现 Ordered 接口控制执行顺序（优先级越高越先执行）
 *
 * @author 龙茶清欢
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper; // 用于序列化 ErrorResponse

    /**
     * 构造函数注入 ObjectMapper（由 Spring 自动注入）
     */
    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 核心方法：处理异常并返回响应
     *
     * @param exchange 当前 HTTP 请求上下文
     * @param ex       发生的异常
     * @return Mono<Void> 异步响应流
     */
    @NullMarked
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取当前请求路径，用于响应中记录
        String path = request.getURI().getPath();

        // 设置响应头：JSON 格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建统一错误响应对象
        ErrorResponse errorResponse;

        // ✅ 分类处理不同类型的异常
        if (ex instanceof GatewayException) {
            // 自定义网关异常（如 JWT 校验失败）
            GatewayException gatewayEx = (GatewayException) ex;
            errorResponse = ErrorResponse.of(gatewayEx.getStatusCode(), gatewayEx.getMessage(), path);

        } else if (ex instanceof NotFoundException) {
            // Spring Cloud Gateway 默认 404 异常（路由未匹配）
            errorResponse = ErrorResponse.of(404, "请求路径不存在，请检查接口地址", path);

        } else if (ex instanceof org.springframework.cloud.gateway.support.TimeoutException) {
            // 请求超时
            errorResponse = ErrorResponse.of(504, "服务请求超时，请稍后再试", path);

        } else if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            // 由业务主动抛出的 HttpStatus 异常（如 @ResponseStatus）
            org.springframework.web.server.ResponseStatusException rse = (org.springframework.web.server.ResponseStatusException) ex;
            errorResponse = ErrorResponse.of(rse.getStatus().value(), rse.getMessage(), path);

        } else {
            // 未知异常（如后端服务崩溃、网络故障）
            // 生产环境建议记录日志，并返回通用错误
            errorResponse = ErrorResponse.of(500, "服务器内部错误，请联系管理员", path);
        }

        // 将 ErrorResponse 序列化为 JSON 字符串
        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            // 序列化失败，返回最基础的错误
            jsonResponse = "{\"code\":500,\"message\":\"系统内部错误\",\"path\":\"" + path + "\",\"timestamp\":\"" + errorResponse.getTimestamp() + "\"}";
        }

        // 将 JSON 写入响应体
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}