<template>
  <div class="py-1" role="menu" aria-orientation="vertical" aria-labelledby="options-menu">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'DropdownMenu'
})
</script>