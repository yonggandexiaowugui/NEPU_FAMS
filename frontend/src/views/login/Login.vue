<template>
  <div class="login-container">
    <!-- Circuit board texture background -->
    <div class="circuit-bg"></div>
    
    <!-- Animated particles with connections -->
    <div class="particles-container" ref="particlesRef">
      <span v-for="n in 40" :key="n" class="particle" :style="particleStyle(n)"></span>
    </div>
    
    <!-- Data stream lines -->
    <div class="data-streams">
      <div v-for="n in 5" :key="'ds-'+n" class="data-stream" :style="dataStreamStyle(n)"></div>
    </div>
    
    <!-- Scan line -->
    <div class="scan-line"></div>
    
    <!-- Oil drilling tower silhouettes -->
    <div class="tower tower-left">
      <div class="tower-base"></div>
      <div class="tower-arm"></div>
      <div class="tower-crown"></div>
      <div class="tower-cable"></div>
    </div>
    <div class="tower tower-right">
      <div class="tower-base"></div>
      <div class="tower-arm"></div>
      <div class="tower-crown"></div>
      <div class="tower-cable"></div>
    </div>
    
    <!-- NEPU Emblem - Concentric Circles Startup Animation -->
    <div class="emblem-container">
      <div class="emblem-outer-ring"></div>
      <div class="emblem-middle-ring"></div>
      <div class="emblem-inner-ring"></div>
      <div class="emblem-core-text">
        <span class="emblem-nepu">NEPU</span>
        <span class="emblem-year">Est. 1960</span>
      </div>
      <div class="emblem-orbit" style="--orbit-duration: 12s; --orbit-size: 100px;"></div>
      <div class="emblem-orbit" style="--orbit-duration: 18s; --orbit-size: 120px; animation-delay: -6s;"></div>
      <div class="emblem-orbit" style="--orbit-duration: 25s; --orbit-size: 140px; animation-delay: -12s;"></div>
      <div class="emblem-label">东北石油大学</div>
    </div>

    <!-- Login Card -->
    <div class="login-card holographic-card">
      <div class="login-card-scan"></div>
      
      <div class="brand-section">
        <h1 class="brand-title glitch-text" data-text="NEPU-FAMS">NEPU-FAMS</h1>
        <div class="brand-divider">
          <span class="divider-line"></span>
          <span class="divider-dot"></span>
          <span class="divider-line"></span>
        </div>
        <p class="brand-subtitle">固定资产管理系统</p>
        <p class="brand-subtitle-en typing-cursor">Fixed Assets Management System</p>
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
            class="cyber-input"
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
            class="cyber-input"
          />
        </el-form-item>
        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" class="neon-checkbox">记住密码</el-checkbox>
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
            {{ loading ? '正在连接' : '登 录 系 统' }}
          </el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>

      <div class="footer-text">
        <span class="footer-dot"></span>
        计算机科学与技术学院 · 企业项目综合实训
        <span class="footer-dot"></span>
      </div>
    </div>
    
    <!-- Version info -->
    <div class="version-info">v2.0.0 // NEPU-FAMS // CYBER EDITION</div>
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
const particlesRef = ref(null)

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

function particleStyle(n) {
  const x = Math.random() * 100
  const y = Math.random() * 100
  const size = 1 + Math.random() * 3
  const duration = 10 + Math.random() * 15
  const delay = Math.random() * 12
  return {
    left: x + '%',
    top: y + '%',
    width: size + 'px',
    height: size + 'px',
    animationDuration: duration + 's',
    animationDelay: delay + 's'
  }
}

function dataStreamStyle(n) {
  const left = 10 + n * 20
  const duration = 6 + n * 2
  const delay = n * 1.5
  return {
    left: left + '%',
    animationDuration: duration + 's',
    animationDelay: delay + 's'
  }
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
  background: #060A12;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* === Circuit Board Background === */
.circuit-bg {
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 30px, rgba(0, 229, 255, 0.025) 30px, rgba(0, 229, 255, 0.025) 31px),
    repeating-linear-gradient(90deg, transparent, transparent 30px, rgba(0, 229, 255, 0.025) 30px, rgba(0, 229, 255, 0.025) 31px);
  animation: circuit-pulse 5s ease-in-out infinite;
  pointer-events: none;
}

@keyframes circuit-pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

/* === Particles === */
.particles-container {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: var(--color-primary, #00E5FF);
  border-radius: 50%;
  opacity: 0;
  animation: float-particle linear infinite;
  filter: blur(0.5px);
  box-shadow: 0 0 4px rgba(0, 229, 255, 0.3);
}

@keyframes float-particle {
  0% { transform: translateY(0) translateX(0); opacity: 0; }
  10% { opacity: 0.7; }
  50% { transform: translateY(-50vh) translateX(15px); opacity: 0.4; }
  90% { opacity: 0.7; }
  100% { transform: translateY(-100vh) translateX(30px); opacity: 0; }
}

/* === Data Streams === */
.data-streams {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.data-stream {
  position: absolute;
  top: -100%;
  width: 1px;
  height: 200px;
  background: linear-gradient(180deg, transparent, rgba(0, 229, 255, 0.15), transparent);
  animation: stream-fall linear infinite;
  opacity: 0.6;
}

@keyframes stream-fall {
  0% { transform: translateY(0); }
  100% { transform: translateY(calc(100vh + 200px)); }
}

/* === Scan Line === */
.scan-line {
  position: absolute;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent 10%, rgba(0, 229, 255, 0.1) 30%, rgba(0, 229, 255, 0.2) 50%, rgba(0, 229, 255, 0.1) 70%, transparent 90%);
  animation: scan-sweep 5s linear infinite;
  pointer-events: none;
  z-index: 50;
}

@keyframes scan-sweep {
  0% { top: -2px; opacity: 0; }
  5% { opacity: 1; }
  95% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* === Oil Drilling Tower Silhouettes === */
.tower {
  position: absolute;
  bottom: 0;
  opacity: 0.06;
  pointer-events: none;

  &.tower-left {
    left: 5%;
    transform: scale(0.8);
  }

  &.tower-right {
    right: 5%;
    transform: scaleX(-1) scale(0.8);
  }

  .tower-base {
    width: 40px;
    height: 180px;
    background: linear-gradient(180deg, var(--color-primary, #00E5FF), transparent);
    clip-path: polygon(30% 0%, 70% 0%, 80% 100%, 20% 100%);
    position: relative;
  }

  .tower-arm {
    position: absolute;
    top: 10px;
    left: -25px;
    width: 90px;
    height: 3px;
    background: var(--color-primary, #00E5FF);
    transform-origin: right center;
    transform: rotate(-15deg);
    border-radius: 2px;
  }

  .tower-crown {
    position: absolute;
    top: -15px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 15px solid transparent;
    border-right: 15px solid transparent;
    border-bottom: 20px solid var(--color-primary, #00E5FF);
  }

  .tower-cable {
    position: absolute;
    top: 0;
    left: 50%;
    width: 1px;
    height: 80px;
    background: linear-gradient(180deg, var(--color-secondary, #FFB800), transparent);
    transform-origin: top center;
    animation: cable-swing 4s ease-in-out infinite;
  }
}

@keyframes cable-swing {
  0%, 100% { transform: translateX(-50%) rotate(-3deg); }
  50% { transform: translateX(-50%) rotate(3deg); }
}

/* === NEPU Emblem with Concentric Circles === */
.emblem-container {
  position: absolute;
  left: 14%;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  pointer-events: none;
}

.emblem-outer-ring {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  border: 1.5px solid rgba(0, 229, 255, 0.15);
  position: absolute;
  animation: ring-pulse-outer 4s ease-in-out infinite;
  box-shadow: 0 0 30px rgba(0, 229, 255, 0.05);
}

.emblem-middle-ring {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 1px solid rgba(0, 229, 255, 0.2);
  position: absolute;
  animation: ring-pulse-middle 3s ease-in-out infinite 0.5s;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.08);
}

.emblem-inner-ring {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 2px solid rgba(0, 229, 255, 0.3);
  position: absolute;
  animation: ring-pulse-inner 2.5s ease-in-out infinite 1s;
  background: radial-gradient(circle, rgba(0, 229, 255, 0.05), transparent);
  box-shadow: 0 0 15px rgba(0, 229, 255, 0.15), inset 0 0 15px rgba(0, 229, 255, 0.05);
}

@keyframes ring-pulse-outer {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.03); }
}

@keyframes ring-pulse-middle {
  0%, 100% { opacity: 0.5; transform: scale(1) rotate(0deg); }
  50% { opacity: 1; transform: scale(1.02) rotate(5deg); }
}

@keyframes ring-pulse-inner {
  0%, 100% { opacity: 0.6; box-shadow: 0 0 15px rgba(0, 229, 255, 0.15), inset 0 0 15px rgba(0, 229, 255, 0.05); }
  50% { opacity: 1; box-shadow: 0 0 25px rgba(0, 229, 255, 0.3), inset 0 0 25px rgba(0, 229, 255, 0.1); }
}

.emblem-core-text {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  z-index: 2;
}

.emblem-nepu {
  font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
  font-size: 20px;
  font-weight: 800;
  color: #00E5FF;
  text-shadow: 0 0 12px rgba(0, 229, 255, 0.6), 0 0 24px rgba(0, 229, 255, 0.2);
  letter-spacing: 5px;
}

.emblem-year {
  font-family: 'JetBrains Mono', monospace;
  font-size: 9px;
  color: rgba(255, 184, 0, 0.5);
  letter-spacing: 2px;
}

.emblem-orbit {
  position: absolute;
  width: calc(var(--orbit-size) * 2);
  height: calc(var(--orbit-size) * 2);
  border-radius: 50%;
  border: 1px dashed rgba(0, 229, 255, 0.08);
  animation: orbit-spin var(--orbit-duration) linear infinite;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    width: 4px;
    height: 4px;
    background: var(--color-primary, #00E5FF);
    border-radius: 50%;
    box-shadow: 0 0 6px rgba(0, 229, 255, 0.6);
    transform: translateX(-50%);
  }
}

@keyframes orbit-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.emblem-label {
  margin-top: 110px;
  font-size: 12px;
  color: rgba(0, 229, 255, 0.4);
  letter-spacing: 8px;
  font-weight: 500;
}

/* === Login Card === */
.login-card {
  position: relative;
  width: 440px;
  padding: 48px 44px 40px;
  background: linear-gradient(
    135deg,
    rgba(10, 15, 28, 0.9) 0%,
    rgba(13, 19, 33, 0.85) 50%,
    rgba(10, 15, 28, 0.9) 100%
  );
  backdrop-filter: blur(24px);
  border: 1px solid rgba(0, 229, 255, 0.12);
  border-top: 1px solid rgba(0, 229, 255, 0.35);
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 60px rgba(0, 229, 255, 0.04),
    inset 0 1px 0 rgba(0, 229, 255, 0.08),
    inset 0 0 60px rgba(0, 229, 255, 0.015);
  animation: card-entrance 1s ease-out forwards;
  z-index: 10;
  overflow: hidden;

  /* Card scan line */
  .login-card-scan {
    position: absolute;
    left: 0;
    width: 100%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.12), transparent);
    animation: card-scan 3s linear infinite;
    pointer-events: none;
  }
}

@keyframes card-scan {
  0% { top: -1px; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

@keyframes card-entrance {
  0% { opacity: 0; transform: translateY(40px) scale(0.96); filter: blur(4px); }
  60% { opacity: 1; transform: translateY(-5px) scale(1.01); filter: blur(0); }
  100% { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}

/* === Brand Section === */
.brand-section {
  text-align: center;
  margin-bottom: 36px;
}

.brand-title {
  font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
  font-size: 34px;
  font-weight: 900;
  color: #00E5FF;
  text-shadow: 0 0 20px rgba(0, 229, 255, 0.5), 0 0 40px rgba(0, 229, 255, 0.15);
  margin: 0 0 16px;
  letter-spacing: 5px;
  position: relative;
  display: inline-block;
}

.brand-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;

  .divider-line {
    width: 40px;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.4));
  }

  .divider-line:last-child {
    background: linear-gradient(90deg, rgba(0, 229, 255, 0.4), transparent);
  }

  .divider-dot {
    width: 5px;
    height: 5px;
    background: var(--color-secondary, #FFB800);
    border-radius: 50%;
    box-shadow: 0 0 6px rgba(255, 184, 0, 0.5);
  }
}

.brand-subtitle {
  font-size: 15px;
  color: rgba(232, 236, 244, 0.75);
  margin: 0 0 4px;
  letter-spacing: 4px;
  font-weight: 500;
}

.brand-subtitle-en {
  font-size: 11px;
  color: rgba(0, 229, 255, 0.4);
  letter-spacing: 2px;
  font-family: 'JetBrains Mono', monospace;
}

/* === Form === */
.login-form {
  .form-options {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .neon-checkbox {
      :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
        box-shadow: 0 0 6px rgba(0, 229, 255, 0.4);
      }
    }
  }

  .login-btn {
    width: 100%;
    height: 46px;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 8px;
    border-radius: 8px !important;
    position: relative;
    overflow: hidden;
    animation: btn-breathe 3s ease-in-out infinite;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px) scale(1.01);
      box-shadow: 0 0 20px rgba(0, 229, 255, 0.6), 0 0 40px rgba(0, 229, 255, 0.2) !important;
    }

    &:active {
      transform: translateY(0) scale(0.99);
    }
  }
}

@keyframes btn-breathe {
  0%, 100% { box-shadow: 0 0 10px rgba(0, 229, 255, 0.3), 0 0 20px rgba(0, 229, 255, 0.1); }
  50% { box-shadow: 0 0 18px rgba(0, 229, 255, 0.6), 0 0 36px rgba(0, 229, 255, 0.25); }
}

/* === Register Link === */
.register-link {
  text-align: center;
  color: rgba(232, 236, 244, 0.3);
  font-size: 13px;
  margin-top: 6px;

  a {
    color: #00E5FF;
    text-decoration: none;
    transition: all 0.3s;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 1px;
      background: #00E5FF;
      box-shadow: 0 0 4px rgba(0, 229, 255, 0.4);
      transition: width 0.3s ease;
    }

    &:hover::after {
      width: 100%;
    }
  }
}

/* === Footer === */
.footer-text {
  text-align: center;
  font-size: 10px;
  color: rgba(0, 229, 255, 0.25);
  margin-top: 28px;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  .footer-dot {
    width: 3px;
    height: 3px;
    background: rgba(0, 229, 255, 0.3);
    border-radius: 50%;
  }
}

/* === Version Info === */
.version-info {
  position: absolute;
  bottom: 12px;
  right: 20px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 9px;
  color: rgba(0, 229, 255, 0.15);
  letter-spacing: 1px;
  pointer-events: none;
}
</style>
