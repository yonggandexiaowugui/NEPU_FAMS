<template>
  <div class="header">
    <div class="header-left">
      <el-icon class="toggle-btn" @click="toggleSidebar">
        <Fold v-if="!collapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" class="user-avatar">
            {{ userName ? userName.charAt(0).toUpperCase() : 'U' }}
          </el-avatar>
          <span class="user-name">{{ userName || '用户' }}</span>
          <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useUserStore } from '@/store'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Fold, Expand, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const collapsed = computed(() => appStore.sidebarCollapsed)
const userName = computed(() => userStore.userInfo?.username || userStore.userInfo?.name || '')

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

function toggleSidebar() {
  appStore.toggleSidebar()
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.header {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  font-size: 18px;
  cursor: pointer;
  color: rgba(232, 236, 244, 0.45);
  transition: all 0.25s;

  &:hover {
    color: #00E5FF;
    text-shadow: 0 0 8px rgba(0, 229, 255, 0.5);
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 14px;
  height: 56px;
  border-radius: 0;
  transition: all 0.25s;

  &:hover {
    background: rgba(0, 229, 255, 0.04);
  }
}

.user-avatar {
  margin-right: 10px;
}

.user-name {
  margin-right: 6px;
  font-size: 13px;
  color: rgba(232, 236, 244, 0.7);
}

.dropdown-icon {
  font-size: 12px;
  color: rgba(232, 236, 244, 0.35);
}
</style>
