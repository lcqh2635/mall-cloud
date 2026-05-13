// // element-plus的图标注册插件
//
// // 引入element-plus的图标库
// import * as ElementPlusIconsVue from "@element-plus/icons-vue";
// import PluginInterface from "@/plugins/PluginInterface.ts";
// import { App } from "vue";
//
// // 插件 (Plugins) 是一种能为 Vue 添加全局功能的工具代码。参考 vue3 官方文档 https://cn.vuejs.org/guide/reusability/plugins.html
// /*
// 插件没有严格定义的使用范围，但是插件发挥作用的常见场景主要包括以下几种：
//     通过 app.component() 和 app.directive() 注册一到多个全局组件或自定义指令。
//     通过 app.provide() 使一个资源可被注入进整个应用。
//     向 app.config.globalProperties 中添加一些全局实例属性或方法
// */
// export default class RegisterElementPlusIcon implements PluginInterface {
//     install(app: App<Element>) {
//         // 此处参考官网，意为将图标库中的每个图标都注册成组件
//         for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
//             app.component(key, component);
//         }
//     }
// }
