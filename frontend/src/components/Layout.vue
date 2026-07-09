<template>
  <el-container class="layout-container">
    <el-aside class="layout-aside" :width="collapsed ? '64px' : '220px'">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <Header />
        <div class="header-glow-line"></div>
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
    <AiAssistant />
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import Header from './Header.vue'
import Sidebar from './Sidebar.vue'
import AiAssistant from './AiAssistant.vue'
import { useAppStore } from '@/store'

const appStore = useAppStore()
const collapsed = computed(() => appStore.sidebarCollapsed)
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  background: #f5f7fb;
}

.layout-header {
  height: 60px;
  padding: 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid #e8edf5;
  position: relative;
  z-index: 5;
}

.header-glow-line {
  display: none;
}

.layout-main {
  position: relative;
  overflow-y: auto;
  padding: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.72) 0%, rgba(245, 247, 251, 0) 220px),
    #f5f7fb;
}

.fade-transform-enter-active {
  animation: fade-up-in 0.28s ease-out;
}

.fade-transform-leave-active {
  animation: fade-up-out 0.18s ease-in forwards;
}

@keyframes fade-up-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fade-up-out {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-6px); }
}
</style>
