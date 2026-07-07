<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>报废审批</span>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="pending" />
        <el-tab-pane label="已审批" name="approved" />
      </el-tabs>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="assetNo" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="proposerName" label="申请人" width="110" />
        <el-table-column prop="collegeName" label="所属学院" width="140" />
        <el-table-column prop="reason" label="报废原因" show-overflow-tooltip min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <template v-if="activeTab === 'pending'">
              <el-button type="success" link size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
            </template>
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

    <el-dialog v-model="detailVisible" title="报废详情" width="650px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="资产编号">{{ detailData.assetNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detailData.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.proposerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ detailData.collegeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报废原因" :span="2">{{ detailData.reason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院审批人" v-if="detailData.collegeApproverName">{{ detailData.collegeApproverName }}</el-descriptions-item>
        <el-descriptions-item label="学院审批意见" v-if="detailData.collegeApprovalOpinion">{{ detailData.collegeApprovalOpinion }}</el-descriptions-item>
        <el-descriptions-item label="校级审批人" v-if="detailData.superApproverName">{{ detailData.superApproverName }}</el-descriptions-item>
        <el-descriptions-item label="校级审批意见" v-if="detailData.superApprovalOpinion">{{ detailData.superApprovalOpinion }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="activeTab === 'pending' && detailData">
          <el-button type="danger" @click="handleReject(detailData)">驳回</el-button>
          <el-button type="success" @click="handleApprove(detailData)">通过</el-button>
        </template>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回申请" width="450px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="80px">
        <el-form-item label="驳回原因" prop="remark">
          <el-input v-model="rejectForm.remark" type="textarea" :rows="4" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="submitLoading">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getScrapApprovalList, approveScrap, rejectScrap, getScrapDetail } from '@/api/scrap'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeTab = ref('pending')
const detailVisible = ref(false)
const rejectVisible = ref(false)
const detailData = ref(null)
const currentRow = ref(null)
const rejectFormRef = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

const rejectForm = reactive({
  remark: ''
})

const rejectRules = {
  remark: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
}

function statusText(status) {
  const map = { PENDING: '待审批', PENDING_COLLEGE: '待学院审批', PENDING_SUPER: '待校级审批', APPROVED: '已通过', REJECTED: '已驳回', COMPLETED: '已完成' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { PENDING: 'warning', PENDING_COLLEGE: 'warning', PENDING_SUPER: 'warning', APPROVED: 'success', REJECTED: 'danger', COMPLETED: 'info' }
  return map[status] || 'info'
}

function handleTabChange() {
  queryForm.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (activeTab.value === 'approved') {
      params.approved = true
      delete params.status
    } else {
      delete params.status
    }
    const res = await getScrapApprovalList(params)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load scrap approval list error:', error)
  } finally {
    loading.value = false
  }
}

function handlePageChange() {
  loadData()
}

async function handleView(row) {
  try {
    const res = await getScrapDetail(row.id)
    detailData.value = res
    detailVisible.value = true
  } catch (error) {
    console.error('Get detail error:', error)
  }
}

function handleApprove(row) {
  currentRow.value = row
  ElMessageBox.confirm(`确定通过该报废申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await approveScrap(row.id, { approvalOpinion: '同意报废' })
      ElMessage.success('已通过')
      detailVisible.value = false
      loadData()
    } catch (error) {
      console.error('Approve error:', error)
    }
  }).catch(() => {})
}

function handleReject(row) {
  currentRow.value = row
  rejectForm.remark = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await rejectScrap(currentRow.value.id, { approvalOpinion: rejectForm.remark })
        ElMessage.success('已驳回')
        rejectVisible.value = false
        detailVisible.value = false
        loadData()
      } catch (error) {
        console.error('Reject error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
</style>
