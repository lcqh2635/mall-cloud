// /**
//  * 用户认证模块类型定义
//  */
//
// // 用户基本信息
// export interface UserProfile {
//     id: string
//     username: string
//     email: string
//     avatar?: string
//     role: 'admin' | 'user' | 'guest'
//     createdAt: Date
//     lastLoginAt?: Date
// }
//
// // 登录凭证
// export interface AuthToken {
//     accessToken: string
//     refreshToken?: string
//     expiresIn: number // 过期时间(秒)
// }
//
// // 登录表单
// export interface LoginForm {
//     email: string
//     password: string
//     rememberMe?: boolean
// }
//
// // 注册表单
// export interface RegisterForm extends LoginForm {
//     username: string
//     confirmPassword: string
// }
//
// // 认证状态
// export interface AuthState {
//     token: AuthToken | null
//     user: UserProfile | null
//     isLoading: boolean
//     error: string | null
// }
//
// // 认证Store类型
// export type AuthStore = ReturnType<typeof useAuthStore>