package com.mallcloud.commons.security.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.mallcloud.commons.security.exception.TokenExpiredException;
import com.mallcloud.commons.security.properties.JwtProperties;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;

    /**
     * 校验 JWT
     */
    public JWT validateToken(String token) {

        JWT jwt = JWTUtil.parseToken(token);
        JWTSigner signer = JWTSignerUtil.hs256(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        // 设置密钥
        jwt.setSigner(signer);

        // 1. 校验签名
        if (!jwt.verify()) {
            throw new SecurityException("JWT 签名校验失败");
        }

        // 2. 校验时间
        // validate(5) 表示允许 5 秒时间误差
        if (!jwt.validate(5)) {
            throw new TokenExpiredException("Token 已过期");
        }

        return jwt;
    }
}
