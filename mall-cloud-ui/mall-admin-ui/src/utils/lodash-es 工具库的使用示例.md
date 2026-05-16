# Vue3 + TS + Vite 项目中 lodash-es 使用示例

下面我将提供在 Vue3 + TypeScript + Vite 项目中使用 lodash-es 的实用示例，包含详细的注释说明和最佳实践。

## 1. 安装与配置

首先安装 lodash-es：

```bash
npm install lodash-es @types/lodash-es
```

## 2. 基础使用示例

### 按需导入单个函数

```typescript
// 导入特定函数（推荐方式，有利于tree-shaking）
import { debounce, throttle, cloneDeep } from 'lodash-es'
```

### 在组件中使用防抖和节流

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { debounce, throttle } from 'lodash-es'

const searchQuery = ref('')

// 防抖搜索函数 (500ms延迟)
const debouncedSearch = debounce((query: string) => {
  console.log('执行搜索:', query)
  // 这里可以替换为实际的API调用
}, 500)

// 节流滚动处理 (200ms间隔)
const throttledScroll = throttle(() => {
  console.log('处理滚动事件', window.scrollY)
}, 200)

// 监听搜索框输入
function handleInput() {
  debouncedSearch(searchQuery.value)
}

// 添加滚动监听
function initScrollListener() {
  window.addEventListener('scroll', throttledScroll)
}

// 组件卸载时取消监听
onUnmounted(() => {
  debouncedSearch.cancel() // 取消防抖函数的待执行操作
  window.removeEventListener('scroll', throttledScroll)
})
</script>

<template>
  <div>
    <input v-model="searchQuery" @input="handleInput" placeholder="搜索...">
    <div class="scroll-area" @scroll="throttledScroll">
      <!-- 长内容区域 -->
    </div>
  </div>
</template>
```

## 3. 数据操作示例

### 深度克隆对象

```typescript
import { cloneDeep } from 'lodash-es'

interface User {
  id: number
  name: string
  address: {
    city: string
    street: string
  }
}

const originalUser: User = {
  id: 1,
  name: '张三',
  address: {
    city: '北京',
    street: '长安街'
  }
}

// 使用cloneDeep进行深拷贝
const clonedUser = cloneDeep(originalUser)

// 修改克隆对象不会影响原对象
clonedUser.address.city = '上海'
console.log(originalUser.address.city) // 输出: '北京'
```

### 数组和对象操作

```typescript
import { 
  uniq, 
  groupBy, 
  orderBy, 
  pick, 
  omit 
} from 'lodash-es'

interface Product {
  id: number
  name: string
  category: string
  price: number
}

const products: Product[] = [
  { id: 1, name: '手机', category: '电子', price: 2999 },
  { id: 2, name: '笔记本', category: '电子', price: 5999 },
  { id: 3, name: '衬衫', category: '服装', price: 199 },
  { id: 4, name: '手机', category: '电子', price: 3999 }
]

// 1. 去重
const uniqueNames = uniq(products.map(p => p.name))
console.log(uniqueNames) // ['手机', '笔记本', '衬衫']

// 2. 按分类分组
const productsByCategory = groupBy(products, 'category')
console.log(productsByCategory)
/*
{
  电子: [
    { id: 1, name: '手机', category: '电子', price: 2999 },
    { id: 2, name: '笔记本', category: '电子', price: 5999 },
    { id: 4, name: '手机', category: '电子', price: 3999 }
  ],
  服装: [
    { id: 3, name: '衬衫', category: '服装', price: 199 }
  ]
}
*/

// 3. 排序 (先按价格降序，再按名称升序)
const sortedProducts = orderBy(products, ['price', 'name'], ['desc', 'asc'])
console.log(sortedProducts)

// 4. 选择对象属性
const product = { id: 1, name: '手机', category: '电子', price: 2999 }
const slimProduct = pick(product, ['id', 'name'])
console.log(slimProduct) // { id: 1, name: '手机' }

// 5. 排除对象属性
const productWithoutId = omit(product, ['id'])
console.log(productWithoutId) // { name: '手机', category: '电子', price: 2999 }
```

## 4. 实用工具函数示例

### 随机数生成

```typescript
import { random, sample, shuffle } from 'lodash-es'

// 生成1-100的随机数
const randomNum = random(1, 100)
console.log(randomNum)

// 从数组中随机选择一个元素
const fruits = ['苹果', '香蕉', '橙子', '葡萄']
const randomFruit = sample(fruits)
console.log(randomFruit)

// 打乱数组顺序 (不改变原数组)
const shuffledFruits = shuffle(fruits)
console.log(shuffledFruits)
```

### 函数记忆化

```typescript
import { memoize } from 'lodash-es'

// 昂贵的计算函数
function factorial(n: number): number {
  console.log(`计算 ${n} 的阶乘`)
  return n <= 1 ? 1 : n * factorial(n - 1)
}

// 创建记忆化版本
const memoizedFactorial = memoize(factorial)

console.log(memoizedFactorial(5)) // 会计算并缓存结果
console.log(memoizedFactorial(5)) // 直接从缓存返回
console.log(memoizedFactorial(3)) // 只计算未缓存的部分
```

## 5. 在组合式函数中使用

```typescript
// src/composables/useArrayUtils.ts
import { orderBy, uniqBy } from 'lodash-es'
import { computed } from 'vue'

export function useArrayUtils<T>(items: Ref<T[]>) {
  // 排序
  const sortedItems = computed(() => orderBy(items.value, ['name'], ['asc']))
  
  // 去重
  const uniqueItems = computed(() => uniqBy(items.value, 'id'))
  
  return {
    sortedItems,
    uniqueItems
  }
}
```

在组件中使用：

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useArrayUtils } from '@/composables/useArrayUtils'

const products = ref([
  { id: 1, name: '手机' },
  { id: 2, name: '笔记本' },
  { id: 1, name: '手机' } // 重复ID
])

const { sortedItems, uniqueItems } = useArrayUtils(products)
</script>

<template>
  <div>
    <h3>排序后的产品:</h3>
    <ul>
      <li v-for="item in sortedItems" :key="item.id">{{ item.name }}</li>
    </ul>
    
    <h3>去重后的产品:</h3>
    <ul>
      <li v-for="item in uniqueItems" :key="item.id">{{ item.name }}</li>
    </ul>
  </div>
</template>
```

## 6. 自定义指令中使用

```typescript
// src/directives/debounceClick.ts
import { debounce } from 'lodash-es'
import type { App } from 'vue'

export function setupDebounceDirective(app: App) {
  app.directive('debounce-click', {
    mounted(el, binding) {
      const { value: callback, arg: wait = 300, modifiers } = binding
      
      if (typeof callback !== 'function') {
        throw new Error('v-debounce-click 的值必须是一个函数')
      }
      
      // 使用lodash的debounce
      const debouncedFn = debounce(callback, Number(wait), {
        leading: modifiers.leading, // 是否立即执行
        trailing: !modifiers.noleading // 是否在延迟后执行
      })
      
      el._debouncedClick = debouncedFn
      el.addEventListener('click', debouncedFn)
    },
    beforeUnmount(el) {
      if (el._debouncedClick) {
        el._debouncedClick.cancel()
        el.removeEventListener('click', el._debouncedClick)
      }
    }
  })
}
```

在 `main.ts` 中注册：

```typescript
import { createApp } from 'vue'
import App from './App.vue'
import { setupDebounceDirective } from '@/directives/debounceClick'

const app = createApp(App)
setupDebounceDirective(app)
app.mount('#app')
```

在组件中使用：

```vue
<template>
  <button v-debounce-click:500.leading="handleClick">
    防抖按钮 (500ms, leading)
  </button>
  
  <button v-debounce-click="handleClick">
    默认防抖按钮 (300ms)
  </button>
</template>

<script setup lang="ts">
function handleClick() {
  console.log('按钮点击 - 实际执行')
}
</script>
```

## 7. 类型安全使用示例

Lodash-es 自带 TypeScript 类型定义，但我们可以进一步增强类型安全：

```typescript
import { get, set } from 'lodash-es'

interface User {
  id: number
  name: string
  address?: {
    city: string
    zipCode?: string
  }
}

const user: User = {
  id: 1,
  name: '张三'
}

// 安全获取嵌套属性 (类型推断正确)
const city = get(user, 'address.city', '未知城市') // 类型: string
console.log(city) // '未知城市'

// 安全设置嵌套属性
set(user, 'address.city', '北京')
console.log(user.address?.city) // '北京'

// 尝试设置不存在的路径会引发TypeScript错误
// set(user, 'address.street.name', '长安街') // TS错误: 路径不存在
```

## 8. 性能优化建议

1. **按需导入**：
   ```typescript
   // 推荐 - 只导入需要的函数
   import debounce from 'lodash-es/debounce'
   
   // 不推荐 - 导入整个模块
   import { debounce } from 'lodash-es'
   ```

2. **Vite 配置优化**：
   ```typescript
   // vite.config.ts
   import { defineConfig } from 'vite'
   import optimizeLodash from 'vite-plugin-optimize-lodash'

   export default defineConfig({
     plugins: [
       optimizeLodash() // 自动优化lodash-es的导入
     ]
   })
   ```

3. **避免在渲染函数中创建**：
   ```typescript
   // 不好 - 每次渲染都创建新的debounce函数
   const handleInput = debounce(() => {...}, 500)
   
   // 好 - 使用useMemo或computed
   const debouncedHandler = useMemo(() => debounce(() => {...}, 500), [])
   ```

## 总结

在 Vue3 + TS + Vite 项目中使用 lodash-es 的最佳实践：

1. **按需导入**：只导入需要的函数以减小打包体积
2. **合理使用**：在防抖、节流、复杂数据操作等场景优先考虑
3. **类型安全**：充分利用内置类型定义，必要时增强类型约束
4. **性能优化**：注意函数创建时机，避免不必要的重复创建
5. **组合式封装**：将常用操作封装为组合式函数提高复用性
6. **自定义指令**：为常用交互模式创建可复用指令

lodash-es 提供了丰富且经过充分测试的工具函数，能够显著提高开发效率和代码质量，是现代 Vue 项目中值得使用的实用工具库。