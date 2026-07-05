import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页', icon: 'Odometer' }
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['SUPER_ADMIN'] }
      },
      {
        path: 'asset/category',
        name: 'Category',
        component: () => import('@/views/asset/Category.vue'),
        meta: { title: '资产分类', icon: 'Collection', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'asset/list',
        name: 'AssetList',
        component: () => import('@/views/asset/AssetList.vue'),
        meta: { title: '资产台账', icon: 'Box', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'borrow/apply',
        name: 'BorrowApply',
        component: () => import('@/views/borrow/BorrowApply.vue'),
        meta: { title: '领用申请', icon: 'DocumentAdd', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN', 'USER'] }
      },
      {
        path: 'borrow/my',
        name: 'MyBorrow',
        component: () => import('@/views/borrow/MyBorrow.vue'),
        meta: { title: '我的领用', icon: 'Document', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN', 'USER'] }
      },
      {
        path: 'borrow/approval',
        name: 'BorrowApproval',
        component: () => import('@/views/borrow/BorrowApproval.vue'),
        meta: { title: '领用审批', icon: 'Check', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'repair/submit',
        name: 'RepairSubmit',
        component: () => import('@/views/repair/RepairSubmit.vue'),
        meta: { title: '报修提交', icon: 'Tools', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN', 'USER'] }
      },
      {
        path: 'repair/list',
        name: 'RepairList',
        component: () => import('@/views/repair/RepairList.vue'),
        meta: { title: '维修工单', icon: 'List', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'scrap/apply',
        name: 'ScrapApply',
        component: () => import('@/views/scrap/ScrapApply.vue'),
        meta: { title: '报废申请', icon: 'Delete', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'scrap/approval',
        name: 'ScrapApproval',
        component: () => import('@/views/scrap/ScrapApproval.vue'),
        meta: { title: '报废审批', icon: 'CircleCheck', roles: ['SUPER_ADMIN'] }
      },
      {
        path: 'inventory/task',
        name: 'InventoryTask',
        component: () => import('@/views/inventory/InventoryTask.vue'),
        meta: { title: '盘点任务', icon: 'ClipboardList', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'inventory/record',
        name: 'InventoryRecord',
        component: () => import('@/views/inventory/InventoryRecord.vue'),
        meta: { title: '盘点录入', icon: 'EditPen', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '统计报表', icon: 'PieChart', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'log/operation',
        name: 'OperationLog',
        component: () => import('@/views/log/OperationLog.vue'),
        meta: { title: '操作日志', icon: 'Tickets', roles: ['SUPER_ADMIN', 'COLLEGE_ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 固定资产管理系统` : '固定资产管理系统'
  
  const userStore = useUserStore()
  
  if (to.meta.public) {
    if (userStore.isLoggedIn && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!userStore.isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      if (to.meta.roles && to.meta.roles.length > 0) {
        const userRole = userStore.userRole
        if (to.meta.roles.includes(userRole)) {
          next()
        } else {
          next('/dashboard')
        }
      } else {
        next()
      }
    }
  }
})

export default router
