import {defineStore} from 'pinia'
import {ref, computed} from 'vue'
import type {UserInfo} from "@/types";

/**
 * 用户信息store
 * - 包含用户状态、token等信息
 * - 自动持久化到 localStorage
 * - 在 Store 中直接引入 Service，避免 Vue 组件直接调用API：一切请求调用统一通过 Store 来完成
 */
export const useUserStore = defineStore(
    'user', // store 的唯一 ID
    () => {
        // state
        const token = ref<string>('')
        const userInfo = ref<UserInfo | null>(null)
        const roles = ref<string[]>([])

        // getters
        const isLogin = computed(() => !!token.value)
        const isAdmin = computed(() => roles.value.includes('admin'))

        // actions
        function setToken(value: string) {
            token.value = value
        }

        function setUserInfo(info: UserInfo) {
            userInfo.value = info
            roles.value = info.roles || []
        }

        function clearUser() {
            token.value = ''
            userInfo.value = null
            roles.value = []
        }

        return {
            token,
            userInfo,
            roles,
            isLogin,
            isAdmin,
            setToken,
            setUserInfo,
            clearUser
        }
    },
    {
        // 持久化配置
        persist: {
            key: 'vue3-admin-user', // 存储的key，默认为 store 的 id
            storage: localStorage, // 存储方式，默认 localStorage，还可以是 sessionStorage
            pick: ['token', 'userInfo'], // 只持久化 token 和 userInfo
            // 如果需要自定义序列化/反序列化，以下是默认配置
            serializer: {
                serialize: JSON.stringify,
                deserialize: JSON.parse
            },
        }
    }
)