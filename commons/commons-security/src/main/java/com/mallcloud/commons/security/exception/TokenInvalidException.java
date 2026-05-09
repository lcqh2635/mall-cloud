package com.mallcloud.commons.security.exception;

/**
 * Token 非法异常
 *
 * <p>
 * 场景：
 * 1. Token 被篡改
 * 2. 签名验证失败
 * 3. 格式错误
 * 4. 非本系统签发
 *
 * <p>
 * 注意：
 * 这种异常通常无法恢复，只能重新登录
 *
 * @author mallcloud
 */
public class TokenInvalidException extends JwtException {

    private static final String DEFAULT_CODE = "TOKEN_INVALID";

    public TokenInvalidException(String message) {
        super(DEFAULT_CODE, message);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(DEFAULT_CODE, message, cause);
    }
}
