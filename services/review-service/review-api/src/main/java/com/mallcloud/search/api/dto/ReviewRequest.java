package com.mallcloud.search.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户创建请求 DTO
 * <p>
 * 注意：
 * DTO 仅用于服务间数据传输
 * 不要放业务逻辑
 */
@Getter
@Setter
public class ReviewRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3~20之间")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 用户邮箱
     */
    private String email;

}