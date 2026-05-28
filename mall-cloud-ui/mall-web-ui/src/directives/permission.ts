// src/plugins/directives/permission.ts
import type { Directive, DirectiveBinding } from 'vue'

// 模拟权限检查函数
const checkPermission = (value: string[]): boolean => {
    // 实际项目中应从store或API获取用户权限
    const userPermissions = ['edit', 'delete'] // 模拟当前用户权限
    return userPermissions.some(permission => value.includes(permission))
}

// 权限指令
const permissionDirective: Directive = {
    mounted(el: HTMLElement, binding: DirectiveBinding<string[]>) {
        const { value } = binding

        if (value && Array.isArray(value)) {
            if (!checkPermission(value)) {
                // 没有权限则移除元素
                el.parentNode?.removeChild(el)
            }
        } else {
            console.error(`需要指定权限数组，如 v-permission="['edit']"`)
        }
    }
}

export default permissionDirective