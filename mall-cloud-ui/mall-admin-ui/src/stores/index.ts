import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 创建 pinia 实例
const pinia = createPinia()

// 使用持久化插件
pinia.use(piniaPluginPersistedstate)

// 导出 pinia 实例，用于在 main.ts 中注册
export default pinia


// 统一导出模块，避免在组件中逐个导入
export * from './modules/user.store.ts'
export * from './modules/app.store.ts'
// ...其他模块