---
# 站点主页配置 https://vitepress.dev/zh/reference/default-theme-home-page

# 指定页面的布局
layout: home

# 当 layout 设置为 home 时，定义主页 hero 部分的内容。
hero:
  name: "VitePress"
  text: "Vite & Vue Powered Static Site Generator"
  tagline: Markdown to Beautiful Docs in Minutes
  # text 和 tagline 区域旁的图片
  image:
    src: /img.png
    alt: VitePress
  # 主页 hero 部分的操作按钮
  actions:
      # 按钮的颜色主题，默认为 `brand`
    - theme: brand
      # 按钮的标签
      text: What is VitePress?
      # 按钮的目标链接
      link: /en/guide/what-is-vitepress
    - theme: alt
      text: Quickstart
      link: /en/guide/getting-started
    - theme: alt
      text: Github
      link: https://gitee.com/llf2635
# 定义当layout 设置为 home 时要在 features 部分中显示的项目。
features:
    # 在每个 feature 框中显示图标
  - icon: 🛠️
    # feature 的标题
    title: Focus on Your Content
    # feature 的详情
    details: Effortlessly create beautiful documentation sites with just markdown.
    # 按钮的目标链接
    link: https://gitee.com/llf2635
  - icon:
      src: /vite.svg
      width: 30px
      height: 30px
    title: Enjoy the Vite DX
    details: Instant server start, lightning fast hot updates, and leverage Vite ecosystem plugins.
    link: /markdown-examples
  - title: Customize with Vue
    details: Use Vue syntax and components directly in markdown, or build custom themes with Vue.
  - title: Ship Fast Sites
    details: Fast initial load with static HTML, fast post-load navigation with client-side routing.
---