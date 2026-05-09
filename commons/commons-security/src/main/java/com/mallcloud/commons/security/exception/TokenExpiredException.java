package com.mallcloud.commons.security.exception;

import lombok.Getter;

/**
 * JWT Token 过期异常
 * <p>
 * 用于区分「签名无效」「格式错误」「已过期」等不同认证失败场景，
 * 便于网关层返回精细化错误响应，前端据此执行刷新或跳转登录。
 *
 * @author mallcloud
 * &#064;date  2026-05-09
 */
@Getter
public class TokenExpiredException extends SecurityException {

    /** Token 过期时间（可选，用于日志或响应） */
    private final Long expiredAt;

    public TokenExpiredException(String message) {
        super(message);
        this.expiredAt = null;
    }

    public TokenExpiredException(String message, Long expiredAt) {
        super(message);
        this.expiredAt = expiredAt;
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
        this.expiredAt = null;
    }
}
