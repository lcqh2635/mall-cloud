搭建一个基于 **Vite + Vue 3 + TypeScript** 及众多现代前端工具链的企业级项目，合理的配置顺序和架构设计至关重要。

以下我将按照**优先级从高到低**（从底层构建到业务赋能）的顺序，为您梳理初始阶段需要配置的核心内容，并提供带有详细中文注释的配置示例。

---

### 📦 0. 依赖安装准备
在开始配置前，请确保安装了所有必需的依赖包：
```bash
# 1. 创建项目
bun create vite my-vue-app --template vue-ts
# 2. 进入目录
cd my-vue-app

# 3. 安装核心依赖
bun add vue-router pinia pinia-plugin-persistedstate axios element-plus @element-plus/icons-vue @vueuse/core vue-i18n

# 4. 安装开发依赖 (UI/样式/构建工具)
bun add -D unocss @unocss/reset @unocss/preset-uno @unocss/preset-attributify @unocss/preset-icons
bun add -D unplugin-auto-import unplugin-vue-components unplugin-icons @iconify/vue
bun add -D @biomejs/biome lefthook
bun add -D @types/bun

```

---

### 🥇 优先级 1：基础构建与路径解析 (Vite + TS)
**说明**：这是项目的地基。Vite 负责极速冷启动和模块热替换，TypeScript 提供类型安全，别名配置能极大改善后续开发时的路径引入体验。


#### 1. `tsconfig.app.json` (TypeScript 配置)
```json
{
  "compilerOptions": {
    "baseUrl": ".",
    // 必须与 Vite 中的 alias 保持一致
    "paths": {
      "@/*": ["./src/*"],           // 根路径别名，通用
      "@api/*": ["./src/api/*"],    // 接口请求别名
      "@assets/*": ["./src/assets/*"], // 静态资源别名
      "@components/*": ["./src/components/*"], // 组件别名 (也可用 @c)
      "@composables/*": ["./src/composables/*"], // 组合式函数别名
      "@layouts/*": ["./src/layouts/*"], // 布局别名
      "@locales/*": ["./src/locales/*"], // 国际化别名
      "@routers/*": ["./src/routers/*"], // 路由别名
      "@stores/*": ["./src/stores/*"], // 状态管理别名
      "@utils/*": ["./src/utils/*"],  // 工具函数别名
      "@views/*": ["./src/views/*"]   // 页面视图别名
    }
  },
  "include": [
    "src/**/*.ts",
    "src/**/*.d.ts",
    "src/**/*.tsx",
    "src/**/*.vue"
  ]
}

```


#### 1. `vite.config.ts` (Vite 核心配置)
```typescript
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {fileURLToPath} from 'url'

// 后续会加入的插件先在此处引入
import UnoCSS from 'unocss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'

export default defineConfig({
   plugins: [
      vue(),
      UnoCSS(), // 原子化 CSS 引擎
      AutoImport({
         // 自动导入 Vue、Vue Router、Pinia、VueUse 等 API，无需手动 import
         imports: ['vue', 'vue-router', 'pinia', '@vueuse/core'],
         // 自动导入 Element Plus 的方法 (如 ElMessage, ElMessageBox)
         resolvers: [ElementPlusResolver()],
         dts: 'src/auto-imports.d.ts', // 生成 TypeScript 声明文件
      }),
      Components({
         // 自动导入 Element Plus 组件
         resolvers: [
            ElementPlusResolver(),
            // 自动注册图标组件，允许在模板中直接使用 <i-ep-edit />
            IconsResolver({enabledCollections: ['ep']}),
         ],
         dts: 'src/components.d.ts',
      }),
      Icons({
         autoInstall: true, // 自动安装用到的图标集
         compiler: 'vue3',
      }),
   ],
   resolve: {
      alias: {
         // 👉 官方推荐的 ESM 写法
         // import.meta.url 返回当前模块(vite.config.ts)的绝对 URL (file:///路径)
         // new URL('./src', import.meta.url) 基于当前文件路径解析出 src 的 URL
         // fileURLToPath() 将 file:// 协议的 URL 转换为系统可识别的绝对文件路径

         '@': fileURLToPath(new URL('./src', import.meta.url)),
         '@api': fileURLToPath(new URL('./src/api', import.meta.url)),
         '@assets': fileURLToPath(new URL('./src/assets', import.meta.url)),
         '@components': fileURLToPath(new URL('./src/components', import.meta.url)),
         '@composables': fileURLToPath(new URL('./src/composables', import.meta.url)),
         '@layouts': fileURLToPath(new URL('./src/layouts', import.meta.url)),
         '@locales': fileURLToPath(new URL('./src/locales', import.meta.url)),
         '@routers': fileURLToPath(new URL('./src/routers', import.meta.url)),
         '@stores': fileURLToPath(new URL('./src/stores', import.meta.url)),
         '@utils': fileURLToPath(new URL('./src/utils', import.meta.url)),
         '@views': fileURLToPath(new URL('./src/views', import.meta.url)),
      },
   },

   server: {
      port: 3000,
      proxy: {
         // 开发环境代理配置，解决跨域问题
         '/api': {
            target: 'http://your-backend-api.com',
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
         },
      },
   },
})
```

---

### 🥈 优先级 2：代码规范与工程化护栏 (Biome + Lefthook)
**说明**：在编写任何业务代码前，先建立代码质量防线。**Biome** 是目前极快的 Linter/Formatter（完美替代 ESLint + Prettier），**Lefthook** 用于管理 Git 钩子（替代 Husky）。

#### 1. `biome.json` (Biome 配置)
```json
{
  "$schema": "https://biomejs.dev/schemas/1.8.0/schema.json",
  "organizeImports": { "enabled": true },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "suspicious": { "noExplicitAny": "warn" },
      "style": { "useImportType": "error" }
    }
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  }
}
```

#### 2. `lefthook.yml` (Git Hooks 配置)
```yaml
# 在代码提交前 (pre-commit) 自动执行代码检查和格式化
pre-commit:
  parallel: true
  commands:
    biome-check:
      glob: "*.{js,ts,vue,json}"
      # 自动修复并格式化暂存区的文件
      run: npx @biomejs/biome check --apply --no-errors-on-unmatched {staged_files}
      stage_fixed: true
```
*执行 `npx lefthook install` 激活钩子。*

---

### 🥉 优先级 3：全局架构核心 (Router + Pinia)
**说明**：路由控制页面流转，Pinia 控制全局状态，持久化插件保证用户刷新页面状态不丢失。

#### 1. `src/router/index.ts` (Vue Router)
```typescript
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    // 路由懒加载，优化首屏体积
    component: () => import('@/views/Home.vue'), 
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
```

#### 2. `src/stores/index.ts` (Pinia 与持久化)
```typescript
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia()
// 注册持久化插件，默认使用 localStorage
pinia.use(piniaPluginPersistedstate)

export default pinia
```

*(业务 Store 示例：`src/stores/user.ts`)*
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const setToken = (newToken: string) => { token.value = newToken }
  
  return { token, setToken }
}, {
  // 开启持久化配置
  persist: true, 
})
```

---

### 🏅 优先级 4：网络通信层 (Axios)
**说明**：封装统一的请求实例，处理 Token 注入、全局错误拦截、Loading 状态和统一的 UI 提示。

#### `src/utils/request.ts`
```typescript
import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    // 自动携带 Token
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 假设后端规范返回格式为 { code: 200, data: any, message: string }
    const { code, data, message } = response.data
    if (code === 200) return data
    
    ElMessage.error(message || '系统错误')
    return Promise.reject(new Error(message || 'Error'))
  },
  (error) => {
    ElMessage.error(error.message || '网络连接异常')
    return Promise.reject(error)
  }
)

export default service
```

---

### 🏅 优先级 5：样式引擎与国际化 (Unocss + I18n)
**说明**：Unocss 提供极速的原子化 CSS 体验，I18n 为后续多语言扩展做好准备。

#### 1. `uno.config.ts` (Unocss 配置)
```typescript
import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(), // 基础预设 (类似 Tailwind)
    presetAttributify(), // 支持属性化写法 (如 <div text="red-500">)
    presetIcons({
      scale: 1.2,
      warn: true,
    }),
  ],
  shortcuts: {
    // 定义常用的样式快捷方式
    'flex-center': 'flex justify-center items-center',
    'btn-primary': 'px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600',
  },
})
```

#### 2. `src/i18n/index.ts` (国际化配置)
```typescript
import { createI18n } from 'vue-i18n'
// 建议在 locales 文件夹下管理语言包
import zhCN from './locales/zh-CN.json'
import enUS from './locales/en-US.json'

const i18n = createI18n({
  legacy: false, // 必须为 false，以使用 Vue 3 Composition API 模式
  locale: 'zh-CN',
  fallbackLocale: 'en-US',
  messages: { 'zh-CN': zhCN, 'en-US': enUS },
})

export default i18n
```

---

### 🏁 优先级 6：应用入口组装 (Main.ts)
**说明**：将上述所有独立配置的模块进行“插拔式”组装，完成 Vue 应用的实例化。

#### `src/main.ts`
```typescript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import i18n from './i18n'

// 引入样式重置与 Unocss 虚拟样式
import '@unocss/reset/tailwind.css'
import 'virtual:uno.css'

// Element Plus 的暗黑模式或全局基础样式(如果需要)
import 'element-plus/theme-chalk/dark/css-vars.css'

const app = createApp(App)

// 挂载核心插件
app.use(router)
app.use(pinia)
app.use(i18n)

// 挂载应用
app.mount('#app')
```

---

### 💡 核心开发体验说明 (为什么这么配？)

1. **摆脱手动 Import (Unplugins)**：
   配置了 `unplugin-auto-import` 和 `unplugin-vue-components` 后，你在 Vue 文件里使用 `ref`、`watch`、`ElMessage` 或者 `<el-button>` 时，**完全不需要在顶部写 `import`**。插件会在编译时自动帮你注入，且会自动生成 `.d.ts` 保证 TS 提示完美。
2. **图标即组件 (Unplugin-icons)**：
   结合 Element Plus 的图标集，你可以直接在模板里写 `<i-ep-user-filled class="text-red-500" />`，它会被编译为按需引入的 SVG 组件，彻底告别手动引入图标组件和注册。
3. **极速格式化 (Biome)**：
   相比 ESLint + Prettier，Biome 使用 Rust 编写，速度提升数十倍。在 Lefthook 的加持下，每次 `git commit` 都会瞬间完成代码校验和格式化，保证进入仓库的代码风格绝对统一。
4. **统一网络出口 (Axios 封装)**：
   业务层只需关注 `await api.getUser()`，无需关心 Token 怎么带、错误怎么弹窗、Loading 怎么控制，实现了 UI 层与数据层的解耦。