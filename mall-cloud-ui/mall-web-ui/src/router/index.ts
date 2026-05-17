import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import { moduleRoutes } from './modules'
// import { setupRouterGuards } from './guards'

/**
 * 创建路由实例
 */
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: moduleRoutes as unknown as RouteRecordRaw[], // 转换类型
    strict: true, // 严格模式
    // 滚动行为，参考：https://router.vuejs.org/zh/guide/advanced/scroll-behavior.html
    scrollBehavior: () => ({ left: 0, top: 0 })
})

/**
 * 重置路由
 * 注意：在动态路由场景下可能需要
 */
export function resetRouter() {
    const resetWhiteNameList = ['Login', 'Home']
    router.getRoutes().forEach(route => {
        const { name } = route
        if (name && !resetWhiteNameList.includes(name as string)) {
            router.removeRoute(name)
        }
    })
}

// 安装路由守卫
// setupRouterGuards(router)

export default router