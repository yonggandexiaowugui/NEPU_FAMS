<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>资产3D场景</span>
          <el-button @click="loadScenes">刷新</el-button>
        </div>
      </template>

      <el-alert
        v-if="!webglSupported"
        title="当前浏览器不支持WebGL，3D场景已自动降级。"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-empty v-else-if="sceneList.length === 0" description="暂无3D场景，请先在后端创建场景布局" />
      <el-table v-else :data="sceneList" border stripe v-loading="loading">
        <el-table-column prop="sceneName" label="场景名称" min-width="160" />
        <el-table-column prop="collegeId" label="学院ID" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="openScene(row)">打开</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const sceneList = ref([])

const webglSupported = computed(() => {
  try {
    const canvas = document.createElement('canvas')
    return !!(window.WebGLRenderingContext && (canvas.getContext('webgl') || canvas.getContext('experimental-webgl')))
  } catch (error) {
    return false
  }
})

async function loadScenes() {
  if (!webglSupported.value) return
  loading.value = true
  try {
    sceneList.value = await request({ url: '/asset-three/scene/list', method: 'get' })
  } catch (error) {
    console.error('Load 3D scenes error:', error)
  } finally {
    loading.value = false
  }
}

function openScene(row) {
  // MVP阶段先验证场景入口和数据链路，后续扩展为真实全景加载。
  ElMessage.info(`打开场景：${row.sceneName}`)
}

onMounted(loadScenes)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
