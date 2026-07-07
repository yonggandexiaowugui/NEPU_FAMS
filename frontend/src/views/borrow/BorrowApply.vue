<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>领用申请</span>
      </template>
      
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px" class="apply-form">
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
        <el-form-item label="领用原因" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" :rows="4" placeholder="请输入领用原因" />
        </el-form-item>
        <el-form-item label="预计归还日期" prop="expectedReturnDate">
          <el-date-picker
            v-model="form.expectedReturnDate"
            type="date"
            placeholder="请选择预计归还日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交申请</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyBorrow } from '@/api/borrow'
import { getAssetList } from '@/api/asset'

const router = useRouter()
const loading = ref(false)
const formRef = ref(null)
const assetList = ref([])

const form = reactive({
  assetId: null,
  purpose: '',
  expectedReturnDate: ''
})

const formRules = {
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入领用原因', trigger: 'blur' }],
  expectedReturnDate: [{ required: true, message: '请选择预计归还日期', trigger: 'change' }]
}

async function loadAssets() {
  try {
    const res = await getAssetList({ pageNum: 1, pageSize: 100, status: 'IDLE' })
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
        await applyBorrow(form)
        ElMessage.success('申请提交成功')
        handleReset()
        router.push('/borrow/my')
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
}

onMounted(() => {
  loadAssets()
})
</script>

<style lang="scss" scoped>
.apply-form {
  max-width: 600px;
  margin: 20px auto;
}
</style>
