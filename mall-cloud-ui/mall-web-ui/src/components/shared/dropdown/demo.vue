<template>
  <div class="p-8">
    <Dropdown placement="bottom-start" trigger="hover">
      <template #trigger>
        <button class="px-4 py-2 bg-blue-500 text-white rounded">
          悬停触发下拉菜单
        </button>
      </template>

      <DropdownMenu>
        <DropdownItem @click="handleAction('action1')">操作一</DropdownItem>
        <DropdownItem @click="handleAction('action2')">操作二</DropdownItem>
        <DropdownItem @click="handleAction('action3')" disabled>禁用操作</DropdownItem>
        <DropdownItem divided>分割线</DropdownItem>
      </DropdownMenu>
    </Dropdown>

    <div class="mt-4">
      <Dropdown placement="bottom-end" trigger="click">
        <template #trigger>
          <button class="px-4 py-2 bg-green-500 text-white rounded">
            点击触发下拉菜单
          </button>
        </template>

        <DropdownMenu>
          <DropdownItem @click="handleAction('action4')">
            <span class="mr-2">👤</span>
            个人中心
          </DropdownItem>
          <DropdownItem @click="handleAction('action5')">
            <span class="mr-2">⚙️</span>
            设置
          </DropdownItem>
          <DropdownItem divided @click="handleAction('logout')">
            <span class="mr-2">🚪</span>
            退出登录
          </DropdownItem>
        </DropdownMenu>
      </Dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import Dropdown from './components/Dropdown.vue'
import DropdownMenu from './components/DropdownMenu.vue'
import DropdownItem from './components/DropdownItem.vue'

const handleAction = (action: string) => {
  console.log('执行操作:', action)
  alert(`执行操作: ${action}`)
}
</script>