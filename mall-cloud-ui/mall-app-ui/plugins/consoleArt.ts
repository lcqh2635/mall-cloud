// Vite 默认内置了 picocolors，用于在终端中打印彩色日志、控制台 ASCII 艺术字输出等
// plugins/consoleArt.ts (Bun 适配版)
import type {Plugin, ResolvedConfig, ViteDevServer} from 'vite'
import c from "picocolors";
// bun add figlet
// bun add -D @types/figlet
import figlet from 'figlet'

/**
 * 自定义控制台艺术字插件，增强 Vite 开发服务器的控制台输出
 *
 * @example
 * // vite.config.ts
 * import consoleArt from '@plugins/consoleArt'
 *
 * export default defineConfig({
 *   plugins: [consoleArt({ style: 'fancy' })]
 * })
 *
 * @param options - 插件配置项
 * @returns Vite 插件实例
 */

// 插件配置项类型定义
interface ConsoleArtOptions {
    /** 是否显示网络地址（默认：true） */
    showNetwork?: boolean
    /** 自定义艺术字样式（默认：内置样式） */
    artText?: string
    /** 是否显示构建信息（默认：开发模式显示） */
    showBuildInfo?: boolean
}

// 自定义 Vite 插件，参数官网： https://cn.vite.dev/guide/api-plugin.html#plugin-ordering
export default function consoleArt(options?: ConsoleArtOptions): Plugin {
    console.log("插件可选配置参数：" + options)
    let viteConfig: ResolvedConfig
    let buildStartTime: number

    //
    const tips = [
        "你知道吗？按住 Alt 点击浏览器刷新可以强制清除缓存",
        "尝试 Ctrl+Shift+R 进行硬刷新",
        "使用 .env.local 文件保存本地敏感配置",
        "Vite 的热更新延迟通常在 50ms 以内",
        "你知道吗？使用 bun upgrade --interactive 可以交互式升级",
    ]

    return {
        // ==================== 核心配置项 ====================
        name: 'vite-plugin-console-art', // 插件唯一标识（必填）
        apply: 'serve',                 // 应用场景：serve|build|both（默认：both）
        enforce: 'post',                // 执行顺序：pre|post（影响插件执行顺序）

        // ==================== 生命周期钩子 ====================
        // 1. 配置解析钩子（修改 Vite 配置）
        // config(config, env) {
        //     return {
        //         // 合并配置（这里示例关闭默认的 clearScreen 行为）
        //         clearScreen: false
        //     }
        // },

        // 获取最终配置，参考 Vite 官方文档 https://cn.vitejs.dev/guide/api-plugin#configresolved
        // 2. 配置解析完成钩子（获取最终配置）
        configResolved(config) {
            viteConfig = config
        },

        // 配置开发服务器
        configureServer(server: ViteDevServer) {
            const info = server.config.logger.info

            console.log("VITE_API_KEY" + process.env.VITE_API_KEY)
            if (process.env.VITE_API_KEY) {
                console.log(c.red('⚠️ 警告：检测到敏感环境变量 VITE_API_KEY 被前端使用！'))
            }

            // Vite 项目启动的默认启动打印函数
            const print = server.printUrls;

            // 只在项目第一次启动时执行
            server.printUrls = async () => {
                // 在默认的打印之前插入自定打印内容

                // 获取项目信息
                // const pkg = JSON.parse(fs.readFileSync('package.json', 'utf-8'))

                // 要使用 Bun 这个全局变量，必须使用 bun run --bun dev 命令运行，使用 --bun 参数来强制指定使用 bun 运行，
                // 否则会使用 node 也就说：bun run dev 启动的也是 node 运行，而不是 bun 运行
                // 使用 Bun.file 读取 package.json 并转为 JSON 对象
                // const pkg = await Bun.file('package.json',  { encoding: "utf-8" }).json();
                const pkg = await Bun.file('package.json').json();

                // 使用 figlet 生成 ASCII 艺术字
                const ascii = figlet.textSync(pkg.name.replace(/-/g, ' '), {
                    // 可选的字体参考官网 https://github.com/xero/figlet-fonts/tree/master
                    // 或者直接去ASCII 艺术字生成工具推荐：https://patorjk.com/software/taag/#p=testall 去在线挑选
                    font: "ANSI Shadow",
                    horizontalLayout: 'default',
                    verticalLayout: 'default',
                    whitespaceBreak: true,
                })
                // 打印 ASCII 艺术字
                // ASCII 艺术字生成工具推荐：https://patorjk.com/software/taag
                console.log(c.green(ascii))
                console.log(`${c.green('➜ 作者:')} ${pkg.author.name}            ${c.green('➜ 邮箱:')} ${pkg.author.email}`)

                // 打印项目基础信息
                const projectInfo = `
${c.bold('🚀 项目已启动!')}
${c.dim('──────────────────────────────')}
${c.green('➜ 项目名称:')} ${pkg.name}     ${c.green('➜ 版本:')} ${pkg.version}
${c.cyan('➜ 运行环境:')} ${c.yellow(viteConfig.mode)}
${c.cyan('➜ Vue版本:')} ${c.blue(`${pkg.dependencies.vue.replace('^', '')}`)}
${c.cyan('➜ TypeScript版本:')} ${c.blue(`${pkg.devDependencies.typescript.replace('~', '')}`)}
${c.cyan('➜ Vite版本:')} ${c.blue(`${pkg.devDependencies.vite.replace('^', '')}`)}
${c.cyan('➜ Bun运行时版本:')} ${c.blue(`${Bun.version}`)}
${c.dim('— 按下')} ${c.cyan('h + enter')} ${c.dim('显示帮助')}
${c.dim('💡 小贴士:')} ${c.italic(c.gray(tips[Math.floor(Math.random() * tips.length)]))}
${c.dim('──────────────────────────────')}
                    `
                const blank = '\n'.repeat(1)     // 一个空行
                info(blank + projectInfo)

                // Vite 项目启动的默认打印
                print();
            }

            // 每次热更新都会触发，但第一次启动不会触发
            server.httpServer?.once('listening', () => {
                console.log(`${c.dim('💡 小贴士:')} ${c.italic(c.gray(tips[Math.floor(Math.random() * tips.length)]))}`)
            })
        },

        // 4. 构建开始钩子
        buildStart() {
            buildStartTime = Date.now()
            if (viteConfig.command === 'build') {
                console.log(c.cyan('\n⚡ 开始构建生产包...'))
            }
        },

        // 5. 代码转换钩子（示例：注入版本信息）
        transform(code, id) {
            if (id.endsWith('.vue') || id.endsWith('.ts')) {
                return code.replace(
                    /__APP_VERSION__/g,
                    JSON.stringify(process.env.npm_package_version)
                )
            }
        },

        // 6. 构建结束钩子
        closeBundle() {
            if (viteConfig.command === 'build') {
                const duration = (Date.now() - buildStartTime) / 1000
                console.log(
                    c.green(`\n✅ 构建完成！耗时 ${c.bold(duration.toFixed(2))}s`) +
                    c.dim(`\n   输出目录：${c.cyan(viteConfig.build.outDir)}`)
                )
            }
        },
    }
}