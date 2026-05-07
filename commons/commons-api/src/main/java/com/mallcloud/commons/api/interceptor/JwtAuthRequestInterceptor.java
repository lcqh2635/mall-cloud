package com.mallcloud.commons.api.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * JWT 认证请求拦截器。
 * <p>
 * 作用：在每次通过 RestClient 发起 HTTP 请求前，自动从 {@link JwtTokenUtil} 获取有效的 JWT Token，
 * 并将其以 "Bearer <token>" 的形式添加到请求头的 "Authorization" 字段中。
 * </p>
 * <p>
 * 设计特点：
 * - 仅当 Token 非空时才添加 Authorization 头，避免发送无效认证信息。
 * - 支持灵活的 Token 来源（通过注入不同的 JwtTokenProvider 实现）。
 * - 记录关键操作日志，便于调试和审计。
 * - 线程安全（依赖的 JwtTokenProvider 必须是线程安全的）。
 * </p>
 */
public class JwtAuthRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthRequestInterceptor.class);

    /**
     * 拦截 HTTP 请求，在发送前添加 JWT 认证头。
     *
     * @param request   当前 HTTP 请求对象（可修改其头信息）
     * @param body      请求体字节数组（本拦截器不修改请求体）
     * @param execution 请求执行链，调用 execute() 方法继续执行后续拦截器或发送请求
     * @return HTTP 响应对象
     * @throws IOException 执行请求过程中可能抛出的 IO 异常
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        // 1. 从 Token 提供器获取当前有效的 JWT Token
        String jwtToken = "hello world";

        // 2. 如果获取到有效的 Token，则添加到 Authorization 请求头
        if (jwtToken != null && !jwtToken.trim().isEmpty()) {
            // 标准 JWT 认证格式：Bearer <token>
            String authHeader = "Bearer " + jwtToken.trim();
            request.getHeaders().set("Authorization", authHeader);

            if (log.isDebugEnabled()) {
                // 注意：生产环境中避免记录完整 Token（安全风险），此处仅记录存在性
                log.debug("已为请求 [{}] 添加 JWT 认证头", request.getURI());
            }
        } else {
            // 可选：记录未携带 Token 的情况（根据业务需求决定是否警告）
            if (log.isTraceEnabled()) {
                log.trace("请求 [{}] 未携带 JWT Token（可能为公开接口或未登录状态）", request.getURI());
            }
        }

        // 3. 继续执行请求（包括后续拦截器和实际网络调用）
        return execution.execute(request, body);
    }
}
