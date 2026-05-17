/*
 * 类型声明文件（.d.ts）
 * 作用：让 TypeScript 识别 .vue 文件模块
 *
 * 注意：
 * 1. 此文件不需要手动导入，TypeScript会自动加载
 * 2. 文件名通常固定为 shims-vue.d.ts
 */

// 声明图片等静态资源模块
declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.gif';
declare module '*.svg';
declare module '*.ico';
declare module '*.webp';

// 声明一个模块，用于匹配所有以 ".vue" 结尾的文件，并将其识别为一个 Vue 组件
declare module '*.vue' {
    import type { ComponentOptions } from 'vue'
    const Component: ComponentOptions
    // 默认导出
    // import Doc from '../doc.md'  // 直接导入默认导出，Doc 就是 component
    export default Component
}

// 新增 Markdown 模块声明（作为 Vue 组件，可以像普通 Vue 组件一样使用 ）
declare module '*.md' {
    import type { ComponentOptions } from 'vue'
    const Component: ComponentOptions
    export default Component
}


// 声明自定义文件格式
declare module '*.yaml' {
    const data: Record<string, any>
    export default data
}
declare module '*.json5' {
    const data: Record<string, any>
    export default data
}