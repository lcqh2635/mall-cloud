# Pinia Store 类型定义最佳实践

将 Store 相关类型定义集中管理在 `stores/types/` 目录下确实是一个推荐的做法，这种组织方式有利于类型复用、维护一致性和团队协作。下面给出一个综合性类型定义示例：

## 推荐的项目结构

```
src/
  stores/
    types/
      auth.types.ts      # 认证相关类型
      product.types.ts   # 商品相关类型
      cart.types.ts      # 购物车相关类型
      order.types.ts     # 订单相关类型
      ui.types.ts        # UI状态相关类型
      index.ts           # 聚合导出所有类型
```

## 综合类型定义示例

### 1. 认证相关类型 (`auth.types.ts`)

```typescript
/**
 * 用户认证模块类型定义
 */

// 用户基本信息
export interface UserProfile {
  id: string
  username: string
  email: string
  avatar?: string
  role: 'admin' | 'user' | 'guest'
  createdAt: Date
  lastLoginAt?: Date
}

// 登录凭证
export interface AuthToken {
  accessToken: string
  refreshToken?: string
  expiresIn: number // 过期时间(秒)
}

// 登录表单
export interface LoginForm {
  email: string
  password: string
  rememberMe?: boolean
}

// 注册表单
export interface RegisterForm extends LoginForm {
  username: string
  confirmPassword: string
}

// 认证状态
export interface AuthState {
  token: AuthToken | null
  user: UserProfile | null
  isLoading: boolean
  error: string | null
}

// 认证Store类型
export type AuthStore = ReturnType<typeof useAuthStore>
```

### 2. 商品相关类型 (`product.types.ts`)

```typescript
/**
 * 商品模块类型定义
 */

// 商品基础信息
export interface Product {
  id: string
  name: string
  description: string
  price: number
  originalPrice?: number
  stock: number
  images: string[]
  category: string
  tags?: string[]
  rating?: number
  reviewsCount?: number
  skus?: ProductSku[]
}

// 商品SKU
export interface ProductSku {
  id: string
  attributes: Record<string, string> // 如 { color: 'red', size: 'XL' }
  price: number
  stock: number
  image?: string
}

// 商品筛选条件
export interface ProductFilter {
  category?: string
  priceRange?: [number, number]
  searchQuery?: string
  tags?: string[]
  sortBy?: 'price' | 'rating' | 'newest'
  sortOrder?: 'asc' | 'desc'
}

// 分页信息
export interface Pagination {
  page: number
  pageSize: number
  total: number
}

// 商品状态
export interface ProductState {
  products: Product[]
  featuredProducts: Product[]
  currentProduct: Product | null
  isLoading: boolean
  error: string | null
  pagination: Pagination
  filters: ProductFilter
}

// 商品Store类型
export type ProductStore = ReturnType<typeof useProductStore>
```

### 3. 购物车相关类型 (`cart.types.ts`)

```typescript
/**
 * 购物车模块类型定义
 */

// 购物车商品项
export interface CartItem {
  product: Product // 引用product.types中的Product类型
  quantity: number
  selectedSku?: ProductSku['id'] // 选择的SKU ID
  addedAt: Date
}

// 购物车状态
export interface CartState {
  items: CartItem[]
  isCartOpen: boolean
  lastUpdated?: Date
}

// 结算信息
export interface CheckoutInfo {
  items: Array<{
    productId: string
    skuId?: string
    quantity: number
  }>
  couponCode?: string
  shippingMethod?: string
}

// 购物车Store类型
export type CartStore = ReturnType<typeof useCartStore>
```

### 4. 订单相关类型 (`order.types.ts`)

```typescript
/**
 * 订单模块类型定义
 */

// 订单状态
export type OrderStatus = 
  | 'pending' 
  | 'paid' 
  | 'shipped' 
  | 'delivered' 
  | 'cancelled' 
  | 'refunded'

// 订单商品项
export interface OrderItem {
  productId: string
  name: string
  price: number
  quantity: number
  skuInfo?: Record<string, string>
  image?: string
}

// 收货地址
export interface ShippingAddress {
  recipient: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  postalCode?: string
}

// 订单详情
export interface Order {
  id: string
  orderNumber: string
  items: OrderItem[]
  totalAmount: number
  actualAmount: number
  status: OrderStatus
  createdAt: Date
  updatedAt: Date
  address: ShippingAddress
  paymentMethod?: string
  trackingNumber?: string
  couponUsed?: string
  discountAmount?: number
}

// 订单状态
export interface OrderState {
  orders: Order[]
  currentOrder: Order | null
  isLoading: boolean
  error: string | null
  filters: {
    status?: OrderStatus
    startDate?: Date
    endDate?: Date
  }
}

// 订单Store类型
export type OrderStore = ReturnType<typeof useOrderStore>
```

### 5. UI 相关类型 (`ui.types.ts`)

```typescript
/**
 * UI状态模块类型定义
 */

// 主题类型
export type ThemeMode = 'light' | 'dark' | 'system'

// 通知消息
export interface Notification {
  id?: string
  message: string
  type: 'success' | 'error' | 'info' | 'warning'
  duration?: number // 显示时长(ms)
  show?: boolean
}

// 加载状态
export interface LoadingState {
  global: boolean
  components: Record<string, boolean>
}

// UI状态
export interface UIState {
  theme: ThemeMode
  notifications: Notification[]
  loading: LoadingState
  drawerOpen: boolean
  modalStack: Array<{
    name: string
    props?: Record<string, any>
  }>
}

// UI Store类型
export type UIStore = ReturnType<typeof useUIStore>
```

### 6. 类型聚合文件 (`index.ts`)

```typescript
/**
 * 聚合所有Store类型定义
 */

// 认证相关
export type { 
  UserProfile, 
  AuthToken, 
  LoginForm, 
  AuthState,
  AuthStore 
} from './auth.types'

// 商品相关
export type { 
  Product, 
  ProductSku, 
  ProductFilter, 
  Pagination,
  ProductState,
  ProductStore 
} from './product.types'

// 购物车相关
export type { 
  CartItem, 
  CartState, 
  CheckoutInfo,
  CartStore 
} from './cart.types'

// 订单相关
export type { 
  OrderStatus, 
  OrderItem, 
  ShippingAddress, 
  Order,
  OrderState,
  OrderStore 
} from './order.types'

// UI相关
export type { 
  ThemeMode, 
  Notification, 
  LoadingState,
  UIState,
  UIStore 
} from './ui.types'
```

## 类型定义的最佳实践建议

1. **按模块划分**：每个业务模块对应一个类型文件
2. **接口优先**：优先使用 `interface` 而非 `type`，便于扩展
3. **合理复用**：跨模块共享的基础类型应提取到公共类型文件
4. **严格命名**：
    - 状态类型：`XxxState`
    - Store类型：`XxxStore`
    - 表单类型：`XxxForm`
5. **文档注释**：为每个主要类型添加注释说明
6. **避免污染**：不要将API响应类型与Store类型混为一谈
7. **版本控制**：对可能变更的类型添加版本标记

## 类型使用示例

在Store中的使用方式：

```typescript
import { defineStore } from 'pinia'
import type { ProductState, ProductFilter } from '@/stores/types/product.types'

export const useProductStore = defineStore('product', {
  state: (): ProductState => ({
    products: [],
    currentProduct: null,
    isLoading: false,
    // ...
  }),
  actions: {
    async loadProducts(filter: ProductFilter) {
      // 实现逻辑
    }
  }
})
```

在组件中的使用方式：

```typescript
<script setup lang="ts">
import type { Product, ProductStore } from '@/stores/types/product.types'

const productStore = useProductStore()
const products = computed(() => productStore.products as Product[])
</script>
```

这种集中化类型管理方式使得：
- 类型定义与业务逻辑解耦
- 便于跨模块类型引用
- 类型修改只需在一处进行
- 提高代码可维护性和类型安全性
- 便于新成员快速理解数据结构