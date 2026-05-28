import {type AppRouteRecordRaw, RouteNames} from '../types'

/**
 * 仪表盘相关路由
 */
const dashboardRouter: AppRouteRecordRaw[] = [
    {
        path: '/',
        name: RouteNames.HOME,
        redirect: '/dashboard',
        meta: {
            hidden: true
        }
    },
    {
        path: '/dashboard',
        name: RouteNames.DASHBOARD,
        component: () => import('@views/dashboard/index.vue'),
        meta: {
            title: '控制台',
            icon: 'dashboard',
            affix: true
        }
    },
    {
        path: '/dashboard/workbench',
        name: 'Workbench',
        component: () => import('@views/dashboard/workbench.vue'),
        meta: {
            title: '工作台',
            keepAlive: true
        }
    }
]

export default dashboardRouter