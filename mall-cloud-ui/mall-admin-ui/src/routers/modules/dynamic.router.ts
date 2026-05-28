// // router/modules/dynamic.router.ts
// import type { AppRouteRecordRaw } from '../types'
// import router from "@/router";
//
// export async function setupDynamicRoutes() {
//     // 模拟从API获取动态路由
//     const asyncRoutes = await fetch('/api/routes').then(res => res.json())
//
//     // 格式化路由
//     const formattedRoutes = formatRoutes(asyncRoutes)
//
//     // 添加路由
//     formattedRoutes.forEach(route => {
//         if (!router.hasRoute(route.name!)) {
//             router.addRoute(route)
//         }
//     })
// }
//
// function formatRoutes(routes: any[]): AppRouteRecordRaw[] {
//     // 实现路由格式化逻辑
//     return []
// }