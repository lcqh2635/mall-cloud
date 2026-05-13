# Vue3 + TS + Vite 动态布局切换实现方案

下面我将详细介绍如何在 Vue3 + TS + Vite 项目中实现动态布局切换，包括项目目录结构组织和具体实现代码。

## 项目目录结构建议

```
src/
├── App.vue                  # 根组件
├── main.ts                  # 应用入口
├── layouts/                 # 布局组件目录
│   ├── DefaultLayout.vue    # 默认布局
│   ├── AdminLayout.vue      # 后台管理布局
│   ├── AuthLayout.vue       # 认证相关布局
│   └── EmptyLayout.vue      # 空布局（无页眉页脚）
├── router/                  # 路由配置
│   ├── index.ts             # 路由主文件
│   └── routes.ts            # 路由定义
├── stores/                  # 状态管理
│   └── layout.ts            # 布局状态管理
├── views/                   # 页面组件
│   ├── Home.vue             # 首页
│   ├── About.vue            # 关于页
│   ├── admin/               # 后台管理相关页面
│   └── auth/                # 认证相关页面
└── types/                   # 类型定义
    └── layout.d.ts          # 布局相关类型
```

## 实现步骤

### 1. 定义布局类型

`src/types/layout.d.ts`

```typescript
// 定义所有可用的布局类型
export type LayoutType = 'default' | 'admin' | 'auth' | 'empty'

// 布局组件映射类型
export interface LayoutComponent {
  default: DefineComponent<{}, {}, any>
}
```

### 2. 创建布局组件

`src/layouts/DefaultLayout.vue`

```vue
<template>
  <div class="default-layout">
    <header>默认布局头部</header>
    <main>
      <slot /> <!-- 页面内容将在这里渲染 -->
    </main>
    <footer>默认布局底部</footer>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'DefaultLayout'
})
</script>
```

其他布局组件类似创建（AdminLayout.vue, AuthLayout.vue, EmptyLayout.vue）

### 3. 创建布局状态管理

`src/stores/layout.ts` (使用 Pinia)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LayoutType } from '@/types/layout'

export const useLayoutStore = defineStore('layout', () => {
  // 当前布局类型
  const layoutType = ref<LayoutType>('default')
  
  // 布局组件动态导入
  const layoutComponent = computed(() => {
    return defineAsyncComponent(() => import(`@/layouts/${layoutType.value.charAt(0).toUpperCase() + layoutType.value.slice(1)}Layout.vue`))
  })
  
  // 设置布局
  function setLayout(type: LayoutType) {
    layoutType.value = type
  }
  
  return {
    layoutType,
    layoutComponent,
    setLayout
  }
})
```

### 4. 配置路由

`src/router/routes.ts`

```typescript
import type { RouteRecordRaw } from 'vue-router'
import type { LayoutType } from '@/types/layout'

export const routes: Array<RouteRecordRaw & { meta?: { layout?: LayoutType } }> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      layout: 'default' // 指定布局
    }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue'),
    meta: {
      layout: 'default'
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Index.vue'),
    meta: {
      layout: 'admin',
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      layout: 'auth'
    }
  }
]
```

`src/router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useLayoutStore } from '@/stores/layout'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫中设置布局
router.beforeEach((to, from, next) => {
  const layoutStore = useLayoutStore()
  
  // 根据路由元信息设置布局
  if (to.meta?.layout) {
    layoutStore.setLayout(to.meta.layout as LayoutType)
  } else {
    // 默认布局
    layoutStore.setLayout('default')
  }
  
  next()
})

export default router
```

### 5. 修改 App.vue 实现动态布局

`src/App.vue`

```vue
<template>
  <component :is="layoutComponent">
    <router-view />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLayoutStore } from '@/stores/layout'

const layoutStore = useLayoutStore()
const layoutComponent = computed(() => layoutStore.layoutComponent)
</script>
```

### 6. 动态切换布局示例

在组件中动态切换布局：

```vue
<template>
  <button @click="changeLayout('admin')">切换到后台布局</button>
  <button @click="changeLayout('default')">切换到默认布局</button>
</template>

<script setup lang="ts">
import { useLayoutStore } from '@/stores/layout'

const layoutStore = useLayoutStore()

function changeLayout(type: LayoutType) {
  layoutStore.setLayout(type)
}
</script>
```

## 高级功能扩展

### 1. 布局过渡动画

```vue
<template>
  <transition name="fade" mode="out-in">
    <component :is="layoutComponent">
      <router-view />
    </component>
  </transition>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
```

### 2. 持久化布局状态

修改 `src/stores/layout.ts`:

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LayoutType } from '@/types/layout'

export const useLayoutStore = defineStore('layout', () => {
  // 从 localStorage 读取保存的布局
  const savedLayout = localStorage.getItem('layout') as LayoutType | null
  const layoutType = ref<LayoutType>(savedLayout || 'default')
  
  const layoutComponent = computed(() => {
    return defineAsyncComponent(() => import(`@/layouts/${layoutType.value.charAt(0).toUpperCase() + layoutType.value.slice(1)}Layout.vue`))
  })
  
  function setLayout(type: LayoutType) {
    layoutType.value = type
    // 保存到 localStorage
    localStorage.setItem('layout', type)
  }
  
  return {
    layoutType,
    layoutComponent,
    setLayout
  }
})
```

### 3. 响应式布局

根据屏幕尺寸自动切换布局：

```typescript
// 在 layout store 中添加
const isMobile = ref(false)

function checkScreenSize() {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value && layoutType.value !== 'mobile') {
    setLayout('mobile')
  } else if (!isMobile.value && layoutType.value === 'mobile') {
    setLayout('default')
  }
}

// 初始化时检查
checkScreenSize()

// 监听窗口变化
window.addEventListener('resize', checkScreenSize)
```

## 最佳实践建议

1. **命名规范**：布局组件统一以 `Layout` 结尾，如 `AdminLayout.vue`
2. **最小化布局组件**：只包含与布局相关的代码，业务逻辑放在页面组件中
3. **性能优化**：使用异步组件加载布局和页面
4. **类型安全**：为所有布局类型和元数据定义 TypeScript 类型
5. **默认布局**：确保有一个合理的默认布局
6. **布局嵌套**：支持布局嵌套可以实现更复杂的需求
7. **错误处理**：处理动态导入布局组件失败的情况

通过以上实现，你的 Vue3 项目将具备灵活的动态布局切换能力，可以根据路由、用户偏好或设备类型自动切换不同的布局结构。