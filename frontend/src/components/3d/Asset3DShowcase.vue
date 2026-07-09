<template>
  <div class="asset-3d-showcase" :class="{ 'is-floating': floating }">
    <div class="showcase-header">
      <div>
        <h3 class="showcase-title">
          <el-icon class="title-icon"><MagicStick /></el-icon>
          3D 资产展厅
        </h3>
        <p class="showcase-subtitle">
          以三维视角浏览已建模的资产，点击卡片查看详情、申请领用或报废。
        </p>
      </div>
      <div class="showcase-actions">
        <el-button v-if="floating" link size="small" @click="$emit('close')">
          <el-icon><Close /></el-icon>
        </el-button>
        <el-tag v-else round type="info" effect="plain">共 {{ assets.length }} 件</el-tag>
      </div>
    </div>

    <div v-if="loading" class="showcase-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载 3D 资产...</span>
    </div>

    <el-empty
      v-else-if="!assets.length"
      description="暂无可展示的 3D 资产"
      :image-size="100"
    />

    <div v-else class="showcase-grid" ref="gridRef">
      <article
        v-for="item in assets"
        :key="item.id"
        class="asset-card"
        :class="`status-${item.status?.toLowerCase()}`"
        @click="openDetail(item)"
        @mouseenter="schedulePreview(item.id)"
        @mouseleave="releasePreview(item.id)"
      >
        <div class="card-preview">
          <div
            class="card-preview-canvas"
            :ref="el => bindCanvasRef(item.id, el)"
            v-show="activePreviewId === item.id"
          />
          <div
            v-show="activePreviewId !== item.id"
            class="card-preview-placeholder"
            :style="placeholderStyle(item)"
          >
            <el-icon class="placeholder-icon"><Box /></el-icon>
            <span class="placeholder-hint">悬停预览 3D</span>
          </div>
          <el-tag
            class="status-tag"
            :type="statusTagType(item.status)"
            effect="dark"
            size="small"
          >
            {{ item.statusName || statusText(item.status) }}
          </el-tag>
        </div>
        <div class="card-info">
          <h4 class="card-title" :title="item.name">{{ item.name }}</h4>
          <p class="card-no">编号：{{ item.assetNo }}</p>
          <p class="card-meta">
            <span>{{ item.categoryName || '未分类' }}</span>
            <span class="dot">·</span>
            <span>{{ item.collegeName || '未指定学院' }}</span>
          </p>
          <div class="card-bottom">
            <span class="card-price">¥ {{ formatMoney(item.purchasePrice) }}</span>
          </div>
        </div>
      </article>
    </div>

    <!-- 资产详情弹层 -->
    <el-dialog
      v-model="detailVisible"
      :title="detailTitle"
      width="960px"
      append-to-body
      destroy-on-close
      class="asset-3d-detail-dialog"
    >
      <div v-if="currentAsset" class="detail-layout">
        <div class="detail-canvas">
          <AssetModelPreview
            v-if="detailVisible"
            :model-url="currentAsset.modelUrl"
            :visible="detailVisible"
          />
        </div>
        <div class="detail-info">
          <div class="info-head">
            <h3>{{ currentAsset.name }}</h3>
            <el-tag :type="statusTagType(currentAsset.status)" effect="dark">
              {{ currentAsset.statusName || statusText(currentAsset.status) }}
            </el-tag>
          </div>
          <p class="info-no">编号：{{ currentAsset.assetNo }}</p>
          <el-descriptions :column="1" border size="small" class="info-table">
            <el-descriptions-item label="分类">{{ currentAsset.categoryName || '未分类' }}</el-descriptions-item>
            <el-descriptions-item label="所属学院">{{ currentAsset.collegeName || '未指定' }}</el-descriptions-item>
            <el-descriptions-item label="存放位置">{{ currentAsset.location || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="购置价值">¥ {{ formatMoney(currentAsset.purchasePrice) }}</el-descriptions-item>
            <el-descriptions-item label="3D 模型">
              <span v-if="currentAsset.modelUrl" class="model-status ready">已建模</span>
              <span v-else class="model-status none">未建模</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="info-actions">
            <el-button
              type="primary"
              :icon="Promotion"
              :disabled="!canBorrow"
              @click="onApplyBorrow(currentAsset)"
            >
              申请领用
            </el-button>
            <el-button
              type="warning"
              :icon="Delete"
              :disabled="!canScrap"
              @click="onApplyScrap(currentAsset)"
            >
              申请报废
            </el-button>
            <el-button
              type="success"
              :icon="Tools"
              :disabled="!canRepair"
              @click="onApplyRepair(currentAsset)"
            >
              提交报修
            </el-button>
          </div>
          <p class="action-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ actionTip }}</span>
          </p>
        </div>
      </div>
    </el-dialog>

    <!-- 申请领用 / 报废 / 报修原因输入弹窗 -->
    <el-dialog
      v-model="applyDialogVisible"
      :title="applyDialogTitle"
      width="540px"
      append-to-body
    >
      <el-form :model="applyForm" label-width="90px">
        <el-form-item label="资产">
          <span class="apply-asset-name">{{ applyForm.assetName }}</span>
        </el-form-item>
        <el-form-item :label="applyForm.reasonLabel" required>
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="applyForm.placeholder"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="applyForm.type === 'borrow'" label="预计归还" required>
          <el-date-picker
            v-model="applyForm.expectedReturnDate"
            type="date"
            placeholder="请选择预计归还日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            :disabled-date="(d) => d && d < new Date()"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="applySubmitting"
          :disabled="!canSubmitApply"
          @click="submitApply"
        >
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  Box,
  Close,
  Delete,
  InfoFilled,
  Loading,
  MagicStick,
  Promotion,
  Tools
} from '@element-plus/icons-vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import AssetModelPreview from '@/components/3d/AssetModelPreview.vue'
import { listModelAssets } from '@/api/assetThree'
import { applyBorrow } from '@/api/borrow'
import { applyScrap } from '@/api/scrap'
import { submitRepair } from '@/api/repair'

defineProps({
  floating: {
    type: Boolean,
    default: false
  }
})
defineEmits(['close'])

const router = useRouter()
const assets = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentAsset = ref(null)

// 缩略图渲染状态：仅当 hover 时才实例化 Three.js，离开后保留 1.2s 再释放
const activePreviewId = ref(null)
const canvasRefs = new Map()
const previewTimers = new Map()
const previewEngines = new Map()

function bindCanvasRef(id, el) {
  if (el) canvasRefs.set(id, el)
  else canvasRefs.delete(id)
}

const detailTitle = computed(() => currentAsset.value ? `资产详情 · ${currentAsset.value.name}` : '资产详情')

const canBorrow = computed(() => {
  const s = currentAsset.value?.status
  return s === 'IDLE'
})
const canScrap = computed(() => {
  const s = currentAsset.value?.status
  return s === 'IDLE' || s === 'IN_USE' || s === 'REPAIRING'
})
const canRepair = computed(() => {
  const s = currentAsset.value?.status
  return s === 'IN_USE' || s === 'IDLE'
})
const actionTip = computed(() => {
  if (!currentAsset.value) return ''
  switch (currentAsset.value.status) {
    case 'IDLE': return '当前资产闲置中，可直接申请领用。'
    case 'IN_USE': return '当前资产在用中，仍可申请领用或报修。'
    case 'REPAIRING': return '资产正在维修，仅可申请报废。'
    case 'SCRAPPED': return '资产已报废，无法再操作。'
    case 'LOSS': return '资产已盘亏，请联系管理员。'
    default: return '请选择操作。'
  }
})

// 申请弹窗状态
const applyDialogVisible = ref(false)
const applySubmitting = ref(false)
const applyForm = ref({
  type: 'borrow', // borrow | scrap | repair
  assetId: null,
  assetName: '',
  reason: '',
  reasonLabel: '领用原因',
  placeholder: '请说明领用用途、归还时间等',
  expectedReturnDate: ''
})
const applyDialogTitle = computed(() => {
  const map = { borrow: '申请领用', scrap: '申请报废', repair: '提交报修' }
  return map[applyForm.value.type] || '申请'
})
const canSubmitApply = computed(() => {
  if (!applyForm.value.reason?.trim()) return false
  if (applyForm.value.type === 'borrow' && !applyForm.value.expectedReturnDate) return false
  return true
})

function statusTagType(s) {
  switch (s) {
    case 'IDLE': return 'success'
    case 'IN_USE': return 'primary'
    case 'REPAIRING': return 'warning'
    case 'SCRAPPED': return 'danger'
    case 'LOSS': return 'info'
    default: return 'info'
  }
}
function statusText(s) {
  return {
    IDLE: '闲置', IN_USE: '在用', REPAIRING: '维修中',
    SCRAPPED: '已报废', LOSS: '盘亏'
  }[s] || s || '未知'
}
function formatMoney(v) {
  if (v === null || v === undefined || v === '') return '0.00'
  const n = Number(v)
  return isNaN(n) ? '0.00' : n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function placeholderStyle(item) {
  // 根据分类生成不同的渐变色作为占位
  const hash = (item.categoryName || item.name || 'x').split('').reduce((acc, c) => acc + c.charCodeAt(0), 0)
  const hue1 = hash % 360
  const hue2 = (hue1 + 40) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue1} 70% 92%), hsl(${hue2} 65% 78%))`
  }
}

async function loadAssets() {
  loading.value = true
  try {
    const res = await listModelAssets()
    assets.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('Load 3D assets error:', error)
    ElMessage.error('3D 资产列表加载失败')
  } finally {
    loading.value = false
  }
}

function openDetail(item) {
  currentAsset.value = item
  detailVisible.value = true
}

function schedulePreview(id) {
  if (activePreviewId.value === id) return
  if (previewTimers.has(id)) {
    clearTimeout(previewTimers.get(id))
    previewTimers.delete(id)
  }
  // 50ms 延迟避免快速划过触发开销
  const timer = setTimeout(() => {
    startPreview(id)
    previewTimers.delete(id)
  }, 50)
  previewTimers.set(id, timer)
}

function releasePreview(id) {
  if (previewTimers.has(id)) {
    clearTimeout(previewTimers.get(id))
    previewTimers.delete(id)
  }
  const timer = setTimeout(() => {
    stopPreview(id)
    previewTimers.delete(id)
  }, 1200)
  previewTimers.set(id, timer)
}

function startPreview(id) {
  if (previewEngines.has(id)) return
  const item = assets.value.find(a => a.id === id)
  if (!item?.modelUrl) return
  const container = canvasRefs.get(id)
  if (!container) return
  if (!container.clientWidth || !container.clientHeight) return
  activePreviewId.value = id

  let renderer, scene, camera, controls, animationId
  try {
    scene = new THREE.Scene()
    scene.background = new THREE.Color(0xf5f7fb)

    camera = new THREE.PerspectiveCamera(40, container.clientWidth / container.clientHeight, 0.1, 1000)
    camera.position.set(2.2, 1.6, 2.6)

    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.setSize(container.clientWidth, container.clientHeight)
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.1
    container.appendChild(renderer.domElement)

    scene.add(new THREE.AmbientLight(0xffffff, 1.0))
    scene.add(new THREE.HemisphereLight(0xffffff, 0xc6d4e5, 1.4))
    const keyLight = new THREE.DirectionalLight(0xffffff, 2.4)
    keyLight.position.set(3, 4, 4)
    scene.add(keyLight)

    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableZoom = false
    controls.enablePan = false
    controls.autoRotate = true
    controls.autoRotateSpeed = 2.4

    const loader = new GLTFLoader()
    loader.load(
      item.modelUrl,
      (gltf) => {
        const model = gltf.scene
        // 居中缩放
        const box = new THREE.Box3().setFromObject(model)
        const size = box.getSize(new THREE.Vector3()).length() || 1
        const center = box.getCenter(new THREE.Vector3())
        model.position.sub(center)
        const scale = 1.6 / size
        model.scale.setScalar(scale)
        scene.add(model)
      },
      undefined,
      (err) => {
        console.warn('加载 GLB 缩略图失败', err)
      }
    )

    const animate = () => {
      animationId = requestAnimationFrame(animate)
      controls?.update()
      renderer.render(scene, camera)
    }
    animate()

    previewEngines.set(id, { renderer, scene, camera, controls, animationId })
  } catch (error) {
    console.error('启动 3D 缩略图失败', error)
  }
}

function stopPreview(id) {
  if (activePreviewId.value === id) activePreviewId.value = null
  const engine = previewEngines.get(id)
  if (!engine) return
  cancelAnimationFrame(engine.animationId)
  engine.controls?.dispose()
  engine.renderer?.dispose()
  // 移除 canvas
  if (engine.renderer?.domElement?.parentNode) {
    engine.renderer.domElement.parentNode.removeChild(engine.renderer.domElement)
  }
  previewEngines.delete(id)
}

// 申请弹窗
function onApplyBorrow(item) {
  applyForm.value = {
    type: 'borrow',
    assetId: item.id,
    assetName: `${item.assetNo} · ${item.name}`,
    reason: '',
    reasonLabel: '领用原因',
    placeholder: '请说明领用用途、计划使用时长等',
    expectedReturnDate: ''
  }
  applyDialogVisible.value = true
}
function onApplyScrap(item) {
  applyForm.value = {
    type: 'scrap',
    assetId: item.id,
    assetName: `${item.assetNo} · ${item.name}`,
    reason: '',
    reasonLabel: '报废原因',
    placeholder: '请说明报废原因（如损坏、老化、超期服役等）',
    expectedReturnDate: ''
  }
  applyDialogVisible.value = true
}
function onApplyRepair(item) {
  applyForm.value = {
    type: 'repair',
    assetId: item.id,
    assetName: `${item.assetNo} · ${item.name}`,
    reason: '',
    reasonLabel: '故障描述',
    placeholder: '请描述故障现象、发生时间、影响范围等',
    expectedReturnDate: ''
  }
  applyDialogVisible.value = true
}

async function submitApply() {
  const { type, assetId, reason, expectedReturnDate } = applyForm.value
  if (!reason?.trim()) {
    ElMessage.warning('请填写原因')
    return
  }
  applySubmitting.value = true
  try {
    if (type === 'borrow') {
      await applyBorrow({ assetId, purpose: reason, expectedReturnDate })
      ElMessage.success('领用申请已提交')
    } else if (type === 'scrap') {
      await applyScrap({ assetId, reason })
      ElMessage.success('报废申请已提交')
    } else if (type === 'repair') {
      await submitRepair({ assetId, faultDescription: reason })
      ElMessage.success('报修申请已提交')
    }
    applyDialogVisible.value = false
    // 关闭详情弹窗并提示用户去查看
    detailVisible.value = false
    setTimeout(() => {
      ElMessageBox.confirm('是否前往"我的申请"页面查看进度？', '提示', {
        confirmButtonText: '前往',
        cancelButtonText: '留在首页',
        type: 'success'
      }).then(() => {
        const map = {
          borrow: '/borrow/my',
          scrap: '/scrap/apply',
          repair: '/repair/my'
        }
        const path = map[type] || '/'
        router.push(path).catch(() => {})
      }).catch(() => {})
    }, 200)
  } catch (error) {
    console.error('提交申请失败', error)
    ElMessage.error(error?.message || '提交失败')
  } finally {
    applySubmitting.value = false
  }
}

onMounted(() => {
  loadAssets()
})
onBeforeUnmount(() => {
  // 清理所有预览
  for (const id of [...previewEngines.keys()]) {
    stopPreview(id)
  }
  for (const t of previewTimers.values()) clearTimeout(t)
  previewTimers.clear()
})
</script>

<style lang="scss" scoped>
.asset-3d-showcase {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e5ebf3;
  padding: 24px 26px 28px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.04);

  &.is-floating {
    height: 100%;
    display: flex;
    flex-direction: column;
    border: none;
    border-radius: 0;
    box-shadow: none;
    padding: 4px;
  }
}

.showcase-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 18px;
}

.showcase-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 800;
  color: #172033;

  .title-icon {
    color: #3157d5;
    font-size: 20px;
  }
}

.showcase-subtitle {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.showcase-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.showcase-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #64748b;

  .is-loading {
    animation: rotating 1.4s linear infinite;
    font-size: 20px;
    color: #3157d5;
  }
}

@keyframes rotating {
  to { transform: rotate(360deg); }
}

.showcase-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.asset-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid #e8edf3;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 40px rgba(31, 41, 91, 0.12);
    border-color: #c2cce5;
  }

  &.status-scrapped {
    opacity: 0.7;
  }
}

.card-preview {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, #f4f7fc, #e9eef8);
  overflow: hidden;
}

.card-preview-canvas {
  position: absolute;
  inset: 0;

  :deep(canvas) {
    display: block;
    width: 100% !important;
    height: 100% !important;
  }
}

.card-preview-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #475569;
  transition: opacity 0.2s ease;

  .placeholder-icon {
    font-size: 48px;
    margin-bottom: 8px;
    opacity: 0.6;
  }

  .placeholder-hint {
    font-size: 12px;
    color: #64748b;
    background: rgba(255, 255, 255, 0.7);
    padding: 4px 10px;
    border-radius: 10px;
  }
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.card-info {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #172033;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-no {
  margin: 0;
  color: #94a3b8;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}

.card-meta {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 12px;

  .dot { color: #cbd5e1; }
}

.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.card-price {
  font-size: 16px;
  font-weight: 800;
  color: #d97706;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 24px;

  @media (max-width: 720px) {
    grid-template-columns: 1fr;
  }
}

.detail-canvas {
  height: 420px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fb;
  border: 1px solid #e5ebf3;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .info-head {
    display: flex;
    align-items: center;
    gap: 10px;
    justify-content: space-between;

    h3 {
      margin: 0;
      font-size: 18px;
      color: #172033;
    }
  }

  .info-no {
    margin: 0;
    color: #94a3b8;
    font-size: 12px;
    font-family: 'JetBrains Mono', 'Consolas', monospace;
  }

  .info-table {
    margin-top: 6px;
  }

  .info-actions {
    display: flex;
    gap: 10px;
    margin-top: 14px;
    flex-wrap: wrap;
  }

  .action-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 4px 0 0;
    color: #64748b;
    font-size: 12px;
  }

  .model-status {
    font-weight: 600;
    &.ready { color: #16a34a; }
    &.none { color: #94a3b8; }
  }
}

.apply-asset-name {
  font-weight: 600;
  color: #172033;
}
</style>
