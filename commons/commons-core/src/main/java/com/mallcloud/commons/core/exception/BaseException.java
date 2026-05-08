package com.mallcloud.commons.core.exception;

import com.mallcloud.commons.core.enums.BizCodeEnum;
import lombok.Getter;

/**
 * 基础业务异常类
 * 所有自定义业务异常应继承此类，便于统一捕获和处理
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final int code;

    /**
     * 构造函数：使用枚举初始化异常
     *
     * @param bizCode 业务错误码枚举
     */
    public BaseException(BizCodeEnum bizCode) {
        super(bizCode.getMessage());
        this.code = bizCode.getCode();
    }

    /**
     * 构造函数：自定义错误码和消息（用于动态拼接）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数：带 cause 的异常（用于异常链）
     *
     * @param bizCode 业务错误码
     * @param cause   原始异常
     */
    public BaseException(BizCodeEnum bizCode, Throwable cause) {
        super(bizCode.getMessage(), cause);
        this.code = bizCode.getCode();
    }
}
