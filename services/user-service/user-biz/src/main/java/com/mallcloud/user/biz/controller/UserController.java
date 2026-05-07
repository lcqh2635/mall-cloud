package com.mallcloud.user.biz.controller;

import com.mallcloud.user.api.client.UserClient;
import com.mallcloud.user.api.dto.UserCreateRequest;
import com.mallcloud.user.api.dto.UserResponse;
import com.mallcloud.user.api.enums.UserStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * <p>
 * 直接实现 UserApi 接口
 */
@RestController
public class UserController implements UserClient {

    /**
     * 根据 ID 查询用户
     */
    @Override
    public UserResponse getById(Long id) {

        // 模拟数据库查询
        UserResponse response = new UserResponse();

        response.setId(id);
        response.setUsername("admin");
        response.setEmail("admin@example.com");
        response.setStatus(UserStatus.ENABLED);

        return response;
    }

    /**
     * 创建用户
     */
    @Override
    public UserResponse create(UserCreateRequest request) {

        // 模拟创建用户
        UserResponse response = new UserResponse();

        response.setId(1L);
        response.setUsername(request.getUsername());
        response.setEmail(request.getEmail());
        response.setStatus(UserStatus.ENABLED);

        return response;
    }
}