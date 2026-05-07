package com.mallcloud.api.user.dto.response;

import com.mallcloud.api.user.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户响应 DTO
 */
@Getter
@Setter
public class UserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态
     */
    private UserStatus status;
}