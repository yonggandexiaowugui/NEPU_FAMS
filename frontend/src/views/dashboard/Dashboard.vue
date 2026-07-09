<template>
  <div class="dashboard" v-loading="loading">
    <div class="welcome-card">
      <div class="welcome-content">
        <p class="welcome-kicker">NEPU-FAMS</p>
        <h2>固定资产管理系统</h2>
        <p>{{ greeting }}，{{ userName }}。这里汇总资产状态、价值分布与学院使用情况。</p>
      </div>
      <div class="welcome-aside">
        <span>今日概览</span>
        <strong>{{ new Date().toLocaleDateString() }}</strong>
      </div>
    </div>

    <section class="ai-insight-card">
      <div class="ai-insight-main">
        <p class="ai-kicker">AI 智能洞察</p>
        <div v-if="aiInsightLoading" class="ai-loading">Agent 正在分析当前资产数据...</div>
        <div v-else class="ai-insight-text" v-html="formatMessage(aiInsight || defaultInsight)"></div>
      </div>
      <el-button type="primary" plain :loading="aiInsightLoading" @click="generateAiInsight">
        生成 AI 洞察
      </el-button>
    </section>

    <el-row :gutter="24" class="stats-row">
      <el-col :span="8" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">{{ stat.title }}</p>
              <p class="stat-value">{{ stat.value }}</p>
            </div>
            <div class="stat-icon" :style="{ background: stat.bgColor }">
              <el-icon :size="32" :style="{ color: stat.iconColor }">
                <component :is="stat.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="charts-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>资产状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>资产分类统计</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>各学院资产统计</span>
            </div>
          </template>
          <div ref="collegeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <section class="showcase-section">
      <Asset3DShowcase />
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { getOverviewStats, getStatusStats, getCategoryStats, getCollegeStats } from '@/api/statistics'
import { Box, Money, CircleCheck, Warning, Tools, Delete } from '@element-plus/icons-vue'
import Asset3DShowcase from '@/components/3d/Asset3DShowcase.vue'

const userStore = useUserStore()
const loading = ref(false)
const statusChartRef = ref(null)
const categoryChartRef = ref(null)
const collegeChartRef = ref(null)
const aiInsight = ref('')
const aiInsightLoading = ref(false)

const defaultInsight = '点击“生成 AI 洞察”，Agent 将结合资产总览、状态分布、学院分布和分类数据，自动输出当前资产管理建议。'

const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '用户')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

const stats = ref([
  { title: '资产总数', value: '0', icon: 'Box', bgColor: '#eef3ff', iconColor: '#3157d5' },
  { title: '资产总值', value: '¥0', icon: 'Money', bgColor: '#f3f0ff', iconColor: '#6d5bd0' },
  { title: '在用资产', value: '0', icon: 'CircleCheck', bgColor: '#ecfdf5', iconColor: '#15803d' },
  { title: '闲置资产', value: '0', icon: 'Warning', bgColor: '#fff7ed', iconColor: '#b7791f' },
  { title: '维修中', value: '0', icon: 'Tools', bgColor: '#fef3c7', iconColor: '#a16207' },
  { title: '已报废', value: '0', icon: 'Delete', bgColor: '#fff1f2', iconColor: '#be123c' }
])

let statusChart = null
let categoryChart = null
let collegeChart = null

async function loadOverview() {
  try {
    const res = await getOverviewStats()
    if (res) {
      stats.value[0].value = res.totalAssetCount || res.totalCount || 0
      stats.value[1].value = '¥' + (res.totalAssetValue || res.totalValue || 0).toLocaleString()
      stats.value[2].value = res.inUseCount || 0
      stats.value[3].value = res.idleCount || 0
      stats.value[4].value = res.repairingCount || 0
      stats.value[5].value = res.scrappedCount || 0
    }
  } catch (error) {
    console.error('Load overview error:', error)
  }
}

function initStatusChart() {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: '#64748b', fontSize: 12 } },
    series: [{
      name: '资产状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      itemStyle: { borderRadius: 10, borderColor: '#ffffff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
      labelLine: { show: false },
      data: []
    }]
  })
}

function initCategoryChart() {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: [] },
    series: [{
      name: '数量',
      type: 'bar',
      barWidth: '60%',
      itemStyle: { color: '#3157d5', borderRadius: [0, 4, 4, 0] },
      data: []
    }]
  })
}

function initCollegeChart() {
  if (!collegeChartRef.value) return
  collegeChart = echarts.init(collegeChartRef.value)
  collegeChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', data: [], axisLabel: { rotate: 30, fontSize: 11 } },
    yAxis: { type: 'value' },
    series: [{
      name: '资产数量',
      type: 'bar',
      barWidth: '50%',
      itemStyle: { color: '#3157d5', borderRadius: [4, 4, 0, 0] },
      data: []
    }]
  })
}

async function loadStatusChart() {
  try {
    const res = await getStatusStats()
    if (Array.isArray(res) && statusChart) {
      const colorMap = {
        IDLE: '#f59e0b',
        IN_USE: '#16a34a',
        REPAIRING: '#2563eb',
        SCRAPPED: '#e11d48',
        LOSS: '#64748b'
      }
      const data = res.map(item => ({
        value: item.count || 0,
        name: item.statusName || item.status,
        itemStyle: { color: colorMap[item.status] || '#3157d5' }
      }))
      statusChart.setOption({ series: [{ data }] })
    }
  } catch (error) {
    console.error('Load status chart error:', error)
  }
}

async function loadCategoryChart() {
  try {
    const res = await getCategoryStats()
    if (Array.isArray(res) && categoryChart) {
      const list = res.slice(0, 8)
      const categories = list.map(item => item.categoryName || item.name || '未分类')
      const values = list.map(item => item.assetCount || item.count || 0)
      categoryChart.setOption({
        yAxis: { data: [...categories].reverse() },
        series: [{ data: [...values].reverse() }]
      })
    }
  } catch (error) {
    console.error('Load category chart error:', error)
  }
}

async function loadCollegeChart() {
  try {
    const res = await getCollegeStats()
    if (Array.isArray(res) && collegeChart) {
      const colleges = res.map(item => item.collegeName || item.name || '未知学院')
      const values = res.map(item => item.assetCount || item.count || 0)
      collegeChart.setOption({
        xAxis: { data: colleges },
        series: [{ data: values }]
      })
    }
  } catch (error) {
    console.error('Load college chart error:', error)
  }
}

async function generateAiInsight() {
  if (aiInsightLoading.value) return
  aiInsightLoading.value = true
  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': userStore.token || ''
      },
      body: JSON.stringify({
        message: '请基于当前 NEPU-FAMS 固定资产系统真实数据，生成首页 AI 智能洞察。要求包含资产总览、闲置风险、维修风险、学院/分类分布特点和 3 条管理建议，回答简洁。'
      })
    })
    const data = await response.json()
    if (!response.ok || data.code !== 200) {
      throw new Error(data.msg || `请求失败: ${response.status}`)
    }
    aiInsight.value = data.data || defaultInsight
  } catch (error) {
    ElMessage.error(error.message || 'AI 洞察生成失败')
  } finally {
    aiInsightLoading.value = false
  }
}

function formatMessage(text) {
  return String(text || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
}

function handleResize() {
  statusChart?.resize()
  categoryChart?.resize()
  collegeChart?.resize()
}

async function loadAllData() {
  loading.value = true
  try {
    await Promise.all([loadOverview(), loadStatusChart(), loadCategoryChart(), loadCollegeChart()])
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initStatusChart()
  initCategoryChart()
  initCollegeChart()
  loadAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  statusChart?.dispose()
  categoryChart?.dispose()
  collegeChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  position: relative;
  z-index: 1;
}

.welcome-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  padding: 28px 32px;
  border: 1px solid #dfe6f1;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(247, 250, 255, 0.92));
  box-shadow: 0 18px 46px rgba(30, 41, 59, 0.08);
}

.welcome-kicker {
  margin: 0 0 8px;
  color: #3157d5;
  font-size: 12px;
  font-weight: 800;
}

.welcome-card h2 {
  margin: 0 0 10px;
  color: #172033;
  font-size: 26px;
  font-weight: 820;
}

.welcome-card p {
  margin: 0;
  max-width: 680px;
  color: #64748b;
  line-height: 1.7;
}

.welcome-aside {
  min-width: 160px;
  padding: 16px 18px;
  border-left: 1px solid #e2e8f0;
  text-align: right;
}

.welcome-aside span {
  display: block;
  color: #8792a5;
  font-size: 12px;
  margin-bottom: 6px;
}

.welcome-aside strong {
  color: #172033;
  font-size: 16px;
}

.ai-insight-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
  padding: 22px 24px;
  border: 1px solid #dfe6f1;
  border-radius: 14px;
  background:
    radial-gradient(circle at 92% 18%, rgba(49, 87, 213, 0.16), transparent 26%),
    linear-gradient(135deg, #ffffff 0%, #f7faff 100%);
  box-shadow: 0 14px 34px rgba(30, 41, 59, 0.07);
}

.ai-insight-main {
  min-width: 0;
}

.ai-kicker {
  margin: 0 0 8px;
  color: #3157d5;
  font-size: 12px;
  font-weight: 800;
}

.ai-insight-text,
.ai-loading {
  color: #334155;
  line-height: 1.8;
}

.stats-row {
  margin-bottom: 34px;
  row-gap: 24px;
}

.stat-card {
  cursor: default;

  :deep(.el-card__body) {
    padding: 22px 24px;
  }

  .stat-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    min-height: 74px;
  }

  .stat-title {
    margin: 0 0 10px;
    color: #8792a5;
    font-size: 12px;
    font-weight: 700;
  }

  .stat-value {
    margin: 0;
    color: #172033;
    font-size: 26px;
    font-weight: 820;
    font-family: 'JetBrains Mono', 'Consolas', monospace;
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
}

.stats-row :deep(.el-col) {
  opacity: 1;

  @media (max-width: 1200px) {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

.charts-row {
  row-gap: 24px;

  :deep(.el-card__header) {
    padding: 18px 20px;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  .card-header {
    font-weight: 700;
    color: #172033;
    font-size: 14px;
  }

  .chart-container {
    height: 340px;
    width: 100%;
  }
}

.showcase-section {
  margin-top: 8px;
}

</style>
