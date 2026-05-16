// // 插件管理者，实际就是一个插件容器。收集所有实现了PluginInterface接口的插件，并提供统一注册功能
// import PluginInterface from "@/plugins/PluginInterface.ts";
// import { App } from "vue";
//
// export default class PluginManager {
//     // 插件容器
//     private plugins: PluginInterface[] = [];
//
//     // 单个注册插件，将具体插件实例添加到插件容器中
//     registerPlugin(plugin: PluginInterface) {
//         // 将传过来的插件添加到插件容器
//         this.plugins.push(plugin);
//     }
//
//     // 批量注册插件
//     registerPlugins() {
//         // 关于全局导入 import.meta.glob 的更多细节可以参考vite官网: https://cn.vitejs.dev/guide/features.html#glob-import-as
//         // 使用 import.meta.glob 进行动态导入，默认为懒加载，添加 eager: true 可以立即一次性加载
//         // 结果是一个类hashmap集合，key是模块文件路径；value是此模块中所有通过 export default 导出的内容
//         const pluginModules = import.meta.glob("@/plugins/plugin/*.ts", {eager: true});
//         console.log(Object.keys(pluginModules).length, Object.keys(pluginModules))
//
//         // 在vue的语法中，一个单文件组件的默认导出值都是当前组件本身。因此，通过 module.default 就能够获取组件对象
//         Object.values(pluginModules).forEach((module: any) => {
//             const Plugin = module.default
//             // console.log("Plugin " + Plugin)
//             const plugin = new Plugin()
//             this.plugins.push(plugin);
//         });
//     }
//
//     // 安装插件，需要传入 app 实例，用它来逐个安装插件
//     installPlugins(app: App<Element>) {
//         this.registerPlugins();
//         // 遍历插件容器，逐个执行插件的 install 方法
//         this.plugins.forEach((plugin: PluginInterface) => {
//             // 逐个执行插件的 install 方法
//             plugin.install(app);
//         });
//     }
// }
