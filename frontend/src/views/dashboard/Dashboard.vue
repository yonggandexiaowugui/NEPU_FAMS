<template>
  <div class="dashboard" v-loading="loading">
    <div class="welcome-card">
      <div class="welcome-emblem">
        <div class="welcome-ring wr-outer"></div>
        <div class="welcome-ring wr-middle"></div>
        <div class="welcome-ring wr-inner"></div>
        <span class="welcome-ring-text">NEPU</span>
      </div>
      <div class="welcome-content">
        <h2>欢迎使用固定资产管理系统</h2>
        <p>{{ greeting }}，{{ userName }}！</p>
        <p class="welcome-sub">东北石油大学 · NEPU-FAMS v2.0</p>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="4" v-for="stat in stats" :key="stat.title">
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

    <el-row :gutter="20" class="charts-row">
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store'
import { getOverviewStats, getStatusStats, getCategoryStats, getCollegeStats } from '@/api/statistics'
import { Box, Money, CircleCheck, Warning, Tools, Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const statusChartRef = ref(null)
const categoryChartRef = ref(null)
const collegeChartRef = ref(null)

const userName = computed(() => userStore.userInfo?.name || userStore.userInfo?.username || '用户')

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
  { title: '资产总数', value: '0', icon: 'Box', color: '#00E5FF', bgColor: 'rgba(0, 229, 255, 0.1)', iconColor: '#00E5FF' },
  { title: '资产总值', value: '¥0', icon: 'Money', color: '#FFB800', bgColor: 'rgba(255, 184, 0, 0.1)', iconColor: '#FFB800' },
  { title: '在用资产', value: '0', icon: 'CircleCheck', color: '#FF8A00', bgColor: 'rgba(255, 138, 0, 0.1)', iconColor: '#FF8A00' },
  { title: '闲置资产', value: '0', icon: 'Warning', color: '#00FF94', bgColor: 'rgba(0, 255, 148, 0.1)', iconColor: '#00FF94' },
  { title: '维修中', value: '0', icon: 'Tools', color: '#FFB800', bgColor: 'rgba(255, 184, 0, 0.1)', iconColor: '#FFB800' },
  { title: '已报废', value: '0', icon: 'Delete', color: '#FF2E63', bgColor: 'rgba(255, 46, 99, 0.1)', iconColor: '#FF2E63' }
])

let statusChart = null
let categoryChart = null
let collegeChart = null

async function loadOverview() {
  try {
    const res = await getOverviewStats()
    if (res) {
      stats.value[0].value = res.totalCount || 0
      stats.value[1].value = '¥' + (res.totalValue || 0).toLocaleString()
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
  
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(13, 19, 33, 0.9)',
      borderColor: 'rgba(0, 229, 255, 0.2)',
      textStyle: { color: '#E8ECF4', fontSize: 13 }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: 'rgba(232, 236, 244, 0.7)', fontSize: 12 }
    },
    series: [
      {
        name: '资产状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#080C16',
          borderWidth: 2,
          shadowBlur: 12,
          shadowColor: 'rgba(0, 229, 255, 0.2)'
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold',
            color: '#E8ECF4',
            textShadowBlur: 8,
            textShadowColor: 'rgba(0, 229, 255, 0.5)'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 0, name: '在用', itemStyle: { color: '#FF8A00' } },
          { value: 0, name: '闲置', itemStyle: { color: '#00FF94' } },
          { value: 0, name: '借出', itemStyle: { color: '#00E5FF' } },
          { value: 0, name: '维修中', itemStyle: { color: '#FFB800' } },
          { value: 0, name: '已报废', itemStyle: { color: '#FF2E63' } }
        ]
      }
    ]
  }
  
  statusChart.setOption(option)
}

function initCategoryChart() {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(13, 19, 33, 0.9)',
      borderColor: 'rgba(0, 229, 255, 0.2)',
      textStyle: { color: '#E8ECF4', fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.15)' } },
      axisTick: { show: false },
      axisLabel: { color: 'rgba(232, 236, 244, 0.45)', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.06)' } }
    },
    yAxis: {
      type: 'category',
      data: [],
      axisLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.15)' } },
      axisTick: { show: false },
      axisLabel: { color: 'rgba(232, 236, 244, 0.6)', fontSize: 12 }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: 'rgba(0, 229, 255, 0.3)' },
            { offset: 1, color: '#00E5FF' }
          ]),
          shadowBlur: 8,
          shadowColor: 'rgba(0, 229, 255, 0.3)',
          borderRadius: [0, 4, 4, 0]
        },
        data: []
      }
    ]
  }
  
  categoryChart.setOption(option)
}

function initCollegeChart() {
  if (!collegeChartRef.value) return
  collegeChart = echarts.init(collegeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(13, 19, 33, 0.9)',
      borderColor: 'rgba(0, 229, 255, 0.2)',
      textStyle: { color: '#E8ECF4', fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: [],
      axisLabel: { rotate: 30, fontSize: 11, color: 'rgba(232, 236, 244, 0.5)' },
      axisLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.15)' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.15)' } },
      axisTick: { show: false },
      axisLabel: { color: 'rgba(232, 236, 244, 0.45)', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(0, 229, 255, 0.06)' } }
    },
    series: [
      {
        name: '资产数量',
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00E5FF' },
            { offset: 1, color: 'rgba(0, 229, 255, 0.2)' }
          ]),
          shadowBlur: 8,
          shadowColor: 'rgba(0, 229, 255, 0.3)',
          borderRadius: [4, 4, 0, 0]
        },
        data: []
      }
    ]
  }
  
  collegeChart.setOption(option)
}

async function loadStatusChart() {
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
    console.error('Load status chart error:', error)
  }
}

async function loadCategoryChart() {
  try {
    const res = await getCategoryStats()
    if (res && categoryChart) {
      const list = Array.isArray(res) ? res : []
      const topList = list.slice(0, 8)
      const categories = topList.map(item => item.name)
      const values = topList.map(item => item.count)
      categoryChart.setOption({
        yAxis: { data: categories.reverse() },
        series: [{ data: values.reverse() }]
      })
    }
  } catch (error) {
    console.error('Load category chart error:', error)
  }
}

async function loadCollegeChart() {
  try {
    const res = await getCollegeStats()
    if (res && collegeChart) {
      const list = Array.isArray(res) ? res : []
      const colleges = list.map(item => item.name)
      const values = list.map(item => item.count)
      collegeChart.setOption({
        xAxis: { data: colleges },
        series: [{ data: values }]
      })
    }
  } catch (error) {
    console.error('Load college chart error:', error)
  }
}

function handleResize() {
  statusChart?.resize()
  categoryChart?.resize()
  collegeChart?.resize()
}

async function loadAllData() {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadStatusChart(),
      loadCategoryChart(),
      loadCollegeChart()
    ])
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

  .welcome-card {
    background: linear-gradient(135deg,
      rgba(0, 229, 255, 0.08) 0%,
      rgba(13, 19, 33, 0.85) 40%,
      rgba(255, 184, 0, 0.04) 100%
    );
    border: 1px solid rgba(0, 229, 255, 0.1);
    border-top: 1px solid rgba(0, 229, 255, 0.25);
    border-radius: 12px;
    padding: 28px 32px;
    color: #E8ECF4;
    margin-bottom: 20px;
    backdrop-filter: blur(16px);
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.35),
      inset 0 1px 0 rgba(0, 229, 255, 0.06),
      inset 0 0 40px rgba(0, 229, 255, 0.015);
    position: relative;
    overflow: hidden;
    animation: card-slide-in 0.6s ease-out;
    display: flex;
    align-items: center;
    gap: 28px;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 50%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.03), transparent);
      animation: holographic-shimmer 5s ease-in-out infinite;
      pointer-events: none;
    }

    &::after {
      content: '';
      position: absolute;
      top: -50%;
      right: -20%;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(0, 229, 255, 0.04), transparent 70%);
      pointer-events: none;
    }

    h2 {
      margin: 0 0 8px;
      font-size: 20px;
      font-weight: 600;
      color: #E8ECF4;
      text-shadow: 0 0 8px rgba(0, 229, 255, 0.15);
    }

    p {
      margin: 0;
      font-size: 14px;
      color: rgba(232, 236, 244, 0.55);
    }

    .welcome-sub {
      margin-top: 4px;
      font-size: 11px;
      color: rgba(0, 229, 255, 0.35);
      letter-spacing: 1px;
      font-family: 'JetBrains Mono', monospace;
    }
  }

  /* Welcome card NEPU emblem */
  .welcome-emblem {
    position: relative;
    width: 64px;
    height: 64px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .welcome-ring {
    position: absolute;
    border-radius: 50%;
    border-style: solid;
    border-color: rgba(0, 229, 255, 0.2);

    &.wr-outer {
      width: 64px;
      height: 64px;
      border-width: 1px;
      animation: wr-spin 25s linear infinite;
      border-style: dashed;

      &::before {
        content: '';
        position: absolute;
        top: -3px;
        left: 50%;
        width: 6px;
        height: 6px;
        background: #00E5FF;
        border-radius: 50%;
        box-shadow: 0 0 8px rgba(0, 229, 255, 0.8);
        transform: translateX(-50%);
      }
    }

    &.wr-middle {
      width: 48px;
      height: 48px;
      border-width: 1px;
      border-color: rgba(255, 184, 0, 0.12);
      animation: wr-spin 18s linear infinite reverse;
    }

    &.wr-inner {
      width: 32px;
      height: 32px;
      border-width: 1.5px;
      border-color: rgba(0, 229, 255, 0.35);
      box-shadow: 0 0 12px rgba(0, 229, 255, 0.12), inset 0 0 12px rgba(0, 229, 255, 0.04);
      background: radial-gradient(circle, rgba(0, 229, 255, 0.05), transparent);
      animation: wr-pulse 3s ease-in-out infinite;
    }
  }

  .welcome-ring-text {
    position: relative;
    z-index: 2;
    font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
    font-size: 11px;
    font-weight: 800;
    color: #00E5FF;
    text-shadow: 0 0 8px rgba(0, 229, 255, 0.5);
    letter-spacing: 1px;
    line-height: 1;
  }

  @keyframes wr-spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  @keyframes wr-pulse {
    0%, 100% { box-shadow: 0 0 12px rgba(0, 229, 255, 0.12), inset 0 0 12px rgba(0, 229, 255, 0.04); }
    50% { box-shadow: 0 0 24px rgba(0, 229, 255, 0.25), inset 0 0 24px rgba(0, 229, 255, 0.08); }
  }

  .stats-row {
    margin-bottom: 20px;
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;

    @media (max-width: 1400px) {
      grid-template-columns: repeat(3, 1fr);
    }

    .stat-card {
      border: 1px solid rgba(0, 229, 255, 0.08);
      border-top: 1px solid rgba(0, 229, 255, 0.2);
      border-radius: 12px;
      background: linear-gradient(135deg,
        rgba(10, 15, 28, 0.9) 0%,
        rgba(13, 19, 33, 0.8) 100%
      );
      backdrop-filter: blur(12px);
      box-shadow:
        0 4px 20px rgba(0, 0, 0, 0.3),
        inset 0 1px 0 rgba(0, 229, 255, 0.05);
      transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
      position: relative;
      overflow: hidden;
      cursor: default;

      /* Holographic shimmer */
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -150%;
        width: 50%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.04), transparent);
        transition: left 0.6s ease;
        pointer-events: none;
      }

      &:hover::before {
        left: 150%;
      }

      &:hover {
        border-color: rgba(0, 229, 255, 0.25);
        box-shadow:
          0 12px 40px rgba(0, 0, 0, 0.4),
          0 0 20px rgba(0, 229, 255, 0.1),
          inset 0 1px 0 rgba(0, 229, 255, 0.1);
        transform: translateY(-4px) scale(1.02);
      }

      :deep(.el-card__body) {
        padding: 16px 18px;
      }

      .stat-content {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .stat-info {
          .stat-title {
            color: rgba(232, 236, 244, 0.4);
            font-size: 12px;
            margin: 0 0 10px;
            letter-spacing: 0.5px;
            text-transform: uppercase;
            font-weight: 500;
          }

          .stat-value {
            font-size: 24px;
            font-weight: 700;
            font-family: 'JetBrains Mono', 'Fira Code', monospace;
            color: #E8ECF4;
            margin: 0;
            text-shadow: 0 0 10px rgba(0, 229, 255, 0.2);
            font-variant-numeric: tabular-nums;
          }
        }

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 1px solid rgba(0, 229, 255, 0.06);
          box-shadow: 0 0 15px rgba(0, 229, 255, 0.05);
          transition: all 0.3s ease;
        }

        &:hover .stat-icon {
          box-shadow: 0 0 20px rgba(0, 229, 255, 0.15);
          transform: scale(1.05);
        }
      }
    }

    /* Stagger animation */
    :deep(.el-col) {
      opacity: 0;
      animation: counter-up 0.5s ease-out forwards;
    }

    :deep(.el-col:nth-child(1)) { animation-delay: 0.05s; }
    :deep(.el-col:nth-child(2)) { animation-delay: 0.1s; }
    :deep(.el-col:nth-child(3)) { animation-delay: 0.15s; }
    :deep(.el-col:nth-child(4)) { animation-delay: 0.2s; }
    :deep(.el-col:nth-child(5)) { animation-delay: 0.25s; }
    :deep(.el-col:nth-child(6)) { animation-delay: 0.3s; }
  }

  .charts-row {
    .el-card {
      border: 1px solid rgba(0, 229, 255, 0.08);
      border-top: 1px solid rgba(0, 229, 255, 0.18);
      border-radius: 12px;
      background: linear-gradient(135deg,
        rgba(10, 15, 28, 0.9) 0%,
        rgba(13, 19, 33, 0.8) 100%
      );
      backdrop-filter: blur(12px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;

      &:hover {
        border-color: rgba(0, 229, 255, 0.2);
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 15px rgba(0, 229, 255, 0.08);
      }
    }

    .card-header {
      font-weight: 600;
      color: rgba(232, 236, 244, 0.8);
      font-size: 14px;
      letter-spacing: 0.5px;
    }

    .chart-container {
      height: 300px;
      width: 100%;
    }
  }
}

@keyframes card-slide-in {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes counter-up {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes holographic-shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
