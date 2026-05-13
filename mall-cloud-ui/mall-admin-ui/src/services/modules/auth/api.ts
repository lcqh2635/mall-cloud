// import http from '../../api/http.ts'
// import type {
//     LoginParams,
//     LoginResponse,
//     RefreshTokenResponse,
//     BaseResponse
// } from './types.ts'
// import type { CustomRequestConfig } from '../../api/types.ts'
//
// /**
//  * 认证服务API
//  */
// export const api = {
//     /**
//      * 用户登录
//      * @param params - 登录参数
//      * @param config - 自定义请求配置
//      */
//     login(params: LoginParams, config?: CustomRequestConfig) {
//         return http.post<BaseResponse<LoginResponse>>(
//             '/auth/login',
//             params,
//             {
//                 showLoading: true,
//                 showError: true,
//                 ...config
//             }
//         )
//     },
//
//     /**
//      * 刷新token
//      * @param refreshToken - 刷新token
//      */
//     refreshToken(refreshToken: string) {
//         return http.post<BaseResponse<RefreshTokenResponse>>(
//             '/auth/refresh-token',
//             { refreshToken },
//             { showError: false } // 刷新token失败不显示错误
//         )
//     },
//
//     /**
//      * 获取验证码
//      */
//     getCaptcha() {
//         return http.get<BaseResponse<{ captcha: string }>>(
//             '/auth/captcha',
//             { responseType: 'blob' }
//         )
//     },
//
//     /**
//      * 用户注销
//      */
//     logout() {
//         return http.post<BaseResponse>('/auth/logout')
//     },
//
//     /**
//      * 获取当前用户权限
//      */
//     getPermissions() {
//         return http.get<BaseResponse<string[]>>('/auth/permissions')
//     }
// }
//
// export type AuthService = typeof api