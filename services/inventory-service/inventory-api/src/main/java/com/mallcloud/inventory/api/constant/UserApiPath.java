package com.mallcloud.inventory.api.constant;

/**
 * 用户服务 API 路径常量
 * <p>
 * 统一管理用户服务所有接口路径
 */
public interface UserApiPath {

    /**
     * 用户模块根路径
     */
    String USER = "/users";

    /**
     * 根据ID查询用户
     */
    String GET_BY_ID = "/{id}";

    /**
     * 创建用户
     */
    String CREATE = "";

}