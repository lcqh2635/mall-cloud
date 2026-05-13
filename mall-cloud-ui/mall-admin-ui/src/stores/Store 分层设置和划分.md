# Vue3 + TS + Vite + Pinia 项目中的 Store 设计指南

在大型 Vue3 项目中，合理的 Store 划分对于代码可维护性和团队协作至关重要。下面我将详细介绍 Pinia Store 的设计原则和常见实现。

## Store 划分依据

1. **业务模块划分**：按照产品功能模块划分（如用户、订单、商品等）
2. **数据关联性**：将关联紧密的数据放在同一个 Store
3. **数据使用频率**：高频使用的数据可独立存储
4. **数据复杂度**：复杂数据逻辑单独管理
5. **团队协作**：便于不同开发者并行工作

## 推荐的 Store 结构

### 1. 用户认证 Store (auth.store.ts)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserProfile } from '@/types/user'
import { login, logout, fetchProfile } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserProfile | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getter
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = null
    localStorage.removeItem('token')
  }

  const loginUser = async (credentials: { email: string; password: string }) => {
    try {
      isLoading.value = true
      error.value = null
      const { token: authToken } = await login(credentials)
      setToken(authToken)
      await loadUserProfile()
    } catch (err) {
      error.value = err instanceof Error ? err.message : '登录失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const loadUserProfile = async () => {
    try {
      if (!token.value) return
      user.value = await fetchProfile()
    } catch (err) {
      clearToken()
      throw err
    }
  }

  const logoutUser = async () => {
    await logout()
    clearToken()
    user.value = null
  }

  return {
    token,
    user,
    isLoading,
    error,
    isAuthenticated,
    isAdmin,
    loginUser,
    logoutUser,
    loadUserProfile
  }
})
```

### 2. 商品管理 Store (product.store.ts)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Product, ProductFilter } from '@/types/product'
import { fetchProducts, fetchProductDetail } from '@/api/products'

export const useProductStore = defineStore('product', () => {
  // 状态
  const products = ref<Product[]>([])
  const featuredProducts = ref<Product[]>([])
  const currentProduct = ref<Product | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  })

  // Getter
  const productCount = computed(() => products.value.length)
  const hasNextPage = computed(
    () => pagination.value.page * pagination.value.pageSize < pagination.value.total
  )

  // Actions
  const loadProducts = async (filter: ProductFilter = {}) => {
    try {
      isLoading.value = true
      error.value = null
      const response = await fetchProducts({
        ...filter,
        page: pagination.value.page,
        pageSize: pagination.value.pageSize
      })
      products.value = response.data
      pagination.value.total = response.total
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载商品失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const loadProductDetail = async (id: string) => {
    try {
      isLoading.value = true
      currentProduct.value = await fetchProductDetail(id)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载商品详情失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const nextPage = () => {
    if (hasNextPage.value) {
      pagination.value.page++
      loadProducts()
    }
  }

  const resetPagination = () => {
    pagination.value.page = 1
  }

  return {
    products,
    featuredProducts,
    currentProduct,
    isLoading,
    error,
    pagination,
    productCount,
    hasNextPage,
    loadProducts,
    loadProductDetail,
    nextPage,
    resetPagination
  }
})
```

### 3. 购物车 Store (cart.store.ts)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem, Product } from '@/types'

export const useCartStore = defineStore('cart', () => {
  // 状态
  const items = ref<CartItem[]>([])
  const isCartOpen = ref(false)

  // Getter
  const totalItems = computed(() => 
    items.value.reduce((total, item) => total + item.quantity, 0)
  )
  
  const subtotal = computed(() =>
    items.value.reduce((sum, item) => sum + (item.product.price * item.quantity), 0)
  )

  const hasItems = computed(() => items.value.length > 0)

  // Actions
  const addToCart = (product: Product, quantity: number = 1) => {
    const existingItem = items.value.find(item => item.product.id === product.id)
    
    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      items.value.push({ product, quantity })
    }
  }

  const removeFromCart = (productId: string) => {
    items.value = items.value.filter(item => item.product.id !== productId)
  }

  const updateQuantity = (productId: string, newQuantity: number) => {
    const item = items.value.find(item => item.product.id === productId)
    if (item) {
      item.quantity = newQuantity
    }
  }

  const clearCart = () => {
    items.value = []
  }

  const toggleCart = () => {
    isCartOpen.value = !isCartOpen.value
  }

  return {
    items,
    isCartOpen,
    totalItems,
    subtotal,
    hasItems,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    toggleCart
  }
})
```

### 4. UI 状态 Store (ui.store.ts)

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // 状态
  const theme = ref<'light' | 'dark'>('light')
  const isLoading = ref(false)
  const notification = ref<{
    message: string
    type: 'success' | 'error' | 'info' | 'warning'
    show: boolean
  }>({
    message: '',
    type: 'info',
    show: false
  })

  // Actions
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  const showNotification = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') => {
    notification.value = { message, type, show: true }
    setTimeout(() => {
      notification.value.show = false
    }, 3000)
  }

  const hideNotification = () => {
    notification.value.show = false
  }

  return {
    theme,
    isLoading,
    notification,
    toggleTheme,
    showNotification,
    hideNotification
  }
})
```

### 5. 订单管理 Store (order.store.ts)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Order, OrderStatus, CreateOrderPayload } from '@/types/order'
import { createOrder, fetchOrders, cancelOrder } from '@/api/orders'

export const useOrderStore = defineStore('order', () => {
  // 状态
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const filters = ref<{
    status?: OrderStatus
    startDate?: string
    endDate?: string
  }>({})

  // Getter
  const pendingOrders = computed(() =>
    orders.value.filter(order => order.status === 'pending')
  )

  const completedOrders = computed(() =>
    orders.value.filter(order => order.status === 'completed')
  )

  // Actions
  const loadOrders = async () => {
    try {
      isLoading.value = true
      orders.value = await fetchOrders(filters.value)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载订单失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const placeOrder = async (payload: CreateOrderPayload) => {
    try {
      isLoading.value = true
      const newOrder = await createOrder(payload)
      orders.value.unshift(newOrder)
      return newOrder
    } catch (err) {
      error.value = err instanceof Error ? err.message : '创建订单失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const setCurrentOrder = (orderId: string) => {
    currentOrder.value = orders.value.find(order => order.id === orderId) || null
  }

  const cancelCurrentOrder = async () => {
    if (!currentOrder.value) return
    
    try {
      isLoading.value = true
      await cancelOrder(currentOrder.value.id)
      currentOrder.value.status = 'cancelled'
      const index = orders.value.findIndex(order => order.id === currentOrder.value?.id)
      if (index !== -1) {
        orders.value[index].status = 'cancelled'
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '取消订单失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    orders,
    currentOrder,
    isLoading,
    error,
    filters,
    pendingOrders,
    completedOrders,
    loadOrders,
    placeOrder,
    setCurrentOrder,
    cancelCurrentOrder
  }
})
```

## 最佳实践建议

1. **单一职责原则**：每个 Store 只管理一个特定领域的状态
2. **组合式 API**：优先使用组合式 API 语法（如上面的示例）
3. **类型安全**：为所有状态、getter 和 action 定义 TypeScript 类型
4. **模块化导入**：在组件中按需导入 Store，而不是全局导入
5. **避免过度集中**：不要创建"上帝 Store"管理所有状态
6. **命名一致性**：使用一致的命名约定（如 useXxxStore）
7. **持久化策略**：对需要持久化的状态（如用户 token）使用 localStorage 或插件

## 高级 Store 模式

对于更复杂的应用，可以考虑：

1. **分层 Store**：基础 Store 和扩展 Store
2. **Store 组合**：一个 Store 使用另一个 Store
3. **插件使用**：如 pinia-plugin-persistedstate 用于状态持久化

通过以上设计，可以创建出结构清晰、易于维护的 Vue3 应用状态管理系统。