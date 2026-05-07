package com.mallcloud.service.user.controller;

import com.mallcloud.api.user.client.UserApi;
import com.mallcloud.api.user.dto.request.UserCreateRequest;
import com.mallcloud.api.user.dto.response.UserResponse;
import com.mallcloud.api.user.enums.UserStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 *
 * 直接实现 UserApi 接口
 */
@RestController
public class UserController implements UserApi {

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