# 前端项目中的 utils 目录详解

`utils` 目录是存放项目中各种工具函数的集中地，这些函数通常具有高度复用性且与业务逻辑解耦。下面我将详细介绍 utils 目录的组织结构和典型内容，特别是 `validators.ts` 的实现示例。

## utils 目录典型结构

```
src/
├── utils/
│   ├── validators.ts       # 表单验证工具
│   ├── formatters.ts       # 数据格式化工具
│   ├── helpers.ts          # 辅助函数
│   ├── date.ts            # 日期处理工具
│   ├── string.ts          # 字符串处理工具
│   ├── array.ts           # 数组处理工具
│   ├── object.ts          # 对象处理工具
│   ├── http.ts            # HTTP相关工具
│   ├── device.ts          # 设备检测工具
│   ├── storage.ts         # 存储封装工具
│   ├── router.ts          # 路由相关工具
│   ├── constants.ts       # 常量定义
│   ├── regexp.ts          # 正则表达式集合
│   └── index.ts           # 统一导出入口
```

## 各类工具文件的作用

### 1. validators.ts - 表单验证工具
包含各种表单验证函数，用于验证输入数据的合法性。

### 2. formatters.ts - 数据格式化
处理数据展示格式，如货币格式化、数字格式化等。

### 3. helpers.ts - 辅助函数
各种通用的辅助功能函数。

### 4. date.ts - 日期处理
日期转换、计算、格式化等操作。

### 5. string.ts - 字符串处理
字符串操作、转换、校验等。

### 6. array.ts - 数组处理
数组操作、过滤、排序等。

### 7. object.ts - 对象处理
对象操作、合并、深度拷贝等。

### 8. http.ts - HTTP相关
封装HTTP请求、处理响应等。

### 9. device.ts - 设备检测
检测设备类型、浏览器信息等。

### 10. storage.ts - 存储封装
对 localStorage/sessionStorage 的封装。

### 11. router.ts - 路由工具
路由相关辅助函数。

### 12. constants.ts - 常量定义
项目中用到的各种常量。

### 13. regexp.ts - 正则表达式
集中管理所有正则表达式。

## validators.ts 详细实现示例

以下是 `validators.ts` 的完整实现示例，包含多种常见验证场景：

```typescript
/**
 * 表单验证工具集
 * 包含常见的表单验证函数
 */

/**
 * 验证是否为空
 * @param value - 要验证的值
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateRequired(value: any, message = '此项为必填项'): string {
  if (value === null || value === undefined || value === '') {
    return message
  }
  if (Array.isArray(value) && value.length === 0) {
    return message
  }
  return ''
}

/**
 * 验证手机号格式
 * @param value - 手机号码
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateMobile(value: string, message = '请输入正确的手机号码'): string {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(value) ? '' : message
}

/**
 * 验证邮箱格式
 * @param value - 邮箱地址
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateEmail(value: string, message = '请输入正确的邮箱地址'): string {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(value) ? '' : message
}

/**
 * 验证密码强度
 * @param value - 密码
 * @param options - 配置选项
 * @returns 错误消息或空字符串
 */
export function validatePassword(
  value: string,
  options: {
    minLength?: number
    requireNumber?: boolean
    requireSpecialChar?: boolean
    message?: string
  } = {}
): string {
  const {
    minLength = 8,
    requireNumber = true,
    requireSpecialChar = true,
    message = '密码必须包含字母和数字，且长度不少于8位'
  } = options

  if (value.length < minLength) {
    return message
  }

  if (requireNumber && !/\d/.test(value)) {
    return message
  }

  if (requireSpecialChar && !/[!@#$%^&*(),.?":{}|<>]/.test(value)) {
    return message
  }

  return ''
}

/**
 * 验证身份证号码
 * @param value - 身份证号码
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateIDCard(value: string, message = '请输入正确的身份证号码'): string {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  return reg.test(value) ? '' : message
}

/**
 * 验证数字范围
 * @param value - 数字值
 * @param options - 配置选项
 * @returns 错误消息或空字符串
 */
export function validateNumberRange(
  value: number,
  options: {
    min?: number
    max?: number
    message?: string
  } = {}
): string {
  const { min, max, message = '数值超出允许范围' } = options

  if (min !== undefined && value < min) {
    return message
  }

  if (max !== undefined && value > max) {
    return message
  }

  return ''
}

/**
 * 验证URL格式
 * @param value - URL地址
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateURL(value: string, message = '请输入正确的URL地址'): string {
  try {
    new URL(value)
    return ''
  } catch {
    return message
  }
}

/**
 * 验证两个字段是否一致（如密码确认）
 * @param value1 - 第一个值
 * @param value2 - 第二个值
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validateMatch(
  value1: any,
  value2: any,
  message = '两次输入不一致'
): string {
  return value1 === value2 ? '' : message
}

/**
 * 自定义正则验证
 * @param value - 要验证的值
 * @param reg - 正则表达式
 * @param message - 自定义错误消息
 * @returns 错误消息或空字符串
 */
export function validatePattern(
  value: string,
  reg: RegExp,
  message = '输入格式不正确'
): string {
  return reg.test(value) ? '' : message
}

/**
 * 组合验证器
 * @param value - 要验证的值
 * @param validators - 验证器数组
 * @returns 第一个验证失败的错误消息，或空字符串
 */
export function validateAll(
  value: any,
  validators: Array<(val: any) => string>
): string {
  for (const validator of validators) {
    const result = validator(value)
    if (result) {
      return result
    }
  }
  return ''
}

// 导出所有验证器
export default {
  required: validateRequired,
  mobile: validateMobile,
  email: validateEmail,
  password: validatePassword,
  idCard: validateIDCard,
  numberRange: validateNumberRange,
  url: validateURL,
  match: validateMatch,
  pattern: validatePattern,
  all: validateAll
}
```

## 使用示例

### 1. 在 Vue 组件中使用

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { 
  validateRequired, 
  validateEmail, 
  validatePassword 
} from '@/utils/validators'

const form = ref({
  email: '',
  password: ''
})

const errors = ref({
  email: '',
  password: ''
})

function validateForm() {
  let isValid = true
  
  // 验证邮箱
  errors.value.email = validateEmail(form.value.email)
  if (errors.value.email) isValid = false
  
  // 验证密码
  errors.value.password = validatePassword(form.value.password, {
    minLength: 8,
    requireNumber: true,
    requireSpecialChar: false
  })
  if (errors.value.password) isValid = false
  
  return isValid
}

function onSubmit() {
  if (validateForm()) {
    // 提交表单
  }
}
</script>

<template>
  <form @submit.prevent="onSubmit">
    <div>
      <label>邮箱</label>
      <input v-model="form.email" type="email">
      <span class="error">{{ errors.email }}</span>
    </div>
    
    <div>
      <label>密码</label>
      <input v-model="form.password" type="password">
      <span class="error">{{ errors.password }}</span>
    </div>
    
    <button type="submit">提交</button>
  </form>
</template>
```

### 2. 组合多个验证器

```typescript
// 验证用户名：必填且长度在4-20个字符之间
function validateUsername(username: string): string {
  return validateAll(username, [
    (val) => validateRequired(val, '用户名不能为空'),
    (val) => validatePattern(val, /^.{4,20}$/, '用户名长度需在4-20个字符之间')
  ])
}
```

### 3. 在表单库中使用

如果你使用类似 VeeValidate 的表单验证库，可以这样集成：

```typescript
import { defineRule } from 'vee-validate'
import * as validators from '@/utils/validators'

// 注册自定义验证规则
defineRule('required', validators.required)
defineRule('email', validators.email)
defineRule('password', (value: string) => 
  validators.password(value, { requireSpecialChar: true }))
```

## 最佳实践建议

1. **保持纯函数**：验证函数应该是纯函数，不依赖外部状态
2. **单一职责**：每个验证函数只验证一个特定规则
3. **可配置性**：通过参数支持自定义错误消息和验证条件
4. **类型安全**：使用 TypeScript 增强类型提示
5. **文档注释**：为每个函数添加清晰的 JSDoc 注释
6. **单元测试**：为验证函数编写全面的单元测试
7. **国际化支持**：考虑将错误消息提取为可翻译的键
8. **性能优化**：对于复杂验证，考虑使用防抖或缓存

通过这种组织方式，你的验证逻辑将保持清晰、可维护和可复用，同时能够灵活应对各种表单验证需求。