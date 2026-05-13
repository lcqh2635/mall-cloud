# Vue3 + TS + Vite 国际化配置指南 (vue-i18n + @intlify/unplugin-vue-i18n)

下面是一个完整的国际化配置方案，包含安装、配置和使用示例。

## 1. 安装依赖

```bash
npm install vue-i18n @intlify/unplugin-vue-i18n
# 或者
yarn add vue-i18n @intlify/unplugin-vue-i18n
```

## 2. 项目结构

建议的国际化文件结构：

```
src/
  ├── locales/
  │   ├── en/          # 英文语言包
  │   │   ├── common.json
  │   │   └── pages/
  │   │       ├── home.json
  │   │       └── about.json
  │   └── zh/          # 中文语言包
  │       ├── common.json
  │       └── pages/
  │           ├── home.json
  │           └── about.json
  └── i18n.ts         # i18n 配置文件
```

## 3. 配置 Vite

`vite.config.ts` 配置：

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueI18n from '@intlify/unplugin-vue-i18n/vite'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    vueI18n({
      // 启用运行时仅支持组合API
      runtimeOnly: true,
      // 国际化文件目录
      include: resolve(
        dirname(fileURLToPath(import.meta.url)),
        './src/locales/**'
      ),
      // 默认语言环境
      defaultSFCLang: 'json',
      // 是否允许组件中使用 legacy 语法
      compositionOnly: false,
      // 是否全局注入 $t 函数
      globalSFCScope: true,
    }),
  ],
})
```

## 4. 创建 i18n 实例

`src/i18n.ts`:

```typescript
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
export function setI18nLanguage(lang: I18nLanguage) {
  i18n.global.locale.value = lang
  // 可以在这里添加 localStorage 保存用户语言偏好
  localStorage.setItem('lang', lang)
  // 设置 html 的 lang 属性
  document.querySelector('html')?.setAttribute('lang', lang)
}

// 初始化语言
export function initI18n() {
  // 从 localStorage 或 navigator 中获取用户语言偏好
  const savedLang = localStorage.getItem('lang')
  const userLang = navigator.language.split('-')[0]
  const defaultLang = savedLang || (Object.keys(messages).includes(userLang) ? userLang : 'en')
  setI18nLanguage(defaultLang as I18nLanguage)
}
```

## 5. 在 main.ts 中引入

```typescript
import { createApp } from 'vue'
import App from './App.vue'
import { i18n, initI18n } from './i18n'

const app = createApp(App)

app.use(i18n)
initI18n() // 初始化语言

app.mount('#app')
```

## 6. 语言包示例

`src/locales/en/common.json`:

```json
{
  "hello": "Hello",
  "welcome": "Welcome to our application",
  "buttons": {
    "submit": "Submit",
    "cancel": "Cancel"
  }
}
```

`src/locales/zh/common.json`:

```json
{
  "hello": "你好",
  "welcome": "欢迎使用我们的应用",
  "buttons": {
    "submit": "提交",
    "cancel": "取消"
  }
}
```

## 7. 在组件中使用

### 选项式 API 用法

```vue
<template>
  <div>
    <h1>{{ $t('hello') }}</h1>
    <p>{{ $t('welcome') }}</p>
    <button>{{ $t('buttons.submit') }}</button>
    
    <!-- 带参数 -->
    <p>{{ $t('message.greeting', { name: 'John' }) }}</p>
    
    <!-- 复数形式 -->
    <p>{{ $tc('message.apple', 5) }}</p>
    
    <!-- 日期格式化 -->
    <p>{{ $d(new Date(), 'long') }}</p>
    
    <!-- 数字格式化 -->
    <p>{{ $n(1000, 'currency') }}</p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'MyComponent',
  methods: {
    showMessage() {
      // 在 JS 中使用
      console.log(this.$t('hello'))
    }
  }
})
</script>
```

### 组合式 API 用法

```vue
<template>
  <div>
    <h1>{{ t('hello') }}</h1>
    <p>{{ t('welcome') }}</p>
    <button @click="changeLanguage('en')">English</button>
    <button @click="changeLanguage('zh')">中文</button>
    
    <!-- 格式化日期 -->
    <p>{{ d(new Date(), 'long') }}</p>
    
    <!-- 格式化数字 -->
    <p>{{ n(1000, 'currency') }}</p>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { setI18nLanguage } from '../i18n'

const { t, d, n, locale } = useI18n()

// 切换语言
const changeLanguage = (lang: string) => {
  setI18nLanguage(lang)
}

// 在 script 中使用
console.log(t('hello'))
</script>
```

## 8. 在 TypeScript 中增强类型支持

为了获得更好的类型提示，可以创建类型定义文件：

`src/types/i18n.d.ts`:

```typescript
import 'vue-i18n'

// 定义语言包类型
declare module 'vue-i18n' {
  // 定义消息类型
  export interface DefineLocaleMessage {
    hello: string
    welcome: string
    buttons: {
      submit: string
      cancel: string
    }
    // 可以在这里添加更多的键和嵌套结构
  }
  
  // 定义日期时间格式类型
  export interface DefineDateTimeFormat {
    short: any
    long: any
  }
  
  // 定义数字格式类型
  export interface DefineNumberFormat {
    currency: any
  }
}
```

## 9. 动态导入语言包 (高级用法)

对于大型项目，可以按需加载语言包：

```typescript
// 动态加载语言包
export async function loadLocaleMessages(locale: I18nLanguage) {
  try {
    const messages = await import(
      /* @vite-ignore */
      `../locales/${locale}.json`
    )
    i18n.global.setLocaleMessage(locale, messages.default)
    return nextTick()
  } catch (error) {
    console.error('Failed to load locale messages', error)
    return Promise.reject(error)
  }
}

// 使用示例
async function changeLanguage(locale: I18nLanguage) {
  if (!i18n.global.availableLocales.includes(locale)) {
    await loadLocaleMessages(locale)
  }
  setI18nLanguage(locale)
}
```

## 10. 最佳实践建议

1. **命名空间**：按功能模块组织语言包，避免将所有翻译放在一个文件中
2. **键命名规范**：使用点分隔的命名空间 (如 `home.title`, `user.profile.name`)
3. **避免硬编码**：不要在代码中直接使用字符串，全部使用翻译键
4. **提取公共部分**：将公共词汇提取到公共语言文件中
5. **代码分割**：对于大型应用，考虑按需加载语言包
6. **类型安全**：完善 `i18n.d.ts` 定义以获得更好的类型提示
7. **自动化工具**：考虑使用 i18n-ally 等 VSCode 插件提高开发效率

通过以上配置，你的 Vue3 + TS + Vite 项目将具备完整的国际化支持，包括类型安全的翻译、日期时间本地化和数字格式化等功能。