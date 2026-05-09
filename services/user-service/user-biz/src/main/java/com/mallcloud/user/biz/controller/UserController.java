package com.mallcloud.user.biz.controller;

import com.mallcloud.user.api.client.UserRemoteClient;
import com.mallcloud.user.api.constant.UserApiPath;
import com.mallcloud.user.api.dto.UserCreateRequest;
import com.mallcloud.user.api.dto.UserResponse;
import com.mallcloud.user.api.enums.UserStatus;
import com.mallcloud.user.biz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * <p>
 * 直接实现 UserApi 接口
 */
@RestController
@RequestMapping(UserApiPath.USER)
@RequiredArgsConstructor
public class UserController implements UserRemoteClient {

    private final UserService userService;

    /**
     * 根据 ID 查询用户
     */
    @GetMapping(UserApiPath.GET_BY_ID)
    @Override
    public UserResponse getUserById(@PathVariable Long id) {
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
    @RequestMapping(UserApiPath.CREATE)
    @Override
    public UserResponse create(@RequestBody UserCreateRequest request) {

        // 模拟创建用户
        UserResponse response = new UserResponse();

        response.setId(1L);
        response.setUsername(request.getUsername());
        response.setEmail(request.getEmail());
        response.setStatus(UserStatus.ENABLED);

        return response;
    }
}