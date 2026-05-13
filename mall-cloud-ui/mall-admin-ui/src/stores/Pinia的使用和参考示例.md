# Pinia + pinia-plugin-persistedstate 在 Vue3+TS+Vite 项目中的使用指南

下面我将为你提供一个完整的配置和使用示例，包含模块化store的创建、持久化配置以及类型安全的使用方式。

## 1. 安装依赖

首先确保已安装必要的依赖：

```bash
npm install pinia pinia-plugin-persistedstate
# 或
yarn add pinia pinia-plugin-persistedstate
```

## 2. 基础配置 (`stores/index.ts`)

```typescript
// stores/index.ts
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 创建 pinia 实例
const pinia = createPinia()

// 使用持久化插件
pinia.use(piniaPluginPersistedstate)

// 导出 pinia 实例，用于在 main.ts 中注册
export default pinia

// 统一导出模块，避免在组件中逐个导入
export * from './modules/user'
export * from './modules/app'
// ...其他模块
```

## 3. 模块化 Store 示例 (`stores/modules/user.ts`)

```typescript
// stores/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user' // 假设有定义的用户类型

/**
 * 用户信息store
 * - 包含用户状态、token等信息
 * - 自动持久化到localStorage
 */
export const useUserStore = defineStore(
  'user', // store的唯一ID
  () => {
    // state
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)
    const roles = ref<string[]>([])

    // getters
    const isLogin = computed(() => !!token.value)
    const isAdmin = computed(() => roles.value.includes('admin'))

    // actions
    function setToken(value: string) {
      token.value = value
    }
    
    function setUserInfo(info: UserInfo) {
      userInfo.value = info
      roles.value = info.roles || []
    }
    
    function clearUser() {
      token.value = ''
      userInfo.value = null
      roles.value = []
    }

    return {
      token,
      userInfo,
      roles,
      isLogin,
      isAdmin,
      setToken,
      setUserInfo,
      clearUser
    }
  },
  {
    // 持久化配置
    persist: {
      key: 'vue3-admin-user', // 存储的key
      storage: localStorage, // 存储方式，默认sessionStorage
      pick: ['token', 'userInfo'], // 只持久化token和userInfo
      // 如果需要自定义序列化/反序列化
      // serializer: {
      //   serialize: JSON.stringify,
      //   deserialize: JSON.parse
      // },
      // 如果需要加密
      // beforeRestore: (ctx) => {
      //   console.log('即将恢复userStore')
      // },
      // afterRestore: (ctx) => {
      //   console.log('userStore恢复完成')
      // }
    }
  }
)
```

## 4. 另一个模块示例 (`stores/modules/app.ts`)

```typescript
// stores/modules/app.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用配置store
 * - 包含主题、布局等全局配置
 * - 持久化到sessionStorage
 */
export const useAppStore = defineStore(
  'app',
  () => {
    // 主题模式 'light' | 'dark'
    const theme = ref<'light' | 'dark'>('light')
    // 侧边栏是否折叠
    const sidebarCollapsed = ref(false)
    // 是否显示设置面板
    const showSettings = ref(false)

    // 切换主题
    function toggleTheme() {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
    }
    
    // 切换侧边栏状态
    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    return {
      theme,
      sidebarCollapsed,
      showSettings,
      toggleTheme,
      toggleSidebar
    }
  },
  {
    persist: {
      key: 'vue3-admin-app',
      storage: sessionStorage, // 使用sessionStorage而不是localStorage
      pick: ['theme', 'sidebarCollapsed'] // 只持久化这两个状态
    }
  }
)
```

## 5. 在 main.ts 中注册 Pinia

```typescript
// main.ts
import { createApp } from 'vue'
import App from './App.vue'
import pinia from './stores'

const app = createApp(App)

app.use(pinia)

app.mount('#app')
```

## 6. 在组件中使用 Store

```vue
<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useUserStore, useAppStore } from '@/stores'

// 获取store实例
const userStore = useUserStore()
const appStore = useAppStore()

// 使用storeToRefs保持响应式解构
const { token, isLogin, userInfo } = storeToRefs(userStore)
const { theme, sidebarCollapsed } = storeToRefs(appStore)

// 调用action
function handleLogin() {
  userStore.setToken('mock-token')
  userStore.setUserInfo({
    id: 1,
    name: 'Admin',
    roles: ['admin']
  })
}

function handleLogout() {
  userStore.clearUser()
}

function toggleTheme() {
  appStore.toggleTheme()
}
</script>

<template>
  <div :class="['app', theme]">
    <header>
      <span v-if="isLogin">欢迎, {{ userInfo?.name }}</span>
      <button @click="toggleTheme">切换主题</button>
      <button v-if="isLogin" @click="handleLogout">退出登录</button>
    </header>
    <main>
      <button v-if="!isLogin" @click="handleLogin">模拟登录</button>
    </main>
  </div>
</template>
```

## 7. 在 Composition API 外部使用 Store

```typescript
// 例如在axios拦截器中使用
import pinia from '@/stores'
import { useUserStore } from '@/stores/modules/user'

// 注意：在setup外部使用时需要传入pinia实例
const userStore = useUserStore(pinia)

axios.interceptors.request.use(config => {
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})
```

## 8. 高级配置选项

`pinia-plugin-persistedstate` 提供了丰富的配置选项：

```typescript
persist: {
  key: 'custom-key', // 存储键名
  storage: sessionStorage, // 指定存储方式
  paths: ['token', 'userInfo'], // 只持久化部分状态
  serializer: { // 自定义序列化
    serialize: (value) => JSON.stringify(value),
    deserialize: (value) => JSON.parse(value)
  },
  beforeRestore: (context) => { // 恢复前钩子
    console.log('即将恢复', context)
  },
  afterRestore: (context) => { // 恢复后钩子
    console.log('恢复完成', context)
  },
  debug: true // 调试模式
}
```

## 9. 类型安全的最佳实践

1. **为每个store定义接口类型**：

```typescript
// types/user.ts
export interface UserInfo {
  id: number
  name: string
  avatar?: string
  roles: string[]
  // 其他用户字段
}

export interface UserState {
  token: string
  userInfo: UserInfo | null
  roles: string[]
}
```

2. **在store中使用类型**：

```typescript
const userStore = defineStore('user', () => {
  // 使用明确的类型注解
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  // ...
})
```

## 10. 测试Store

```typescript
// stores/modules/__tests__/user.spec.ts
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('should set token and user info', () => {
    const store = useUserStore()
    
    expect(store.token).toBe('')
    expect(store.isLogin).toBe(false)
    
    store.setToken('test-token')
    store.setUserInfo({ id: 1, name: 'Test', roles: ['user'] })
    
    expect(store.token).toBe('test-token')
    expect(store.isLogin).toBe(true)
    expect(store.userInfo?.name).toBe('Test')
  })

  it('should persist state', () => {
    const store1 = useUserStore()
    store1.setToken('persisted-token')
    
    // 模拟页面刷新后重新创建store
    const store2 = useUserStore()
    
    expect(store2.token).toBe('persisted-token')
  })
})
```

## 总结

1. **模块化组织**：将不同的业务状态拆分到不同的store文件中
2. **类型安全**：为每个store和它的state、getters、actions提供明确的类型定义
3. **持久化策略**：
    - 关键用户信息使用`localStorage`长期存储
    - 临时配置使用`sessionStorage`会话级存储
    - 敏感信息考虑加密或避免持久化
4. **性能优化**：
    - 只持久化必要的状态(`paths`配置)
    - 避免存储大量数据
5. **调试**：开发时开启`debug`选项帮助排查问题

这种组织方式可以很好地支持中大型项目的状态管理需求，保持代码的可维护性和可扩展性。