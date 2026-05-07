package com.mallcloud.commons.api.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP请求/响应日志拦截器
 * 记录详细的请求和响应信息，便于调试和问题排查
 */
@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 配置哪些路径需要记录详细日志
    private static final String[] LOGGING_PATHS = {
        "/users", "/orders", "/payments"
    };
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        
        long startTime = System.currentTimeMillis();
        String requestId = request.getHeaders().getFirst("X-Request-ID");
        
        // 检查是否需要记录详细日志
        boolean shouldLogDetails = shouldLogDetails(request.getURI().getPath());
        
        // 记录请求信息
        if (shouldLogDetails) {
            logRequest(request, body, requestId);
        } else {
            log.info("HTTP请求开始 [{}]: {} {}", 
                requestId, request.getMethod(), request.getURI());
        }
        
        ClientHttpResponse response = null;
        try {
            // 执行请求
            response = execution.execute(request, body);
            
            // 记录响应信息
            if (shouldLogDetails) {
                response = logResponse(request, response, requestId, startTime);
            } else {
                long duration = System.currentTimeMillis() - startTime;
                log.info("HTTP请求完成 [{}]: {} {} - 状态: {}, 耗时: {}ms",
                    requestId, request.getMethod(), request.getURI(),
                    response.getStatusCode(), duration);
            }
            
            return response;
            
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("HTTP请求失败 [{}]: {} {} - 异常: {}, 耗时: {}ms",
                requestId, request.getMethod(), request.getURI(),
                e.getMessage(), duration, e);
            throw e;
        }
    }
    
    /**
     * 记录详细的请求信息
     */
    private void logRequest(HttpRequest request, byte[] body, String requestId) {
        try {
            String requestBody = body.length > 0 ? 
                new String(body, StandardCharsets.UTF_8) : "[空请求体]";
            
            // 敏感信息脱敏
            requestBody = maskSensitiveInfo(requestBody);
            
            log.debug("""
                ======== HTTP请求详情 [{}] ========
                方法: {}
                URL: {}
                请求头: {}
                请求体: {}
                ==================================""",
                requestId,
                request.getMethod(),
                request.getURI(),
                request.getHeaders(),
                requestBody
            );
        } catch (Exception e) {
            log.warn("记录请求日志失败: {}", e.getMessage());
        }
    }
    
    /**
     * 记录详细的响应信息
     */
    private ClientHttpResponse logResponse(
            HttpRequest request,
            ClientHttpResponse response,
            String requestId,
            long startTime) throws IOException {
        
        // 包装Response以便多次读取body
        BufferingClientHttpResponseWrapper wrappedResponse = 
            new BufferingClientHttpResponseWrapper(response);
        
        long duration = System.currentTimeMillis() - startTime;
        
        try {
            String responseBody = StreamUtils.copyToString(
                wrappedResponse.getBody(), StandardCharsets.UTF_8);
            
            // 敏感信息脱敏
            responseBody = maskSensitiveInfo(responseBody);
            
            log.debug("""
                ======== HTTP响应详情 [{}] ========
                状态码: {}
                响应头: {}
                响应体: {}
                耗时: {}ms
                ==================================""",
                requestId,
                wrappedResponse.getStatusCode(),
                wrappedResponse.getHeaders(),
                responseBody,
                duration
            );
            
        } catch (Exception e) {
            log.warn("记录响应日志失败: {}", e.getMessage());
        }
        
        return wrappedResponse;
    }
    
    /**
     * 判断是否需要记录详细日志
     */
    private boolean shouldLogDetails(String path) {
        for (String loggingPath : LOGGING_PATHS) {
            if (path.startsWith(loggingPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 敏感信息脱敏
     */
    private String maskSensitiveInfo(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // 脱敏手机号
        content = content.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        // 脱敏身份证号
        content = content.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1**********$2");
        // 脱敏银行卡号
        content = content.replaceAll("(\\d{4})\\d{8,12}(\\d{4})", "$1************$2");
        
        return content;
    }
    
    /**
     * 可重复读取的Response包装器
     */
    private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private byte[] body;
        
        public BufferingClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
            this.response = response;
            this.body = StreamUtils.copyToByteArray(response.getBody());
        }
        
        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }
        
        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }
        
        @Override
        public void close() {
            response.close();
        }
        
        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }
        
        @Override
        public java.io.InputStream getBody() throws IOException {
            return new java.io.ByteArrayInputStream(body);
        }
    }
}