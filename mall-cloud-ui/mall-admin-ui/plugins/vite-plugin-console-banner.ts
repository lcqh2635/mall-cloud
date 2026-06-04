import type {Plugin} from 'vite'
import {readFileSync} from 'fs'
import {resolve} from 'path'
// 引入 boxen
import boxen from 'boxen'
// picocolors 是 Vite 内置的终端着色库
import pc from 'picocolors'

export interface ConsoleBannerOptions {
    extraInfo?: Record<string, string>
}

export default function consoleBannerPlugin(options: ConsoleBannerOptions = {}): Plugin {
    // ==========================================
    // 🌟 第一部分：Node 终端（IDEA 控制台）带框打印
    // ==========================================
    let version = 'unknown'
    try {
        const pkgPath = resolve(process.cwd(), 'package.json')
        const pkgStr = readFileSync(pkgPath, 'utf-8')
        version = JSON.parse(pkgStr).version || 'unknown'
    } catch (error) {
        console.warn('[vite-plugin-console-banner] 无法读取 package.json 的版本号')
    }

    // 1. 组装框内的文本内容（带颜色）
    // 注意：为了保证对齐，中文和全角字符的占位需要留意，这里用空格微调
    const contentLines = [
        `${pc.bold('版本: ')}  ${pc.cyan(version)}`,
        `${pc.bold('环境: ')}  ${pc.cyan(process.env.NODE_ENV || 'development')}`,
        `${pc.bold('时间: ')}  ${pc.cyan(new Date().toLocaleString('zh-CN'))}`,
    ]

    // 追加额外信息
    if (options.extraInfo) {
        Object.keys(options.extraInfo).forEach(key => {
            contentLines.push(`${pc.bold(key + ': ')}  ${pc.cyan(options.extraInfo![key])}`)
        })
    }

    // 将数组拼接成换行分隔的字符串
    const contentText = contentLines.join('\n')

    // 2. 使用 boxen 包裹内容，配置样式
    const terminalBanner = boxen(contentText, {
        padding: {top: 0, bottom: 0, left: 2, right: 2}, // 内边距
        margin: {top: 1, bottom: 1, left: 0, right: 0},  // 外边距（顶部留空，与上方日志隔开）
        borderStyle: 'round',     // 边框样式：'single' (直角), 'round' (圆角), 'double', 'bold' 等
        borderColor: 'green',     // 边框颜色
        title: '🚀 My Awesome Project', // 框的标题（非常酷的功能）
        titleAlignment: 'center', // 标题居中
    })

    // 3. 打印到终端
    console.log(terminalBanner)

    // ==========================================
    // 🌟 第二部分：浏览器控制台打印逻辑（保持不变）
    // ==========================================
    const buildTime = new Date().toLocaleString('zh-CN')

    return {
        name: 'vite-plugin-console-banner',
        enforce: 'pre',

        transform(code, id) {
            if (id.endsWith('src/main.ts') || id.endsWith('src/main.js')) {
                const injectCode = `
;(function() {
  const env = import.meta.env.MODE;
  const version = "${version}";
  const buildTime = "${buildTime}";
  ${options.extraInfo ? `const extraInfo = ${JSON.stringify(options.extraInfo)};` : ''}

  const headerStyle = 'color: #fff; background: linear-gradient(90deg, #42b983, #35495e); padding: 8px 15px; border-radius: 4px; font-size: 16px; font-weight: bold; text-shadow: 1px 1px 2px rgba(0,0,0,0.3);';
  const itemStyle = 'color: #333; font-size: 13px; font-weight: bold;';
  const valueStyle = 'color: #42b983; font-size: 13px; font-weight: normal;';

  console.log('%c 🚀 My Awesome Project ', headerStyle);
  console.log('%c版本: %c' + version, itemStyle, valueStyle);
  console.log('%c构建: %c' + buildTime, itemStyle, valueStyle);
  console.log('%c环境: %c' + env, itemStyle, valueStyle);

  ${options.extraInfo ? `
    Object.keys(extraInfo).forEach(key => {
      console.log('%c' + key + ': %c' + extraInfo[key], itemStyle, valueStyle);
    });
  ` : ''}

  if (env === 'production') {
    console.log('%c🛑 生产环境提示：请勿在此处粘贴执行任何未知代码！', 'color: #ff4d4f; font-size: 14px; font-weight: bold;');
  }
})();
`
                return injectCode + code
            }
        }
    }
}
