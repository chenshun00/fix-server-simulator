<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>FIX Server Simulator</h1>
        <el-menu mode="horizontal" :default-active="activeMenu" router>
          <el-menu-item index="/sessions">会话管理</el-menu-item>
          <el-menu-item index="/messages">消息查询</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
#app {
  min-height: 100vh;
}
.el-header {
  background-color: #409eff;
  color: white;
}
.el-header h1 {
  margin: 0;
  padding: 10px 0;
  float: left;
}
.el-menu {
  float: right;
}
</style>
