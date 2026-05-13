# Pinia 和 pinia-plugin-persistedstate 使用注意事项与最佳实践

在实际开发中使用 Pinia 配合持久化插件时，需要注意以下关键点和优化建议：

## 核心注意事项

### 1. 数据安全与敏感信息处理
- **绝对不要存储**：密码、信用卡号、API密钥等敏感信息
- **加密存储**：对必要敏感字段(如token)进行加密
```typescript
persist: {
  serializer: {
    serialize: (value) => CryptoJS.AES.encrypt(JSON.stringify(value), 'secret_key'),
    deserialize: (value) => JSON.parse(CryptoJS.AES.decrypt(value, 'secret_key').toString(CryptoJS.enc.Utf8))
  }
}
```

### 2. 存储容量限制
- localStorage 有约5MB限制(各浏览器不同)
- 监控存储使用情况：
```typescript
function checkLocalStorageSpace() {
  let total = 0
  for (const x in localStorage) {
    const amount = (localStorage[x].length * 2) / 1024 / 1024
    total += amount
  }
  console.log(`已使用: ${total.toFixed(2)}MB / 5MB`)
}
```

### 3. 数据类型与序列化
- 避免存储无法序列化的对象(如类实例、函数)
- 处理特殊类型(Date、RegExp等)：
```typescript
persist: {
  serializer: {
    serialize: (value) => JSON.stringify(value, (_, val) => 
      val instanceof Date ? { __type: 'Date', value: val.toISOString() } : val
    ),
    deserialize: (value) => JSON.parse(value, (_, val) => 
      val?.__type === 'Date' ? new Date(val.value) : val
    )
  }
}
```

## 性能优化建议

### 1. 按需持久化
- 只持久化必要字段而非整个store
```typescript
persist: {
  paths: ['user.token', 'ui.theme'], // 仅持久化特定路径
  storage: sessionStorage // 对临时数据使用sessionStorage
}
```

### 2. 防抖写入
- 对高频变更的状态使用防抖
```typescript
import { debounce } from 'lodash-es'

const updateStorage = debounce((store) => {
  localStorage.setItem(store.$id, JSON.stringify(store.$state))
}, 500)

store.$subscribe((_, state) => {
  updateStorage(store)
})
```

### 3. 压缩大对象
```typescript
import { compress, decompress } from 'lz-string'

persist: {
  serializer: {
    serialize: (value) => compress(JSON.stringify(value)),
    deserialize: (value) => JSON.parse(decompress(value))
  }
}
```

## 工程化最佳实践

### 1. 统一配置管理
创建 `src/stores/persist-config.ts`：
```typescript
export const persistConfig = {
  auth: {
    key: 'auth-store',
    paths: ['token', 'user'],
    storage: localStorage
  },
  cart: {
    key: 'cart-store',
    paths: ['items'],
    storage: sessionStorage,
    beforeRestore: (ctx) => {
      if (ctx.store.$state.version !== CURRENT_VERSION) {
        ctx.store.$reset()
        return false
      }
    }
  }
}

// 在store中使用
export const useAuthStore = defineStore('auth', () => {...}, { 
  persist: persistConfig.auth 
})
```

### 2. 版本迁移方案
```typescript
persist: {
  afterRestore: (ctx) => {
    // 从v1迁移到v2
    if (ctx.store.$state.version === 1) {
      ctx.store.$patch({
        user: { ...ctx.store.$state.user, roles: ['member'] },
        version: 2
      })
    }
  }
}
```

### 3. 测试策略
- 编写持久化测试用例：
```typescript
describe('auth store persistence', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should persist token', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password' })
    
    // 模拟页面刷新
    window.dispatchEvent(new Event('pagehide'))
    const newStore = useAuthStore()
    
    expect(newStore.token).toBe(store.token)
  })
})
```

## 调试技巧

### 1. 开发工具集成
```typescript
// vite.config.ts
export default defineConfig({
  plugins: [
    vue(),
    PiniaDevTools({
      persist: true // 启用持久化调试支持
    })
  ]
})
```

### 2. 自定义调试输出
```typescript
persist: {
  debug: true, // 开启调试日志
  onRestored: (ctx) => {
    console.log(`[${ctx.store.$id}] 已恢复`, ctx.store.$state)
  }
}
```

## 高级场景处理

### 1. 多标签页同步
```typescript
// 在store初始化时添加
window.addEventListener('storage', (event) => {
  if (event.key === `pinia:${store.$id}`) {
    store.$persist.resume()
  }
})
```

### 2. 服务端渲染(SSR)适配
```typescript
persist: {
  storage: process.client ? localStorage : {
    getItem: () => null,
    setItem: () => {}
  }
}
```

### 3. 过期时间控制
```typescript
persist: {
  afterRestore: (ctx) => {
    if (ctx.store.$state.expiresAt < Date.now()) {
      ctx.store.$reset()
    }
  }
}
```

## 推荐的项目结构

```
src/
  stores/
    modules/
      auth.store.ts       # 认证相关
      user.store.ts       # 用户资料
      product.store.ts    # 商品数据
      cart.store.ts       # 购物车
      ui.store.ts         # UI状态(不持久化)
    index.ts              # 统一导出
    persist-config.ts     # 持久化配置
    types/                # Store相关类型定义
```

通过遵循这些实践建议，可以确保 Pinia 状态持久化在项目中既安全又高效地工作，同时保持代码的可维护性和可扩展性。