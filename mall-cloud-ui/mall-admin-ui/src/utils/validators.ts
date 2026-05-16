/**
 * 表单验证工具集
 * 包含常见的表单验证函数
 */

/**
 * 验证是否为空
 * @param value - 要验证的值
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateRequired(value: any, message = '此项为必填项'): string {
    if (value === null || value === undefined || value === '') {
        return message
    }
    if (Array.isArray(value) && value.length === 0) {
        return message
    }
    return ''
}

/**
 * 验证手机号格式
 * @param value - 手机号码
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateMobile(value: string, message = '请输入正确的手机号码'): string {
    const reg = /^1[3-9]\d{9}$/
    return reg.test(value) ? '' : message
}

/**
 * 验证邮箱格式
 * @param value - 邮箱地址
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateEmail(value: string, message = '请输入正确的邮箱地址'): string {
    const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
    return reg.test(value) ? '' : message
}

/**
 * 验证密码强度
 * @param value - 密码
 * @param options - 配置选项
 * @returns 错误消息或空字符串
 */
export function validatePassword(
    value: string,
    options: {
        minLength?: number
        requireNumber?: boolean
        requireSpecialChar?: boolean
        message?: string
    } = {}
): string {
    const {
        minLength = 8,
        requireNumber = true,
        requireSpecialChar = true,
        message = '密码必须包含字母和数字，且长度不少于8位'
    } = options

    if (value.length < minLength) {
        return message
    }

    if (requireNumber && !/\d/.test(value)) {
        return message
    }

    if (requireSpecialChar && !/[!@#$%^&*(),.?":{}|<>]/.test(value)) {
        return message
    }

    return ''
}

/**
 * 验证身份证号码
 * @param value - 身份证号码
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateIDCard(value: string, message = '请输入正确的身份证号码'): string {
    const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
    return reg.test(value) ? '' : message
}

/**
 * 验证数字范围
 * @param value - 数字值
 * @param options - 配置选项
 * @returns 错误消息或空字符串
 */
export function validateNumberRange(
    value: number,
    options: {
        min?: number
        max?: number
        message?: string
    } = {}
): string {
    const { min, max, message = '数值超出允许范围' } = options

    if (min !== undefined && value < min) {
        return message
    }

    if (max !== undefined && value > max) {
        return message
    }

    return ''
}

/**
 * 验证URL格式
 * @param value - URL地址
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateURL(value: string, message = '请输入正确的URL地址'): string {
    try {
        new URL(value)
        return ''
    } catch {
        return message
    }
}

/**
 * 验证两个字段是否一致（如密码确认）
 * @param value1 - 第一个值
 * @param value2 - 第二个值
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateMatch(
    value1: any,
    value2: any,
    message = '两次输入不一致'
): string {
    return value1 === value2 ? '' : message
}

/**
 * 自定义正则验证
 * @param value - 要验证的值
 * @param reg - 正则表达式
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validatePattern(
    value: string,
    reg: RegExp,
    message = '输入格式不正确'
): string {
    return reg.test(value) ? '' : message
}

/**
 * 组合验证器
 * @param value - 要验证的值
 * @param validators - 验证器数组
 * @returns 第一个验证失败的错误消息，或空字符串
 */
export function validateAll(
    value: any,
    validators: Array<(val: any) => string>
): string {
    for (const validator of validators) {
        const result = validator(value)
        if (result) {
            return result
        }
    }
    return ''
}

// 导出所有验证器
export default {
    required: validateRequired,
    mobile: validateMobile,
    email: validateEmail,
    password: validatePassword,
    idCard: validateIDCard,
    numberRange: validateNumberRange,
    url: validateURL,
    match: validateMatch,
    pattern: validatePattern,
    all: validateAll
}