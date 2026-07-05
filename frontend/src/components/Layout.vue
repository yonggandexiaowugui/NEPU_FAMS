<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="layout-aside">
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
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import { useAppStore } from '@/store'

const appStore = useAppStore()
const sidebarWidth = computed(() => appStore.sidebarWidth)
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  background: #060A12;
  background-image:
    radial-gradient(ellipse at 15% 50%, rgba(0, 229, 255, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 85% 20%, rgba(255, 184, 0, 0.015) 0%, transparent 50%);
}

.layout-aside {
  background: rgba(6, 10, 18, 0.98);
  border-right: 1px solid rgba(0, 229, 255, 0.06);
  transition: width 0.3s ease;
  overflow: hidden;
  box-shadow: 1px 0 24px rgba(0, 0, 0, 0.4);
  position: relative;

  /* Subtle circuit texture */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      repeating-linear-gradient(0deg, transparent, transparent 28px, rgba(0, 229, 255, 0.02) 28px, rgba(0, 229, 255, 0.02) 29px),
      repeating-linear-gradient(90deg, transparent, transparent 28px, rgba(0, 229, 255, 0.02) 28px, rgba(0, 229, 255, 0.02) 29px);
    animation: circuit-pulse 5s ease-in-out infinite;
    pointer-events: none;
    opacity: 0.5;
  }
}

@keyframes circuit-pulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 0.7; }
}

.layout-header {
  background: rgba(6, 10, 18, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: none;
  padding: 0;
  height: 56px;
  position: relative;
}

.header-glow-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg,
    transparent 5%,
    rgba(0, 229, 255, 0.4) 20%,
    rgba(255, 184, 0, 0.25) 50%,
    rgba(0, 229, 255, 0.4) 80%,
    transparent 95%
  );
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.25), 0 0 20px rgba(0, 229, 255, 0.08);
}

.layout-main {
  background: #060A12;
  background-image:
    radial-gradient(ellipse at 50% 0%, rgba(0, 229, 255, 0.015) 0%, transparent 60%),
    linear-gradient(rgba(0, 229, 255, 0.015) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 229, 255, 0.015) 1px, transparent 1px);
  background-size: 100% 100%, 50px 50px, 50px 50px;
  padding: 20px;
  overflow-y: auto;
  position: relative;

  /* Scan line overlay */
  &::after {
    content: '';
    position: absolute;
    left: 0;
    width: 100%;
    height: 1px;
    background: linear-gradient(90deg, transparent 10%, rgba(0, 229, 255, 0.08) 30%, rgba(0, 229, 255, 0.15) 50%, rgba(0, 229, 255, 0.08) 70%, transparent 90%);
    animation: scan-sweep 6s linear infinite;
    pointer-events: none;
    z-index: 0;
  }
}

@keyframes scan-sweep {
  0% { top: -1px; opacity: 0; }
  5% { opacity: 1; }
  95% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* Glitch route transition */
.glitch-enter-active {
  animation: glitch-in 0.35s ease-out;
}
.glitch-leave-active {
  animation: glitch-out 0.25s ease-in forwards;
}

@keyframes glitch-in {
  0% { opacity: 0; transform: translateX(-3px) skewX(-1deg); filter: blur(2px); }
  50% { opacity: 0.7; transform: translateX(2px) skewX(0.5deg); filter: blur(0); }
  100% { opacity: 1; transform: translateX(0) skewX(0); filter: blur(0); }
}

@keyframes glitch-out {
  0% { opacity: 1; transform: translateX(0); }
  100% { opacity: 0; transform: translateX(3px); filter: blur(2px); }
}

.fade-transform-enter-active {
  animation: fade-up-in 0.4s ease-out;
}

.fade-transform-leave-active {
  animation: fade-up-out 0.25s ease-in forwards;
}

@keyframes fade-up-in {
  0% { opacity: 0; transform: translateY(16px) scale(0.98); filter: blur(2px); }
  100% { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}

@keyframes fade-up-out {
  0% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(-10px); filter: blur(1px); }
}
</style>
