import type { Router } from 'vue-router'
import nProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 自定义配置
nProgress.configure({
    minimum: 0.1,          // 初始进度
    easing: 'ease-in-out', // 动画曲线
    speed: 500,            // 动画速度
    trickle: true,         // 是否自动增长
    trickleSpeed: 200,     // 自动增长间隔
    showSpinner: false,    // 隐藏加载动画
    parent: '#app',        // 挂载到 #app 元素下
})

/**
 * 页面加载进度条守卫
 * @param router - 路由实例
 */
export function setupProgressGuard(router: Router) {
    router.beforeEach(() => {
        nProgress.start()
    })

    router.afterEach(() => {
        nProgress.done()
    })

    router.onError(() => {
        nProgress.done()
    })
}