// https://vitepress.dev/zh/guide/custom-theme
import {h} from 'vue'
import type {Theme} from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import './style.css'

export default {
    extends: DefaultTheme,
    Layout: () => {
        return h(DefaultTheme.Layout, null, {
            // https://vitepress.dev/zh/guide/extending-default-theme
        })
    },
    enhanceApp({app, router, siteData}) {
        // ...
    }
} satisfies Theme
