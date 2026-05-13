// import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
// import router from '@/router'
// import {useAuthStore} from "@/stores/modules/auth.store.ts";
//
// /**
//  * 配置拦截器
//  * @param instance - Axios实例
//  */
// export function setupInterceptors(instance: AxiosInstance) {
//     // 请求拦截器
//     instance.interceptors.request.use(
//         (config: InternalAxiosRequestConfig) => {
//             const authStore = useAuthStore()
//
//             // 添加认证token
//             if (authStore.token && !isPublicEndpoint(config.url!)) {
//                 config.headers.Authorization = `Bearer ${authStore.token}`
//             }
//
//             // 可在此处添加全局loading开始逻辑
//             return config
//         },
//         (error) => {
//             return Promise.reject(error)
//         }
//     )
//
//     // 响应拦截器
//     instance.interceptors.response.use(
//         (response: AxiosResponse) => {
//             // 可在此处添加全局loading结束逻辑
//
//             // 处理业务成功但code非200的情况
//             if (response.data?.code !== 200) {
//                 return handleBusinessError(response.data)
//             }
//
//             return response.data.data ?? response.data // 返回实际数据
//         },
//         (error) => {
//             // 处理HTTP错误状态码
//             return handleHttpError(error)
//         }
//     )
// }
//
// /**
//  * 判断是否为公开端点
//  */
// function isPublicEndpoint(url: string): boolean {
//     const publicEndpoints = ['/auth/login', '/auth/refresh-token']
//     return publicEndpoints.some(endpoint => url.includes(endpoint))
// }
//
// /**
//  * 处理业务逻辑错误
//  */
// function handleBusinessError(response: any) {
//     // 可根据不同code做不同处理
//     if (response.code === 401) {
//         // token过期处理
//         return handleUnauthorized()
//     }
//
//     // 其他业务错误
//     return Promise.reject(response.message || '业务错误')
// }
//
// /**
//  * 处理HTTP错误
//  */
// function handleHttpError(error: any) {
//     if (error.response) {
//         switch (error.response.status) {
//             case 401:
//                 return handleUnauthorized()
//             case 403:
//                 router.push('/403')
//                 break
//             case 404:
//                 router.push('/404')
//                 break
//             case 500:
//                 router.push('/500')
//                 // 服务端错误处理
//                 break
//         }
//     }
//     return Promise.reject(error)
// }
//
// /**
//  * 处理未授权情况
//  */
// function handleUnauthorized() {
//     const authStore = useAuthStore()
//     authStore.logout()
//     router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
//     return Promise.reject('登录已过期，请重新登录')
// }