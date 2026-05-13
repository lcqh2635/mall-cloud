// 认证相关API类型定义

// import type { BaseResponse } from '../../api/types.ts'

/**
 * 登录请求参数
 */
export interface LoginParams {
    username: string
    password: string
    captcha?: string
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
    token: string
    refreshToken: string
    expiresIn: number
    userInfo: {
        userId: string
        username: string
        avatar?: string
        roles: string[]
    }
}

/**
 * 刷新token响应
 */
export interface RefreshTokenResponse {
    token: string
    expiresIn: number
}

// 其他认证相关类型定义...