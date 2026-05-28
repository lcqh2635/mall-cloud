import {type AppRouteRecordRaw, RouteNames} from '../types'

/**
 * 异常页面路由
 */
const exceptionRouter: AppRouteRecordRaw[] = [
    {
        path: '/403',
        name: RouteNames.FORBIDDEN,
        component: () => import('@/views/exception/403.vue'),
        meta: {
            title: '无权限访问',
            hidden: true
        }
    },
    {
        path: '/404',
        name: RouteNames.NOT_FOUND,
        component: () => import('@/views/exception/404.vue'),
        meta: {
            title: '页面不存在',
            hidden: true
        }
    },
    {
        path: '/500',
        name: RouteNames.SERVER_ERROR,
        component: () => import('@/views/exception/500.vue'),
        meta: {
            title: '服务器错误',
            hidden: true
        }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/404',
        meta: {
            hidden: true
        }
    }
]

export default exceptionRouter