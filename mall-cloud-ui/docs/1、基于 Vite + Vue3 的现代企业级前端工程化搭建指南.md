搭建一个基于 **Vite + Vue 3 + TypeScript** 及众多现代前端工具链的企业级项目，合理的配置顺序和架构设计至关重要。

以下我将按照**优先级从高到低**（从底层构建到业务赋能）的顺序，为您梳理初始阶段需要配置的核心内容，并提供带有详细中文注释的配置示例。

---

### 📦 0. 依赖安装准备
在开始配置前，请确保安装了所有必需的依赖包：
```bash
npm config set registry https://registry.npmmirror.com
npm install -g bun create-vite
# 1. 创建项目
bun create vite my-vue-app --template vue-ts --no-interactive
# 2. 进入目录
cd my-vue-app
# 必须初始化 Git 仓库，下面的 lefthook 执行命令要求项目必须存在 Git 仓库
git init

# 3. 安装核心依赖
bun add vue-router pinia pinia-plugin-persistedstate axios @vueuse/core vue-i18n
bun add element-plus echarts vue-echarts screenfull nprogress mitt

# 4. 安装开发依赖 (UI/样式/构建工具)
bun remove @types/node
bun add -D @types/bun
# unocss 这个主包的 dependencies 已经把常用的预设（如 preset-uno、preset-attributify、preset-icons）、
# 转换器和集成工具（如 @unocss/vite）都包含进去了。官方这样设计就是为了方便开发者“一键安装”，避免遗漏。不需要再单独安装这些子包了
bun add -D unocss @unocss/reset
# 当你安装了 @iconify-json/ep 并配合 unplugin-icons 使用后，@element-plus/icons-vue 就变得完全多余了。
bun add -D unplugin-auto-import unplugin-vue-components unplugin-icons @iconify-json/ep
bun add -D rollup-plugin-visualizer vitest vitepress @intlify/unplugin-vue-i18n
bun add -D vite-plugin-vue-devtools vite-plugin-mock-dev-server vite-plugin-compression2
bun add -D boxen figlet @types/figlet
bun add -D @biomejs/biome lefthook

# 代码规范配置 (Biome + Lefthook)
# 虽然 Biome 可以零配置运行，但你可能需要根据项目需求调整一些设置。在这种情况下，你可以运行以下命令生成 biome.json 配置文件。
bunx --bun biome init
# 如果不存在 lefthook 配置文件，则创建一个空的 lefthook.yml 文件，里面包含配置一些示例。
bunx --bun lefthook install

# 创建根目录结构
mkdir -vp mocks plugins docs tests
mkdir -vp tests/{unit,e2e}
# 创建环境配置文件
touch .env .env.development .env.production .env.test
# 创建配置文件
touch bunfig.toml README.zh-CN.md uno.config.ts
# 创建 src 源码目录结构
mkdir -vp src/{api,composables,directives,layouts,locales,plugins,router,stores,styles,types,utils,views}
mkdir -vp src/assets/{images,fonts}
touch src/composables/{index,useAuth}.ts
touch src/directives/{index,auth,permission}.ts
touch src/layouts/{index,DefaultLayout}.vue
mkdir -vp src/layouts/components
# 创建 router 目录结构
touch src/router/{index,routes,constants}.ts
mkdir -vp src/router/{guards,modules}
touch src/router/guards/{auth,permission}.ts
# 创建 stores 目录结构
touch src/stores/index.ts
mkdir -vp src/stores/modules
# 创建 locales 目录结构
touch src/locales/index.ts
mkdir -vp src/locales/lang/{zh-CN,en-US}
touch src/locales/lang/zh-CN/{common,layout,login,dashboard,index}.ts
touch src/locales/lang/en-US/{common,layout,login,dashboard,index}.ts
touch src/styles/{index,variables}.scss
touch src/utils/{index,storage,format,validate,request}.ts
mkdir -vp src/views/{dashboard,user}
```

---

### 🥇 优先级 1：基础构建与路径解析 (Vite + TS)
**说明**：这是项目的地基。Vite 负责极速冷启动和模块热替换，TypeScript 提供类型安全，别名配置能极大改善后续开发时的路径引入体验。


#### 1. `tsconfig.node.json` (该文件的作用是：指导 IDE（如 VSCode）和 TypeScript 语言服务如何进行类型检查和代码提示）
```json
{
   "compilerOptions": {
      "target": "ESNext",
      "lib": ["ESNext", "DOM", "DOM.Iterable"],
      "module": "ESNext",
      // 💡 提示：如果你想要 Bun 的专属类型提示（如 Bun.env 等），可以运行 bun add -d @types/bun，
      // 然后将上面 types 数组改为 ["bun"] 或 ["node", "bun"]。但对于 vite.config.ts 来说，
      // 仅保留 "node" 通常足够了，因为里面用的都是 Node 的 API（如 path/url）。
      "types": ["bun"]
   }
}
```

#### 2. `tsconfig.app.json` (TypeScript 配置)
```json
{
  "compilerOptions": {
    // 解析非相对模块的基准目录。
    // 设为 "." 代表以当前文件 (tsconfig.app.json) 所在的项目根目录为基准。
    // 它的主要作用是配合下方的 "paths" 配置项，让 TypeScript 知道如何将别名 (如 @/*) 映射到实际的物理路径 (如 src/*)。
    // 注意：如果没有配置 baseUrl，paths 中的相对路径将无法正确解析。
    "baseUrl": ".",
    // 必须与 Vite 中的 alias 保持一致
    "paths": {
      "@/*": ["src/*"],           // 根路径别名，通用
      "@api/*": ["src/api/*"],    // 接口请求别名
      "@assets/*": ["src/assets/*"], // 静态资源别名
      "@components/*": ["src/components/*"], // 组件别名 (也可用 @c)
      "@composables/*": ["src/composables/*"], // 组合式函数别名
      "@layouts/*": ["src/layouts/*"], // 布局别名
      "@locales/*": ["src/locales/*"], // 国际化别名
      "@routers/*": ["src/routers/*"], // 路由别名
      "@stores/*": ["src/stores/*"], // 状态管理别名
      "@types/*": ["src/types/*"],  // 全局类型别名
      "@utils/*": ["src/utils/*"],  // 工具函数别名
      "@views/*": ["src/views/*"]   // 页面视图别名
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

#### 3. `vite.config.ts` (Vite 核心配置)
```typescript
import {defineConfig} from 'vite'
// 👉 引入 Bun 内置的 URL 与路径工具 API，用于 ESM 规范的路径解析
import { fileURLToPath } from 'bun'

// 👉 封装 ESM 规范的路径解析辅助函数，简化代码
const r = (path: string) => fileURLToPath(new URL(path, import.meta.url))

export default defineConfig({
   // ============================================================
   // 1. 路径解析配置 (Resolve)
   // ============================================================
   resolve: {
      alias: {
         // 👉 官方推荐的 ESM 写法配置路径别名，避免使用 __dirname
         // import.meta.url 返回当前模块(vite.config.ts)的绝对 URL (file:///路径)
         // new URL('src', import.meta.url) 基于当前文件路径解析出 src 的 URL
         // fileURLToPath() 将 file:// 协议的 URL 转换为系统可识别的绝对文件路径

         '@': r('src'),
         '@api': r('src/api'),
         '@assets': r('src/assets'),
         '@components': r('src/components'),
         '@composables': r('src/composables'),
         '@layouts': r('src/layouts'),
         '@locales': r('src/locales'),
         '@routers': r('src/routers'),
         '@stores': r('src/stores'),
         '@types': r('src/types'),
         '@utils': r('src/utils'),
         '@views': r('src/views'),
      },
      // 导入时想要省略的扩展名列表
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
   }
})
```

---

### 🥈 优先级 2：代码规范与工程化护栏 (Biome + Lefthook)
**说明**：在编写任何业务代码前，先建立代码质量防线。**Biome** 是目前极快的 Linter/Formatter（完美替代 ESLint + Prettier），**Lefthook** 用于管理 Git 钩子（替代 Husky）。

*执行 `bunx --bun biome init` 创建初始 biome.json 配置文件。*

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

*执行 `bunx --bun lefthook install` 创建初始 lefthook.yml 配置文件。*
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

### 🥉 优先级 3：`vite.config.ts` 常用插件配置
```typescript
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// 👉 引入 Bun 内置的 URL 与路径工具 API，用于 ESM 规范的路径解析
import { fileURLToPath } from 'bun'

// ====================================================================
// 插件：@vitejs/plugin-vue
// 作用：提供对 Vue 3 单文件组件 (SFC) 的编译支持
// 效果：让 Vite 能够识别并编译 .vue 文件
// 官方文档：https://github.com/vitejs/vite-plugin-vue
// ====================================================================
import vue from '@vitejs/plugin-vue'

// ====================================================================
// 插件：unocss
// 作用：即时原子化 CSS 引擎
// 效果：通过类名快速编写样式，生成极小的 CSS 文件，提升开发效率
// 官方文档：https://unocss.dev/
// ====================================================================
import UnoCSS from 'unocss/vite'

// ====================================================================
// 插件：unplugin-auto-import
// 作用：自动导入 Vue、VueRouter、Pinia 等库的 API，以及自定义的 hooks
// 效果：无需再写 import { ref, reactive, useRouter } from 'xxx'
// 官方文档：https://github.com/unplugin/unplugin-auto-import
// ====================================================================
import AutoImport from 'unplugin-auto-import/vite'

// ====================================================================
// 插件：unplugin-vue-components
// 作用：自动导入和注册 Vue 组件（包括 UI 库组件和自定义组件）
// 效果：无需再写 import MyComponent from '@/components/xxx.vue' 并手动注册
// 官方文档：https://github.com/unplugin/unplugin-vue-components
// ====================================================================
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// ====================================================================
// 插件：unplugin-icons
// 作用：按需访问海量图标集 (基于 Iconify)，将图标作为 Vue 组件使用
// 效果：直接在模板中使用图标组件，自动按需编译所需图标，无需手动引入 SVG
// 官方文档：https://github.com/unplugin/unplugin-icons
// ====================================================================
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'

// ====================================================================
// 插件：@intlify/unplugin-vue-i18n
// 作用：为 Vue I18n 提供编译时优化
// 效果：预编译 locale 文件，消除运行时警告，大幅提升国际化切换性能
// 官方文档：https://github.com/intlify/bundle-tools/tree/main/packages/unplugin-vue-i18n
// ====================================================================
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'

// ====================================================================
// 插件：vite-plugin-vue-devtools
// 作用：集成 Vue 官方开发者工具至浏览器
// 效果：在开发环境下提供组件树查看、状态审查、路由追踪等调试功能
// 官方文档：https://devtools.vuejs.org/
// ====================================================================
import VueDevTools from 'vite-plugin-vue-devtools'

// ====================================================================
// 插件：vite-plugin-mock-dev-server
// 作用：基于文件系统的 Mock 开发服务器，支持热更新及 WebSocket
// 效果：在开发环境拦截 API 请求并返回模拟数据，无需依赖真实后端
// 官方文档：https://vite-plugin-mock-dev-server.netlify.app/zh/
// ====================================================================
import { mockDevServerPlugin } from 'vite-plugin-mock-dev-server'

// ====================================================================
// 插件：vite-plugin-compression2
// 作用：在构建打包时生成压缩文件 (如 .gz)
// 效果：配合服务器静态压缩功能，大幅减小传输体积，提升首屏加载速度
// 官方文档：https://github.com/nonzzz/vite-plugin-compression
// ====================================================================
import { compression } from 'vite-plugin-compression2'

// ====================================================================
// 插件：rollup-plugin-visualizer
// 作用：打包体积分析与可视化
// 效果：生成直观的 HTML 报告，帮助排查依赖体积过大问题，优化打包策略
// 官方文档：https://github.com/btd/rollup-plugin-visualizer
// ====================================================================
import { visualizer } from 'rollup-plugin-visualizer'

// 👉 封装 ESM 规范的路径解析辅助函数
const r = (path: string) => fileURLToPath(new URL(path, import.meta.url))

// https://cn.vite.dev/config/
export default defineConfig(({ mode, command }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  // 判断是否为生产环境构建
  const isBuild = command === 'build'

  return {
    // ============================================================
    // 1. 路径解析配置 (Resolve)
    // ============================================================
    resolve: {
      alias: {
        '@': r('src'),
        '@api': r('src/api'),
        '@assets': r('src/assets'),
        '@components': r('src/components'),
        '@composables': r('src/composables'),
        '@layouts': r('src/layouts'),
        '@locales': r('src/locales'),
        '@routers': r('src/routers'),
        '@stores': r('src/stores'),
        '@types': r('src/types'),
        '@utils': r('src/utils'),
        '@views': r('src/views'),
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
    },

    // ============================================================
    // 2. 插件配置 (Plugins)
    // ============================================================
    plugins: [
      vue(),
      // 【Vue 开发者工具】(仅开发环境生效，生产包自动剔除)
      VueDevTools(),
      // 【Mock 数据服务器】(开发环境提供接口模拟)
      mockDevServerPlugin({
        // 1. 接口前缀匹配规则
        // 只有以这些前缀开头的请求路径，才会被 Mock 服务拦截。
        // 默认值为 ['/api/']。支持字符串、正则表达式或数组。
        // 如果你的后端接口统一前缀是 /api，这里保持默认即可；
        // 如果有多个前缀（如 /api, /auth, /service），可以写成数组。
        prefix: ['/api', '/auth'],
        // 2. WebSocket 路径前缀匹配
        // 如果你需要模拟 WebSocket 通信，需要配置此项。
        // 匹配到该前缀的 WS 请求会被插件拦截并进入 WS Mock 逻辑。
        // 如果不需要模拟 WS，可以忽略此配置。
        wsPrefix: ['/ws', '/socket.io'],
        // 目录配置：将默认的 mock 改为 mocks
        dir: 'mocks',
        // 3. 修改 Mock 文件时是否强制刷新页面
        // 默认值为 false。
        // 当你修改了 mock 文件，插件会自动热更新接口数据，无需刷新页面。
        // 如果你希望修改 mock 文件后，连带着整个页面 UI 状态重置（比如清空输入框状态），可以设为 true。
        reload: false,
        // 4. 终端日志级别控制
        // 控制在开发环境终端打印的 Mock 请求日志信息量。
        // 'error' | 'warn' | 'info' | 'debug' | 'silent'
        // 默认 'info' 会打印每个被拦截的请求路径和耗时，方便调试。
        // 如果觉得终端输出太吵，可以改为 'warn' 或 'silent'。
        log: 'info',
        // 5. 跨域资源配置 (CORS)
        // 默认情况下，插件会为 Mock 接口自动添加 CORS 头，方便前端开发。
        // 如果需要自定义 CORS 规则，可以在这里配置；如果设为 false，则不添加 CORS 头。
        cors: {
          origin: '*',
          methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
          allowedHeaders: ['Content-Type', 'Authorization'],
        },
      }),
      // 【UnoCSS 原子化引擎】
      UnoCSS(),
      // ====================================================================
      // 插件一：unplugin-auto-import
      // 作用：自动导入 Vue、VueRouter、Pinia 等库的 API，以及自定义的 hooks
      // 效果：无需再写 import { ref, reactive, useRouter } from 'xxx'
      // ====================================================================
      AutoImport({
        // 1. 指定需要自动导入 API 的第三方库
        imports: [
          'vue',         // 自动导入 ref, reactive, computed, watch, onMounted 等
          'vue-router',  // 自动导入 useRouter, useRoute, onBeforeRouteLeave 等
          'pinia',       // 自动导入 defineStore, storeToRefs 等
          '@vueuse/core',// 自动导入 VueUse 的所有工具函数 (如 useMouse, useStorage)
          'vue-i18n',    // 自动导入 useI18n, t 等 (需结合 vue-i18n 配置)
        ],
        // 2. 解析器：用于按需导入特定 UI 库的 API 或其他需要特殊处理的模块
        resolvers: [
          // Element Plus 解析器：自动导入 ElMessage, ElMessageBox, ElNotification 等方法
          // 配置后，直接调用 ElMessage('提示') 不会报错，且打包时只包含用到的部分
          ElementPlusResolver(),
          // Icons 解析器：配合 unplugin-icons，自动导入图标组件作为函数使用
          // prefix: 'Icon' 表示组件名前缀，例如在代码中使用 const EditIcon = IconEpEdit
          IconsResolver({
            prefix: 'Icon',
          }),
        ],
        // 3. 自动扫描并导入指定目录下的自定义模块导出，非常适合管理项目的组合式函数
        dirs: [
          'src/composables', // 扫描 src/composables 目录下的所有 ts/js 文件
          'src/stores',      // 扫描 stores，配合 pinia 的 defineStore 无需手动引入
        ],
        // 4. 生成类型声明文件的位置
        // 这对于 TypeScript 项目至关重要，默认情况下，它会在项目根目录生成 auto-imports.d.ts
        // 告诉 IDE 这些全局变量存在，避免红色波浪线报错
        dts: 'src/types/auto-imports.d.ts',
        // 5. Vue 模板支持
        // 开启后，在 .vue 文件的 <template> 中也可以直接使用自动导入的 API，无需在 <script> 中声明
        vueTemplate: true,
        // 6. ESLint 配置 (由于本项目使用 Biome 替代了 ESLint，此配置可忽略或设为 false)
        eslintrc: {
          enabled: false,
        },
      }),
      // ====================================================================
      // 插件二：unplugin-vue-components
      // 作用：自动导入并注册 Vue 组件（包括 UI 库组件和项目自定义组件）
      // 效果：无需再写 import MyComponent from '@/components/MyComponent.vue'
      // ====================================================================
      Components({
        // 1. 解析器：用于按需导入第三方 UI 组件库
        resolvers: [
          // Element Plus 组件解析器
          // 配置后，在模板中直接写 <el-button> 即可，打包时自动按需引入对应组件代码
          ElementPlusResolver(),
          // Icons 组件解析器
          // 配置后，在模板中直接写 <IconEpEdit /> 即可渲染 Element Plus 的 Edit 图标
          // enabledCollections: 指定允许自动导入的图标集合范围，'ep' 对应 @iconify-json/ep
          IconsResolver({
            enabledCollections: ['ep'],
          }),
        ],
        // 2. 自动扫描并注册项目自定义组件的目录
        // 默认就是扫描 'src/components'
        dirs: ['src/components'],
        // 6. 生成组件类型声明文件的位置
        // 生成 components.d.ts，让 TypeScript 识别自动注册的全局组件
        dts: 'src/types/components.d.ts',
      }),
      // ====================================================================
      // 插件三：unplugin-icons
      // 作用：按需访问海量图标集 (基于 Iconify)，并将图标作为 Vue 组件使用
      // ====================================================================
      Icons({
        // 1. 自动安装缺失的图标集依赖
        // 当你使用了一个未安装的图标集(如 @iconify-json/carbon)，
        // 开启此选项会自动执行 npm install 安装，非常省心
        autoInstall: true,
        // 2. 编译器：指定图标组件最终编译成哪种形态
        // 'vue3' 会将图标编译为 Vue3 的单文件组件形式，性能最佳
        compiler: 'vue3',
        // 3. 图标默认样式/类名
        // 因为 Iconify 图标默认是 SVG，宽高可能未定，这里可以设置默认样式
        defaultClass: 'inline-block', // 通常设置为 inline-block，方便排版对齐
        // 4. 缩放比例，默认为：1.2
        // 默认 1，配合 UnoCSS 的 text-xs 等类名控制图标大小效果最佳
        scale: 1,
      }),
      // 【Vue I18n 编译优化】
      // 作用：预编译 locale 文件，消除运行时警告，大幅提升性能
      VueI18nPlugin({
        // 指定语言包路径，支持 json/yaml/yml 格式
        include: [r('src/locales/**')],
        // 允许在 SFC 的 <i18n> 块中使用，如果没用到可设为 false 减少开销
        allowDynamic: true,
        // 需要 @intlify/vue-i18n-extensions 配合，开启 composition API 优化
        fullInstall: false,
      }),

      // 【vite-plugin-compression2 生产环境 Gzip 压缩】
      isBuild &&
      compression({
        threshold: 10240, // 大于 10KB 才压缩
        deleteOriginalAssets: false, // 不删除原文件，保留备选
      }),
      // 【rollup-plugin-visualizer 生产环境包体积分析】
      // 运行 build 后会在项目根目录生成 stats.html，可视化查看依赖占比
      isBuild &&
      visualizer({
        filename: 'stats.html',
        open: true, // 构建完成后自动在浏览器打开报告
        gzipSize: true, // 显示 gzip 后的大小
        brotliSize: true, // 显示 brotli 后的大小
      }),
    ].filter(Boolean), // 过滤掉条件为 false 的插件

    // ============================================================
    // 3. CSS 配置
    // ============================================================
    css: {
      preprocessorOptions: {
        // 如果使用了 SCSS，可以在此注入全局变量
        // scss: {
        //   additionalData: `@use "@assets/styles/variables.scss" as *;`
        // }
      },
    },

    // ============================================================
    // 4. 构建与打包配置 (Build)
    // ============================================================
    build: {
      target: 'modules',
      outDir: 'dist',
      assetsDir: 'assets',
      minify: 'esbuild',
      chunkSizeWarningLimit: 1000,
      rollupOptions: {
        output: {
          // 精细化分包策略，充分利用浏览器缓存
          manualChunks(id) {
            if (id.includes('node_modules')) {
              if (id.includes('element-plus')) return 'vendor-element-plus'
              if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) return 'vendor-vue'
              if (id.includes('vue-i18n')) return 'vendor-i18n'
              return 'vendor-libs'
            }
          },
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
        },
      },
    },

    // ============================================================
    // 5. 开发服务器配置 (Server)
    // ============================================================
    server: {
      host: '0.0.0.0',
      port: 3000,
      open: false,
      cors: true,
      // 代理配置
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          ws: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },

    // ============================================================
    // 6. 依赖优化配置 (OptimizeDeps)
    // ============================================================
    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'axios',
        'element-plus/es/locale/lang/zh-cn',
        '@vueuse/core',
        'vue-i18n', // 预构建 vue-i18n 加速开发环境
      ],
    },

    // ============================================================
    // 7. 单元测试配置 (Vitest)
    // ============================================================
    test: {
      // 使用 vitest，配置全局 API
      globals: true,
      // 使用 jsdom 模拟浏览器环境
      environment: 'jsdom',
    },
  }
})
```

---

### 🥉 优先级 4：全局架构核心 (Router + Pinia)
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

### 🏅 优先级 5：网络通信层 (Axios)
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

### 🏅 优先级 6：样式引擎与国际化 (Unocss + I18n)
**说明**：Unocss 提供极速的原子化 CSS 体验，I18n 为后续多语言扩展做好准备。

#### 1. `uno.config.ts` (Unocss 配置)
```typescript
import { defineConfig, presetWind3, presetAttributify, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetWind3(), // 基础预设 (类似 Tailwind)
    presetAttributify(), // 支持属性化写法 (如 <div text="red-500">)
    presetIcons(),
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

### 🏁 优先级 7：应用入口组装 (Main.ts)
**说明**：将上述所有独立配置的模块进行“插拔式”组装，完成 Vue 应用的实例化。

#### `src/main.ts`
```typescript
import { createApp } from 'vue'
import App from '@/App.vue'
import router from '@router'
import pinia from '@stores'
import i18n from '@i18n'

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