<template>
  <div class="asset-model-preview">
    <div v-if="!webglSupported" class="fallback-box">
      当前浏览器不支持WebGL，无法预览3D模型。
    </div>
    <div v-else-if="errorMessage" class="fallback-box">
      <p>{{ errorMessage }}</p>
      <el-button size="small" @click="initScene">重试</el-button>
    </div>
    <div v-else ref="containerRef" class="viewer-box" v-loading="loading" element-loading-text="模型加载中..." />
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

const props = defineProps({
  modelUrl: {
    type: String,
    default: ''
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const containerRef = ref(null)
const loading = ref(false)
const errorMessage = ref('')

let renderer = null
let scene = null
let camera = null
let controls = null
let animationId = null
let resizeObserver = null

const webglSupported = computed(() => {
  try {
    const canvas = document.createElement('canvas')
    return !!(window.WebGLRenderingContext && (canvas.getContext('webgl') || canvas.getContext('experimental-webgl')))
  } catch (error) {
    return false
  }
})

watch(() => [props.visible, props.modelUrl], async () => {
  if (props.visible && props.modelUrl) {
    await nextTick()
    initScene()
  }
}, { immediate: true })

async function initScene() {
  if (!webglSupported.value || !props.modelUrl || !containerRef.value) return
  cleanup()
  loading.value = true
  errorMessage.value = ''

  try {
    const width = containerRef.value.clientWidth || 720
    const height = containerRef.value.clientHeight || 420

    scene = new THREE.Scene()
    scene.background = new THREE.Color(0xf5f7fb)

    camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
    camera.position.set(2.5, 2, 3)

    renderer = new THREE.WebGLRenderer({ antialias: true })
    renderer.setSize(width, height)
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1.35
    renderer.shadowMap.enabled = true
    renderer.shadowMap.type = THREE.PCFSoftShadowMap
    containerRef.value.appendChild(renderer.domElement)

    // 写实模型通常依赖 PBR 材质和环境补光，避免黑色外壳糊成一坨。
    scene.add(new THREE.AmbientLight(0xffffff, 1.4))
    scene.add(new THREE.HemisphereLight(0xffffff, 0xb9c7d8, 2.6))
    const keyLight = new THREE.DirectionalLight(0xffffff, 3.2)
    keyLight.position.set(4, 6, 5)
    keyLight.castShadow = true
    scene.add(keyLight)
    const fillLight = new THREE.DirectionalLight(0xcfe8ff, 1.8)
    fillLight.position.set(-4, 3, -3)
    scene.add(fillLight)
    const rimLight = new THREE.DirectionalLight(0xffffff, 1.4)
    rimLight.position.set(0, 4, -5)
    scene.add(rimLight)

    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true
    controls.dampingFactor = 0.08

    const loader = new GLTFLoader()
    const gltf = await loader.loadAsync(props.modelUrl)
    const model = gltf.scene
    enhanceModelMaterial(model)
    scene.add(model)

    // 自动居中并适配相机，保证不同模型都能完整展示。
    const box = new THREE.Box3().setFromObject(model)
    const size = box.getSize(new THREE.Vector3())
    const center = box.getCenter(new THREE.Vector3())
    model.position.sub(center)
    const maxSize = Math.max(size.x, size.y, size.z) || 1
    camera.position.set(maxSize * 1.6, maxSize * 1.2, maxSize * 2)
    camera.near = maxSize / 100
    camera.far = maxSize * 100
    camera.updateProjectionMatrix()
    addPreviewStage(maxSize)
    controls.target.set(0, 0, 0)
    controls.update()

    resizeObserver = new ResizeObserver(handleResize)
    resizeObserver.observe(containerRef.value)
    animate()
  } catch (error) {
    console.error('Load 3D model error:', error)
    errorMessage.value = '3D模型加载失败，请检查模型文件后重试。'
  } finally {
    loading.value = false
  }
}

function enhanceModelMaterial(model) {
  model.traverse((child) => {
    if (!child.isMesh) return
    child.castShadow = true
    child.receiveShadow = true

    const materials = Array.isArray(child.material) ? child.material : [child.material]
    materials.forEach((material) => {
      if (!material) return
      material.side = THREE.DoubleSide
      if ('roughness' in material) material.roughness = Math.min(material.roughness ?? 0.55, 0.72)
      if ('metalness' in material) material.metalness = material.metalness ?? 0.15

      // 低模或暗色材质没有贴图时，稍微提亮，避免黑色设备失去细节层次。
      if (material.color && !material.map) {
        const brightness = material.color.r + material.color.g + material.color.b
        if (brightness < 0.18) {
          material.color.offsetHSL(0, -0.08, 0.16)
        }
      }
      material.needsUpdate = true
    })
  })
}

function addPreviewStage(maxSize) {
  const floorSize = Math.max(maxSize * 3, 4)
  const floor = new THREE.Mesh(
    new THREE.CircleGeometry(floorSize / 2, 96),
    new THREE.MeshStandardMaterial({ color: 0xe8edf5, roughness: 0.82, metalness: 0 })
  )
  floor.name = 'preview-stage-floor'
  floor.rotation.x = -Math.PI / 2
  floor.position.y = -maxSize * 0.5
  floor.receiveShadow = true
  scene.add(floor)

  const grid = new THREE.GridHelper(floorSize, 12, 0xc8d2df, 0xdde4ee)
  grid.position.y = floor.position.y + 0.002
  scene.add(grid)
}

function animate() {
  animationId = requestAnimationFrame(animate)
  controls?.update()
  renderer?.render(scene, camera)
}

function handleResize() {
  if (!containerRef.value || !renderer || !camera) return
  const width = containerRef.value.clientWidth || 720
  const height = containerRef.value.clientHeight || 420
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

function cleanup() {
  if (animationId) cancelAnimationFrame(animationId)
  resizeObserver?.disconnect()
  controls?.dispose()
  renderer?.dispose()
  if (renderer?.domElement?.parentNode) {
    renderer.domElement.parentNode.removeChild(renderer.domElement)
  }
  animationId = null
  resizeObserver = null
  renderer = null
  scene = null
  camera = null
  controls = null
}

onBeforeUnmount(cleanup)
</script>

<style scoped>
.asset-model-preview,
.viewer-box {
  width: 100%;
  height: 460px;
}

.viewer-box {
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f5f7fb;
}

.fallback-box {
  height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #606266;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafafa;
}
</style>
