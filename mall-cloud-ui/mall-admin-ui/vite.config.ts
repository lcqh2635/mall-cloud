import {defineConfig, loadEnv} from 'vite';
import {defaultExclude} from 'vitest/config';
import {dirname, resolve} from 'node:path';
import vue from '@vitejs/plugin-vue';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import Icons from 'unplugin-icons/vite';
import UnoCSS from 'unocss/vite';
import vueI18n from '@intlify/unplugin-vue-i18n/vite'
import postcssPresetEnv from 'postcss-preset-env';
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers';
import {FileSystemIconLoader} from 'unplugin-icons/loaders';
import {presetIcons} from 'unocss';
import {fileURLToPath} from 'url';
import consoleArt from "./plugins/consoleArt";

const host = process.env.TAURI_DEV_HOST;

// 更多 vite 配置详细细节可以参考官网: https://cn.vitejs.dev/config/
// 在 Vite 的 API 中，开发环境下 command 的值为 serve（在 CLI 中， vite dev 和 vite serve 是 vite 的别名），而在生产环境下为 build（vite build）。
// mode 参数的值为 development（开发环境）或 production（生产环境）。具体的传递值通过 --mode 选项指定，例如 vite --mode dev 那么 mode 的值为 dev。这意味着读取 .env.development 文件中的环境变量。
/** @type {import('vite').UserConfig} */
export default defineConfig(async ({command, mode}) => {
    // 执行一些异步操作
    // const data = await asyncFunction()

    console.log("执行的命令为: ", command);
    // 要使用 Bun 这个全局变量，必须使用 bun run --bun dev 命令运行，使用 --bun 参数来强制指定使用 bun 运行，
    // 否则会使用 node 也就说：bun run dev 启动的也是 node 运行，而不是 bun 运行
    console.log("Bun 版本：", Bun.version)
    // 根据当前工作目录中的 `mode` 加载 .env 文件
    // 设置第三个参数为 '' 来加载所有环境变量，而不管是否有 VITE_ 前缀。
    // 如果是两个参数则读取我们配置在 .env.mode的环境变量；加第三个参数 ”“ 则当前主机的全量环境变量配置
    const env = loadEnv(mode, process.cwd());
    const {VITE_VERSION, VITE_PORT, VITE_BASE_URL, VITE_API_URL} = env;
    console.log(`🚀 API_URL = ${VITE_API_URL}`);
    console.log(`🚀 BASE_URL = ${VITE_BASE_URL}`);
    console.log(`🚀 PORT = ${VITE_PORT}`, VITE_PORT);
    console.log(`🚀 VERSION = ${VITE_VERSION}`);

    console.log("项目根目录： ", process.cwd());
    console.log("读取.env中配置的环境变量", env);
    // 读取运行模式，如果是dev运行 "dev": "vite --mode development", 则mode为development
    console.log("读取mode变量", mode);
    console.log("读取当前环境变量： ", env.VITE_APP_BASE_URL);
    // process.cwd() 可以获取当前工作目录，例如 D:\Git\Git-Repository\vue3-template-web

    // vite 配置
    return {
        // 插件配置
        plugins: [
            vue(),
            // 自定义 Vite 插件，艺术输出控制台信息
            consoleArt(),
            // 自动导入插件，自动引入全局组件、插件、指令等
            // 自动导入 Vue 相关 API（如 ref, computed）
            AutoImport({
                // 全局导入注册，配置的插件中的函数、对象能够自动引入，不用显示声明
                imports: ["vue", "vue-router", "vue-i18n", "pinia"],
                // 生成相应的.d.ts文件的文件路径。
                // 当“typescript”本地安装时，默认为“./auto-imports.d.ts”。
                dts: "src/types/auto-import.d.ts",
                // 自定义 resolvers, 需要配合 unplugin-vue-components 插件一起使用
                resolvers: [
                    ElementPlusResolver()
                ],
            }),
            // 全局组件注册插件，自动扫描并注册全局组件
            Components({
                // 如果使用了本插件，则会自动扫描并注册全局组件，就不需要自己在 main.ts 中手动逐个注册，或者额外写一个插件来扫描并注册全局组件。
                // 用于搜索组件的目录的相对路径。自定义封装的组件目录，dirs 为本项目的组件目录，而 resolver 用于引入第三方组件
                dirs: ["src/components"],
                // 生成`components.d.ts`全局声明，还接受自定义文件名的路径
                dts: "src/types/components.d.ts",
                // 自定义组件的解析器，为特定的插件提供按需导入的功能
                resolvers: [
                    ElementPlusResolver({
                        importStyle: "sass",
                    }),
                ],
            }),
            // 配置参考 https://github.com/unplugin/unplugin-icons/blob/main/examples/vite-vue3/vite.config.ts
            // 按需通用地访问数千个图标作为组件。文档 https://github.com/unplugin/unplugin-icons
            // 为了提高工作效率，还可以启用 autoInstall 自动安装选项，让 unplugin-icons 来处理安装工作。这样就不用手动 bun add -D @iconify/json
            // 导入图标时，它会安装图标集。它会自动检测正确的软件包管理器（npm、yarn 或 pnpm）。
            Icons({
                /* options */
                autoInstall: true,
                compiler: "vue3",
                // 从 0.11 版开始，您现在可以载入自己的图标！
                // 从 v0.13 版开始，您还可以为 FileSystemIconLoader 提供转换回调。
                customCollections: {
                    // 用于从文件系统加载图标的帮助程序
                    // './assets/icons' 下扩展名为 '.svg' 的文件将按其文件名加载
                    // 您还可以提供 transform 回调来更改每个图标（可选）
                    "my-yet-other-icons": FileSystemIconLoader(
                        "./src/assets/icons",
                        (svg) => svg.replace(/^<svg /, '<svg fill="currentColor" '),
                    ),
                },
            }),
            // 官方文档 https://unocss.net/guide/
            // 配置 UnoCSS 参考 https://github.com/element-plus/element-plus-vite-starter/blob/main/vite.config.ts
            // UnoCSS 是一个即时的原子化 CSS 引擎，旨在灵活和可扩展。核心是不拘一格的，所有的 CSS 工具类都是通过预设提供的。
            // 为什么使用 UnoCSS 而不是 TailwindCSS ？ 参考 https://juejin.cn/post/7244818201976078394
            //  UnoCSS 提供了更多可选方案，并且兼容多种风格的原子类框架，除了 tailwindcss ，同样支持 Bootstrap， Tachyons 等
            UnoCSS({
                // UnoCSS 的预设，包含原子类、图标、媒体查询、dark 模式、字体规则等
                presets: [
                    // UnoCSS 的图标预设，包含图标、字体图标、图片等图标
                    // 配置参考 https://unocss.net/presets/icons#options
                    presetIcons({
                        // 是否在检测到使用时自动安装图标源包，默认为 false
                        autoInstall: true,
                    }),
                ],
            }),
            // 配置参考 https://vue-i18n.intlify.dev/guide/advanced/optimization#how-to-configure
            vueI18n({
                // 启用运行时仅支持组合API
                runtimeOnly: true,
                // 国际化文件目录
                include: resolve(
                    dirname(fileURLToPath(import.meta.url)),
                    './src/locales/**'
                ),
                // 默认语言环境
                defaultSFCLang: 'json',
                // 是否允许组件中使用 legacy 语法
                compositionOnly: false,
                // 是否全局注入 $t 函数
                globalSFCScope: true,
            }),
        ],

        // 开发服务器配置，绝对不能配置 https 选项，否则导致整个文件报错
        // 2. Tauri 需要一个固定端口，如果该端口不可用，则失败
        server: {
            // 指定开发服务器端口。注意：如果端口已经被使用，Vite 会自动尝试下一个可用的端口
            port: 1420,
            // 固定端口，设为 true 时若端口已占用则会直接退出，而不是尝试下一个可用端口。
            strictPort: true,
            // 指定服务器应该监听哪个 IP 地址。 如果将此设置为 0.0.0.0 或者 true 将监听所有地址，包括局域网和公网地址。
            host: host || false,
            // 为开发服务器配置 CORS,默认启用并允许任何源
            cors: true,
            // 开发服务器启动时，自动在浏览器中打开应用程序
            open: true,
            // 热更新
            hmr: host
                ? {
                    protocol: "ws",
                    host,
                    port: 1420,
                }
                : undefined,
            // 忽略文件监听
            watch: {
                // 3. 告诉 Vite 忽略观看 “src-tauri” 文件夹
                ignored: ["./src-tauri/**"],
            },
            // 为开发服务器配置自定义代理规则，代理所有从vite发出的url中带/api的请求
            // 代理配置参考 https://cn.vitejs.dev/config/server-options.html#server-proxy
            proxy: {
                "/api": {
                    // 匹配上则转发到target 目标Host
                    target: env.VITE_APP_BASE_URL,
                    // 是否跨域
                    changeOrigin: true,
                    // 路径重写，剔除/api，然后将剩余的path拼接到target后，组成最终发出去请求
                    // path 参数代表的是端口后的路径，例如http://localhost:6666/api/userInfo ，则path代表/api/userInfo
                    rewrite: (path: string) => path.replace(/^\/api/, ""),
                },
                "/api/gen": {
                    //单体架构下特殊处理代码生成模块代理
                    target:
                        env.VITE_IS_MICRO === "true"
                            ? env.VITE_ADMIN_PROXY_PATH
                            : env.VITE_GEN_PROXY_PATH,
                    changeOrigin: true,
                    rewrite: (path: string) => path.replace(/^\/api/, ""),
                },
                // 正则表达式写法：http://localhost:5173/fallback/ -> http://jsonplaceholder.typicode.com/
                "^/fallback/.*": {
                    target: "https://jsonplaceholder.typicode.com",
                    changeOrigin: true,
                    rewrite: (path: string) => path.replace(/^\/fallback/, ""),
                },
            },
        },

        // 配置 CSS 相关的选项，包括内联的 PostCSS 配置、CSS 预处理器配置等
        css: {
            // 配置 PostCSS 选项，采用内嵌配置的方式，无需创建 postcss.config.js 两者效果相同
            postcss: {
                plugins: [
                    // 配置 PostCSS 插件，包括 autoprefixer 和 cssnano。
                    // autoprefixer 用于自动添加浏览器前缀，cssnano 用于压缩 CSS。
                    // autoprefixer 插件的配置选项为 browserslist 配置，默认为 last 2 versions。
                    // cssnano 插件的配置选项为 preset 配置，默认为 default。
                    // postcss-preset-env 插件内包含 autoprefixer ，因此无需再额外添加该依赖，参考 https://www.npmjs.com/package/postcss-preset-env
                    postcssPresetEnv({
                        /* 使用 Stage 3 特性 + CSS 嵌套规则 */
                        stage: 3,
                        features: {
                            "nesting-rules": true,
                        },
                        // 自动添加浏览器前缀
                        autoprefixer: {
                            grid: true,
                        },
                        // 浏览器支持
                        browsers: [
                            'last 2 versions',
                            'Firefox ESR',
                            '> 1%',
                            'ie >= 8',
                            'iOS >= 8',
                            'Android >= 4',
                        ],
                    }),
                    // 参考 https://tailwind.nodejs.cn/docs/optimizing-for-production
                    // ...(process.env.NODE_ENV === 'production' ? { cssnano: {} } : {})
                ],
            },
            // 指定传递给 CSS 预处理器的选项，参考 https://cn.vitejs.dev/config/shared-options.html#css-preprocessoroptions
            preprocessorOptions: {
                scss: {
                    // 全局注入变量和 Mixin，全局注入 SCSS 代码到所有样式文件的头部（相当于自动在每个文件开头添加代码）。避免在每个文件手动写 @use
                    // @use 和 @import 都可以用来引入其他样式文件，但 @use 是现代 Sass 模块系统的推荐方式，而 @import 已被官方标记为逐步淘汰
                    // as * 的作用：将 variables.scss 和 mixins.scss 的所有成员全局暴露，使得项目中任何 SCSS 文件无需再次 @use 即可直接使用这些变量和 Mixin
                    additionalData: `
                        @use "@/styles/variables.scss" as *;
                        @use "@/styles/mixins.scss" as *;
                        $injected-color: ${process.env.NODE_ENV === 'production' ? 'green' : 'red'};
                    `,
                },
            },
        },

        // 配置路径别名，方便在项目中使用 import.meta.env.VITE_APP_BASE_URL 获取配置的变量
        // https://cn.vitejs.dev/config/shared-options.html#resolve-alias
        // 对应的也要在 tsconfig.json 中配置 alias 以获得智能类型提示
        resolve: {
            alias: {
                "@": resolve(process.cwd(), "src"),
                "@views": resolve(process.cwd(), "src/views"),
                "@assets": resolve(process.cwd(), "src/assets"),
                "@components": resolve(process.cwd(), "src/components"),
                "@utils": resolve(process.cwd(), "src/utils"),
                "@hooks": resolve(process.cwd(), "src/hooks"),
                "@plugins": resolve(process.cwd(), "src/plugins"),
            },
        },

        // 定义全局常量替换方式。其中每项在开发环境下会被定义在全局，而在构建时被静态替换。
        // 对于使用 TypeScript 的开发者来说，请确保在 env.d.ts 或 vite-env.d.ts 文件中添加类型声明，以获得类型检查以及代码提示。
        // declare const __APP_VERSION__: string
        // declare const __APP_ENV__: string
        define: {
            __APP_ENV__: JSON.stringify(env.APP_ENV),
            __APP_VERSION__: JSON.stringify(env.APP_VERSION),
        },

        // ========== 测试配置 ==========
        // vitest 单元测试配置，参考官方文档 https://cn.vitest.dev/
        // vitest 一个原生支持 Vite 的测试框架。非常快速 并且非常容易上手。
        // 官方文档 https://cn.vitest.dev/
        // 如果使用 Bun 作为软件包管理器，请确保使用 bun run test 命令而不是 bun test 命令，否则 Bun 将运行自己的测试运行程序。
        test: {
            // ...
            testTimeout: 30_000,
            name: "unit",
            exclude: [...defaultExclude, "**/svelte-scoped/**", "**/test-dom/**"],
        },

        // 设为 false 可以避免 Vite 清屏而错过在终端中打印某些关键信息
        // 1. 防止 Vite 掩盖 Rust 错误
        clearScreen: false,
        // 以 envPrefix 内元素开头的环境变量会通过 import.meta.env 暴露在你的客户端源码中。默认只对外暴露以 VITE_ 开头的环境变量。
        envPrefix: ["VITE_", "BUN_", "MODE_"],
    }
})