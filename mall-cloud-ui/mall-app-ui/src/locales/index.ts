// i18n 多语言配置
import { createI18n } from 'vue-i18n'
import type { I18nOptions } from 'vue-i18n'

// 自动加载 locales 下的所有 json 文件
const messages = Object.fromEntries(
    Object.entries(
        import.meta.glob<{ default: any }>('../locales/*.json', { eager: true })
    ).map(([key, value]) => {
        const matched = key.match(/([A-Za-z0-9-_]+)\./i)?.[1]
        return [matched, value.default]
    })
)

// i18n 配置
const i18nOptions: I18nOptions = {
    legacy: false, // 使用 Composition API 模式
    locale: 'en', // 默认语言
    fallbackLocale: 'en', // 回退语言
    messages, // 语言包
    globalInjection: true, // 全局注入 $t 函数
    // 日期时间本地化配置
    datetimeFormats: {
        en: {
            short: {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            },
            long: {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                weekday: 'long',
                hour: 'numeric',
                minute: 'numeric'
            }
        },
        zh: {
            short: {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            },
            long: {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                weekday: 'long',
                hour: 'numeric',
                minute: 'numeric',
                hour12: true
            }
        }
    },
    // 数字本地化配置
    numberFormats: {
        en: {
            currency: {
                style: 'currency',
                currency: 'USD'
            }
        },
        zh: {
            currency: {
                style: 'currency',
                currency: 'CNY'
            }
        }
    }
}

export const i18n = createI18n(i18nOptions)

// 语言类型
export type I18nLanguage = keyof typeof messages

// 切换语言
// export function setI18nLanguage(lang: I18nLanguage) {
//     i18n.global.locale.value = lang
//     // 可以在这里添加 localStorage 保存用户语言偏好
//     localStorage.setItem('lang', lang)
//     // 设置 html 的 lang 属性
//     document.querySelector('html')?.setAttribute('lang', lang)
// }
//
// // 初始化语言
// export function initI18n() {
//     // 从 localStorage 或 navigator 中获取用户语言偏好
//     const savedLang = localStorage.getItem('lang')
//     const userLang = navigator.language.split('-')[0]
//     const defaultLang = savedLang || (Object.keys(messages).includes(userLang) ? userLang : 'en')
//     setI18nLanguage(defaultLang as I18nLanguage)
// }