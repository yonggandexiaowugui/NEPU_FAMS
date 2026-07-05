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
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
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
import { submitRepair } from '@/api/repair'
import { getAssetList } from '@/api/asset'

const loading = ref(false)
const formRef = ref(null)
const assetList = ref([])

const form = reactive({
  assetId: null,
  faultDescription: '',
  priority: 'MEDIUM',
  remark: ''
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

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await submitRepair(form)
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
  form.priority = 'MEDIUM'
}

onMounted(() => {
  loadAssets()
})
</script>

<style lang="scss" scoped>
.submit-form {
  max-width: 600px;
  margin: 20px auto;
}
</style>
