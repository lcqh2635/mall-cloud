import type { PluginOption } from 'vite'
import * as path from "node:path";
import * as fs from "node:fs";

/**
 * 自定义 Vite 插件：动态生成 robots.txt
 * @param options 配置选项
 * @param options.env 当前环境 (development|production|staging...)
 * @param options.siteUrl 网站基础URL
 * @param options.allowAll 是否允许所有爬虫访问 (开发环境通常设为false)
 * @param options.disallowPaths 需要禁止爬取的路径数组
 * @param options.sitemapPath 网站地图路径
 * @returns Vite 插件配置
 */
export default function robotsPlugin(options: {
    env: string
    siteUrl?: string
    allowAll?: boolean
    disallowPaths?: string[]
    sitemapPath?: string
}): PluginOption {
    // 默认配置
    const defaultOptions = {
        env: 'development',
        allowAll: false,
        disallowPaths: ['/admin', '/api', '/private'],
        sitemapPath: '/sitemap.xml'
    }

    // 合并用户配置和默认配置
    const mergedOptions = { ...defaultOptions, ...options }

    return {
        name: 'vite-plugin-robots',
        // 在构建完成后执行
        closeBundle() {
            // 开发环境不生成或生成限制性robots.txt
            if (mergedOptions.env === 'development') {
                generateRobots({
                    allow: false,
                    siteUrl: mergedOptions.siteUrl,
                    disallowPaths: ['/'], // 开发环境禁止所有爬取
                    sitemapPath: mergedOptions.sitemapPath
                })
            } else {
                // 生产环境根据配置生成
                generateRobots({
                    allow: mergedOptions.allowAll,
                    siteUrl: mergedOptions.siteUrl,
                    disallowPaths: mergedOptions.disallowPaths,
                    sitemapPath: mergedOptions.sitemapPath
                })
            }
        }
    }
}

/**
 * 实际生成robots.txt文件的函数
 * @param params 生成参数
 */
function generateRobots(params: {
    allow: boolean
    siteUrl?: string
    disallowPaths: string[]
    sitemapPath?: string
}) {
    // 构建输出目录 (通常是dist)
    const outputDir = path.resolve(process.cwd(), 'dist')

    // 确保输出目录存在
    if (!fs.existsSync(outputDir)) {
        fs.mkdirSync(outputDir, { recursive: true })
    }

    // 生成robots.txt内容
    let content = '# 自动生成的robots.txt\n'
    content += `User-agent: *\n`

    if (params.allow) {
        content += 'Allow: /\n'
    } else {
        // 添加所有禁止路径
        params.disallowPaths.forEach(path => {
            content += `Disallow: ${path}\n`
        })
    }

    // 如果有网站URL和sitemap路径，添加sitemap
    if (params.siteUrl && params.sitemapPath) {
        const sitemapUrl = new URL(params.sitemapPath, params.siteUrl).href
        content += `Sitemap: ${sitemapUrl}\n`
    }

    // 写入文件
    fs.writeFileSync(path.join(outputDir, 'robots.txt'), content)

    console.log('robots.txt 已生成')
}