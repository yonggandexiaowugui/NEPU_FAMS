<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <img src="/vite.svg" alt="logo" class="logo" />
        <h1 class="title">固定资产管理系统</h1>
        <p class="subtitle">用户注册</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="registerForm.collegeId" placeholder="请选择学院" style="width: 100%">
            <el-option
              v-for="college in collegeList"
              :key="college.id"
              :label="college.name"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import { getAllColleges } from '@/api/college'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const collegeList = ref([])

const registerForm = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  collegeId: null,
  role: 'USER'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ]
}

async function loadColleges() {
  try {
    const res = await getAllColleges()
    collegeList.value = res || []
  } catch (error) {
    console.error('Load colleges error:', error)
  }
}

async function handleRegister() {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register(registerForm)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('Register error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadColleges()
})
</script>

<style lang="scss" scoped>
.register-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-box {
  width: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  .logo {
    width: 50px;
    height: 50px;
    margin-bottom: 10px;
  }

  .title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 5px;
  }

  .subtitle {
    color: #909399;
    font-size: 14px;
  }
}

.register-form {
  .register-btn {
    width: 100%;
  }

  .login-link {
    text-align: center;
    color: #909399;
    font-size: 14px;

    a {
      color: #409eff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
