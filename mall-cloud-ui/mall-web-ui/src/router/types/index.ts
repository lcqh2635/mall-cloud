import 'vue-router'
import type {RouteMeta} from "vue-router";

// 扩展路由的 meta 字段类型
declare module 'vue-router' {
    interface RouteMeta {
        /** 页面标题 */
        title?: string
        /** 是否需要认证 */
        requiresAuth?: boolean
        /** 需要的权限角色 */
        roles?: string[]
        /** 是否在菜单中隐藏 */
        hidden?: boolean
        /** 菜单图标 */
        icon?: string
        /** 是否缓存该页面 */
        keepAlive?: boolean
        /** 是否固定在标签页 */
        affix?: boolean
    }
}

// 基础路由类型
export interface AppRouteRecordRaw {
    path: string
    name?: string
    component?: Component | string
    components?: Component
    redirect?: string
    meta?: RouteMeta
    children?: AppRouteRecordRaw[]
}

// 路由名称常量定义，使用常量替代枚举（推荐）
export const RouteNames = {
    HOME: 'Home',
    LOGIN: 'Login',
    DASHBOARD: 'Dashboard',
    FORBIDDEN: 'Forbidden',
    NOT_FOUND: 'NotFound',
    SERVER_ERROR: 'ServerError',
} as const