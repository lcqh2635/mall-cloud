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

// 应用级错误处理 可以用来向追踪服务报告错误
app.config.errorHandler = (error) => {
    /* 处理错误 */
    console.log("the error is: " + error);
};