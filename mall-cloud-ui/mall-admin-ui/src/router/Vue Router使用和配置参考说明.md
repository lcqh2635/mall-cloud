# 模块化路由目录结构最佳实践

您提出的路由目录结构是非常合理的，特别适合中大型 Vue3 + TS + Vite 项目。这种模块化组织方式有以下优势：

1. **职责分离**：将不同业务域的路由配置分开管理
2. **可维护性**：每个模块的路由独立维护，避免单一文件过大
3. **可扩展性**：新增业务模块时只需添加新的路由文件
4. **团队协作**：不同开发者可以并行处理不同模块的路由

下面我给出完整的实现示例：

## 目录结构

```
src/
├── router/
│   ├── guards/          # 路由守卫
│   │   ├── auth.ts      # 认证守卫
│   │   ├── progress.ts  # 页面进度条守卫
│   │   └── index.ts     # 路由守卫入口
│   ├── modules/         # 模块化路由
│   │   ├── auth.router.ts       # 认证相关路由
│   │   ├── exception.router.ts  # 异常相关路由
│   │   ├── dashboard.router.ts  # 仪表盘相关路由
│   │   └── index.ts     # 模块路由聚合
│   ├── types/           # 路由类型定义
│   │   └── index.ts     
│   └── index.ts         # Router 实例导出
```

## 1. 路由类型定义 (`router/types/index.ts`)

```typescript
import 'vue-router'

// 扩展路由的meta字段类型
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
```

## 2. 路由守卫 (`router/guards/`)

### 认证守卫 (`router/guards/auth.ts`)

```typescript
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { RouteNames } from '../constants'

/**
 * 认证守卫
 * @param router - 路由实例
 */
export function setupAuthGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const isAuthenticated = userStore.isAuthenticated
    
    // 需要认证但未登录
    if (to.meta.requiresAuth && !isAuthenticated) {
      next({
        name: RouteNames.LOGIN,
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // 已登录但访问登录页，重定向到首页
    if (to.name === RouteNames.LOGIN && isAuthenticated) {
      next({ name: RouteNames.HOME })
      return
    }
    
    // 检查角色权限
    if (to.meta.roles) {
      const hasRole = userStore.roles?.some(role => to.meta.roles?.includes(role))
      if (!hasRole) {
        next({ name: RouteNames.FORBIDDEN })
        return
      }
    }
    
    next()
  })
}
```

### 进度条守卫 (`router/guards/progress.ts`)

```typescript
import type { Router } from 'vue-router'
import nProgress from 'nprogress'
import 'nprogress/nprogress.css'

/**
 * 页面加载进度条守卫
 * @param router - 路由实例
 */
export function setupProgressGuard(router: Router) {
  router.beforeEach(() => {
    nProgress.start()
  })
  
  router.afterEach(() => {
    nProgress.done()
  })
  
  router.onError(() => {
    nProgress.done()
  })
}
```

### 守卫入口 (`router/guards/index.ts`)

```typescript
import type { Router } from 'vue-router'
import { setupAuthGuard } from './auth'
import { setupProgressGuard } from './progress'

/**
 * 安装路由守卫
 * @param router - 路由实例
 */
export function setupRouterGuards(router: Router) {
  setupAuthGuard(router)
  setupProgressGuard(router)
  // 可以添加更多守卫...
}
```

## 3. 模块化路由 (`router/modules/`)

### 常量定义 (`router/constants.ts`)

```typescript
export enum RouteNames {
  LOGIN = 'Login',
  HOME = 'Home',
  DASHBOARD = 'Dashboard',
  FORBIDDEN = 'Forbidden',
  NOT_FOUND = 'NotFound'
}
```

### 认证路由 (`router/modules/auth.router.ts`)

```typescript
import type { AppRouteRecordRaw } from '../types'
import { RouteNames } from '../constants'

/**
 * 认证相关路由
 */
const authRouter: AppRouteRecordRaw[] = [
  {
    path: '/login',
    name: RouteNames.LOGIN,
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      hidden: true // 不在菜单显示
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '注册',
      hidden: true
    }
  }
]

export default authRouter
```

### 异常路由 (`router/modules/exception.router.ts`)

```typescript
import type { AppRouteRecordRaw } from '../types'
import { RouteNames } from '../constants'

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
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: {
      hidden: true
    }
  }
]

export default exceptionRouter
```

### 仪表盘路由 (`router/modules/dashboard.router.ts`)

```typescript
import type { AppRouteRecordRaw } from '../types'
import { RouteNames } from '../constants'

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
    component: () => import('@/views/dashboard/index.vue'),
    meta: {
      title: '控制台',
      icon: 'dashboard',
      affix: true
    }
  },
  {
    path: '/dashboard/workbench',
    name: 'Workbench',
    component: () => import('@/views/dashboard/workbench.vue'),
    meta: {
      title: '工作台',
      keepAlive: true
    }
  }
]

export default dashboardRouter
```

### 模块路由聚合 (`router/modules/index.ts`)

```typescript
import type { AppRouteRecordRaw } from '../types'
import authRouter from './auth.router'
import exceptionRouter from './exception.router'
import dashboardRouter from './dashboard.router'

/**
 * 聚合所有模块路由
 */
export const moduleRoutes: AppRouteRecordRaw[] = [
  ...authRouter,
  ...dashboardRouter,
  ...exceptionRouter
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
```

## 4. 路由实例 (`router/index.ts`)

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import { moduleRoutes } from './modules'
import { setupRouterGuards } from './guards'
import { AppRouteRecordRaw } from './types'

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: moduleRoutes as unknown as RouteRecordRaw[], // 转换类型
  strict: true, // 严格模式
  scrollBehavior: () => ({ left: 0, top: 0 }) // 滚动行为
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
setupRouterGuards(router)

export default router
```

## 5. 在 main.ts 中使用

```typescript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

const app = createApp(App)

app.use(createPinia())
app.use(router)

router.isReady().then(() => {
  app.mount('#app')
})
```

## 6. 动态路由扩展示例

对于需要动态添加路由的场景（如权限路由），可以这样扩展：

```typescript
// router/modules/dynamic.router.ts
import type { AppRouteRecordRaw } from '../types'

export async function setupDynamicRoutes() {
  // 模拟从API获取动态路由
  const asyncRoutes = await fetch('/api/routes').then(res => res.json())
  
  // 格式化路由
  const formattedRoutes = formatRoutes(asyncRoutes)
  
  // 添加路由
  formattedRoutes.forEach(route => {
    if (!router.hasRoute(route.name!)) {
      router.addRoute(route)
    }
  })
}

function formatRoutes(routes: any[]): AppRouteRecordRaw[] {
  // 实现路由格式化逻辑
  return []
}
```

## 最佳实践建议

1. **路由命名规范**：
    - 使用常量枚举管理路由名称 (`RouteNames`)
    - 命名风格保持一致 (PascalCase 或 camelCase)

2. **路由元信息**：
    - 使用 `meta` 字段存储路由额外信息
    - 扩展 `RouteMeta` 类型获得更好的 TS 支持

3. **动态路由**：
    - 按需加载路由组件
    - 根据用户权限动态添加路由

4. **路由守卫**：
    - 将不同职责的守卫拆分到不同文件
    - 全局守卫处理通用逻辑 (如认证、进度条)
    - 路由独享守卫处理特定逻辑

5. **性能优化**：
    - 使用路由懒加载
    - 合理使用 `keep-alive` 缓存组件
    - 避免在全局守卫中执行耗时操作

这种模块化结构能够很好地支持项目规模的增长，保持代码的可维护性和可扩展性。根据项目实际需求，可以进一步调整或扩展这个结构。