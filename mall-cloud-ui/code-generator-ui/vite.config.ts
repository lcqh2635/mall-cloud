import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import consoleBannerPlugin from "./plugins/vite-plugin-console-banner.ts";

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        consoleBannerPlugin({
            extraInfo: {
                '作者': '前端小智',
                '文档': 'https://vuejs.org/'
            }
        })
    ],
})
