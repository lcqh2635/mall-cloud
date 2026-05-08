package com.mallcloud.commons.core.model;

import com.mallcloud.commons.core.enums.BizCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 统一 API 响应结果封装类
 * 所有 RESTful 接口应返回此类型，确保前后端约定一致
 *
 * @param <T> 响应数据类型
 */
@Getter
@Setter
@Schema(description = "统一响应结果")
public class ResponseResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    @Schema(description = "响应状态码", example = "200")
    private int code;

    /**
     * 响应消息（可覆盖枚举默认消息）
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据（null 时不序列化）
     */
    @Schema(description = "响应数据")
    private T data;

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
        return success(BizCodeEnum.SUCCESS);
    }

    /**
     * 创建带数据的成功响应
     */
    public static <T> ResponseResult<T> success(T data) {
        return success(BizCodeEnum.SUCCESS, data);
    }

    /**
     * 使用指定结果码创建成功响应（通常用于 SUCCESS）
     */
    public static <T> ResponseResult<T> success(BizCodeEnum resultCode) {
        return new ResponseResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 使用指定结果码创建成功响应（通常用于 SUCCESS，但带有 data 数据）
     */
    public static <T> ResponseResult<T> success(BizCodeEnum resultCode, T data) {
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
        return error(BizCodeEnum.BAD_REQUEST.getCode(), BizCodeEnum.BAD_REQUEST.getMessage());
    }

    /**
     * 创建未授权错误响应（401 状态码）
     *
     * @param <T> 业务数据类型
     * @return 未授权的响应对象
     */
    public static <T> ResponseResult<T> unauthorized() {
        return error(BizCodeEnum.UNAUTHORIZED.getCode(), BizCodeEnum.UNAUTHORIZED.getMessage());
    }

    /**
     * 创建禁止访问错误响应（403 状态码）
     *
     * @param <T> 业务数据类型
     * @return 禁止访问的响应对象
     */
    public static <T> ResponseResult<T> forbidden() {
        return error(BizCodeEnum.FORBIDDEN.getCode(), BizCodeEnum.FORBIDDEN.getMessage());
    }

    /**
     * 创建资源不存在错误响应（404 状态码）
     *
     * @param <T> 业务数据类型
     * @return 资源不存在的响应对象
     */
    public static <T> ResponseResult<T> notFound() {
        return error(BizCodeEnum.NOT_FOUND.getCode(), BizCodeEnum.NOT_FOUND.getMessage());
    }

    /**
     * 创建服务器内部错误响应（500 状态码）
     *
     * @param <T> 业务数据类型
     * @return 服务器内部错误的响应对象
     */
    public static <T> ResponseResult<T> serverError() {
        return error(BizCodeEnum.INTERNAL_SERVER_ERROR.getCode(), BizCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 创建失败响应（通用系统错误）
     */
    public static <T> ResponseResult<T> error() {
        return error(BizCodeEnum.INTERNAL_SERVER_ERROR);
    }

    /**
     * 使用结果码创建失败响应
     */
    public static <T> ResponseResult<T> error(BizCodeEnum resultCode) {
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
    public static <T> ResponseResult<T> error(BizCodeEnum resultCode, String customMessage) {
        return new ResponseResult<>(resultCode.getCode(), customMessage, null);
    }

    /**
     * 使用自定义状态码和消息创建失败响应（兼容遗留系统）
     *
     * <p>
     * 【注意】
     * - 应优先使用 {@link BizCodeEnum} 枚举
     * - 此方法仅用于无法预定义的场景
     * </p>
     */
    public static <T> ResponseResult<T> error(int code, String message) {
        return new ResponseResult<>(code, message, null);
    }
}
