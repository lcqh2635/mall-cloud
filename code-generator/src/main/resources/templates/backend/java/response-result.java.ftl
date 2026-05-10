package com.urbane.commons.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.Instant;

/**
 * <p>
 * 统一响应结果封装类（含状态码枚举）
 * </p>
 *
 * <p>
 * 【核心改进】
 * 1. 内部定义 {@link ResponseCode} 枚举，集中管理所有响应码
 * 2. 响应码与消息解耦（枚举自带默认消息，可被覆盖）
 * 3. 支持业务自定义扩展（通过枚举常量）
 * 4. 保留泛型数据结构，确保类型安全
 * </p>
 *
 * <p>
 * 【使用示例】
 * - 成功：{@code ApiResponse.success(user)}
 * - 业务失败：{@code ApiResponse.error(ResultCode.USER_NOT_FOUND)}
 * - 自定义消息：{@code ApiResponse.error(ResultCode.VALIDATION_ERROR, "邮箱格式错误")}
 * </p>
 *
 * @param <T> 响应数据的泛型类型
 * @author ${author}
 * @since ${date}
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // 全局忽略 null 值字段, 即 null 值字段不会被序列化
@Schema(description = "统一响应结果", examples = {

})
public class ResponseResult<T> {

    /**
     * 响应状态码（使用枚举，避免硬编码）
     *
     * <p>
     * 【设计优势】
     * - 枚举值即文档（如 SUCCESS, VALIDATION_ERROR）
     * - 编译期检查，杜绝无效状态码
     * - 便于 IDE 自动补全和重构
     * </p>
     */
    @Schema(description = "响应状态码", example = "200/404")
    private int code;

    /**
     * 响应消息（可覆盖枚举默认消息）
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据（null 时不序列化）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "响应数据")
    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "响应时间戳（UTC）", example = "2023-01-01 12:00:00")
    private Instant timestamp;

    /**
     * 全参构造函数
     */
    private ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    // ==================== 成功响应工厂方法 ====================

    /**
     * 创建成功响应（使用枚举默认消息）
     */
    public static <T> ResponseResult<T> success() {
        return success(ResponseCode.SUCCESS);
    }

    /**
     * 创建带数据的成功响应
     */
    public static <T> ResponseResult<T> success(T data) {
        return success(ResponseCode.SUCCESS, data);
    }

    /**
     * 使用指定结果码创建成功响应（通常用于 SUCCESS）
     */
    public static <T> ResponseResult<T> success(ResponseCode resultCode) {
        return new ResponseResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 使用指定结果码创建成功响应（通常用于 SUCCESS，但带有 data 数据）
     */
    public static <T> ResponseResult<T> success(ResponseCode resultCode, T data) {
        return new ResponseResult<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    // ==================== 失败响应工厂方法 ====================

    /**
     * 创建客户端错误响应（400 状态码）
     *
     * <p>适用于参数校验失败、业务逻辑错误等客户端问题。</p>
     *
     * @param <T> 业务数据类型
     * @return 客户端错误的响应对象
     */
    public static <T> ResponseResult<T> badRequest() {
        return error(ResponseCode.BAD_REQUEST.getCode(), ResponseCode.BAD_REQUEST.getMessage());
    }

    /**
     * 创建未授权错误响应（401 状态码）
     *
     * @param <T> 业务数据类型
     * @return 未授权的响应对象
     */
    public static <T> ResponseResult<T> unauthorized() {
        return error(ResponseCode.UNAUTHORIZED.getCode(), ResponseCode.UNAUTHORIZED.getMessage());
    }

    /**
     * 创建禁止访问错误响应（403 状态码）
     *
     * @param <T> 业务数据类型
     * @return 禁止访问的响应对象
     */
    public static <T> ResponseResult<T> forbidden() {
        return error(ResponseCode.FORBIDDEN.getCode(), ResponseCode.FORBIDDEN.getMessage());
    }

    /**
     * 创建资源不存在错误响应（404 状态码）
     *
     * @param <T> 业务数据类型
     * @return 资源不存在的响应对象
     */
    public static <T> ResponseResult<T> notFound() {
        return error(ResponseCode.NOT_FOUND.getCode(), ResponseCode.NOT_FOUND.getMessage());
    }

    /**
     * 创建服务器内部错误响应（500 状态码）
     *
     * @param <T> 业务数据类型
     * @return 服务器内部错误的响应对象
     */
    public static <T> ResponseResult<T> serverError() {
        return error(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), ResponseCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 创建失败响应（通用系统错误）
     */
    public static <T> ResponseResult<T> error() {
        return error(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 使用结果码创建失败响应
     */
    public static <T> ResponseResult<T> error(ResponseCode resultCode) {
        Assert.notNull(resultCode, "HttpStatusCode must not be null");
        return new ResponseResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 使用结果码和自定义消息创建失败响应
     *
     * <p>
     * 【典型场景】
     * - 参数校验失败时，用通用码 + 具体字段错误
     *   {@code fail(ResultCode.VALIDATION_ERROR, "用户名不能为空")}
     * </p>
     */
    public static <T> ResponseResult<T> error(ResponseCode resultCode, String customMessage) {
        return new ResponseResult<>(resultCode.getCode(), customMessage, null);
    }

    /**
     * 使用自定义状态码和消息创建失败响应（兼容遗留系统）
     *
     * <p>
     * 【注意】
     * - 应优先使用 {@link ResponseCode} 枚举
     * - 此方法仅用于无法预定义的场景
     * </p>
     */
    public static <T> ResponseResult<T> error(int code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    // ==================== 响应状态码枚举 ====================

    /**
     * <p>
     * 响应状态码枚举
     * </p>
     *
     * <p>
     * 【设计原则】
     * 1. 覆盖通用场景（成功、系统错误、校验错误等）
     * 2. 预留业务扩展空间（如 USER_XXX, ORDER_XXX）
     * 3. 每个枚举包含：
     *    - code：整数状态码（与 HTTP 状态码风格一致）
     *    - message：默认用户友好消息
     * 4. 状态码范围建议：
     *    - 200-299：成功
     *    - 400-499：客户端错误
     *    - 500-599：服务端错误
     *    - 1000+：业务自定义错误
     * </p>
     */
    @Getter
    @RequiredArgsConstructor
    public enum ResponseCode {
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
        PERMISSION_DENIED(3001, "权限不足");

        /**
         * 状态码（整数）
         */
        private final int code;

        /**
         * 默认消息
         */
        private final String message;
    }

}