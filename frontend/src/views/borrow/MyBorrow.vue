<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的领用</span>
          <el-button type="primary" @click="$router.push('/borrow/apply')">新建申请</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待学院审批" value="PENDING_COLLEGE" />
            <el-option label="待校级审批" value="PENDING_SUPER" />
            <el-option label="待领用" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已领用" value="BORROWED" />
            <el-option label="待归还确认" value="RETURNING" />
            <el-option label="已归还" value="RETURNED" />
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
        <el-table-column prop="purpose" label="领用原因" show-overflow-tooltip min-width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column prop="expectedReturnDate" label="预计归还" width="140" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING_COLLEGE' || row.status === 'PENDING_SUPER'" type="warning" link size="small" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.status === 'APPROVED'" type="success" link size="small" @click="handleConfirm(row)">确认领用</el-button>
            <el-button v-if="row.status === 'BORROWED'" type="primary" link size="small" @click="handleReturn(row)">申请归还</el-button>
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
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getMyBorrowList, cancelBorrow, confirmBorrow, returnBorrow, getBorrowDetail } from '@/api/borrow'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  assetName: ''
})

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
    const res = await getMyBorrowList(queryForm)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load borrow list error:', error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.status = ''
  queryForm.assetName = ''
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

function handleCancel(row) {
  ElMessageBox.confirm(`确定要取消该领用申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelBorrow(row.id)
      ElMessage.success('取消成功')
      loadData()
    } catch (error) {
      console.error('Cancel error:', error)
    }
  }).catch(() => {})
}

function handleConfirm(row) {
  ElMessageBox.confirm(`确定已领取该资产吗？`, '确认领用', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await confirmBorrow(row.id)
      ElMessage.success('确认领用成功')
      loadData()
    } catch (error) {
      console.error('Confirm error:', error)
    }
  }).catch(() => {})
}

function handleReturn(row) {
  ElMessageBox.confirm(`确定要申请归还该资产吗？`, '申请归还', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await returnBorrow(row.id)
      ElMessage.success('归还申请已提交')
      loadData()
    } catch (error) {
      console.error('Return error:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}
</style>
