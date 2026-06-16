import {defineConfig, loadEnv} from 'vite'

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
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'

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
// 插件：unplugin-turbo-console
// 作用：增强 console.log 输出，自动为日志附加文件名、行号和变量名上下文
// 效果：在终端或控制台中快速定位日志来源，无需再手动拼接标识符，极大提升调试效率
// 官方文档：https://github.com/unplugin/unplugin-turbo-console
// ====================================================================
import TurboConsole from 'unplugin-turbo-console/vite'

// ====================================================================
// 插件：vite-plugin-mock-dev-server
// 作用：基于文件系统的 Mock 开发服务器，支持热更新及 WebSocket
// 效果：在开发环境拦截 API 请求并返回模拟数据，无需依赖真实后端
// 官方文档：https://vite-plugin-mock-dev-server.netlify.app/zh/
// ====================================================================
import {mockDevServerPlugin} from 'vite-plugin-mock-dev-server'

// ====================================================================
// 插件：vite-plugin-compression2
// 作用：在构建打包时生成压缩文件 (如 .gz)
// 效果：配合服务器静态压缩功能，大幅减小传输体积，提升首屏加载速度
// 官方文档：https://github.com/nonzzz/vite-plugin-compression
// ====================================================================
import {compression} from 'vite-plugin-compression2'

// ====================================================================
// 插件：rollup-plugin-visualizer
// 作用：打包体积分析与可视化
// 效果：生成直观的 HTML 报告，帮助排查依赖体积过大问题，优化打包策略
// 官方文档：https://github.com/btd/rollup-plugin-visualizer
// ====================================================================
import {visualizer} from 'rollup-plugin-visualizer'

// 👉 引入 Bun 内置的 URL 与路径工具 API，用于 ESM 规范的路径解析
import {fileURLToPath} from 'url'
// 👉 封装 ESM 规范的路径解析辅助函数
const r = (path: string) => fileURLToPath(new URL(path, import.meta.url))
// 封装创建代理函数
const createProxy = (env: Record<string, string>) => ({
    '/api': {
        target: env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api/, ''),
    },
})

// Vite 配置官网：https://cn.vite.dev/config/
// Unplugin 统一插件系统官网：https://unplugin.unjs.io/
export default defineConfig(({mode, command}) => {
    // 加载环境变量
    const env = loadEnv(mode, process.cwd(), 'VITE_')
    // 判断应用运行的环境
    const isDev = command === 'serve'
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
                '@router': r('src/router'),
                '@stores': r('src/stores'),
                '@styles': r('src/styles'),
                '@types': r('src/types'),
                '@utils': r('src/utils'),
                '@views': r('src/views'),
            },
            // 在导入文件时可以省略后缀名
            // 默认：'.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json'
            extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json', '.vue'] // 加上 .vue
        },

        // ============================================================
        // 2. 插件配置 (Plugins)
        // ============================================================
        plugins: [
            vue(),
            // UnoCSS 原子化引擎
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
                    ElementPlusResolver({
                        // 使用 CSS 变量版本，支持动态主题切换
                        importStyle: 'css',
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
                    ElementPlusResolver({
                        importStyle: 'css',
                    }),
                    // Icons 组件解析器
                    // 参考：https://unplugin.unjs.io/showcase/unplugin-icons.html#auto-importing
                    // 默认配置后，在模板中直接写 <i-ep-edit /> 即可渲染 Element Plus 的 Edit 图标
                    // 图标将按照以下命名模式自动导入：{prefix}-{collection}-{icon}
                    // prefix：组件名称前缀（默认：i）
                    // collection：Iconify 集合 ID（例如：mdi, carbon, ep）
                    // icon：图标名称（使用下划线命名法）
                    // enabledCollections: 指定允许自动导入的图标集合范围，'ep' 对应 @iconify-json/ep
                    IconsResolver({
                        prefix: 'i',
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

                // autoInstall: true 在 CI/CD 环境的潜在风险
                // 你在本地开发时，随手写了一个 <i-carbon-add />，插件发现你没装 @iconify-json/carbon，就自动帮你 npm install 了，一切正常。
                // 但是，当你把代码推送到 GitLab/GitHub，触发 CI/CD 自动化打包时，可能会导致依赖包缺失，进而流水线失败报错
                autoInstall: false,
                // 2. 编译器：指定图标组件最终编译成哪种形态
                // 'vue3' 会将图标编译为 Vue3 的单文件组件形式，性能最佳
                compiler: 'vue3',
                // 3. 图标默认样式/类名
                // 因为 Iconify 图标默认是 SVG，宽高可能未定，这里可以设置默认样式
                defaultClass: 'inline-block', // 通常设置为 inline-block，方便排版对齐
                // 4. 缩放比例，默认为：1.2 图标大小相对于1em
                // 配合 UnoCSS 的 text-xs 等类名控制图标大小效果最佳
                scale: 1,
            }),
            // 【Vue I18n 编译优化】
            // 作用：预编译 locale 文件，消除运行时警告，大幅提升性能
            VueI18nPlugin({
                // 指定语言包路径，支持 json/yaml/yml 格式
                include: [r('src/locales/modules/**')],
                // 运行时模式（更小的包体积）
                runtimeOnly: true,
                // 允许在 SFC 的 <i18n> 块中使用，如果没用到可设为 false 减少开销
                allowDynamic: true,
                // 需要 @intlify/vue-i18n-extensions 配合，开启 composition API 优化
                fullInstall: false,
            }),

            // ----- 3.5 开发体验插件（仅开发环境生效，生产包自动剔除）-----
            // Vue 开发者工具
            isDev && env.VITE_ENABLE_DEVTOOLS === 'true' && VueDevTools(),
            // 👉 Turbo Console 插件，通常仅在开发环境生效即可，无需打包进生产代码
            isDev && TurboConsole({
                // 👉 可选配置项
                prefix: '🚀',            // 在日志前添加统一的前缀标识
                highlight: true,         // 在终端中使用高亮颜色区分不同来源的日志
            }),
            // ----- 3.6 Mock 服务插件（仅开发环境生效，提供接口模拟）-----
            isDev &&
            env.VITE_USE_MOCK === 'true' &&
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

            // ----- 3.7 构建优化插件（仅生产环境）-----
            isBuild &&
            compression({
                threshold: 10240, // 大于 10KB 才压缩
                deleteOriginalAssets: false, // 不删除原文件，保留备选
            }),
            // 【rollup-plugin-visualizer 生产环境包体积分析】
            // 运行 build 后会在项目根目录生成 stats.html，可视化查看依赖占比
            isBuild &&
            visualizer({
                filename: 'dist/stats.html',
                open: true, // 构建完成后自动在浏览器打开报告
                gzipSize: true, // 显示 gzip 后的大小
                brotliSize: true, // 显示 brotli 后的大小
            }),

        ].filter(Boolean), // 👈 记得加上这个，用来过滤掉生产环境返回的 null 或 false 的插件

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

        // ==========================================
        // 4. 构建配置
        // ==========================================
        build: {
            // 目标浏览器环境（Bun 支持 ESNext）
            target: 'esnext',
            // 构建前清空 dist 目录，避免旧文件残留
            emptyOutDir: true,
            // CSS 代码分割
            cssCodeSplit: true,
            // 生产环境禁用 Source Map
            sourcemap: false,
            // 构建产物目录
            outDir: 'dist',
            // 静态资源目录
            assetsDir: 'assets',
            // 资源内联阈值（小于 4KB 转为 base64）
            assetsInlineLimit: 4096,
            // 大于 1024KB 也就是 1MB 的包发出警告
            chunkSizeWarningLimit: 1024,
            // 压缩配置，速度快
            minify: 'esbuild',
            esbuildOptions: {
                // 移除 console 和 debugger 打印
                drop: ['console', 'debugger'],
            },
            // Rollup 配置
            rollupOptions: {
                output: {
                    // 精细化分包策略，充分利用浏览器缓存
                    manualChunks(id) {
                        if (id.includes('node_modules')) {
                            //  Vue 核心全家桶：匹配 /vue/ 时、不会匹配到以 vue 开头的库，例如：vue-router、vue-echarts 等
                            if (id.includes('/vue/') || id.includes('/pinia/') || id.includes('/vue-router/')) {
                                return 'vendor-vue-core'
                            }
                            // Element Plus
                            if (id.includes('/element-plus/')) {
                                return 'vendor-element-plus'
                            }
                            // vue-i18n
                            if (id.includes('/vue-i18n/')) {
                                return 'vendor-i18n'
                            }
                            // ECharts 及其依赖
                            if (id.includes('/echarts/') || id.includes('/vue-echarts/') || id.includes('/zrender/')) {
                                return 'vendor-echarts'
                            }
                            // 其他第三方库放入 vendor
                            return 'vendor-libs'
                        }
                    },
                    // 入口文件命名
                    entryFileNames: 'js/[name]-[hash].js',
                    // 代码分割文件命名
                    chunkFileNames: 'js/[name]-[hash].js',
                    // 静态资源命名
                    assetFileNames: (assetInfo) => {
                        const info = assetInfo.name || ''
                        if (info.endsWith('.css')) return 'css/[name]-[hash][extname]'
                        if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(info))
                            return 'images/[name]-[hash][extname]'
                        if (/\.(woff2?|eot|ttf|otf)$/.test(info))
                            return 'fonts/[name]-[hash][extname]'
                        return 'assets/[name]-[hash][extname]'
                    },
                },
            },
        },

        // ==========================================
        // 5. 开发服务器配置
        // ==========================================
        server: {
            // 开发服务器端口
            port: 3000,
            // 自动打开浏览器
            open: true,
            // 启用 CORS
            cors: true,
            // 代理配置
            proxy: createProxy(env),
        },

        // ==========================================
        // 6. 预览服务器配置（build 后的预览）
        // ==========================================
        preview: {
            port: 4173,
            open: true,
            proxy: createProxy(env),
        },

        // ==========================================
        // 7. 依赖优化（预构建）
        // ==========================================
        optimizeDeps: {
            // 强制预构建的依赖（避免开发时冷启动慢）
            include: [
                'vue',
                'vue-router',
                'pinia',
                'axios',
                '@vueuse/core',
                'vue-i18n',
                'echarts',
                'vue-echarts',
            ],
        },

    }
})