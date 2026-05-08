package com.mallcloud.commons.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务异常码枚举
 * 统一管理系统中所有业务错误码，便于前端识别和日志追踪
 * 格式建议：模块码(2位) + 业务码(4位)，例如：10_0001
 */
@Getter
@RequiredArgsConstructor
public enum BizCodeEnum {

    // =============== 通用成功 ===============
    SUCCESS(200, "操作成功"),

    // =============== 客户端错误 (4xx) ===============
    VALIDATION_ERROR(400, "请求参数校验失败"),
    BAD_REQUEST(400, "错误的请求"),
    UNAUTHORIZED(401, "未认证，请登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "请求资源不存在"),

    // =============== 服务端错误 (5xx) ===============
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // =============== 业务自定义错误 (1000+) ===============
    // 【用户模块】
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_EXISTS(1002, "用户名已存在"),
    USER_DISABLED(1003, "用户已被禁用"),

    // 【订单模块】
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_INVALID(2002, "订单状态不合法"),

    // 【权限模块】
    PERMISSION_DENIED(3001, "权限不足"),

    // ========== 通知模块 ==========
    NOTIFICATION_SEND_FAILED(300101, "通知推送失败");

    /**
     * 错误码（建议全局唯一）
     */
    private final int code;

    /**
     * 错误描述
     */
    private final String message;

}
