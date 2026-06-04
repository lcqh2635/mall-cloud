<script setup lang="ts">

</script>

<template>
  <!-- 最外层水平容器 -->
  <el-container class="h-screen w-full">

    <!-- 左侧侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '210px'" class="transition-all duration-300 bg-[#001529]">
      <!-- Logo 放在侧边栏顶部 -->
      <Logo :collapse="isCollapse" />
      <!-- 侧边栏菜单 -->
      <Sidebar />
    </el-aside>

    <!-- 右侧垂直容器 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header height="50px" class="p-0 border-b">
        <Navbar />
      </el-header>

      <!-- 标签页（固定不滚动） -->
      <TagsView />

      <!-- 内容主体 -->
      <AppMain />
    </el-container>

  </el-container>
</template>
<style scoped>

</style>