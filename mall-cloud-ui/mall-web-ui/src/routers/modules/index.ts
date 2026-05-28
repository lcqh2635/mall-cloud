import type { AppRouteRecordRaw } from '../types'
import authRoutes from './auth.router'
import exceptionRoutes from './exception.router'
import dashboardRoutes from './dashboard.router'

/**
 * 聚合所有模块路由
 */
export const moduleRoutes: AppRouteRecordRaw[] = [
    ...authRoutes,
    ...dashboardRoutes,
    ...exceptionRoutes
]

/**
 * 获取路由名称映射表
 * 用于快速查找路由是否存在
 */
export function getRouteNameMap(routes: AppRouteRecordRaw[]) {
    const map = new Map<string, boolean>()

    function traverse(routes: AppRouteRecordRaw[]) {
        routes.forEach(route => {
            if (route.name) map.set(route.name as string, true)
            if (route.children) traverse(route.children)
        })
    }

    traverse(routes)
    return map
}