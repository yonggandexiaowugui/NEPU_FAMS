<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span>维修工单</span></template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="维修中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="需报废" value="SCRAPPED" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable style="width: 120px">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="queryForm.assetName" placeholder="请输入资产名称" clearable style="width: 160px" />
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
        <el-table-column label="报修人" width="110">
          <template #default="{ row }">{{ row.reporterName || row.userName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="faultDescription" label="故障描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }"><el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityText(row.priority) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="维修人" width="110">
          <template #default="{ row }">{{ row.repairerName || row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="报修时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="primary" link size="small" @click="handleAssign(row)">派单</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" type="success" link size="small" @click="handleProgress(row)">更新</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" type="success" link size="small" @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" v-model:page-num="queryForm.pageNum" v-model:page-size="queryForm.pageSize" @change="handlePageChange" />
    </el-card>

    <el-dialog v-model="detailVisible" title="维修详情" width="720px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="资产编号">{{ detailData.assetNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detailData.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ detailData.reporterName || detailData.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级"><el-tag :type="priorityTagType(detailData.priority)">{{ priorityText(detailData.priority) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="维修人">{{ detailData.repairerName || detailData.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ detailData.faultDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="维修结果" :span="2">{{ detailData.repairResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
          <template v-if="parseAttachments(detailData.attachmentUrls).length">
            <el-link v-for="url in parseAttachments(detailData.attachmentUrls)" :key="url" :href="url" target="_blank" type="primary" class="attachment-link">{{ url.split('/').pop() }}</el-link>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="派单" width="460px">
      <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" label-width="90px">
        <el-form-item label="维修人员" prop="assigneeId">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择维修人员" style="width: 100%" filterable>
            <el-option v-for="user in repairerList" :key="user.id" :label="user.realName || user.name || user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="assignForm.priority" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitLoading">确认派单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="progressVisible" title="更新维修进度" width="460px">
      <el-form :model="progressForm" :rules="progressRules" ref="progressFormRef" label-width="90px">
        <el-form-item label="处理结果" prop="repairResult">
          <el-input v-model="progressForm.repairResult" type="textarea" :rows="4" placeholder="请描述维修进度或处理结果" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="progressForm.status">
            <el-radio value="COMPLETED">已完成</el-radio>
            <el-radio value="SCRAPPED">需报废</el-radio>
          </el-radio-group>
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
import { getRepairList, assignRepair, updateRepairProgress, getRepairDetail } from '@/api/repair'
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

const queryForm = reactive({ pageNum: 1, pageSize: 10, status: '', priority: '', assetName: '' })
const assignForm = reactive({ assigneeId: null, priority: 'NORMAL' })
const progressForm = reactive({ status: 'COMPLETED', repairResult: '' })

const assignRules = { assigneeId: [{ required: true, message: '请选择维修人员', trigger: 'change' }] }
const progressRules = { repairResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }] }

function statusText(status) {
  return { PENDING: '待处理', IN_PROGRESS: '维修中', COMPLETED: '已完成', SCRAPPED: '需报废' }[status] || status
}
function statusTagType(status) {
  return { PENDING: 'warning', IN_PROGRESS: 'primary', COMPLETED: 'success', SCRAPPED: 'danger' }[status] || 'info'
}
function priorityText(priority) {
  return { LOW: '低', NORMAL: '中', MEDIUM: '中', HIGH: '高', URGENT: '紧急' }[priority] || priority
}
function priorityTagType(priority) {
  return { LOW: 'info', NORMAL: 'warning', MEDIUM: 'warning', HIGH: 'danger', URGENT: 'danger' }[priority] || 'info'
}
function parseAttachments(value) {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    return String(value).split(',').filter(Boolean)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRepairList(queryForm)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
async function loadRepairers() {
  const res = await getUserList({ pageNum: 1, pageSize: 100 })
  repairerList.value = res.records || res.list || []
}
function handleQuery() { queryForm.pageNum = 1; loadData() }
function handleReset() { queryForm.status = ''; queryForm.priority = ''; queryForm.assetName = ''; handleQuery() }
function handlePageChange() { loadData() }
async function handleView(row) { detailData.value = await getRepairDetail(row.id); detailVisible.value = true }
function handleAssign(row) { currentRow.value = row; assignForm.assigneeId = null; assignForm.priority = row.priority || 'NORMAL'; assignVisible.value = true }
async function submitAssign() {
  await assignFormRef.value?.validate(async valid => {
    if (!valid) return
    submitLoading.value = true
    try {
      await assignRepair(currentRow.value.id, assignForm)
      ElMessage.success('派单成功')
      assignVisible.value = false
      loadData()
    } finally { submitLoading.value = false }
  })
}
function handleProgress(row) { currentRow.value = row; progressForm.status = 'COMPLETED'; progressForm.repairResult = ''; progressVisible.value = true }
async function submitProgress() {
  await progressFormRef.value?.validate(async valid => {
    if (!valid) return
    submitLoading.value = true
    try {
      await updateRepairProgress(currentRow.value.id, progressForm)
      ElMessage.success('进度更新成功')
      progressVisible.value = false
      loadData()
    } finally { submitLoading.value = false }
  })
}
function handleComplete(row) {
  ElMessageBox.confirm('确定该维修工单已完成吗？', '提示', { type: 'success' }).then(async () => {
    await updateRepairProgress(row.id, { status: 'COMPLETED', repairResult: '维修完成' })
    ElMessage.success('已完成')
    loadData()
  }).catch(() => {})
}

onMounted(() => { loadData(); loadRepairers() })
</script>

<style lang="scss" scoped>
.query-form { margin-bottom: 20px; }
.attachment-link { margin-right: 12px; }
</style>
