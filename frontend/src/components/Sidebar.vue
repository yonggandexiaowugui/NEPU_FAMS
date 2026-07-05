<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <div class="nepu-emblem" v-if="!collapsed">
        <div class="emblem-ring emblem-ring-outer"></div>
        <div class="emblem-ring emblem-ring-middle"></div>
        <div class="emblem-ring emblem-ring-inner"></div>
        <span class="emblem-text">NEPU</span>
      </div>
      <div class="nepu-emblem-mini" v-else>
        <div class="emblem-ring emblem-ring-inner"></div>
        <span class="emblem-text-mini">N</span>
      </div>
      <div class="logo-info" v-if="!collapsed">
        <span class="logo-text">NEPU-FAMS</span>
        <span class="logo-sub">固定资产管理系统</span>
      </div>
    </div>
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <template v-for="item in menuList" :key="item.path">
        <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
          <template #title>
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
            <el-icon><component :is="child.icon" /></el-icon>
            <span>{{ child.title }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useUserStore } from '@/store'
import {
  Odometer,
  User,
  Collection,
  Box,
  DocumentAdd,
  Document,
  Check,
  Tools,
  List,
  Delete,
  CircleCheck,
  Files,
  EditPen,
  PieChart,
  Tickets,
  UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const collapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)

const iconMap = {
  Odometer,
  User,
  Collection,
  Box,
  DocumentAdd,
  Document,
  Check,
  Tools,
  List,
  Delete,
  CircleCheck,
  Files,
  EditPen,
  PieChart,
  Tickets,
  UserFilled
}

const allMenus = [
  { path: '/dashboard', title: '首页', icon: 'Odometer' },
  { path: '/user/list', title: '用户管理', icon: 'User', roles: ['SUPER_ADMIN'] },
  { path: '/asset/category', title: '资产分类', icon: 'Collection', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/asset/list', title: '资产台账', icon: 'Box', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/borrow/apply', title: '领用申请', icon: 'DocumentAdd', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN', 'USER'] },
  { path: '/borrow/my', title: '我的领用', icon: 'Document', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN', 'USER'] },
  { path: '/borrow/approval', title: '领用审批', icon: 'Check', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/repair/submit', title: '报修提交', icon: 'Tools', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN', 'USER'] },
  { path: '/repair/list', title: '维修工单', icon: 'List', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/scrap/apply', title: '报废申请', icon: 'Delete', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/scrap/approval', title: '报废审批', icon: 'CircleCheck', roles: ['SUPER_ADMIN'] },
  { path: '/inventory/task', title: '盘点任务', icon: 'Files', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/inventory/record', title: '盘点录入', icon: 'EditPen', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/statistics', title: '统计报表', icon: 'PieChart', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/log/operation', title: '操作日志', icon: 'Tickets', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/profile', title: '个人中心', icon: 'UserFilled' }
]

const menuList = computed(() => {
  const userRole = userStore.userRole
  return allMenus.filter(item => {
    if (!item.roles) return true
    return item.roles.includes(userRole)
  })
})
</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #060A12;
}

.sidebar-logo {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  background: linear-gradient(180deg, rgba(0, 229, 255, 0.06) 0%, rgba(0, 229, 255, 0.01) 100%);
  border-bottom: 1px solid rgba(0, 229, 255, 0.08);
  overflow: hidden;
  position: relative;

  /* Subtle glow at bottom */
  &::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.3), transparent);
    box-shadow: 0 0 6px rgba(0, 229, 255, 0.2);
  }
}

/* === NEPU Emblem - Concentric Circles === */
.nepu-emblem {
  position: relative;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nepu-emblem-mini {
  position: relative;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emblem-ring {
  position: absolute;
  border-radius: 50%;
  border-style: solid;
  border-color: rgba(0, 229, 255, 0.25);

  &.emblem-ring-outer {
    width: 40px;
    height: 40px;
    border-width: 1.5px;
    animation: ring-rotate 20s linear infinite;
    box-shadow: 0 0 10px rgba(0, 229, 255, 0.08);
    border-style: dashed;

    &::before {
      content: '';
      position: absolute;
      top: -2px;
      left: 50%;
      width: 4px;
      height: 4px;
      background: #00E5FF;
      border-radius: 50%;
      box-shadow: 0 0 6px rgba(0, 229, 255, 0.8);
      transform: translateX(-50%);
    }
  }

  &.emblem-ring-middle {
    width: 30px;
    height: 30px;
    border-width: 1px;
    border-color: rgba(255, 184, 0, 0.15);
    animation: ring-rotate 15s linear infinite reverse;
  }

  &.emblem-ring-inner {
    width: 20px;
    height: 20px;
    border-width: 1.5px;
    border-color: rgba(0, 229, 255, 0.4);
    box-shadow: 0 0 8px rgba(0, 229, 255, 0.15), inset 0 0 8px rgba(0, 229, 255, 0.05);
    background: radial-gradient(circle, rgba(0, 229, 255, 0.06), transparent);
    animation: ring-pulse 3s ease-in-out infinite;
  }

  /* Mini version for collapsed sidebar */
  .nepu-emblem-mini & {
    &.emblem-ring-outer {
      width: 32px;
      height: 32px;
    }

    &.emblem-ring-middle {
      width: 24px;
      height: 24px;
    }

    &.emblem-ring-inner {
      width: 16px;
      height: 16px;
    }
  }
}

@keyframes ring-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes ring-pulse {
  0%, 100% { box-shadow: 0 0 8px rgba(0, 229, 255, 0.15), inset 0 0 8px rgba(0, 229, 255, 0.05); }
  50% { box-shadow: 0 0 16px rgba(0, 229, 255, 0.3), inset 0 0 16px rgba(0, 229, 255, 0.1); }
}

.emblem-text {
  position: relative;
  z-index: 2;
  font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
  font-size: 8px;
  font-weight: 800;
  color: #00E5FF;
  text-shadow: 0 0 6px rgba(0, 229, 255, 0.6);
  letter-spacing: 1px;
  line-height: 1;
}

.emblem-text-mini {
  position: relative;
  z-index: 2;
  font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
  font-size: 10px;
  font-weight: 800;
  color: #00E5FF;
  text-shadow: 0 0 6px rgba(0, 229, 255, 0.6);
  line-height: 1;
}

/* === Logo Text Area === */
.logo-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.logo-text {
  font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: #00E5FF;
  letter-spacing: 2px;
  text-shadow: 0 0 8px rgba(0, 229, 255, 0.4);
  white-space: nowrap;
  animation: logo-breathe 4s ease-in-out infinite;
}

.logo-sub {
  font-size: 10px;
  color: rgba(232, 236, 244, 0.35);
  letter-spacing: 1px;
  white-space: nowrap;
}

@keyframes logo-breathe {
  0%, 100% { text-shadow: 0 0 8px rgba(0, 229, 255, 0.3); }
  50% { text-shadow: 0 0 16px rgba(0, 229, 255, 0.6), 0 0 32px rgba(0, 229, 255, 0.15); }
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent;

  .el-menu-item,
  .el-sub-menu__title {
    color: rgba(232, 236, 244, 0.5);
    margin: 2px 8px;
    border-radius: 8px;
    height: 44px;
    line-height: 44px;
    font-size: 14px;
    transition: all 0.25s ease;

    &:hover {
      background: rgba(0, 229, 255, 0.06);
      color: #00E5FF;
      text-shadow: 0 0 6px rgba(0, 229, 255, 0.3);
    }

    .el-icon {
      color: inherit;
    }
  }

  .el-menu-item.is-active {
    background: linear-gradient(90deg, rgba(0, 229, 255, 0.14), rgba(0, 229, 255, 0.04));
    color: #00E5FF;
    text-shadow: 0 0 8px rgba(0, 229, 255, 0.4);
    box-shadow: inset 3px 0 0 #00E5FF, 0 0 12px rgba(0, 229, 255, 0.15);
    font-weight: 600;

    .el-icon {
      color: #00E5FF;
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 60%;
      background: #00E5FF;
      border-radius: 0 2px 2px 0;
      box-shadow: 0 0 8px rgba(0, 229, 255, 0.5);
    }
  }

  .el-sub-menu {
    .el-menu {
      background: rgba(0, 229, 255, 0.02);
    }
  }
}
</style>
