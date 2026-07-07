<template>
  <div class="login-container">
    <section class="login-visual">
      <div class="brand-lockup">
        <div class="brand-mark-large">NE</div>
        <p class="school-name">东北石油大学</p>
        <h1>NEPU-FAMS</h1>
        <p class="visual-copy">固定资产管理系统</p>
      </div>
      <div class="visual-metrics">
        <div>
          <strong>资产台账</strong>
          <span>分类、状态、学院统一管理</span>
        </div>
        <div>
          <strong>协同审批</strong>
          <span>领用、报修、报废流程闭环</span>
        </div>
        <div>
          <strong>盘点统计</strong>
          <span>数据看板辅助资产决策</span>
        </div>
      </div>
    </section>

    <div class="login-card">
      <div class="brand-section">
        <h2 class="brand-title">登录系统</h2>
        <p class="brand-subtitle">使用账号进入固定资产管理平台</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            class="login-input"
          />
        </el-form-item>
        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" class="remember-checkbox">记住密码</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn ripple-effect"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '正在登录' : '登录' }}
          </el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>

      <div class="footer-text">东北石油大学 · 企业项目综合实训</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login({
          username: loginForm.username,
          password: loginForm.password
        })
        ElMessage.success('登录成功')
        const redirect = route.query.redirect || '/dashboard'
        router.push(redirect)
      } catch (error) {
        console.error('Login error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 1fr) 520px;
  align-items: stretch;
  background: #f5f7fb;
  overflow: hidden;
}

.login-visual {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 72px 76px;
  color: #ffffff;
  background:
    linear-gradient(135deg, rgba(22, 36, 66, 0.92), rgba(32, 52, 92, 0.86)),
    url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20university%20asset%20management%20operations%20center%2C%20quiet%20professional%20office%20with%20computer%20screens%2C%20inventory%20shelves%20and%20campus%20architecture%20visible%20through%20large%20windows%2C%20realistic%20photography%2C%20clean%20lighting%2C%20premium%20enterprise%20atmosphere%2C%20no%20text%2C%20no%20logos&image_size=landscape_16_9') center/cover;
  isolation: isolate;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, rgba(15, 23, 42, 0.08), rgba(15, 23, 42, 0.34));
    z-index: -1;
  }
}

.brand-lockup {
  max-width: 560px;
  animation: visual-enter 0.7s ease both;
}

.brand-mark-large {
  width: 54px;
  height: 54px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #ffffff;
  font-weight: 850;
  margin-bottom: 32px;
  backdrop-filter: blur(8px);
}

.school-name {
  margin: 0 0 12px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 15px;
  font-weight: 600;
}

.brand-lockup h1 {
  margin: 0;
  font-size: 56px;
  line-height: 1;
  font-weight: 850;
}

.visual-copy {
  margin: 18px 0 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 20px;
}

.visual-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  animation: visual-enter 0.7s 0.12s ease both;

  div {
    padding-top: 18px;
    border-top: 1px solid rgba(255, 255, 255, 0.24);
  }

  strong {
    display: block;
    margin-bottom: 7px;
    font-size: 15px;
    color: #ffffff;
  }

  span {
    display: block;
    color: rgba(255, 255, 255, 0.64);
    font-size: 13px;
    line-height: 1.6;
  }
}

.login-card {
  width: 100%;
  max-width: 420px;
  align-self: center;
  justify-self: center;
  padding: 44px 42px 36px;
  background: #ffffff;
  border: 1px solid #e7ecf4;
  border-radius: 16px;
  box-shadow: 0 28px 70px rgba(30, 41, 59, 0.16);
  animation: card-enter 0.5s ease both;
}

.brand-section {
  margin-bottom: 32px;
}

.brand-title {
  margin: 0 0 8px;
  color: #172033;
  font-size: 28px;
  font-weight: 800;
}

.brand-subtitle {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.login-form {
  .form-options {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
    font-weight: 650;
    letter-spacing: 0;
    border-radius: 8px !important;
  }
}

.register-link {
  text-align: center;
  color: #8792a5;
  font-size: 13px;
  margin-top: 8px;

  a {
    color: #3157d5;
    font-weight: 650;
  }
}

.footer-text {
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid #eef2f7;
  text-align: center;
  color: #9aa5b8;
  font-size: 12px;
}

@keyframes visual-enter {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes card-enter {
  from { opacity: 0; transform: translateY(18px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@media (max-width: 980px) {
  .login-container {
    grid-template-columns: 1fr;
    padding: 24px;
    background:
      linear-gradient(rgba(245, 247, 251, 0.86), rgba(245, 247, 251, 0.94)),
      url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20university%20asset%20management%20operations%20center%2C%20quiet%20professional%20office%20with%20computer%20screens%2C%20inventory%20shelves%20and%20campus%20architecture%20visible%20through%20large%20windows%2C%20realistic%20photography%2C%20clean%20lighting%2C%20premium%20enterprise%20atmosphere%2C%20no%20text%2C%20no%20logos&image_size=landscape_16_9') center/cover;
  }

  .login-visual {
    display: none;
  }

  .login-card {
    max-width: 420px;
  }
}
</style>
