<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="user-card">
          <div class="user-info">
            <el-avatar :size="80" class="avatar">
              {{ userName ? userName.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <h3 class="name">{{ userInfo?.name || userInfo?.username || '用户' }}</h3>
            <p class="role">
              <el-tag :type="roleTagType">{{ roleText }}</el-tag>
            </p>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ userInfo?.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话：</span>
              <span class="value">{{ userInfo?.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">学院：</span>
              <span class="value">{{ userInfo?.collegeName || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <el-form :model="infoForm" :rules="infoRules" ref="infoFormRef" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="infoForm.username" disabled />
              </el-form-item>
              <el-form-item label="姓名" prop="name">
                <el-input v-model="infoForm.name" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="infoForm.email" />
              </el-form-item>
              <el-form-item label="电话" prop="phone">
                <el-input v-model="infoForm.phone" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateInfo" :loading="infoLoading">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="修改密码" name="password">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { updateInfo, changePassword } from '@/api/user'

const userStore = useUserStore()
const activeTab = ref('info')
const infoLoading = ref(false)
const passwordLoading = ref(false)
const infoFormRef = ref(null)
const passwordFormRef = ref(null)

const userInfo = computed(() => userStore.userInfo)
const userName = computed(() => userInfo.value?.name || userInfo.value?.username)

const roleText = computed(() => {
  const map = { SUPER_ADMIN: '超级管理员', COLLEGE_ADMIN: '学院管理员', USER: '普通用户' }
  return map[userInfo.value?.role] || userInfo.value?.role || '普通用户'
})

const roleTagType = computed(() => {
  const map = { SUPER_ADMIN: 'danger', COLLEGE_ADMIN: 'warning', USER: 'success' }
  return map[userInfo.value?.role] || 'info'
})

const infoForm = reactive({
  username: '',
  name: '',
  email: '',
  phone: ''
})

const infoRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function loadUserInfo() {
  if (userInfo.value) {
    infoForm.username = userInfo.value.username || ''
    infoForm.name = userInfo.value.name || ''
    infoForm.email = userInfo.value.email || ''
    infoForm.phone = userInfo.value.phone || ''
  }
}

async function handleUpdateInfo() {
  if (!infoFormRef.value) return
  await infoFormRef.value.validate(async (valid) => {
    if (valid) {
      infoLoading.value = true
      try {
        const res = await updateInfo(infoForm)
        userStore.updateUserInfo(res)
        ElMessage.success('修改成功')
      } catch (error) {
        console.error('Update info error:', error)
      } finally {
        infoLoading.value = false
      }
    }
  })
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordFormRef.value?.resetFields()
      } catch (error) {
        console.error('Change password error:', error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.page-container {
  .user-card {
    .user-info {
      text-align: center;
      padding: 20px 0;

      .avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        font-size: 32px;
        color: #fff;
      }

      .name {
        margin: 15px 0 5px 0;
        font-size: 20px;
        color: #303133;
      }

      .role {
        margin: 0;
      }
    }

    .info-list {
      .info-item {
        display: flex;
        padding: 10px 0;
        font-size: 14px;

        .label {
          color: #909399;
          width: 80px;
        }

        .value {
          color: #303133;
          flex: 1;
        }
      }
    }
  }
}
</style>
