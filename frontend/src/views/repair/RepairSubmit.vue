<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>报修提交</span>
      </template>
      
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px" class="submit-form">
        <el-form-item label="选择资产" prop="assetId">
          <el-select v-model="form.assetId" filterable placeholder="请选择资产" style="width: 100%">
            <el-option
              v-for="asset in assetList"
              :key="asset.id"
              :label="`${asset.assetNo} - ${asset.name}`"
              :value="asset.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="form.faultDescription" type="textarea" :rows="4" placeholder="请详细描述故障情况" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="NORMAL">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="附件说明">
          <el-upload
            v-model:file-list="fileList"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽图片或文件到此处，或点击上传</div>
            <template #tip>
              <div class="el-upload__tip">支持故障照片、说明文档等附件，单个文件不超过 10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            :http-request="handleUpload"
            :file-list="fileList"
            :on-remove="handleRemove"
            multiple
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">可上传故障照片、截图或说明文件。</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交报修</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { submitRepair, uploadRepairAttachment } from '@/api/repair'
import { getAssetList } from '@/api/asset'

const loading = ref(false)
const formRef = ref(null)
const assetList = ref([])
const fileList = ref([])

const form = reactive({
  assetId: null,
  faultDescription: '',
  priority: 'NORMAL',
  remark: '',
  attachmentUrls: []
})

const formRules = {
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

async function loadAssets() {
  try {
    const res = await getAssetList({ pageNum: 1, pageSize: 100 })
    assetList.value = res.records || res.list || []
  } catch (error) {
    console.error('Load assets error:', error)
  }
}

function beforeUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('单个附件不能超过 10MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  try {
    const res = await uploadRepairAttachment(options.file)
    const url = typeof res === 'string' ? res : res.url
    form.attachmentUrls.push(url)
    options.onSuccess?.(url)
    ElMessage.success('附件上传成功')
  } catch (error) {
    options.onError?.(error)
  }
}

function handleRemove(file) {
  const url = file.url || file.response
  form.attachmentUrls = form.attachmentUrls.filter(item => item !== url)
  fileList.value = fileList.value.filter(item => item.uid !== file.uid && item.url !== url)
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await submitRepair({
          ...form,
          attachmentUrls: JSON.stringify(form.attachmentUrls)
        })
        ElMessage.success('报修提交成功')
        handleReset()
      } catch (error) {
        console.error('Submit error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

function handleReset() {
  formRef.value?.resetFields()
  form.priority = 'NORMAL'
  form.remark = ''
  form.attachmentUrls = []
  fileList.value = []
}

onMounted(() => {
  loadAssets()
})
</script>

<style lang="scss" scoped>
.submit-form {
  max-width: 680px;
  margin: 20px auto;
}
</style>
