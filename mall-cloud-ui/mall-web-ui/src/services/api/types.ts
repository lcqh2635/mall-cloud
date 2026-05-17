import type { AxiosRequestConfig } from 'axios'

/**
 * 通用响应结构
 * @template T - 数据类型
 */
export interface BaseResponse<T = any> {
    /**
     * 状态码
     */
    code: number;
    /**
     * 数据
     */
    data: T;
    /**
     * 消息
     */
    message: string;
}

/**
 * 分页数据返回类型
 */
export interface PaginatedResponse<T> extends BaseResponse<T[]> {
    /**
     * 总条数
     */
    total: number;
    /**
     * 当前页码
     */
    pageNum: number;
    /**
     * 每页条数
     */
    pageSize: number;
}

/**
 * 分页查询参数基础接口
 */
export interface PageQuery {
    pageNum: number;
    pageSize: number;
}

/**
 * 分页响应结果基础接口
 * @template T - 数据类型
 */
export interface PageResult<T> {
    /**
     * 数据列表
     */
    list: T[];
    /**
     * 总条数
     */
    total: number;
    /**
     * 当前页码
     */
    pageNum: number;
    /**
     * 每页条数
     */
    pageSize: number;
}

/**
 * 带分页的请求参数
 */
export interface PageParams {
    page?: number
    pageSize?: number
}

/**
 * 自定义请求配置
 */
export interface CustomRequestConfig extends AxiosRequestConfig {
    /**
     * 是否显示全局loading
     * @default true
     */
    showLoading?: boolean

    /**
     * 是否显示错误提示
     * @default true
     */
    showError?: boolean

    /**
     * 是否跳过拦截器处理
     * @default false
     */
    skipInterceptors?: boolean
}