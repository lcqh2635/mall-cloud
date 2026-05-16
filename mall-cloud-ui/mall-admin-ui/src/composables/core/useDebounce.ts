import { ref, watch, onUnmounted } from 'vue'
import type { Ref } from 'vue'

/**
 * 防抖Hook
 * @param value - 需要防抖的值(Ref或普通值)
 * @param delay - 防抖延迟时间(ms)，默认300ms
 * @returns 防抖处理后的Ref值
 *
 * @example
 * const searchText = useDebounce(ref(''), 500)
 * watch(searchText, (val) => { console.log(val) })
 */
export function useDebounce<T>(value: Ref<T> | T, delay = 300) {
    const debouncedValue = ref(value) as Ref<T>
    let timeoutId: ReturnType<typeof setTimeout>

    // 处理值变化
    const updateValue = (newValue: T) => {
        clearTimeout(timeoutId)
        timeoutId = setTimeout(() => {
            debouncedValue.value = newValue
        }, delay)
    }

    // 如果传入的是Ref，设置监听
    if (isRef(value)) {
        watch(value, (newVal) => updateValue(newVal))
    } else {
        updateValue(value)
    }

    // 组件卸载时清除定时器
    onUnmounted(() => {
        clearTimeout(timeoutId)
    })

    return debouncedValue
}