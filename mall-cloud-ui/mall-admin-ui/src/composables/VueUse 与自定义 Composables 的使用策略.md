# VueUse 与自定义 Composables 的使用策略

在 Vue3 项目中，VueUse 和自定义 Composables 各有其适用场景，理解它们的区别和适用情况可以帮助你做出更好的架构决策。

## VueUse 与自定义 Composables 的对比

| 特性                | VueUse                          | 自定义 Composables               |
|---------------------|--------------------------------|--------------------------------|
| **定位**            | 通用工具库                     | 项目特定逻辑封装                |
| **复杂度**          | 简单、标准化                   | 可简单可复杂                   |
| **复用性**          | 跨项目通用                     | 项目内复用                     |
| **维护**            | 社区维护                       | 自行维护                      |
| **学习成本**        | 需要学习 API                   | 需自行设计实现                 |
| **灵活性**          | 有限配置                       | 完全自定义                    |
| **业务相关性**      | 无业务逻辑                     | 可包含业务逻辑                |

## 何时使用 VueUse

### 推荐使用 VueUse 的场景

1. **基础功能需求**：
   ```typescript
   // 使用 VueUse 实现鼠标位置跟踪
   import { useMouse } from '@vueuse/core'
   
   const { x, y } = useMouse()
   ```

2. **浏览器 API 封装**：
   ```typescript
   // 使用 VueUse 的本地存储功能
   import { useLocalStorage } from '@vueuse/core'
   
   const token = useLocalStorage('auth-token', '')
   ```

3. **UI 交互辅助**：
   ```typescript
   // 使用 VueUse 实现元素拖拽
   import { useDraggable } from '@vueuse/core'
   
   const target = ref(null)
   const { x, y } = useDraggable(target)
   ```

4. **性能优化工具**：
   ```typescript
   // 使用 VueUse 的防抖函数
   import { useDebounceFn } from '@vueuse/core'
   
   const debouncedFn = useDebounceFn(() => {
     // 你的逻辑
   }, 500)
   ```

### VueUse 的优势

1. **经过充分测试**：社区广泛使用，稳定性高
2. **持续更新**：跟随 Vue 和浏览器特性更新
3. **体积优化**：支持按需导入，Tree-shaking
4. **跨项目一致**：团队使用相同实现减少认知负担

## 何时需要自定义 Composables

### 需要自定义的场景

1. **业务逻辑封装**：
   ```typescript
   // 自定义认证逻辑 Composables
   export function useAuth() {
     const user = ref(null)
     
     const login = async (credentials) => {
       // 项目特定的登录逻辑
     }
     
     return { user, login }
   }
   ```

2. **领域特定功能**：
   ```typescript
   // 电商项目中的购物车逻辑
   export function useCart() {
     const items = ref([])
     
     const addToCart = (product) => {
       // 项目特定的添加逻辑
     }
     
     return { items, addToCart }
   }
   ```

3. **复杂组合逻辑**：
   ```typescript
   // 组合多个数据源的仪表盘逻辑
   export function useDashboard() {
     const { data: sales } = useSalesData()
     const { data: inventory } = useInventoryData()
     
     const metrics = computed(() => {
       // 复杂的业务计算
     })
     
     return { metrics }
   }
   ```

4. **特殊需求定制**：
   ```typescript
   // 与特定后端API交互的定制逻辑
   export function useCustomAPI() {
     // 实现特定的错误处理
     // 实现特定的缓存策略
   }
   ```

### 自定义 Composables 的优势

1. **业务契合度**：完全匹配项目需求
2. **可定制性**：可以包含特定业务规则
3. **知识保留**：业务逻辑保留在项目中
4. **无依赖**：不增加外部依赖项

## 混合使用的最佳实践

### 1. 基础功能优先使用 VueUse

```typescript
import { useLocalStorage, useMouse } from '@vueuse/core'

// 使用 VueUse 的基础功能
const token = useLocalStorage('token', '')
const { x, y } = useMouse()
```

### 2. 业务逻辑使用自定义 Composables

```typescript
// 在自定义 Composables 中使用 VueUse
import { useDebounceFn } from '@vueuse/core'

export function useProductSearch() {
  const searchQuery = ref('')
  
  // 使用 VueUse 的防抖函数
  const debouncedSearch = useDebounceFn(() => {
    // 业务特定的搜索逻辑
  }, 500)
  
  return { searchQuery, debouncedSearch }
}
```

### 3. 创建项目特定的工具 Composables

```typescript
// 封装项目中常用的组合
import { useFetch } from '@vueuse/core'

export function useApi(endpoint: string) {
  const { data, error, execute } = useFetch(`/api/${endpoint}`, {
    // 项目统一的配置
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
  
  // 项目统一的错误处理
  watch(error, (err) => {
    showError(err.message)
  })
  
  return { data, execute }
}
```

## 目录结构建议

对于混合使用的情况，推荐如下结构：

```
src/
├── composables/
│   ├── lib/                # 从 VueUse 二次封装的工具
│   │   ├── useApi.ts       # 基于 useFetch 的封装
│   │   └── useStorage.ts   # 增强的存储功能
│   │
│   ├── business/           # 业务相关 Composables
│   │   ├── useAuth.ts
│   │   ├── useCart.ts
│   │   └── ...
│   │
│   └── index.ts            # 统一导出
```

## 性能考量

1. **按需导入**：
   ```typescript
   // 推荐 - 只导入需要的函数
   import { useMouse } from '@vueuse/core'
   
   // 不推荐 - 导入整个库
   import VueUse from '@vueuse/core'
   ```

2. **轻量封装**：
   ```typescript
   // 好的封装 - 保持简单
   export function useAuth() {
     const user = useLocalStorage('user', null)
     // ...
   }
   
   // 不好的封装 - 过度包装
   export function useAuth() {
     const { user, setUser } = useUserStorage()
     const { token, setToken } = useTokenStorage()
     // 太多间接层
   }
   ```

## 决策流程图

```
是否需要解决通用问题?
├── 是 → 检查 VueUse 是否提供
│   ├── 有且满足需求 → 使用 VueUse
│   └── 不满足 → 考虑基于 VueUse 封装
└── 否 → 是业务特定需求?
    ├── 是 → 创建自定义 Composables
    └── 否 → 可能不需要 Composables
```

## 总结建议

1. **优先使用 VueUse** 解决通用问题
2. **合理创建自定义 Composables** 处理业务逻辑
3. **避免重复造轮子**，但也不要过度依赖外部库
4. **保持适度抽象**，平衡复用性和简单性
5. **建立项目规范**，统一团队的使用方式

通过合理结合 VueUse 和自定义 Composables，你可以获得以下好处：
- 减少重复工作（利用 VueUse 的现成解决方案）
- 保持业务逻辑的清晰性和可控性（通过自定义 Composables）
- 提高代码的一致性和可维护性
- 加速开发过程同时不牺牲灵活性