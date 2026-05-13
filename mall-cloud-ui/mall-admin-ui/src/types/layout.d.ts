// 定义所有可用的布局类型
export type LayoutType = 'default' | 'admin' | 'auth' | 'empty'

// 布局组件映射类型
export interface LayoutComponent {
    default: DefineComponent<{}, {}, any>
}