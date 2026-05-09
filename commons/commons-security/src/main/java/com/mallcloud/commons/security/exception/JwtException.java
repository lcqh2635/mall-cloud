package com.mallcloud.commons.security.exception;

import lombok.Getter;

/**
 * JWT 统一异常基类
 *
 * <p>
 * 所有 JWT 相关异常都应该继承此类
 * 方便统一捕获、统一返回错误码
 *
 * @author mallcloud
 */
@Getter
public class JwtException extends RuntimeException {

    /**
     * 错误码（用于前端识别错误类型）
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String message;

    public JwtException(String code, String message) {
        super(message); // 交给 RuntimeException 管理堆栈信息
        this.code = code;
        this.message = message;
    }

    public JwtException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}