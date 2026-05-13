<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useUserStore, useAppStore } from '@/stores'

// 获取store实例
const userStore = useUserStore()
const appStore = useAppStore()

// 使用storeToRefs保持响应式解构
const { token, isLogin, userInfo } = storeToRefs(userStore)
const { theme, sidebarCollapsed } = storeToRefs(appStore)

// 调用action
function handleLogin() {
  userStore.setToken('mock-token')
  userStore.setUserInfo({
    id: 1,
    name: 'Admin',
    roles: ['admin']
  })
}

function handleLogout() {
  userStore.clearUser()
}

function toggleTheme() {
  appStore.toggleTheme()
}
</script>

<template>
  <div :class="['app', theme]">
    <header>
      <span v-if="isLogin">欢迎, {{ userInfo?.name }}</span>
      <button @click="toggleTheme">切换主题</button>
      <button v-if="isLogin" @click="handleLogout">退出登录</button>
    </header>
    <main>
      <button v-if="!isLogin" @click="handleLogin">模拟登录</button>
    </main>
  </div>
</template>