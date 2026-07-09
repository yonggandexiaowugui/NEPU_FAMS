<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <div class="ai-fab" @click="visible = !visible" v-if="!visible">
      <el-icon :size="24"><ChatDotRound /></el-icon>
      <span class="ai-fab-badge">AI</span>
    </div>

    <!-- 聊天面板 -->
    <transition name="ai-slide">
      <div class="ai-panel" v-if="visible">
        <div class="ai-panel-header">
          <div class="ai-panel-title">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            <span>智能助手</span>
          </div>
          <div class="ai-panel-actions">
            <el-icon class="ai-action-btn" @click="clearMessages" title="清空对话"><Delete /></el-icon>
            <el-icon class="ai-action-btn" @click="visible = false" title="关闭"><Close /></el-icon>
          </div>
        </div>

        <div class="ai-panel-body" ref="chatBodyRef">
          <div class="ai-welcome" v-if="messages.length === 0">
            <div class="ai-welcome-icon"><el-icon :size="32"><ChatDotRound /></el-icon></div>
            <p class="ai-welcome-title">您好，我是 NEPU-FAMS 智能助手</p>
            <p class="ai-welcome-desc">我可以帮您查询资产、统计数据、处理审批等</p>
            <div class="ai-suggestions">
              <div class="ai-suggestion-item" v-for="s in suggestions" :key="s" @click="sendMessage(s)">
                {{ s }}
              </div>
            </div>
          </div>

          <div class="ai-message" v-for="(msg, i) in messages" :key="i" :class="msg.role">
            <div class="ai-message-avatar" v-if="msg.role === 'assistant'">AI</div>
            <div class="ai-message-content">
              <div class="ai-message-text" v-html="formatMessage(msg.content)"></div>
            </div>
            <div class="ai-message-avatar user" v-if="msg.role === 'user'">
              {{ userInitial }}
            </div>
          </div>

          <div class="ai-message assistant" v-if="loading">
            <div class="ai-message-avatar">AI</div>
            <div class="ai-message-content">
              <div class="ai-typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="ai-voice-bar">
          <span>{{ voiceStatusText }}</span>
          <el-switch v-model="speechEnabled" size="small" active-text="播报" inactive-text="静音" />
        </div>

        <div class="ai-panel-footer">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入您的问题..."
            resize="none"
            @keydown.enter.exact.prevent="handleEnter"
            :disabled="loading"
          />
          <el-button
            class="ai-voice-btn"
            :class="{ listening: isListening }"
            :type="isListening ? 'danger' : 'default'"
            @click="toggleVoiceInput"
            :disabled="loading || !speechSupported"
            :title="speechSupported ? '语音输入' : '当前浏览器不支持语音识别'"
          >
            <el-icon><Microphone /></el-icon>
          </el-button>
          <el-button
            class="ai-send-btn"
            type="primary"
            :loading="loading"
            @click="handleSend"
            :disabled="!inputText.trim()"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onUnmounted } from 'vue'
import { useUserStore } from '@/store'
import { ChatDotRound, Delete, Close, Promotion, Microphone } from '@element-plus/icons-vue'

const userStore = useUserStore()
const visible = ref(false)
const inputText = ref('')
const loading = ref(false)
const chatBodyRef = ref(null)
const messages = ref([])
const isListening = ref(false)
const speechEnabled = ref(true)
const speechRecognition = ref(null)

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
const speechSupported = Boolean(SpeechRecognition)

const voiceStatusText = computed(() => {
  if (!speechSupported) return '当前浏览器不支持语音识别'
  if (isListening.value) return '正在聆听，请说出您的问题'
  return '支持语音输入，AI 回复可自动播报'
})

const userInitial = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const suggestions = [
  '查询资产总览统计',
  '查询闲置资产列表',
  '按学院统计资产分布',
  '查询待审批的领用申请'
]

function handleEnter() {
  if (inputText.value.trim() && !loading.value) {
    handleSend()
  }
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true

  await scrollToBottom()

  try {
    const token = userStore.token
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `${token}` : ''
      },
      body: JSON.stringify({ message: text })
    })

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }

    const data = await response.json()
    const reply = data.data || data.msg || data.message || data.content || '暂无回复'
    messages.value.push({ role: 'assistant', content: reply })
    speakText(reply)
  } catch (err) {
    messages.value.push({
      role: 'assistant',
      content: `抱歉，处理请求时出错：${err.message}。请确保 AI 服务已正确配置。`
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function sendMessage(text) {
  inputText.value = text
  await handleSend()
}

function clearMessages() {
  messages.value = []
  stopSpeaking()
}

function toggleVoiceInput() {
  if (!speechSupported) return
  if (isListening.value) {
    speechRecognition.value?.stop()
    return
  }
  startVoiceInput()
}

function startVoiceInput() {
  const recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.continuous = false
  recognition.interimResults = true

  let finalText = ''

  recognition.onstart = () => {
    isListening.value = true
    stopSpeaking()
  }

  recognition.onresult = (event) => {
    let interimText = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      const text = event.results[i][0].transcript
      if (event.results[i].isFinal) {
        finalText += text
      } else {
        interimText += text
      }
    }
    inputText.value = finalText || interimText
  }

  recognition.onerror = () => {
    isListening.value = false
  }

  recognition.onend = async () => {
    isListening.value = false
    speechRecognition.value = null
    if (finalText.trim() && !loading.value) {
      inputText.value = finalText.trim()
      await handleSend()
    }
  }

  speechRecognition.value = recognition
  recognition.start()
}

function speakText(text) {
  if (!speechEnabled.value || !window.speechSynthesis || !text) return
  stopSpeaking()
  const utterance = new SpeechSynthesisUtterance(String(text).replace(/<[^>]+>/g, ''))
  utterance.lang = 'zh-CN'
  utterance.rate = 1
  utterance.pitch = 1
  window.speechSynthesis.speak(utterance)
}

function stopSpeaking() {
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
}

onUnmounted(() => {
  speechRecognition.value?.stop()
  stopSpeaking()
})

async function scrollToBottom() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

function formatMessage(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
}
</script>

<style lang="scss" scoped>
.ai-assistant {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

.ai-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3157d5 0%, #1e3a8a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(49, 87, 213, 0.35);
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 12px 32px rgba(49, 87, 213, 0.45);
  }

  .el-icon {
    color: #fff;
  }

  .ai-fab-badge {
    position: absolute;
    top: -2px;
    right: -2px;
    background: #ff4757;
    color: #fff;
    font-size: 10px;
    font-weight: 700;
    padding: 1px 5px;
    border-radius: 999px;
    line-height: 1.4;
  }
}

.ai-panel {
  width: 380px;
  height: 560px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-panel-header {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: linear-gradient(135deg, #3157d5 0%, #1e3a8a 100%);
  color: #fff;
  flex-shrink: 0;
}

.ai-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
}

.ai-panel-actions {
  display: flex;
  gap: 12px;

  .ai-action-btn {
    cursor: pointer;
    font-size: 16px;
    opacity: 0.85;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }
  }
}

.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 999px;
  }
}

.ai-welcome {
  text-align: center;
  padding: 32px 16px;

  .ai-welcome-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    border-radius: 50%;
    background: #eef3ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #3157d5;
  }

  .ai-welcome-title {
    font-size: 15px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 6px;
  }

  .ai-welcome-desc {
    font-size: 13px;
    color: #64748b;
    margin: 0 0 20px;
  }
}

.ai-suggestions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-suggestion-item {
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #3157d5;
    color: #3157d5;
    background: #eef3ff;
  }
}

.ai-message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;

  &.user {
    flex-direction: row-reverse;
  }
}

.ai-message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3157d5 0%, #1e3a8a 100%);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.user {
    background: linear-gradient(135deg, #64748b 0%, #334155 100%);
  }
}

.ai-message-content {
  max-width: 260px;
}

.ai-message-text {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;

  .assistant & {
    background: #fff;
    border: 1px solid #e2e8f0;
    color: #1e293b;
    border-top-left-radius: 4px;
  }

  .user & {
    background: #3157d5;
    color: #fff;
    border-top-right-radius: 4px;
  }
}

.ai-typing {
  display: flex;
  gap: 4px;
  padding: 12px 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  border-top-left-radius: 4px;
  width: fit-content;

  span {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #94a3b8;
    animation: ai-bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes ai-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.ai-voice-bar {
  min-height: 34px;
  padding: 8px 12px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
  background: #fff;
  border-top: 1px solid #e2e8f0;
}

.ai-panel-footer {
  padding: 10px 12px 12px;
  display: flex;
  gap: 8px;
  align-items: flex-end;
  border-top: 1px solid #e2e8f0;
  background: #fff;
  flex-shrink: 0;

  :deep(.el-textarea__inner) {
    font-size: 13px;
    border-radius: 10px;
    padding: 8px 12px;
  }
}

.ai-voice-btn,
.ai-send-btn {
  height: 40px;
  width: 40px;
  border-radius: 10px;
  padding: 0;
  flex-shrink: 0;

  .el-icon {
    font-size: 16px;
  }
}

.ai-voice-btn.listening {
  animation: voice-pulse 1.2s infinite;
}

@keyframes voice-pulse {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.42); }
  70% { box-shadow: 0 0 0 8px rgba(245, 108, 108, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}

.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: all 0.3s ease;
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
