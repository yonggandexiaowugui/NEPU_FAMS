<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <div class="brand-mark" v-if="!collapsed">NE</div>
      <div class="brand-mark mini" v-else>N</div>
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
      background-color="#ffffff"
      text-color="#64748b"
      active-text-color="#3157d5"
      @select="handleMenuSelect"
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
  DataAnalysis,
  Monitor,
  Tickets,
  UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
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
  DataAnalysis,
  Monitor,
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
  { path: '/ai/analysis', title: '智能分析', icon: 'DataAnalysis', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] },
  { path: '/spring/ai/loom/index.html', title: 'AI 工作台', icon: 'Monitor', roles: ['SUPER_ADMIN'], external: true },
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

function handleMenuSelect(path) {
  const item = menuList.value.find(menu => menu.path === path)
  if (item?.external) {
    window.open(path, '_blank')
    return
  }
  router.push(path)
}
</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.sidebar-logo {
  height: 76px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid #eef2f7;
  overflow: hidden;
}

.brand-mark {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #20345c 0%, #3157d5 100%);
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
  box-shadow: 0 10px 22px rgba(49, 87, 213, 0.18);

  &.mini {
    width: 34px;
    height: 34px;
    font-size: 12px;
  }
}

.logo-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.logo-text {
  font-size: 15px;
  font-weight: 800;
  color: #172033;
  white-space: nowrap;
}

.logo-sub {
  font-size: 11px;
  color: #8792a5;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 12px 10px;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent;

  .el-menu-item,
  .el-sub-menu__title {
    height: 42px;
    line-height: 42px;
    margin: 3px 0;
    border-radius: 8px;
    color: #64748b;
    font-size: 14px;
    font-weight: 550;
    transition: all 0.18s ease;

    &:hover {
      background: #f4f7fb;
      color: #172033;
    }

    .el-icon {
      color: inherit;
      font-size: 17px;
    }
  }

  .el-menu-item.is-active {
    position: relative;
    background: #eef3ff;
    color: #3157d5;
    font-weight: 700;
    box-shadow: none;

    .el-icon {
      color: #3157d5;
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 9px;
      width: 3px;
      height: 24px;
      border-radius: 0 999px 999px 0;
      background: #3157d5;
    }
  }

  .el-sub-menu .el-menu {
    background: #fbfcff;
    border-radius: 8px;
    margin: 2px 0 6px;
  }
}
</style>
