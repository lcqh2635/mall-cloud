import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
// import { setupInterceptors } from './interceptors.ts'

/**
 * 创建Axios实例
 * @param config - 自定义配置
 */
export function createAxiosInstance(config?: AxiosRequestConfig): AxiosInstance {
    const instance = axios.create({
        baseURL: import.meta.env.VITE_API_BASE_URL, // 从环境变量获取
        timeout: 10000, // 10秒超时
        headers: {
            'Content-Type': 'application/json',
            ...config?.headers
        },
        ...config
    })

    // 设置拦截器
    // setupInterceptors(instance)

    return instance
}

// 默认导出的axios实例
const http = createAxiosInstance()

export default http