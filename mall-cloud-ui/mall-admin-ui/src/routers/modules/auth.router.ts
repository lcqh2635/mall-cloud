import {type AppRouteRecordRaw, RouteNames} from '../types'

/**
 * 认证相关路由
 */
const authRouter: AppRouteRecordRaw[] = [
    {
        path: '/login',
        name: RouteNames.LOGIN,
        component: () => import('@views/auth/LoginView.vue'),
        meta: {
            title: '登录',
            hidden: true // 不在菜单显示
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@views/auth/RegisterView.vue'),
        meta: {
            title: '注册',
            hidden: true
        }
    }
]

export default authRouter