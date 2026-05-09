package com.mallcloud.search.api.client;

import com.mallcloud.user.api.constant.UserApiPath;
import com.mallcloud.user.api.dto.UserCreateRequest;
import com.mallcloud.user.api.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 用户服务远程调用接口
 * <p>
 * 基于 Spring 6+ Http Interface 实现
 * <p>
 * 注意：
 * 1. 这里只定义接口契约
 * 2. 不写业务逻辑
 * 3. 不依赖 Service
 * 4. 服务提供方直接实现该接口
 */
@HttpExchange(UserApiPath.USER)
public interface UserRemoteClient {

    /**
     * 根据 ID 查询用户
     * <p>
     * GET /users/{id}
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetExchange(UserApiPath.GET_BY_ID)
    UserResponse getById(@PathVariable Long id);

    /**
     * 创建用户
     * <p>
     * POST /users
     *
     * @param request 创建请求
     * @return 用户信息
     */
    @PostExchange(UserApiPath.CREATE)
    UserResponse create(@RequestBody UserCreateRequest request);

}