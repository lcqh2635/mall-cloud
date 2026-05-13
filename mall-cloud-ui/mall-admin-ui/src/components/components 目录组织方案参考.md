# Vue3 + TS + Vite 项目中 components 目录组织方案

一个良好组织的 components 目录可以显著提高项目的可维护性和开发效率。下面是我推荐的组件目录结构方案，基于实际项目经验总结。

## 推荐目录结构

```
src/
├── components/
│   ├── ui/                  # 基础UI组件 (可复用)
│   │   ├── buttons/
│   │   │   ├── BaseButton.vue
│   │   │   ├── IconButton.vue
│   │   │   └── index.ts     # 统一导出
│   │   ├── forms/
│   │   │   ├── BaseInput.vue
│   │   │   ├── BaseSelect.vue
│   │   │   └── ...
│   │   ├── feedback/
│   │   │   ├── Alert.vue
│   │   │   ├── Modal.vue
│   │   │   └── ...
│   │   └── ...
│   │
│   ├── domain/              # 领域组件 (业务相关)
│   │   ├── user/
│   │   │   ├── UserAvatar.vue
│   │   │   ├── UserProfile.vue
│   │   │   └── ...
│   │   ├── product/
│   │   │   ├── ProductCard.vue
│   │   │   ├── ProductList.vue
│   │   │   └── ...
│   │   └── ...
│   │
│   ├── layouts/             # 布局组件 (已单独说明)
│   │
│   ├── composables/         # 组合式函数 (Vue3特有)
│   │   ├── useFetch.ts
│   │   ├── useForm.ts
│   │   └── ...
│   │
│   ├── icons/               # SVG图标组件
│   │   ├── IconHome.vue
│   │   ├── IconUser.vue
│   │   └── ...
│   │
│   ├── shared/              # 跨项目共享组件
│   │
│   └── index.ts             # 全局组件注册
```

## 详细说明

### 1. UI 基础组件 (`ui/`)

**特点**：
- 与业务逻辑无关的纯展示组件
- 高度可复用
- 通常有自己的样式和基本交互

**示例结构**：
```
ui/
├── buttons/
│   ├── BaseButton.vue      # 基础按钮
│   ├── IconButton.vue      # 图标按钮
│   └── index.ts            # 统一导出
├── forms/
│   ├── BaseInput.vue
│   ├── BaseSelect.vue
│   ├── BaseCheckbox.vue
│   └── ...
└── feedback/
    ├── Alert.vue
    ├── Modal.vue
    ├── Toast.vue
    └── ...
```

**index.ts 示例**：
```typescript
export { default as BaseButton } from './BaseButton.vue'
export { default as IconButton } from './IconButton.vue'
```

### 2. 领域组件 (`domain/`)

**特点**：
- 包含业务逻辑
- 通常组合多个基础UI组件
- 与特定功能领域相关

**示例结构**：
```
domain/
├── user/
│   ├── UserAvatar.vue      # 用户头像组件
│   ├── UserProfile.vue     # 用户资料卡片
│   └── UserList.vue        # 用户列表
└── product/
    ├── ProductCard.vue     # 产品卡片
    ├── ProductGallery.vue  # 产品图库
    └── ProductFilter.vue   # 产品筛选器
```

### 3. 布局组件 (`layouts/`)

虽然布局组件可以放在这里，但在大型项目中我推荐如之前所述单独放在 `src/layouts/` 目录下。

### 4. 组合式函数 (`composables/`)

**特点**：
- Vue3 的组合式 API 函数
- 封装可复用的逻辑
- 通常以 `use` 前缀命名

**示例**：
```typescript
// useFetch.ts
import { ref } from 'vue'

export function useFetch<T>(url: string) {
  const data = ref<T | null>(null)
  const error = ref(null)
  const loading = ref(false)

  const fetchData = async () => {
    loading.value = true
    try {
      const response = await fetch(url)
      data.value = await response.json()
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  return { data, error, loading, fetchData }
}
```

### 5. 图标组件 (`icons/`)

**推荐做法**：
- 每个 SVG 图标作为一个单独组件
- 使用 `vite-plugin-svg-icons` 或类似工具自动导入

**示例**：
```vue
<template>
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
    <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
  </svg>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({
  name: 'IconHome'
})
</script>
```

### 6. 全局组件注册

`components/index.ts` 示例：

```typescript
import type { App } from 'vue'

// UI 组件
import { BaseButton, IconButton } from './ui/buttons'
import { BaseInput, BaseSelect } from './ui/forms'

// 全局组件
const components = {
  BaseButton,
  IconButton,
  BaseInput,
  BaseSelect
}

export function registerComponents(app: App) {
  Object.entries(components).forEach(([name, component]) => {
    app.component(name, component)
  })
}

// 单个组件导出
export * from './ui/buttons'
export * from './ui/forms'
// 其他导出...
```

在 `main.ts` 中使用：

```typescript
import { createApp } from 'vue'
import { registerComponents } from '@/components'

const app = createApp(App)
registerComponents(app)
```

## 组件命名规范

1. **基础UI组件**：前缀 `Base` (如 `BaseButton`, `BaseInput`)
2. **领域组件**：前缀为领域名 (如 `UserAvatar`, `ProductCard`)
3. **布局组件**：后缀 `Layout` (如 `AdminLayout`, `AuthLayout`)
4. **图标组件**：前缀 `Icon` (如 `IconHome`, `IconUser`)

## 自动导入配置 (推荐)

使用 `unplugin-vue-components` 实现自动导入：

1. 安装依赖：
```bash
npm i -D unplugin-vue-components
```

2. `vite.config.ts` 配置：

```typescript
import Components from 'unplugin-vue-components/vite'

export default defineConfig({
  plugins: [
    Components({
      dts: true, // 生成类型声明文件
      dirs: ['src/components/ui'], // 自动导入的目录
      // 可以添加更多配置
    }),
  ],
})
```

## 类型安全增强

创建 `src/types/components.d.ts`：

```typescript
import type { DefineComponent } from 'vue'

// 为自动导入的组件提供类型
declare module 'vue' {
  export interface GlobalComponents {
    BaseButton: DefineComponent<{ /* props */ }>
    BaseInput: DefineComponent<{ /* props */ }>
    // 其他组件...
  }
}
```

## 最佳实践建议

1. **组件职责单一**：每个组件只做一件事
2. **合理划分层级**：
    - 基础UI组件 (无状态)
    - 复合组件 (组合基础组件)
    - 业务组件 (包含业务逻辑)
3. **Props设计**：
    - 使用 TypeScript 接口明确定义
    - 提供合理的默认值
4. **样式管理**：
    - 基础组件可以使用 scoped CSS
    - 业务组件推荐使用 CSS Modules
5. **文档化**：
    - 为每个组件添加注释说明
    - 复杂组件可以添加 README.md
6. **测试**：
    - 为可复用组件添加单元测试
    - 测试文件放在同目录下 (如 `BaseButton.spec.ts`)

通过这种组织方式，你的项目将具备良好的可维护性和可扩展性，团队成员也能快速定位和理解组件结构。