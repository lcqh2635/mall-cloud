// import type { Router } from 'vue-router'
// import { useUserStore } from '@/stores/modules/user.store'
// import {RouteNames} from "@/router/types";
//
// /**
//  * 认证路由守卫
//  * @param router - 路由实例
//  */
// export function setupAuthGuard(router: Router) {
//     router.beforeEach(async (to, from, next) => {
//         const userStore = useUserStore()
//         const isAuthenticated = userStore.isAuthenticated
//
//         // 需要认证但未登录
//         if (to.meta.requiresAuth && !isAuthenticated) {
//             next({
//                 name: RouteNames.LOGIN,
//                 query: { redirect: to.fullPath }
//             })
//             return
//         }
//
//         // 已登录但访问登录页，重定向到首页
//         if (to.name === RouteNames.LOGIN && isAuthenticated) {
//             next({ name: RouteNames.HOME })
//             return
//         }
//
//         // 检查角色权限
//         if (to.meta.roles) {
//             const hasRole = userStore.roles?.some(role => to.meta.roles?.includes(role))
//             if (!hasRole) {
//                 next({ name: RouteNames.FORBIDDEN })
//                 return
//             }
//         }
//
//         next()
//     })
// }