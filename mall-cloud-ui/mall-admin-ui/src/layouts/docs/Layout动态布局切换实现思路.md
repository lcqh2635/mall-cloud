# Vue3 + TS + Vite + Tailwind CSS + Sass 实现动态布局切换和主题选择

下面我将详细介绍如何实现一个包含动态布局切换和主题选择功能的前端项目，并提供完整的代码示例。

## 实现思路

1. **状态管理**：使用 Pinia 来管理布局和主题状态
2. **布局组件**：创建多个预设布局组件
3. **主题系统**：通过 CSS 变量和 Tailwind 配置实现主题切换
4. **设置抽屉**：实现一个右侧滑出的设置面板
5. **过渡动画**：使用 Vue 的 Transition 组件实现平滑过渡
6. **持久化**：将用户选择保存到 localStorage

## 项目结构

```
src/
├── assets/
│   └── scss/              # Sass 样式文件
│       ├── _variables.scss
│       └── main.scss
├── components/
│   ├── layouts/           # 布局组件
│   │   ├── ClassicLayout.vue
│   │   ├── HorizontalLayout.vue
│   │   └── SidebarLayout.vue
│   ├── SettingsDrawer.vue # 设置抽屉组件
│   └── ThemeColorPicker.vue # 主题颜色选择器
├── composables/           # 组合式函数
│   └── useTheme.ts
├── stores/                # Pinia 状态管理
│   └── settings.ts
├── App.vue
└── main.ts
```

## 完整实现

### 1. 安装必要依赖

```bash
npm install pinia @pinia/nuxt sass
```

### 2. 配置 Tailwind (tailwind.config.js)

```js
const colors = require('tailwindcss/colors')

module.exports = {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  darkMode: 'class', // 启用暗黑模式
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: 'var(--color-primary)',
          light: 'var(--color-primary-light)',
          dark: 'var(--color-primary-dark)',
        },
        // 其他自定义颜色
      }
    }
  },
  plugins: [],
}
```

### 3. 创建 Pinia 状态管理 (stores/settings.ts)

```ts
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 定义布局类型
type LayoutType = 'classic' | 'horizontal' | 'sidebar'

// 定义主题颜色
type ThemeColor = 'blue' | 'green' | 'red' | 'purple' | 'orange' | 'teal' | 'pink'

export const useSettingsStore = defineStore('settings', () => {
  // 从 localStorage 加载设置或使用默认值
  const defaultLayout = (localStorage.getItem('layout') as LayoutType) || 'classic'
  const defaultTheme = (localStorage.getItem('theme') as ThemeColor) || 'blue'
  const defaultDarkMode = localStorage.getItem('darkMode') === 'true'

  // 响应式状态
  const layout = ref<LayoutType>(defaultLayout)
  const themeColor = ref<ThemeColor>(defaultTheme)
  const darkMode = ref<boolean>(defaultDarkMode)
  const settingsDrawerVisible = ref<boolean>(false)

  // 切换布局
  const switchLayout = (newLayout: LayoutType) => {
    layout.value = newLayout
  }

  // 切换主题颜色
  const switchTheme = (color: ThemeColor) => {
    themeColor.value = color
  }

  // 切换暗黑模式
  const toggleDarkMode = () => {
    darkMode.value = !darkMode.value
  }

  // 持久化设置到 localStorage
  watch([layout, themeColor, darkMode], ([newLayout, newTheme, newDarkMode]) => {
    localStorage.setItem('layout', newLayout)
    localStorage.setItem('theme', newTheme)
    localStorage.setItem('darkMode', String(newDarkMode))
  })

  return {
    layout,
    themeColor,
    darkMode,
    settingsDrawerVisible,
    switchLayout,
    switchTheme,
    toggleDarkMode
  }
})
```

### 4. 创建主题 Hook (composables/useTheme.ts)

```ts
import { watch } from 'vue'
import { useSettingsStore } from '@/stores/settings'

// 主题颜色映射
const themeColors = {
  blue: {
    primary: '#3b82f6',
    primaryLight: '#93c5fd',
    primaryDark: '#1d4ed8'
  },
  green: {
    primary: '#10b981',
    primaryLight: '#6ee7b7',
    primaryDark: '#047857'
  },
  red: {
    primary: '#ef4444',
    primaryLight: '#fca5a5',
    primaryDark: '#b91c1c'
  },
  purple: {
    primary: '#8b5cf6',
    primaryLight: '#c4b5fd',
    primaryDark: '#6d28d9'
  },
  orange: {
    primary: '#f97316',
    primaryLight: '#fdba74',
    primaryDark: '#c2410c'
  },
  teal: {
    primary: '#14b8a6',
    primaryLight: '#5eead4',
    primaryDark: '#0d9488'
  },
  pink: {
    primary: '#ec4899',
    primaryLight: '#f9a8d4',
    primaryDark: '#be185d'
  }
}

export const useTheme = () => {
  const settingsStore = useSettingsStore()

  // 应用主题颜色
  const applyTheme = () => {
    const colors = themeColors[settingsStore.themeColor]
    document.documentElement.style.setProperty('--color-primary', colors.primary)
    document.documentElement.style.setProperty('--color-primary-light', colors.primaryLight)
    document.documentElement.style.setProperty('--color-primary-dark', colors.primaryDark)
  }

  // 应用暗黑模式
  const applyDarkMode = () => {
    if (settingsStore.darkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 初始化应用主题
  applyTheme()
  applyDarkMode()

  // 监听主题和暗黑模式变化
  watch(() => settingsStore.themeColor, applyTheme)
  watch(() => settingsStore.darkMode, applyDarkMode)
}
```

### 5. 创建布局组件 (components/layouts/)

#### ClassicLayout.vue - 经典布局

```vue
<template>
  <div class="flex flex-col h-screen">
    <!-- 顶部导航 -->
    <header class="bg-primary text-white shadow-md">
      <div class="container mx-auto px-4 py-3 flex items-center justify-between">
        <h1 class="text-xl font-bold">经典布局</h1>
        <button @click="toggleSettings" class="p-2 rounded-full hover:bg-primary-dark">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
        </button>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <!-- 侧边栏 -->
      <aside class="w-64 bg-gray-100 dark:bg-gray-800 shadow-md">
        <nav class="p-4">
          <ul class="space-y-2">
            <li v-for="item in menuItems" :key="item.name">
              <router-link :to="item.path" class="block px-4 py-2 rounded hover:bg-primary-light hover:text-white">
                {{ item.name }}
              </router-link>
            </li>
          </ul>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="flex-1 overflow-auto p-6 bg-white dark:bg-gray-900">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const menuItems = [
  { name: '仪表盘', path: '/' },
  { name: '用户管理', path: '/users' },
  { name: '产品管理', path: '/products' },
  { name: '设置', path: '/settings' }
]

const toggleSettings = () => {
  settingsStore.settingsDrawerVisible = !settingsStore.settingsDrawerVisible
}
</script>

<style scoped lang="scss">
// 可以添加一些过渡效果
aside {
  transition: all 0.3s ease;
}
</style>
```

#### HorizontalLayout.vue - 水平布局

```vue
<template>
  <div class="flex flex-col h-screen">
    <!-- 顶部导航和侧边栏合并 -->
    <header class="bg-primary text-white shadow-md">
      <div class="container mx-auto px-4 py-3 flex items-center">
        <h1 class="text-xl font-bold mr-8">水平布局</h1>
        <nav class="flex space-x-4">
          <router-link 
            v-for="item in menuItems" 
            :key="item.name" 
            :to="item.path" 
            class="px-3 py-2 rounded hover:bg-primary-dark"
          >
            {{ item.name }}
          </router-link>
        </nav>
        <button @click="toggleSettings" class="ml-auto p-2 rounded-full hover:bg-primary-dark">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
        </button>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="flex-1 overflow-auto p-6 bg-white dark:bg-gray-900">
      <router-view />
    </main>
  </div>
</template>

<script lang="ts" setup>
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const menuItems = [
  { name: '仪表盘', path: '/' },
  { name: '用户管理', path: '/users' },
  { name: '产品管理', path: '/products' },
  { name: '设置', path: '/settings' }
]

const toggleSettings = () => {
  settingsStore.settingsDrawerVisible = !settingsStore.settingsDrawerVisible
}
</script>
```

### 6. 创建设置抽屉组件 (components/SettingsDrawer.vue)

```vue
<template>
  <transition name="slide-fade">
    <div 
      v-if="visible" 
      class="fixed inset-0 overflow-hidden z-50"
      @click.self="closeDrawer"
    >
      <div class="absolute inset-y-0 right-0 max-w-full flex">
        <div class="relative w-screen max-w-md">
          <div class="h-full flex flex-col bg-white dark:bg-gray-800 shadow-xl">
            <!-- 抽屉头部 -->
            <div class="px-4 py-6 bg-primary text-white">
              <div class="flex items-center justify-between">
                <h2 class="text-xl font-bold">主题设置</h2>
                <button @click="closeDrawer" class="p-2 rounded-full hover:bg-primary-dark">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 抽屉内容 -->
            <div class="flex-1 overflow-y-auto p-6">
              <!-- 布局选择 -->
              <div class="mb-8">
                <h3 class="text-lg font-medium mb-4 dark:text-white">布局样式</h3>
                <div class="grid grid-cols-3 gap-4">
                  <button
                    v-for="layout in layouts"
                    :key="layout.value"
                    @click="switchLayout(layout.value)"
                    class="p-4 border rounded-lg transition-all"
                    :class="{
                      'border-primary shadow-md': currentLayout === layout.value,
                      'border-gray-200 hover:border-primary': currentLayout !== layout.value,
                      'dark:border-gray-600': currentLayout !== layout.value
                    }"
                  >
                    <div class="flex flex-col items-center">
                      <div class="w-full h-12 mb-2" :class="layout.previewClass"></div>
                      <span class="text-sm dark:text-white">{{ layout.label }}</span>
                    </div>
                  </button>
                </div>
              </div>

              <!-- 主题颜色选择 -->
              <div class="mb-8">
                <h3 class="text-lg font-medium mb-4 dark:text-white">主题颜色</h3>
                <ThemeColorPicker />
              </div>

              <!-- 暗黑模式切换 -->
              <div class="flex items-center justify-between">
                <span class="text-gray-700 dark:text-white">暗黑模式</span>
                <button
                  @click="toggleDarkMode"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors"
                  :class="darkMode ? 'bg-primary' : 'bg-gray-200'"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                    :class="darkMode ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import ThemeColorPicker from './ThemeColorPicker.vue'

const settingsStore = useSettingsStore()

const visible = computed(() => settingsStore.settingsDrawerVisible)
const currentLayout = computed(() => settingsStore.layout)
const darkMode = computed(() => settingsStore.darkMode)

const layouts = [
  {
    value: 'classic',
    label: '经典布局',
    previewClass: 'bg-gradient-to-r from-blue-500 to-blue-300'
  },
  {
    value: 'horizontal',
    label: '水平布局',
    previewClass: 'bg-gradient-to-r from-green-500 to-green-300'
  },
  {
    value: 'sidebar',
    label: '侧边栏布局',
    previewClass: 'bg-gradient-to-r from-purple-500 to-purple-300'
  }
]

const closeDrawer = () => {
  settingsStore.settingsDrawerVisible = false
}

const switchLayout = (layout: 'classic' | 'horizontal' | 'sidebar') => {
  settingsStore.switchLayout(layout)
}

const toggleDarkMode = () => {
  settingsStore.toggleDarkMode()
}
</script>

<style scoped lang="scss">
/* 抽屉动画效果 */
.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from, .slide-fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-to, .slide-fade-leave-from {
  opacity: 1;
}

/* 背景遮罩 */
.slide-fade-enter-active::before, .slide-fade-leave-active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  transition: opacity 0.3s ease;
}

.slide-fade-enter-from::before, .slide-fade-leave-to::before {
  opacity: 0;
}

.slide-fade-enter-to::before, .slide-fade-leave-from::before {
  opacity: 1;
}
</style>
```

### 7. 创建主题颜色选择器 (components/ThemeColorPicker.vue)

```vue
<template>
  <div class="flex flex-wrap gap-3">
    <button
      v-for="color in colors"
      :key="color.value"
      @click="switchTheme(color.value)"
      class="w-8 h-8 rounded-full flex items-center justify-center transition-transform hover:scale-110"
      :class="color.bgClass"
      :aria-label="color.label"
    >
      <svg
        v-if="color.value === currentTheme"
        class="w-4 h-4 text-white"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
      </svg>
    </button>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const currentTheme = computed(() => settingsStore.themeColor)

const colors = [
  { value: 'blue', label: '蓝色', bgClass: 'bg-blue-500' },
  { value: 'green', label: '绿色', bgClass: 'bg-green-500' },
  { value: 'red', label: '红色', bgClass: 'bg-red-500' },
  { value: 'purple', label: '紫色', bgClass: 'bg-purple-500' },
  { value: 'orange', label: '橙色', bgClass: 'bg-orange-500' },
  { value: 'teal', label: '青色', bgClass: 'bg-teal-500' },
  { value: 'pink', label: '粉色', bgClass: 'bg-pink-500' }
]

const switchTheme = (color: string) => {
  settingsStore.switchTheme(color as any)
}
</script>
```

### 8. 主应用组件 (App.vue)

```vue
<template>
  <!-- 根据当前布局动态渲染对应布局组件 -->
  <component :is="currentLayoutComponent" />
  
  <!-- 设置抽屉 -->
  <SettingsDrawer />
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { useTheme } from '@/composables/useTheme'
import ClassicLayout from '@/components/layouts/ClassicLayout.vue'
import HorizontalLayout from '@/components/layouts/HorizontalLayout.vue'
import SidebarLayout from '@/components/layouts/SidebarLayout.vue'
import SettingsDrawer from '@/components/SettingsDrawer.vue'

// 初始化主题
useTheme()

const settingsStore = useSettingsStore()

// 动态布局组件
const currentLayoutComponent = computed(() => {
  switch (settingsStore.layout) {
    case 'classic':
      return ClassicLayout
    case 'horizontal':
      return HorizontalLayout
    case 'sidebar':
      return SidebarLayout
    default:
      return ClassicLayout
  }
})
</script>

<style lang="scss">
// 全局样式
@import '@/assets/scss/main.scss';

// 定义CSS变量
:root {
  --color-primary: #3b82f6;
  --color-primary-light: #93c5fd;
  --color-primary-dark: #1d4ed8;
}

// 暗黑模式样式
.dark {
  --color-primary: #3b82f6;
  --color-primary-light: #1e40af;
  --color-primary-dark: #1e3a8a;
  
  background-color: #111827;
  color: #f3f4f6;
}
</style>
```

### 9. 主入口文件 (main.ts)

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 导入全局样式
import '@/assets/scss/main.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
```

## 实现说明

1. **状态管理**：
    - 使用 Pinia 管理布局、主题颜色和暗黑模式状态
    - 状态变化自动保存到 localStorage 实现持久化

2. **主题系统**：
    - 通过 CSS 变量定义主题颜色
    - 使用 `useTheme` composable 动态更新主题
    - 暗黑模式通过添加/移除 `dark` 类实现

3. **布局切换**：
    - 使用动态组件 (`<component :is="">`) 根据当前布局状态渲染对应布局
    - 每个布局组件独立开发，保持高内聚

4. **设置抽屉**：
    - 右侧滑出式设计，点击外部或关闭按钮可关闭
    - 包含布局预览、主题颜色选择和暗黑模式切换
    - 使用 Vue Transition 实现平滑动画

5. **样式处理**：
    - 结合 Tailwind CSS 和 Sass 提供灵活样式控制
    - 使用 CSS 变量实现动态主题色

## 使用方式

1. 在任意布局中点击设置图标打开设置抽屉
2. 在布局样式中选择喜欢的布局
3. 在主题颜色中选择喜欢的颜色
4. 切换暗黑模式开关

## 优化建议

1. **性能优化**：
    - 使用 `defineAsyncComponent` 异步加载布局组件
    - 添加布局切换时的加载状态

2. **用户体验**：
    - 添加布局切换动画
    - 实现设置项的实时预览效果

3. **扩展性**：
    - 支持自定义主题颜色
    - 添加更多预设布局

这个实现提供了完整的动态布局切换和主题选择功能，代码结构清晰，注释详细，可以直接集成到您的 Vue3 项目中。