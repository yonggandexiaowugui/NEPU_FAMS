<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>领用审批</span>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待我审批" name="pending" />
        <el-tab-pane label="已审批" name="approved" />
      </el-tabs>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="资产名称">
          <el-input v-model="queryForm.assetName" placeholder="请输入资产名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item v-if="activeTab === 'approved'" label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userName" label="申请人" width="110" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="assetNo" label="资产编号" width="140" />
        <el-table-column prop="purpose" label="领用原因" show-overflow-tooltip min-width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedReturnDate" label="预计归还" width="140" />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <template v-if="activeTab === 'pending' && row.status !== 'RETURNING'">
              <el-button type="success" link size="small" @click="handleApprove(row)">批准</el-button>
              <el-button type="danger" link size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button v-if="activeTab === 'pending' && row.status === 'RETURNING'" type="success" link size="small" @click="handleConfirmReturn(row)">确认归还</el-button>
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

    <el-dialog v-model="detailVisible" title="领用详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="资产编号">{{ detailData.assetNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detailData.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ detailData.collegeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预计归还">{{ detailData.expectedReturnDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="领用原因" :span="2">{{ detailData.purpose || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">审批记录</el-divider>
      <el-timeline v-if="detailData.approvalHistory && detailData.approvalHistory.length > 0">
        <el-timeline-item
          v-for="(record, index) in detailData.approvalHistory"
          :key="index"
          :type="record.approvalStatus === 'PASS' ? 'success' : 'danger'"
          :timestamp="record.createTime"
        >
          <h4 style="margin: 0 0 5px 0">
            {{ record.approverName || '系统' }}
            <el-tag size="small" :type="record.approvalStatus === 'PASS' ? 'success' : 'danger'">
              {{ record.statusName || record.approvalStatus }}
            </el-tag>
          </h4>
          <p style="margin: 0; color: #606266; font-size: 13px">{{ record.opinion || '无备注' }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审批记录" :image-size="80" />

      <template #footer>
        <template v-if="activeTab === 'pending' && detailData?.status !== 'RETURNING'">
          <el-button type="danger" @click="handleReject(detailData)">拒绝</el-button>
          <el-button type="success" @click="handleApprove(detailData)">批准</el-button>
        </template>
        <el-button v-if="activeTab === 'pending' && detailData?.status === 'RETURNING'" type="success" @click="handleConfirmReturn(detailData)">确认归还</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="拒绝申请" width="450px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="80px">
        <el-form-item label="拒绝原因" prop="remark">
          <el-input v-model="rejectForm.remark" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="submitLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getBorrowList, approveBorrow, getBorrowDetail, confirmReturn } from '@/api/borrow'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const detailData = ref(null)
const currentRow = ref(null)
const activeTab = ref('pending')
const rejectFormRef = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  assetName: ''
})

const rejectForm = reactive({
  remark: ''
})

const rejectRules = {
  remark: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

function statusText(status) {
  const map = {
    PENDING_COLLEGE: '待学院审批',
    PENDING_SUPER: '待校级审批',
    APPROVED: '待领用',
    REJECTED: '已拒绝',
    BORROWED: '已领用',
    RETURNING: '待归还确认',
    RETURNED: '已归还',
    PENDING: '待审批'
  }
  return map[status] || status
}

function statusTagType(status) {
  const map = {
    PENDING_COLLEGE: 'warning',
    PENDING_SUPER: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    BORROWED: 'primary',
    RETURNING: 'warning',
    RETURNED: 'info',
    PENDING: 'warning'
  }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (activeTab.value === 'pending') {
      params.pending = true
    } else {
      params.approved = true
    }
    const res = await getBorrowList(params)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load borrow list error:', error)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  queryForm.pageNum = 1
  queryForm.status = ''
  loadData()
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.assetName = ''
  queryForm.status = ''
  handleQuery()
}

function handlePageChange() {
  loadData()
}

async function handleView(row) {
  try {
    const res = await getBorrowDetail(row.id)
    detailData.value = res
    detailVisible.value = true
  } catch (error) {
    console.error('Get detail error:', error)
  }
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要批准该领用申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await approveBorrow(row.id, { approved: true, remark: '' })
      ElMessage.success('批准成功')
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

function handleConfirmReturn(row) {
  ElMessageBox.confirm('确定该资产已经归还吗？', '确认归还', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await confirmReturn(row.id)
      ElMessage.success('归还确认成功')
      detailVisible.value = false
      loadData()
    } catch (error) {
      console.error('Confirm return error:', error)
    }
  }).catch(() => {})
}

async function submitReject() {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await approveBorrow(currentRow.value.id, { approved: false, remark: rejectForm.remark })
        ElMessage.success('已拒绝')
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
.query-form {
  margin-bottom: 20px;
}
</style>
