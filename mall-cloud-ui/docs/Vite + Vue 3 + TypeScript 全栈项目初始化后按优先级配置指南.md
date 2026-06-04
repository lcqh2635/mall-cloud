以下是根据你列出的技术栈，按“基础先行、核心后建、体验优化收尾”优先级排序的项目初始化配置文档。每一步都附带了详细中文注释的示例，确保你可直接复制调整。

---

## 优先级排序清单

| 优先级 | 配置项 | 说明 |
|-------|--------|------|
| 1 | 项目脚手架 + TypeScript | 基础骨架，一切的前提 |
| 2 | 路径别名 | 导入路径整洁，开发体验好 |
| 3 | 代码规范 (Biome) + Git 钩子 (Lefthook) | 确保提交质量，越早引入越省心 |
| 4 | Vite 核心插件 (自动导入、组件按需、图标) | 提升开发效率，减少手动导入 |
| 5 | 路由 (Vue Router) | 页面跳转骨架 |
| 6 | 状态管理 (Pinia) + 持久化 | 全局数据与刷新保持 |
| 7 | HTTP 客户端 (Axios 封装) | 统一请求拦截与错误处理 |
| 8 | UI 库 (Element Plus) + 原子 CSS (UnoCSS) | 界面构建效率翻倍 |
| 9 | 国际化 (vue-i18n) | 多语言支持，后期抽离更费劲 |
| 10 | 应用入口整合 | 串联所有模块，保证启动正常 |

---

## 1. 创建 Vite 项目并安装依赖

```bash
# 使用 Vite 官方模板创建 Vue + TypeScript 项目
bun create vite my-vue-app --template vue-ts --no-interactive
cd my-vue-app

# 生产依赖
bun add vue-router pinia pinia-plugin-persistedstate vue-i18n 
bun add axios mitt
bun add element-plus @element-plus/icons-vue
bun add nprogress screenfull figlet
bun add echarts vue-echarts


# 开发依赖
bun remove @types/node
# 使用 Bun 安装 UnoCSS 及其核心生态包作为开发依赖 (-D)：
# - unocss                      : UnoCSS 核心引擎
# - @unocss/preset-uno          : 默认预设（提供类似 Tailwind CSS / Windi CSS 的原子化实用类）
# - @unocss/reset               : CSS 样式重置（消除不同浏览器的默认样式差异）
# - @unocss/preset-attributify  : 属性化模式预设（允许直接在 HTML 标签属性中编写样式，如 <div text-red-400>）
# - @unocss/preset-icons        : 图标预设（支持按需纯 CSS 加载 Iconify 图标集中的任意图标）
bun add -D unocss @unocss/preset-uno @unocss/reset @unocss/preset-attributify @unocss/preset-icons
# 使用 Bun 安装 Vue/Vite 生态常用的自动化插件作为开发依赖 (-D)：
# - unplugin-auto-import      : 按需自动导入 API（如 Vue、Vue Router、Pinia 等），免去手动编写 import 语句
# - unplugin-vue-components   : 按需自动注册组件（支持 Element Plus、Ant Design Vue 等 UI 库或本地组件），无需手动引入和注册
# - unplugin-icons            : 图标插件（通常配合 unplugin-vue-components 使用，支持按需将 Iconify 图标集中的图标作为 Vue 组件加载）
bun add -D unplugin-auto-import unplugin-vue-components unplugin-icons @iconify/vue
bun add -D @biomejs/biome lefthook vitest vitepress
bun add -D @intlify/unplugin-vue-i18n
bun add -D rollup-plugin-visualizer
bun add -D vite-plugin-vue-devtools
bun add -D vite-plugin-mock-dev-server
bun add -D vite-plugin-compression2
bun add -D @types/bun @types/nprogress @types/figlet

# 初始化配置文件
bunx biome init
bunx lefthook install

# 如果使用 less/sass 等预处理器，可额外安装，这里假设只使用 UnoCSS + Element Plus 内置样式
```

---

## 2. 配置 TypeScript 与路径别名

**`tsconfig.json`**（确保 `compilerOptions.paths` 与 `vite.config.ts` 中的 `resolve.alias` 同步）

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "noEmit": true,
    
    // 路径别名配置，与 vite.config 中的 resolve.alias 保持一致
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

**`tsconfig.node.json`**（用于 Vite 配置文件等 Node 环境代码）

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true,
    "strict": true
  },
  "include": ["vite.config.ts"]
}
```

---

## 3. 代码规范 (Biome) 与 Git 钩子 (Lefthook)

创建 **`biome.json`** 在项目根目录：

```json
{
  "$schema": "https://biomejs.dev/schemas/1.7.3/schema.json",
  "organizeImports": {
    "enabled": true
  },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      // 可自定义规则，例如：
      "style": {
        "noNonNullAssertion": "off"
      }
    }
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "trailingComma": "all",
      "semicolons": "always"
    }
  },
  // 覆盖特定文件类型
  "overrides": [
    {
      "include": ["*.vue"],
      "linter": {
        "rules": {
          // Vue 文件中可能不需要某些规则
        }
      }
    }
  ]
}
```

创建 **`lefthook.yml`** 用于提交前自动格式化与检查：

```yaml
# 左钩子配置：提交前自动执行检查和格式化
pre-commit:
  parallel: true
  commands:
    # 1. 格式化代码（Biome）
    format:
      run: npx @biomejs/biome format --write --no-errors-on-unmatched --files-ignore-unknown=true {staged_files}
      stage_fixed: true
    # 2. 代码检查（Biome）
    lint:
      run: npx @biomejs/biome check --apply --no-errors-on-unmatched --files-ignore-unknown=true {staged_files}
      stage_fixed: true
    # 3. 类型检查（可选，较耗时，可放到 pre-push）
    # type-check:
    #   run: npx vue-tsc --noEmit
```

在 `package.json` 中添加脚本方便手动调用：

```json
{
  "scripts": {
    "lint": "biome check src/",
    "format": "biome format src/ --write",
    "prepare": "lefthook install"
  }
}
```

执行 `pnpm prepare` 安装 Git 钩子。

---

## 4. 配置 Vite 核心插件（自动导入、组件按需、图标）

编辑 **`vite.config.ts`**，这是整个工程配置的核心：

```typescript
import { resolve } from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 自动导入 Composition API 等（无需手动 import { ref } from 'vue'）
import AutoImport from 'unplugin-auto-import/vite'
// 自动按需导入组件（Element Plus 组件无需手动注册）
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
// 图标自动导入，使用 iconify 图标集
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'
// UnoCSS
import UnoCSS from 'unocss/vite'

export default defineConfig({
  resolve: {
    // 路径别名，与 tsconfig.json 中的 paths 对应
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  plugins: [
    vue(),
    // UnoCSS 原子化 CSS 引擎
    UnoCSS(),
    // 自动导入 Vue 相关 API 和自定义目录下的模块
    AutoImport({
      // 需要自动导入的库
      imports: [
        'vue',
        'vue-router',
        'pinia',
        // 可添加自定义 API 集合，例如 @vueuse/core
      ],
      // 生成类型声明文件，以便 TypeScript 识别
      dts: 'src/types/auto-imports.d.ts',
      // 自动导入的目录下的模块（如 src/composables 下的组合式函数）
      dirs: ['src/composables', 'src/stores'],
      // 为 Vue 模板中的组件自动导入提供支持
      vueTemplate: true,
      // 解决 Element Plus 的一些自动导入冲突（如 ElMessage 等需要样式）
      resolvers: [ElementPlusResolver()],
    }),
    // 自动按需导入组件
    Components({
      // 指定组件目录，src/components 下的组件会被自动注册
      dirs: ['src/components'],
      // 生成组件类型声明
      dts: 'src/types/components.d.ts',
      // 组件名称扩展名
      extensions: ['vue'],
      // 深度扫描子目录
      deep: true,
      // 解析器：Element Plus 组件和图标组件
      resolvers: [
        ElementPlusResolver(),
        IconsResolver({
          // 图标集前缀，例如 icon-ep-xxx 表示 Element Plus 图标
          prefix: 'icon',
          // 默认使用 ep (Element Plus 图标)
          enabledCollections: ['ep'],
        }),
      ],
    }),
    // 图标插件
    Icons({
      // 自动安装图标集（如果本地没有）
      autoInstall: true,
      // 默认 scale
      scale: 1,
      // 默认使用组件模式，支持直接 <icon-ep-edit /> 使用
      compiler: 'vue3',
    }),
  ],
})
```

配置完成后，你需要创建两个目录，以确保自动导入功能正常工作：
- `src/composables` —— 放组合式函数
- `src/stores` —— 放 Pinia store

另外，确保 `src/auto-imports.d.ts` 和 `src/components.d.ts` 被 Git 忽略或提交（一般生成后提交），并且将它们加入 `.gitignore` 可能需要排除生成文件，建议保留并提交以方便协作。

---

## 5. 配置 Vue Router

创建 **`src/router/index.ts`**：

```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 路由元信息类型扩展（可选）
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
  }
}

// 公共路由表
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'), // 懒加载
    meta: { title: '首页' },
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue'),
    meta: { title: '关于' },
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' },
  },
]

// 创建路由实例
const router = createRouter({
  // 使用 HTML5 History 模式
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // 页面切换时滚动到顶部
  scrollBehavior() {
    return { top: 0 }
  },
})

// 全局前置守卫示例：设置页面标题
router.beforeEach((to) => {
  const title = to.meta.title
  if (title) {
    document.title = `${title} - My App`
  }
})

export default router
```

确保 `src/views/` 下存在对应的页面文件，例如 `Home.vue`、`About.vue`、`NotFound.vue`。

---

## 6. 配置 Pinia 与持久化插件

创建 Pinia 实例并挂载持久化插件 **`src/stores/index.ts`**：

```typescript
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 创建 Pinia 实例并安装持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

export default pinia
```

示例 Store：**`src/stores/user.ts`**

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 用户信息 store，使用组合式 API 风格
export const useUserStore = defineStore(
  'user',
  () => {
    // 状态
    const token = ref<string>('')
    const username = ref<string>('')

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)

    // 操作
    function login(newToken: string, name: string) {
      token.value = newToken
      username.value = name
    }

    function logout() {
      token.value = ''
      username.value = ''
    }

    // 返回需要暴露的状态和方法
    return { token, username, isLoggedIn, login, logout }
  },
  {
    // 持久化配置：默认存储到 localStorage，key 为 store 的 id
    persist: {
      // 可以自定义存储方式
      // storage: sessionStorage,
      // 指定需要持久化的状态字段
      // pick: ['token'],
    },
  },
)
```

因为已经在 `AutoImport` 中指定了 `dirs: ['src/stores']`，所以 `useUserStore` 可以直接在组件中使用而无需手动导入。

---

## 7. 封装 Axios 网络请求

创建 **`src/utils/request.ts`**：

```typescript
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建 Axios 实例
const service: AxiosInstance = axios.create({
  // 从环境变量读取接口基础地址
  baseURL: import.meta.env.VITE_API_BASE_URL,
  // 超时时间
  timeout: 15000,
  // 跨域请求时携带 cookie
  // withCredentials: true,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么：例如添加 token
    const userStore = useUserStore()
    if (userStore.token) {
      // 注意：header 名称请根据后端要求修改
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  },
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 2xx 范围内的状态码都会触发该函数
    const res = response.data

    // 如果后端定义了统一的返回格式，例如 { code, data, message }
    // 可以根据业务状态码进行判断
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 如果需要登出或特殊处理，可在此进行
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        // 跳转登录页
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    // 超出 2xx 范围的状态码都会触发该函数
    console.error('响应错误:', error)
    let message = '网络错误'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `连接错误 ${error.response.status}`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default service
```

在组件或 store 中，可以直接导入 `request` 发起请求：

```typescript
import request from '@/utils/request'

// 示例
export function fetchUserInfo() {
  return request.get('/user/info')
}
```

---

## 8. 引入 Element Plus 与 UnoCSS

### Element Plus

Element Plus 已经在 `vite.config.ts` 中通过 `unplugin-vue-components` 的 `ElementPlusResolver` 实现按需导入，无需在 `main.ts` 中全局注册。但需要引入基础样式（reset 和基础变量）。

在 **`src/main.ts`** 顶部或 `src/assets/styles` 中引入：

```typescript
// 引入 Element Plus 基础样式（仅需一次）
import 'element-plus/dist/index.css'
// 或者按需引入暗黑主题变量等
// import 'element-plus/theme-chalk/dark/css-vars.css'
```

UnoCSS 不需要额外引入样式文件，它通过扫描生成原子类。

### UnoCSS

创建 **`uno.config.ts`** 在项目根目录：

```typescript
import { defineConfig, presetAttributify, presetIcons, presetUno, presetWebFonts } from 'unocss'

export default defineConfig({
  // 快捷键组合（可选）
  shortcuts: {
    'btn': 'py-2 px-4 font-semibold rounded-lg shadow-md',
    'btn-green': 'text-white bg-green-500 hover:bg-green-700',
    'center': 'flex justify-center items-center',
  },
  // 主题定制（可选）
  theme: {
    colors: {
      // 自定义品牌色
      brand: {
        primary: '#409EFF',
        success: '#67C23A',
        warning: '#E6A23C',
        danger: '#F56C6C',
      },
    },
  },
  // 预设
  presets: [
    // 默认 UnoCSS 预设，提供 wind 类等
    presetUno(),
    // 属性化模式，例如 <div flex="~ col"></div>
    presetAttributify(),
    // 图标预设，配合 unplugin-icons 使用
    presetIcons({
      scale: 1.2,
      warn: true,
      extraProperties: {
        'display': 'inline-block',
        'vertical-align': 'middle',
      },
    }),
    // 网络字体预设（可选）
    presetWebFonts({
      fonts: {
        sans: 'Inter',
        mono: 'Fira Code',
      },
    }),
  ],
})
```

此时，可以在组件中像 `class="text-brand-primary btn"` 一样使用原子类了。

---

## 9. 配置国际化 (vue-i18n)

创建 **`src/locales/index.ts`** 作为 i18n 入口：

```typescript
import { createI18n } from 'vue-i18n'
// 引入语言包（可根据需要拆分文件）
import zh from './zh-CN.json'
import en from './en-US.json'

// 定义消息类型（增强类型提示）
type MessageSchema = typeof zh

// 创建 i18n 实例
const i18n = createI18n<[MessageSchema], 'zh-CN' | 'en-US'>({
  // 默认语言
  locale: localStorage.getItem('lang') || 'zh-CN',
  // 兜底语言
  fallbackLocale: 'zh-CN',
  // 关闭控制台警告（生产环境可开启）
  silentFallbackWarn: true,
  // 消息
  messages: {
    'zh-CN': zh,
    'en-US': en,
  },
  // 全局可用 $t 函数
  globalInjection: true,
  // 传统模式（使用 options API 时可开启），组合式 API 推荐关闭
  legacy: false,
})

export default i18n
```

创建简单的语言文件示例：

**`src/locales/zh-CN.json`**
```json
{
  "hello": "你好",
  "welcome": "欢迎使用 My App"
}
```

**`src/locales/en-US.json`**
```json
{
  "hello": "Hello",
  "welcome": "Welcome to My App"
}
```

注意：Element Plus 自身也有国际化配置，需要在 `main.ts` 中同步：

```typescript
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
// 根据 i18n locale 动态设置
const elementLocale = i18n.global.locale.value === 'zh-CN' ? zhCn : en
app.use(ElementPlus, { locale: elementLocale })
```

更优雅的做法是封装一个 composable 监听 locale 变化自动切换 Element Plus 语言。

---

## 10. 应用入口整合 (`main.ts` 和 `App.vue`)

**`src/main.ts`** 完整示例：

```typescript
import { createApp } from 'vue'
import App from './App.vue'

// 路由
import router from './router'
// 状态管理
import pinia from './stores'
// 国际化
import i18n from './locales'
// Element Plus 基础样式
import 'element-plus/dist/index.css'
// UnoCSS 重置样式（可选，如果使用 Uno 自己的 reset）
import '@unocss/reset/tailwind.css'
// 自定义全局样式
import '@/assets/styles/global.css'

const app = createApp(App)

// 按顺序注册插件
app.use(pinia)
app.use(router)
app.use(i18n)

// 挂载应用
app.mount('#app')
```

**`src/App.vue`** 基础示例：

```vue
<script setup lang="ts">
// 由于配置了自动导入，这里 ref 无需 import
const count = ref(0)
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
    <h1 class="text-brand-primary text-3xl font-bold mb-4">
      {{ $t('welcome') }}
    </h1>
    <el-button type="primary" @click="count++">
      {{ $t('hello') }}：{{ count }}
    </el-button>
    <!-- 图标使用示例：Element Plus 图标自动按需，通过 Icons 插件 -->
    <icon-ep-edit class="text-xl ml-2" />
    <router-view />
  </div>
</template>
```

此时，启动 `pnpm dev` 应该可以看到 Element Plus 按钮、UnoCSS 原子类、国际化文本和图标都正常工作。

---

## 11. 环境变量与 VSCode 配置（可选）

创建 **`.env.development`** 和 **`.env.production`**：

```env
# .env.development
VITE_API_BASE_URL = /api
```

```env
# .env.production
VITE_API_BASE_URL = https://api.example.com
```

在 `vite.config.ts` 中可配置代理（开发环境），避免跨域：

```typescript
export default defineConfig({
  // ...
  server: {
    proxy: {
      '/api': {
        target: 'http://backend-api.local',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
```

为了团队成员保持一致的编辑器设置，增加 **`.vscode/settings.json`**：

```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.organizeImports.biome": true,
    "quickfix.biome": true
  },
  "editor.defaultFormatter": "biomejs.biome",
  "[vue]": {
    "editor.defaultFormatter": "biomejs.biome"
  },
  "typescript.tsdk": "node_modules/typescript/lib"
}
```

并推荐安装 Biome 扩展。

---

## 总结

以上配置完成后，你的项目将拥有：

- ✅ 开箱即用的 Vue 3 + TypeScript 开发环境
- ✅ 自动导入 API 和组件，告别重复 `import`
- ✅ 图标按需加载，零负担使用成千上万的图标
- ✅ 原子化 CSS 与 Element Plus 完美结合
- ✅ 严格且自动化的代码规范，提交即自动修复
- ✅ 类型安全的路由、多语言、状态持久化
- ✅ 统一的 HTTP 请求拦截与错误处理

你可以按照优先级顺序逐个落地，每一环节都有详细的注释帮助你理解每个选项的作用。如果某些工具暂时不需要，也可以灵活裁剪。希望这份文档能助你顺利起航！