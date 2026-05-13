// Vue 插件
import type { App } from 'vue'

// const plugins = import.meta.glob('./*.ts')
//
// export default {
//     install: (app: App) => {
//         Object.values(plugins).forEach(async (plugin) => {
//             const module = await plugin()
//             app.use(module.default)
//         })
//     }
// }

// 自动导入指令
// 修改 src/plugins/directives/index.ts
const directives = import.meta.glob('./*.ts', { eager: true })

export default {
    install(app: App) {
        Object.entries(directives).forEach(([path, module]) => {
            if (path === './index.ts') return

            const name = path
                .replace(/^\.\//, '')
                .replace(/\.ts$/, '')
                .replace(/-([a-z])/g, (_, letter) => letter.toUpperCase())

            app.directive(name, (module as any).default)
        })
    }
}