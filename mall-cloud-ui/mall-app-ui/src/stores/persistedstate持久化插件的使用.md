# Pinia 状态持久化策略指南

在 Vue3 + Pinia 项目中使用 `pinia-plugin-persistedstate` 插件时，合理的持久化策略对应用性能和安全性至关重要。

## 需要持久化的 Store

### 1. 应持久化的 Store 类型

| Store 类型 | 持久化理由 | 存储方式建议 |
|------------|------------|--------------|
| 用户认证 (auth) | 保持登录状态，避免重复登录 | localStorage |
| 用户偏好设置 (preferences) | 保存用户个性化配置 | localStorage |
| 购物车 (cart) | 防止用户意外丢失已选商品 | sessionStorage |
| 表单草稿 (formDrafts) | 保存未提交的表单数据 | sessionStorage |

### 2. 持久化配置示例 (auth.store.ts)

```typescript
export const useAuthStore = defineStore('auth', () => {
  // ...状态和逻辑
  
  return {
    // ...返回的状态和方法
  }
}, {
  persist: {
    key: 'auth', // 存储键名
    storage: localStorage, // 使用 localStorage
    pick: ['token', 'user'], // 只持久化 token 和 user
  }
})
```

## 不应持久化的 Store

### 1. 避免持久化的 Store 类型

| Store 类型 | 不持久化理由 |
|------------|--------------|
| UI 状态 (ui) | 临时状态，不需要跨会话保留 |
| 实时数据 (realtime) | 数据时效性强，重新获取更合理 |
| 敏感数据 (sensitive) | 安全考虑，如支付信息 |
| 大型数据集 (largeData) | 避免 localStorage 容量限制(通常5MB) |
| 服务端状态 (serverState) | 应与服务端保持同步，本地持久化可能导致不一致 |

### 2. 示例：不需要持久化的 UI Store

```typescript
export const useUIStore = defineStore('ui', () => {
  // 这些状态不需要持久化
  const isMenuOpen = ref(false)
  const modalState = ref({
    show: false,
    type: ''
  })
  
  // ...其他逻辑
  
  return {
    isMenuOpen,
    modalState
    // ...
  }
}) // 不添加 persist 配置
```

## localStorage vs sessionStorage 选择策略

### 1. 选择标准对比

| 标准 | localStorage | sessionStorage |
|------|--------------|----------------|
| 生命周期 | 永久存储，直到显式删除 | 浏览器标签页关闭时清除 |
| 作用域 | 同源的所有标签页共享 | 仅当前标签页可用 |
| 适用场景 | 长期偏好设置、登录状态 | 临时工作状态、敏感中间数据 |
| 容量 | 通常5MB | 通常5MB |
| 访问速度 | 略慢(需要读取磁盘) | 略快(内存存储) |

### 2. 具体选择建议

**使用 localStorage 的情况**:
- 用户登录凭证(token)
- 主题偏好(暗黑/明亮模式)
- 语言设置
- 长期使用的用户配置
- 需要跨标签页共享的状态

**使用 sessionStorage 的情况**:
- 购物车数据(特别是含敏感信息时)
- 多步骤表单的中间状态
- 当前工作会话的临时筛选条件
- 包含敏感信息的临时数据
- 标签页独有的状态

### 3. 混合持久化示例 (cart.store.ts)

```typescript
export const useCartStore = defineStore('cart', () => {
  // ...状态和逻辑
  
  return {
    // ...返回的状态和方法
  }
}, {
  persist: {
    key: 'cart',
    storage: sessionStorage, // 使用 sessionStorage
    pick: ['items'], // 只持久化商品项
    // 可以添加序列化配置
    serializer: {
      serialize: JSON.stringify,
      deserialize: JSON.parse
    }
  }
})
```

## 高级持久化策略

### 1. 条件持久化

```typescript
persist: {
  storage: {
    getItem(key) {
      return userPrefersSessionStorage ? 
        sessionStorage.getItem(key) : 
        localStorage.getItem(key)
    },
    setItem(key, value) {
      userPrefersSessionStorage ? 
        sessionStorage.setItem(key, value) : 
        localStorage.setItem(key, value)
    }
  }
}
```

### 2. 加密敏感数据

```typescript
import { encrypt, decrypt } from '@/utils/crypto'

persist: {
  serializer: {
    serialize: (value) => encrypt(JSON.stringify(value)),
    deserialize: (value) => JSON.parse(decrypt(value))
  }
}
```

### 3. 部分状态持久化

```typescript
// 只持久化特定字段
persist: {
  paths: [
    'token', 
    'user.preferences',
    'user.profile.email'
  ]
}
```

## 最佳实践建议

1. **最小化原则**：只持久化必要状态，避免存储大型数据集
2. **安全考虑**：不要存储原始密码、支付信息等敏感数据
3. **版本控制**：为持久化数据添加版本号，便于未来迁移
4. **清理机制**：提供手动清理持久化数据的方法
5. **性能优化**：对大对象使用压缩或索引存储
6. **错误处理**：处理存储配额超出等异常情况

通过合理规划持久化策略，可以在保证用户体验的同时，兼顾应用性能和安全性。