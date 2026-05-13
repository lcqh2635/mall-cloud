// src/plugins/directives/index.ts
import type { App } from 'vue'
import permission from './permission'

// 所有指令的映射
const directives = {
    permission,
}

export default {
    install(app: App) {
        Object.keys(directives).forEach(key => {
            app.directive(key, directives[key as keyof typeof directives])
        })
    }
}