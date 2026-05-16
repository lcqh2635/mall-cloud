import type { BaseResponse, PaginationResponse } from '../../api/types.ts'

/**
 * 用户基本信息
 */
export interface UserInfo {
    id: string
    username: string
    email?: string
    phone?: string
    avatar?: string
    createTime: string
    status: 0 | 1 // 0-禁用 1-启用
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
    username?: string
    email?: string
    status?: number
}

/**
 * 创建/更新用户参数
 */
export interface UserFormData {
    username: string
    password?: string
    email?: string
    phone?: string
    avatar?: string
    roles?: string[]
}