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
          <template #header>
            <div class="card-header">
              <span>资产状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>资产分类统计</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>各学院资产统计</span>
            </div>
          </template>
          <div ref="collegeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>盘点统计</span>
            </div>
          </template>
          <div ref="inventoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getOverviewStats, getStatusStats, getCategoryStats, getCollegeStats, getInventoryStats } from '@/api/statistics'
import { Box, Money, CircleCheck, Warning } from '@element-plus/icons-vue'

const statusChartRef = ref(null)
const categoryChartRef = ref(null)
const collegeChartRef = ref(null)
const inventoryChartRef = ref(null)

const overviewStats = ref([
  { title: '资产总数', value: 0, icon: 'Box', color: '#00E5FF', bgColor: 'rgba(0, 229, 255, 0.1)' },
  { title: '资产总值(元)', value: 0, icon: 'Money', color: '#FFB800', bgColor: 'rgba(255, 184, 0, 0.1)' },
  { title: '在用资产', value: 0, icon: 'CircleCheck', color: '#FF8A00', bgColor: 'rgba(255, 138, 0, 0.1)' },
  { title: '闲置资产', value: 0, icon: 'Warning', color: '#00FF94', bgColor: 'rgba(0, 255, 148, 0.1)' }
])

let statusChart = null
let categoryChart = null
let collegeChart = null
let inventoryChart = null

function initCharts() {
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    const option = {
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      series: [{
        name: '资产状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
        data: []
      }]
    }
    statusChart.setOption(option)
  }

  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: [] },
      series: [{
        name: '数量',
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: 'rgba(0, 229, 255, 0.3)' },
            { offset: 1, color: '#00E5FF' }
          ])
        },
        data: []
      }]
    }
    categoryChart.setOption(option)
  }

  if (collegeChartRef.value) {
    collegeChart = echarts.init(collegeChartRef.value)
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: [], axisLabel: { rotate: 30 } },
      yAxis: { type: 'value' },
      series: [{
        name: '资产数量',
        type: 'bar',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00E5FF' },
            { offset: 1, color: 'rgba(0, 229, 255, 0.2)' }
          ])
        }
      }]
    }
    collegeChart.setOption(option)
  }

  if (inventoryChartRef.value) {
    inventoryChart = echarts.init(inventoryChartRef.value)
    const option = {
      tooltip: { trigger: 'item' },
      series: [{
        name: '盘点情况',
        type: 'pie',
        radius: '60%',
        data: [],
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
      }]
    }
    inventoryChart.setOption(option)
  }
}

async function loadOverview() {
  try {
    const res = await getOverviewStats()
    if (res) {
      overviewStats.value[0].value = res.totalCount || 0
      overviewStats.value[1].value = (res.totalValue || 0).toLocaleString()
      overviewStats.value[2].value = res.inUseCount || 0
      overviewStats.value[3].value = res.idleCount || 0
    }
  } catch (error) {
    console.error('Load overview error:', error)
  }
}

async function loadStatusStats() {
  try {
    const res = await getStatusStats()
    if (res && statusChart) {
      const data = [
        { value: res.inUse || 0, name: '在用', itemStyle: { color: '#FF8A00' } },
        { value: res.idle || 0, name: '闲置', itemStyle: { color: '#00FF94' } },
        { value: res.borrowed || 0, name: '借出', itemStyle: { color: '#00E5FF' } },
        { value: res.repairing || 0, name: '维修中', itemStyle: { color: '#FFB800' } },
        { value: res.scrapped || 0, name: '已报废', itemStyle: { color: '#FF2E63' } }
      ]
      statusChart.setOption({ series: [{ data }] })
    }
  } catch (error) {
    console.error('Load status stats error:', error)
  }
}

async function loadCategoryStats() {
  try {
    const res = await getCategoryStats()
    if (res && categoryChart) {
      const categories = res.map(item => item.name)
      const values = res.map(item => item.count)
      categoryChart.setOption({
        yAxis: { data: categories.reverse() },
        series: [{ data: values.reverse() }]
      })
    }
  } catch (error) {
    console.error('Load category stats error:', error)
  }
}

async function loadCollegeStats() {
  try {
    const res = await getCollegeStats()
    if (res && collegeChart) {
      const colleges = res.map(item => item.name)
      const values = res.map(item => item.count)
      collegeChart.setOption({
        xAxis: { data: colleges },
        series: [{ data: values }]
      })
    }
  } catch (error) {
    console.error('Load college stats error:', error)
  }
}

async function loadInventoryStats() {
  try {
    const res = await getInventoryStats()
    if (res && inventoryChart) {
      const data = [
        { value: res.checked || 0, name: '已盘点', itemStyle: { color: '#00FF94' } },
        { value: res.unchecked || 0, name: '未盘点', itemStyle: { color: '#FFB800' } },
        { value: res.diff || 0, name: '盘亏', itemStyle: { color: '#FF2E63' } }
      ]
      inventoryChart.setOption({ series: [{ data }] })
    }
  } catch (error) {
    console.error('Load inventory stats error:', error)
  }
}

function handleResize() {
  statusChart?.resize()
  categoryChart?.resize()
  collegeChart?.resize()
  inventoryChart?.resize()
}

onMounted(() => {
  initCharts()
  loadOverview()
  loadStatusStats()
  loadCategoryStats()
  loadCollegeStats()
  loadInventoryStats()
  window.addEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.page-container {
  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      :deep(.el-card__body) {
        padding: 20px;
      }

      .stat-content {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .stat-info {
          .stat-title {
            color: #909399;
            font-size: 14px;
            margin: 0 0 8px 0;
          }

          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin: 0;
          }
        }

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;

    .card-header {
      font-weight: 600;
    }

    .chart-container {
      height: 300px;
      width: 100%;
    }
  }
}
</style>
