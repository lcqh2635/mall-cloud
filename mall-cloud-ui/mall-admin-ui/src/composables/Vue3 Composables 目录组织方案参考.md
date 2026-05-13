# Vue3 Composables 目录组织方案

Composables 是 Vue3 组合式 API 的核心实践，良好的组织可以显著提高代码复用性和可维护性。下面是一个基于实际项目的组织方案，包含详细注释和示例。

## 推荐目录结构

```
src/
├── composables/
│   ├── core/                # 核心基础hooks
│   │   ├── useDebounce.ts   # 防抖hook
│   │   ├── useThrottle.ts   # 节流hook
│   │   ├── useToggle.ts     # 状态切换hook
│   │   └── ...             
│   │
│   ├── dom/                 # DOM相关hooks
│   │   ├── useClickOutside.ts # 点击外部hook
│   │   ├── useElementSize.ts  # 元素尺寸hook
│   │   └── ...
│   │
│   ├── network/             # 网络请求相关
│   │   ├── useFetch.ts      # 基础请求hook
│   │   ├── useAxios.ts      # axios封装hook
│   │   └── ...
│   │
│   ├── state/               # 状态管理相关
│   │   ├── useLocalStorage.ts # localStorage hook
│   │   ├── useSessionStorage.ts
│   │   └── ...
│   │
│   ├── ui/                  # UI交互相关
│   │   ├── useDarkMode.ts   # 暗黑模式hook
│   │   ├── useModal.ts      # 弹窗控制hook
│   │   └── ...
│   │
│   ├── business/            # 业务相关hooks
│   │   ├── auth/            # 认证相关
│   │   │   ├── useAuth.ts   # 认证逻辑
│   │   │   └── usePermissions.ts
│   │   ├── cart/           # 购物车相关
│   │   │   └── useCart.ts
│   │   └── ...
│   │
│   ├── index.ts             # 统一导出入口
│   └── types/               # 类型定义
│       ├── network.d.ts
│       ├── dom.d.ts
│       └── ...
```

## 核心文件示例与注释

### 1. 基础工具类 Hook (`core/useDebounce.ts`)

```typescript
import { ref, watch, onUnmounted } from 'vue'
import type { Ref } from 'vue'

/**
 * 防抖Hook
 * @param value - 需要防抖的值(Ref或普通值)
 * @param delay - 防抖延迟时间(ms)，默认300ms
 * @returns 防抖处理后的Ref值
 * 
 * @example
 * const searchText = useDebounce(ref(''), 500)
 * watch(searchText, (val) => { console.log(val) })
 */
export function useDebounce<T>(value: Ref<T> | T, delay = 300) {
  const debouncedValue = ref(value) as Ref<T>
  let timeoutId: ReturnType<typeof setTimeout>

  // 处理值变化
  const updateValue = (newValue: T) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  }

  // 如果传入的是Ref，设置监听
  if (isRef(value)) {
    watch(value, (newVal) => updateValue(newVal))
  } else {
    updateValue(value)
  }

  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearTimeout(timeoutId)
  })

  return debouncedValue
}
```

### 2. DOM 相关 Hook (`dom/useClickOutside.ts`)

```typescript
import { onMounted, onUnmounted, Ref } from 'vue'

/**
 * 检测点击元素外部区域的Hook
 * @param targetRef - 目标元素的Ref
 * @param callback - 点击外部时的回调函数
 * 
 * @example
 * const dropdownRef = ref(null)
 * useClickOutside(dropdownRef, () => {
 *   // 关闭下拉菜单
 * })
 */
export function useClickOutside(
  targetRef: Ref<HTMLElement | null>,
  callback: (event: MouseEvent) => void
) {
  const listener = (event: MouseEvent) => {
    // 检查点击是否在目标元素内部
    if (!targetRef.value || targetRef.value.contains(event.target as Node)) {
      return
    }
    callback(event)
  }

  onMounted(() => {
    document.addEventListener('click', listener)
  })

  onUnmounted(() => {
    document.removeEventListener('click', listener)
  })
}
```

### 3. 网络请求 Hook (`network/useFetch.ts`)

```typescript
import { ref } from 'vue'
import type { Ref } from 'vue'

interface UseFetchOptions {
  immediate?: boolean // 是否立即执行
}

/**
 * 封装fetch请求的Hook
 * @param url - 请求URL
 * @param options - 请求选项
 * @param fetchOptions - fetch API选项
 * @returns 响应数据、错误状态和加载状态
 * 
 * @example
 * const { data, error, loading, execute } = useFetch('/api/data')
 */
export function useFetch<T = any>(
  url: string,
  options: UseFetchOptions = { immediate: true },
  fetchOptions?: RequestInit
) {
  const data: Ref<T | null> = ref(null)
  const error: Ref<Error | null> = ref(null)
  const loading: Ref<boolean> = ref(false)

  const execute = async (body?: any) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await fetch(url, {
        ...fetchOptions,
        body: body ? JSON.stringify(body) : undefined,
        headers: {
          'Content-Type': 'application/json',
          ...fetchOptions?.headers
        }
      })
      
      if (!response.ok) {
        throw new Error(response.statusText)
      }
      
      data.value = await response.json()
    } catch (err) {
      error.value = err as Error
    } finally {
      loading.value = false
    }
  }

  // 立即执行
  if (options.immediate) {
    execute()
  }

  return { data, error, loading, execute }
}
```

### 4. 业务相关 Hook (`business/auth/useAuth.ts`)

```typescript
import { computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

interface LoginParams {
  username: string
  password: string
}

/**
 * 认证相关逻辑Hook
 * 封装登录、登出、权限检查等业务逻辑
 * 
 * @example
 * const { isAuthenticated, login, logout } = useAuth()
 */
export function useAuth() {
  const router = useRouter()
  const userStore = useUserStore()

  const state = reactive({
    loading: false,
    error: null as string | null
  })

  // 计算属性：是否已认证
  const isAuthenticated = computed(() => !!userStore.token)

  // 登录方法
  const login = async (params: LoginParams) => {
    state.loading = true
    state.error = null
    
    try {
      await userStore.login(params)
      router.push('/dashboard')
    } catch (error) {
      state.error = error.message || '登录失败'
    } finally {
      state.loading = false
    }
  }

  // 登出方法
  const logout = () => {
    userStore.logout()
    router.push('/login')
  }

  return {
    isAuthenticated,
    login,
    logout,
    ...state
  }
}
```

### 5. 统一导出文件 (`composables/index.ts`)

```typescript
// 核心Hooks
export { useDebounce } from './core/useDebounce'
export { useThrottle } from './core/useThrottle'
export { useToggle } from './core/useToggle'

// DOM相关
export { useClickOutside } from './dom/useClickOutside'
export { useElementSize } from './dom/useElementSize'

// 网络相关
export { useFetch } from './network/useFetch'
export { useAxios } from './network/useAxios'

// 状态管理
export { useLocalStorage } from './state/useLocalStorage'

// 业务相关
export { useAuth } from './business/auth/useAuth'
export { useCart } from './business/cart/useCart'

// 类型导出
export type { LoginParams } from './business/auth/useAuth'
```

## 类型定义示例 (`composables/types/network.d.ts`)

```typescript
// 网络请求相关类型定义

/**
 * 基础请求返回类型
 */
interface BaseResponse<T = any> {
  code: number
  data: T
  message?: string
}

/**
 * 分页数据返回类型
 */
interface PaginatedResponse<T> extends BaseResponse<T[]> {
  total: number
  page: number
  pageSize: number
}

/**
 * 请求错误类型
 */
interface RequestError {
  message: string
  status?: number
  code?: string
}

export type { BaseResponse, PaginatedResponse, RequestError }
```

## 使用示例

### 在组件中使用

```vue
<script setup lang="ts">
import { useFetch, useDebounce } from '@/composables'
import { ref, watch } from 'vue'

// 使用防抖Hook处理搜索输入
const searchQuery = ref('')
const debouncedQuery = useDebounce(searchQuery, 500)

// 使用fetch Hook获取数据
const { data: products, loading, error, execute } = useFetch('/api/products', {
  immediate: false
})

// 监听防抖后的查询变化
watch(debouncedQuery, (newQuery) => {
  if (newQuery) {
    execute(`/api/products?q=${newQuery}`)
  }
})
</script>
```

### 在业务逻辑中使用

```vue
<script setup lang="ts">
import { useAuth } from '@/composables'

const { isAuthenticated, login, logout, loading, error } = useAuth()

const form = reactive({
  username: '',
  password: ''
})

const handleSubmit = () => {
  login(form)
}
</script>
```

## 最佳实践建议

1. **命名规范**：
    - 始终以 `use` 前缀开头 (如 `useDarkMode`)
    - 使用驼峰命名法
    - 动词+名词形式 (如 `useFetchData`)

2. **单一职责**：
    - 每个 Hook 只解决一个特定问题
    - 复杂逻辑拆分为多个小 Hook 组合使用

3. **类型安全**：
    - 为所有 Hook 提供 TypeScript 类型定义
    - 为参数和返回值添加类型注释

4. **可配置性**：
    - 提供合理的默认值
    - 通过选项对象参数支持灵活配置

5. **生命周期管理**：
    - 清理副作用 (如事件监听器、定时器)
    - 使用 `onUnmounted` 确保资源释放

6. **文档注释**：
    - 使用 JSDoc 格式注释
    - 包含使用示例
    - 说明参数和返回值

7. **测试**：
    - 为可复用 Hook 编写单元测试
    - 测试文件放在同目录下 (如 `useFetch.test.ts`)

通过这种组织方式，你的 Composables 将保持清晰的结构和高复用性，团队成员可以轻松找到和使用现有的逻辑封装。