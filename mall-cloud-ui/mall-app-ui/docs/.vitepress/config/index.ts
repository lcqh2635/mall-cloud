// .vitepress/config/index.ts
import { defineConfig } from 'vitepress'
import { shared } from './shared'
import { enUS } from './en-US'
import { zhCN } from './zh-CN'

// 参考博客 https://juejin.cn/post/7454266644480475187
export default defineConfig({
    ...shared,

    // 多语言国际化，参考官方 https://vitepress.dev/zh/guide/i18n
    // VitePress 默认不会将 / 重定向到 /zh/。需要配置服务器来实现这一点。
    // 参考  https://vitepress.dev/zh/guide/i18n#separate-directory-for-each-locale
    locales: {
        root: { label: '简体中文', ...zhCN },   // 默认 / -- 显示在导航栏翻译菜单上，可以是外部的
        en: { label: 'English', ...enUS },  // 默认 /en/ -- 显示在导航栏翻译菜单上，可以是外部的

        // 其余 locale 特定属性...
    }
})
