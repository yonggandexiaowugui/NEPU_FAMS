<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>维修工单</span>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="维修中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable style="width: 120px">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="queryForm.assetName" placeholder="请输入资产名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="assetNo" label="资产编号" width="140" />
        <el-table-column prop="reporterName" label="报修人" width="110" />
        <el-table-column prop="faultDescription" label="故障描述" show-overflow-tooltip min-width="150" />
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repairerName" label="维修人" width="110" />
        <el-table-column prop="createTime" label="报修时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="primary" link size="small" @click="handleAssign(row)">派单</el-button>
            <el-button v-if="row.status === 'PROCESSING'" type="success" link size="small" @click="handleProgress(row)">更新进度</el-button>
            <el-button v-if="row.status === 'PROCESSING'" type="success" link size="small" @click="handleComplete(row)">完成</el-button>
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

    <el-dialog v-model="detailVisible" title="维修详情" width="650px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="资产编号">{{ detailData.assetNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detailData.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ detailData.reporterName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityTagType(detailData.priority)">{{ priorityText(detailData.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="维修人">{{ detailData.repairerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detailData.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ detailData.faultDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="维修方案" :span="2">{{ detailData.solution || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left" v-if="detailData && detailData.progressList && detailData.progressList.length > 0">维修进度</el-divider>
      <el-timeline v-if="detailData && detailData.progressList && detailData.progressList.length > 0">
        <el-timeline-item
          v-for="(progress, index) in detailData.progressList"
          :key="index"
          :timestamp="progress.createTime"
          type="primary"
        >
          <h4 style="margin: 0 0 5px 0">{{ progress.operatorName || '系统' }}</h4>
          <p style="margin: 0; color: #606266; font-size: 13px">{{ progress.description || '无描述' }}</p>
        </el-timeline-item>
      </el-timeline>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="派单" width="450px">
      <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" label-width="80px">
        <el-form-item label="维修人员" prop="repairerId">
          <el-select v-model="assignForm.repairerId" placeholder="请选择维修人员" style="width: 100%" filterable>
            <el-option
              v-for="user in repairerList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="assignForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitLoading">确认派单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="progressVisible" title="更新进度" width="450px">
      <el-form :model="progressForm" :rules="progressRules" ref="progressFormRef" label-width="80px">
        <el-form-item label="进度描述" prop="description">
          <el-input v-model="progressForm.description" type="textarea" :rows="4" placeholder="请描述当前维修进度" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getRepairList, assignRepair, completeRepair, updateRepairProgress, getRepairDetail } from '@/api/repair'
import { getUserList } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const assignVisible = ref(false)
const progressVisible = ref(false)
const detailData = ref(null)
const currentRow = ref(null)
const assignFormRef = ref(null)
const progressFormRef = ref(null)
const repairerList = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  priority: '',
  assetName: ''
})

const assignForm = reactive({
  repairerId: null,
  remark: ''
})

const assignRules = {
  repairerId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

const progressForm = reactive({
  description: ''
})

const progressRules = {
  description: [{ required: true, message: '请输入进度描述', trigger: 'blur' }]
}

function statusText(status) {
  const map = { PENDING: '待处理', PROCESSING: '维修中', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { PENDING: 'warning', PROCESSING: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

function priorityText(priority) {
  const map = { LOW: '低', MEDIUM: '中', HIGH: '高', URGENT: '紧急' }
  return map[priority] || priority
}

function priorityTagType(priority) {
  const map = { LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', URGENT: 'danger' }
  return map[priority] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRepairList(queryForm)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load repair list error:', error)
  } finally {
    loading.value = false
  }
}

async function loadRepairers() {
  try {
    const res = await getUserList({ pageNum: 1, pageSize: 100 })
    repairerList.value = res.records || res.list || []
  } catch (error) {
    console.error('Load repairers error:', error)
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.status = ''
  queryForm.priority = ''
  queryForm.assetName = ''
  handleQuery()
}

function handlePageChange() {
  loadData()
}

async function handleView(row) {
  try {
    const res = await getRepairDetail(row.id)
    detailData.value = res
    detailVisible.value = true
  } catch (error) {
    console.error('Get detail error:', error)
  }
}

function handleAssign(row) {
  currentRow.value = row
  assignForm.repairerId = null
  assignForm.remark = ''
  assignVisible.value = true
}

async function submitAssign() {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await assignRepair(currentRow.value.id, assignForm)
        ElMessage.success('派单成功')
        assignVisible.value = false
        loadData()
      } catch (error) {
        console.error('Assign error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

function handleProgress(row) {
  currentRow.value = row
  progressForm.description = ''
  progressVisible.value = true
}

async function submitProgress() {
  if (!progressFormRef.value) return
  await progressFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await updateRepairProgress(currentRow.value.id, progressForm)
        ElMessage.success('进度更新成功')
        progressVisible.value = false
        loadData()
      } catch (error) {
        console.error('Progress error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

function handleComplete(row) {
  ElMessageBox.confirm(`确定该维修工单已完成吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await completeRepair(row.id)
      ElMessage.success('已完成')
      loadData()
    } catch (error) {
      console.error('Complete error:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
  loadRepairers()
})
</script>

<style lang="scss" scoped>
.query-form {
  margin-bottom: 20px;
}
</style>
