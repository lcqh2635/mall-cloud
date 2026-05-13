---
# 站点主页配置 https://vitepress.dev/zh/reference/default-theme-home-page

# 指定页面的布局
layout: home

# 当 layout 设置为 home 时，定义主页 hero 部分的内容。
hero:
  name: "VitePress 中文"
  text: "由 Vite 和 Vue 驱动的静态站点生成器"
  tagline: 将 Markdown 变成优雅的文档，只需几分钟
  # text 和 tagline 区域旁的图片
  image:
    src: /img.png
    alt: VitePress
  # 主页 hero 部分的操作按钮
  actions:
      # 按钮的颜色主题，默认为 `brand`
    - theme: brand
      # 按钮的标签
      text: 什么是 VitePress?
      # 按钮的目标链接
      link: /guide/what-is-vitepress
    - theme: alt
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: Github
      link: https://gitee.com/llf2635
# 定义当layout 设置为 home 时要在 features 部分中显示的项目。
features:
    # 在每个 feature 框中显示图标
  - icon: 🛠️
    # feature 的标题
    title: 专注内容
    # feature 的详情
    details: 只需 Markdown 即可轻松创建美观的文档站点。
    # 按钮的目标链接
    link: https://gitee.com/llf2635
  - icon:
      src: /vite.svg
      width: 30px
      height: 30px
    title: 享受 Vite 无可比拟的体验
    details: 服务器即时启动，闪电般的热更新，还可以使用基于 Vite 生态的插件。
    link: /markdown-examples
  - title: 使用 Vue 自定义
    details: 直接在 Markdown 中使用 Vue 语法和组件，或者使用 Vue 组件构建自定义主题。
  - title: 速度真的很快！
    details: 采用静态 HTML 实现快速的页面初次加载，使用客户端路由实现快速的页面切换导航。
---