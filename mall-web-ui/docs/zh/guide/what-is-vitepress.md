# VitePress 是什么？

VitePress 是一个静态站点生成器 (SSG)，专为构建快速、以内容为中心的站点而设计。简而言之，VitePress 获取用 Markdown 编写的内容，对其应用主题，并生成可以轻松部署到任何地方的静态 HTML 页面。

## 使用场景
文档

VitePress 附带一个专为技术文档设计的默认主题。你现在正在阅读的这个页面以及 Vite、Rollup、Pinia、VueUse、Vitest、D3、UnoCSS、Iconify 等文档都是基于这个主题的。

Vue.js 官方文档也是基于 VitePress 的。但是为了可以在不同的翻译文档之间切换，它自定义了自己的主题。

博客、档案和营销网站

VitePress 支持完全的自定义主题，具有标准 Vite + Vue 应用程序的开发体验。基于 Vite 构建还意味着可以直接利用其生态系统中丰富的 Vite 插件。此外，VitePress 提供了灵活的 API 来加载数据 (本地或远程)，也可以动态生成路由。只要可以在构建时确定数据，就可以使用它来构建几乎任何东西。

Vue.js 官方博客是一个简单的博客页面，它根据本地内容生成其索引页面。


### Title <Badge type="info" text="default" />
### Title <Badge type="tip" text="^1.9.0" />
### Title <Badge type="warning" text="beta" />
### Title <Badge type="danger" text="caution" />

VitePress provides Syntax Highlighting powered by [Shiki](https://github.com/shikijs/shiki), with additional features like line-highlighting:

**Input**

````md
```js{4}
export default {
  data () {
    return {
      msg: 'Highlighted!'
    }
  }
}
```
````

**Output**

```js{4}
export default {
  data () {
    return {
      msg: 'Highlighted!'
    }
  }
}
```

## Custom Containers

**Input**

```md
::: info
This is an info box.
:::

::: tip
This is a tip.
:::

::: warning
This is a warning.
:::

::: danger
This is a dangerous warning.
:::

::: details
This is a details block.
:::
```

**Output**

::: info
This is an info box.
:::

::: tip
This is a tip.
:::

::: warning
This is a warning.
:::

::: danger
This is a dangerous warning.
:::

::: details
This is a details block.
:::

## More

Check out the documentation for the [full list of markdown extensions](https://vitepress.dev/guide/markdown).
