import {createApp} from "vue";
import App from "@/App.vue";
import pinia from "@/stores";
import router from "@/routers";

// import '@/styles/main.scss'
// UnoCSS 样式
import 'virtual:uno.css'
// import '@unocss/reset/tailwind.css'
import {createI18n} from "vue-i18n";


const i18n = createI18n({
    // something vue-i18n options here ...
})

// 创建vue实例
const app = createApp(App);

// 注册i18n
app.use(i18n);
// 注册pinia
app.use(pinia)
// 注册路由
app.use(router)

// 等待路由准备就绪后再挂载应用
router.isReady().then(() => {
    // 路由初始化完成后挂载Vue根实例
    app.mount('#app')
})


/**
 * 全局错误处理器
 * @param err 错误对象
 * @param instance 触发错误的组件实例
 * @param info 错误来源信息（如生命周期钩子名称 'mounted hook'、事件 'v-on handler' 等）
 */
app.config.errorHandler = (err, instance, info) => {
    // 1. 生产环境：将错误上报到日志服务器
    if (import.meta.env.PROD) {
        // trackErrorToServer({ err, info, componentName: instance?.$options?.name })
        console.log('🚨 线上错误已上报')
    }

    // 2. 开发环境：在控制台打印更友好的错误提示
    if (import.meta.env.DEV) {
        console.error(`[Vue ErrorHandler] 组件: <${instance?.$options?.name || 'Anonymous'}>, 来源: ${info}`)
        console.error(err)
    }

    // 3. 可选：向用户展示全局的错误提示弹窗（配合 ElMessage / Toast 等）
    // showGlobalErrorToast('系统开小差了，请稍后再试')
}

/**
 * 全局警告处理器（仅开发环境生效，生产环境自动忽略）
 * @param msg 警告信息
 * @param instance 触发警告的组件实例
 * @param trace 组件层级追踪信息
 */
app.config.warnHandler = (msg, instance, trace) => {
    // ==========================================
    // 1. 按组件精准过滤
    // ==========================================
    // 获取组件名称（优先使用 name 选项，否则使用文件名等）
    const componentName = instance?.$options?.name || instance?.$options?.__name

    // 示例：过滤掉第三方组件 ElTable 和 ElForm 触发的特定警告
    // 这样如果是你自己的组件触发了 'Extraneous non-emits'，依然会在控制台提醒你
    if (msg.includes('Extraneous non-emits event listeners')) {
        if (componentName && componentName.startsWith('El')) {
            return // 只有 Element Plus 组件触发的才忽略
        }
    }

    // ==========================================
    // 2. 测试环境：直接屏蔽所有剩余警告
    // ==========================================
    // 如果是单元测试环境，直接过滤掉所有警告（保持测试控制台干净）
    // if (import.meta.env.MODE === 'test') return

    // ==========================================
    // 3. 本地开发环境：增强警告信息，让排查更高效
    // ==========================================
    // 提取组件路径信息（Vite 在开发环境下会给组件注入 __file 属性）
    const file = instance?.$options?.__file
    const fileShort = file ? `(${file.replace(/^(.*[\\\/])/, '')})` : ''

    // 自定义格式化输出
    console.warn(
        `\n%c[Vue Warn]%c ${msg}%c\n` +
        `%c组件: <${componentName || 'Anonymous'}> ${fileShort}\n` +
        `%c追踪:\n${trace}`,
        // 样式配置
        'color: red; font-weight: bold;', // [Vue Warn] 样式
        'color: inherit;',                // 消息内容样式
        '',                               // 间隔
        'color: #42b983; font-weight: bold;', // 组件名样式
        'color: #999;'                    // 追踪信息样式
    )
}