<template>
  <div class="page-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in overviewStats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">{{ stat.title }}</p>
              <p class="stat-value">{{ stat.value }}</p>
            </div>
            <div class="stat-icon" :style="{ background: stat.bgColor }">
              <el-icon :size="28" :style="{ color: stat.color }">
                <component :is="stat.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><div class="card-header"><span>资产状态分布</span></div></template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><div class="card-header"><span>资产分类统计</span></div></template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><div class="card-header"><span>各学院资产统计</span></div></template>
          <div ref="collegeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><div class="card-header"><span>盘点统计</span></div></template>
          <div ref="inventoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getOverviewStats, getStatusStats, getCategoryStats, getCollegeStats, getInventoryStats } from '@/api/statistics'
import { Box, Money, CircleCheck, Warning } from '@element-plus/icons-vue'

const statusChartRef = ref(null)
const categoryChartRef = ref(null)
const collegeChartRef = ref(null)
const inventoryChartRef = ref(null)

const overviewStats = ref([
  { title: '资产总数', value: 0, icon: 'Box', color: '#3157d5', bgColor: '#eef3ff' },
  { title: '资产总值(元)', value: 0, icon: 'Money', color: '#6d5bd0', bgColor: '#f3f0ff' },
  { title: '在用资产', value: 0, icon: 'CircleCheck', color: '#15803d', bgColor: '#ecfdf5' },
  { title: '闲置资产', value: 0, icon: 'Warning', color: '#b7791f', bgColor: '#fff7ed' }
])

let statusChart = null
let categoryChart = null
let collegeChart = null
let inventoryChart = null

function initCharts() {
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    statusChart.setOption({
      tooltip: { trigger: 'item', backgroundColor: '#ffffff', borderColor: '#e2e8f0', textStyle: { color: '#172033', fontSize: 13 } },
      legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: '#64748b', fontSize: 12 } },
      series: [{ name: '资产状态', type: 'pie', radius: ['40%', '70%'], center: ['35%', '50%'], itemStyle: { borderRadius: 10, borderColor: '#ffffff', borderWidth: 2 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold', color: '#172033' } }, data: [] }]
    })
  }

  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#ffffff', borderColor: '#e2e8f0', textStyle: { color: '#172033', fontSize: 13 } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value', axisLine: { lineStyle: { color: '#e2e8f0' } }, axisTick: { show: false }, axisLabel: { color: '#8792a5', fontSize: 11 }, splitLine: { lineStyle: { color: '#eef2f7' } } },
      yAxis: { type: 'category', data: [], axisLine: { lineStyle: { color: '#e2e8f0' } }, axisTick: { show: false }, axisLabel: { color: '#64748b', fontSize: 12 } },
      series: [{ name: '数量', type: 'bar', barWidth: '60%', itemStyle: { color: '#3157d5', borderRadius: [0, 4, 4, 0] }, data: [] }]
    })
  }

  if (collegeChartRef.value) {
    collegeChart = echarts.init(collegeChartRef.value)
    collegeChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#ffffff', borderColor: '#e2e8f0', textStyle: { color: '#172033', fontSize: 13 } },
      grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
      xAxis: { type: 'category', data: [], axisLabel: { rotate: 30, color: '#8792a5', fontSize: 11 }, axisLine: { lineStyle: { color: '#e2e8f0' } }, axisTick: { show: false } },
      yAxis: { type: 'value', axisLine: { lineStyle: { color: '#e2e8f0' } }, axisTick: { show: false }, axisLabel: { color: '#8792a5', fontSize: 11 }, splitLine: { lineStyle: { color: '#eef2f7' } } },
      series: [{ name: '资产数量', type: 'bar', data: [], itemStyle: { color: '#3157d5', borderRadius: [4, 4, 0, 0] } }]
    })
  }

  if (inventoryChartRef.value) {
    inventoryChart = echarts.init(inventoryChartRef.value)
    inventoryChart.setOption({
      tooltip: { trigger: 'item', backgroundColor: '#ffffff', borderColor: '#e2e8f0', textStyle: { color: '#172033', fontSize: 13 } },
      series: [{ name: '盘点情况', type: 'pie', radius: '60%', data: [], itemStyle: { borderColor: '#ffffff', borderWidth: 2 }, emphasis: { itemStyle: { shadowBlur: 12, shadowOffsetX: 0, shadowColor: 'rgba(30, 41, 59, 0.12)' } } }]
    })
  }
}

async function loadOverview() {
  const res = await getOverviewStats()
  if (!res) return
  overviewStats.value[0].value = res.totalAssetCount || res.totalCount || 0
  overviewStats.value[1].value = (res.totalAssetValue || res.totalValue || 0).toLocaleString()
  overviewStats.value[2].value = res.inUseCount || 0
  overviewStats.value[3].value = res.idleCount || 0
}

async function loadStatusStats() {
  const res = await getStatusStats()
  if (!Array.isArray(res) || !statusChart) return
  const colorMap = { IDLE: '#b7791f', IN_USE: '#15803d', REPAIRING: '#a16207', SCRAPPED: '#be123c', LOSS: '#64748b' }
  const data = res.map(item => ({ value: item.count || 0, name: item.statusName || item.status, itemStyle: { color: colorMap[item.status] || '#3157d5' } }))
  statusChart.setOption({ series: [{ data }] })
}

async function loadCategoryStats() {
  const res = await getCategoryStats()
  if (!Array.isArray(res) || !categoryChart) return
  const categories = res.map(item => item.categoryName || item.name || '未分类')
  const values = res.map(item => item.assetCount || item.count || 0)
  categoryChart.setOption({ yAxis: { data: [...categories].reverse() }, series: [{ data: [...values].reverse() }] })
}

async function loadCollegeStats() {
  const res = await getCollegeStats()
  if (!Array.isArray(res) || !collegeChart) return
  const colleges = res.map(item => item.collegeName || item.name || '未知学院')
  const values = res.map(item => item.assetCount || item.count || 0)
  collegeChart.setOption({ xAxis: { data: colleges }, series: [{ data: values }] })
}

async function loadInventoryStats() {
  const res = await getInventoryStats()
  if (!res || !inventoryChart) return
  const checkedCount = res.checkedCount || 0
  const totalCount = res.totalCount || 0
  const data = [
    { value: checkedCount, name: '已盘点', itemStyle: { color: '#15803d' } },
    { value: Math.max(totalCount - checkedCount, 0), name: '未盘点', itemStyle: { color: '#b7791f' } },
    { value: res.lossCount || 0, name: '盘亏', itemStyle: { color: '#be123c' } }
  ]
  inventoryChart.setOption({ series: [{ data }] })
}

function handleResize() {
  statusChart?.resize()
  categoryChart?.resize()
  collegeChart?.resize()
  inventoryChart?.resize()
}

onMounted(() => {
  initCharts()
  Promise.all([loadOverview(), loadStatusStats(), loadCategoryStats(), loadCollegeStats(), loadInventoryStats()])
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  statusChart?.dispose()
  categoryChart?.dispose()
  collegeChart?.dispose()
  inventoryChart?.dispose()
})
</script>

<style lang="scss" scoped>
.page-container {
  .stats-row { margin-bottom: 22px; }
  .stat-card {
    :deep(.el-card__body) { padding: 18px; }
    .stat-content { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
    .stat-title { color: #64748b; font-size: 13px; font-weight: 600; margin: 0 0 8px 0; }
    .stat-value { color: #172033; font-size: 24px; font-weight: 820; font-family: 'JetBrains Mono', 'Consolas', monospace; font-variant-numeric: tabular-nums; margin: 0; }
    .stat-icon { width: 48px; height: 48px; flex-shrink: 0; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
  }
  .charts-row {
    margin-bottom: 22px;
    .card-header { color: #172033; font-size: 15px; font-weight: 700; }
    .chart-container { height: 300px; width: 100%; }
  }
}
</style>
