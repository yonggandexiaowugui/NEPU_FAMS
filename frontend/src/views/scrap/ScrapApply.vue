<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>报废申请</span>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-button type="primary" style="margin-bottom: 20px" @click="handleAdd">新增报废申请</el-button>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="assetNo" label="资产编号" width="150" />
        <el-table-column prop="reason" label="报废原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="warning" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page-num="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        @change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增报废申请" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
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
        <el-form-item label="报废原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入报废原因" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getScrapList, applyScrap, cancelScrap } from '@/api/scrap'
import { getAssetList } from '@/api/asset'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const assetList = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

const form = reactive({
  assetId: null,
  reason: '',
  remark: ''
})

const formRules = {
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  reason: [{ required: true, message: '请输入报废原因', trigger: 'blur' }]
}

function statusText(status) {
  const map = { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已拒绝' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getScrapList(queryForm)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load scrap list error:', error)
  } finally {
    loading.value = false
  }
}

async function loadAssets() {
  try {
    const res = await getAssetList({ pageNum: 1, pageSize: 100 })
    assetList.value = res.records || res.list || []
  } catch (error) {
    console.error('Load assets error:', error)
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.status = ''
  handleQuery()
}

function handlePageChange() {
  loadData()
}

function handleAdd() {
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await applyScrap(form)
        ElMessage.success('申请提交成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('Submit error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

function handleCancel(row) {
  ElMessageBox.confirm(`确定要取消该报废申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelScrap(row.id)
      ElMessage.success('取消成功')
      loadData()
    } catch (error) {
      console.error('Cancel error:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
  loadAssets()
})
</script>

<style lang="scss" scoped>
.query-form {
  margin-bottom: 20px;
}
</style>
