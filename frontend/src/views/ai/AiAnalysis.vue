<template>
  <div class="ai-analysis">
    <section class="hero-card">
      <div>
        <p class="kicker">AI INSIGHT CENTER</p>
        <h2>智能分析中心</h2>
        <p class="desc">将灵梭 Agent 融入固定资产管理业务，自动生成资产运行、闲置、维修、报废与盘点分析。</p>
      </div>
      <el-button type="primary" size="large" @click="openLoomWorkbench">
        打开 AI 工作台
      </el-button>
    </section>

    <el-row :gutter="20" class="scenario-row">
      <el-col :span="8" v-for="item in scenarios" :key="item.title">
        <el-card class="scenario-card" shadow="hover" @click="runAnalysis(item.prompt)">
          <div class="scenario-icon" :style="{ background: item.bg, color: item.color }">
            <el-icon :size="26"><component :is="item.icon" /></el-icon>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
          <span>点击生成分析</span>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="result-header">
          <span>AI 分析结果</span>
          <el-button text type="primary" @click="runAnalysis(defaultPrompt)" :loading="loading">重新生成</el-button>
        </div>
      </template>
      <div v-if="!analysis && !loading" class="empty-state">
        <p>选择上方分析场景，Agent 将调用系统业务工具生成管理建议。</p>
      </div>
      <div v-else-if="loading" class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>
      <div v-else class="analysis-content" v-html="formatMessage(analysis)"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { DataAnalysis, Warning, Tools, Delete, Files, TrendCharts } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const analysis = ref('')

const defaultPrompt = '请基于当前 NEPU-FAMS 系统真实数据，生成一份固定资产运行分析报告，包含资产总览、状态分布、学院分布、分类结构、风险提醒和管理建议。'

const scenarios = [
  {
    title: '资产运行分析',
    desc: '综合资产数量、价值、状态、学院与分类数据，生成管理层概览。',
    icon: DataAnalysis,
    bg: '#eef3ff',
    color: '#3157d5',
    prompt: defaultPrompt
  },
  {
    title: '闲置资产分析',
    desc: '分析闲置资产规模、占比和可调拨方向，辅助提升使用效率。',
    icon: Warning,
    bg: '#fff7ed',
    color: '#b7791f',
    prompt: '请查询并分析当前闲置资产情况，说明闲置规模、可能风险、调拨建议和优先处理方向。'
  },
  {
    title: '维修风险分析',
    desc: '汇总维修工单与维修中资产，识别需要跟进的设备风险。',
    icon: Tools,
    bg: '#fef3c7',
    color: '#a16207',
    prompt: '请查询维修工单和维修中资产，分析维修风险、超期隐患和后续处理建议。'
  },
  {
    title: '报废建议报告',
    desc: '结合状态、价值和使用情况，生成资产报废管理建议。',
    icon: Delete,
    bg: '#fff1f2',
    color: '#be123c',
    prompt: '请基于当前资产状态和业务数据，生成一份报废建议报告，包含建议关注对象、报废风险和审批注意事项。'
  },
  {
    title: '盘点总结报告',
    desc: '面向盘点任务和盘点结果，生成差异分析与整改建议。',
    icon: Files,
    bg: '#ecfdf5',
    color: '#15803d',
    prompt: '请结合盘点相关数据，生成盘点总结报告，包含盘点进展、异常风险和整改建议。'
  },
  {
    title: '配置均衡分析',
    desc: '对比各学院资产分布，识别配置不均衡和资源优化空间。',
    icon: TrendCharts,
    bg: '#f3f0ff',
    color: '#6d5bd0',
    prompt: '请按学院和分类分析资产配置是否均衡，指出资源集中、闲置和优化调拨建议。'
  }
]

async function runAnalysis(prompt) {
  if (loading.value) return
  loading.value = true
  analysis.value = ''
  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': userStore.token || ''
      },
      body: JSON.stringify({ message: prompt })
    })
    const data = await response.json()
    if (!response.ok || data.code !== 200) {
      throw new Error(data.msg || `请求失败: ${response.status}`)
    }
    analysis.value = data.data || '暂无分析结果'
  } catch (error) {
    ElMessage.error(error.message || 'AI 分析失败')
  } finally {
    loading.value = false
  }
}

function openLoomWorkbench() {
  window.open('/spring/ai/loom/index.html', '_blank')
}

function formatMessage(text) {
  return String(text || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
}
</script>

<style lang="scss" scoped>
.ai-analysis {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 30px 34px;
  border: 1px solid #dfe6f1;
  border-radius: 16px;
  background:
    radial-gradient(circle at 88% 18%, rgba(49, 87, 213, 0.14), transparent 28%),
    linear-gradient(135deg, #ffffff 0%, #f7faff 100%);
  box-shadow: 0 18px 46px rgba(30, 41, 59, 0.08);
}

.kicker {
  margin: 0 0 8px;
  color: #3157d5;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.hero-card h2 {
  margin: 0 0 10px;
  color: #172033;
  font-size: 28px;
  font-weight: 820;
}

.desc {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.scenario-row {
  row-gap: 20px;
}

.scenario-card {
  height: 100%;
  cursor: pointer;
  border-radius: 14px;
  transition: transform 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 38px rgba(30, 41, 59, 0.12);
  }
}

.scenario-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  margin-bottom: 18px;
}

.scenario-card h3 {
  margin: 0 0 10px;
  color: #172033;
  font-size: 17px;
}

.scenario-card p {
  min-height: 48px;
  margin: 0 0 16px;
  color: #64748b;
  line-height: 1.7;
}

.scenario-card span {
  color: #3157d5;
  font-size: 13px;
  font-weight: 700;
}

.result-card {
  border-radius: 14px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 800;
  color: #172033;
}

.empty-state {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8792a5;
}

.loading-state {
  padding: 12px 0;
}

.analysis-content {
  min-height: 220px;
  color: #263244;
  line-height: 1.9;
  white-space: normal;
}
</style>
