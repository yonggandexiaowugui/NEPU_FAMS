<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>盘点录入</span>
          <div>
            <el-button @click="handleViewDiff" :disabled="!queryForm.taskId">查看差异</el-button>
            <el-button type="success" @click="handleAnalyzeDiff" :disabled="!queryForm.taskId">智能分析</el-button>
            <el-button type="primary" @click="handleBatchSave" :disabled="!queryForm.taskId || !hasChanges">保存盘点</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="盘点任务">
          <el-select v-model="queryForm.taskId" placeholder="请选择盘点任务" style="width: 220px" @change="handleTaskChange">
            <el-option
              v-for="task in taskList"
              :key="task.id"
              :label="task.name"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资产编号">
          <el-input v-model="queryForm.assetNo" placeholder="请输入资产编号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="盘点状态">
          <el-select v-model="queryForm.isChecked" placeholder="全部" clearable style="width: 120px">
            <el-option label="已盘点" :value="true" />
            <el-option label="未盘点" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20" class="stats-row" v-if="queryForm.taskId && stats">
        <el-col :span="6">
          <el-card class="mini-stat" shadow="hover">
            <div class="mini-stat-content">
              <div class="mini-stat-info">
                <p class="mini-stat-title">账面总数</p>
                <p class="mini-stat-value">{{ stats.total || 0 }}</p>
              </div>
              <div class="mini-stat-icon info">
                <el-icon><Collection /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="mini-stat" shadow="hover">
            <div class="mini-stat-content">
              <div class="mini-stat-info">
                <p class="mini-stat-title">已盘点</p>
                <p class="mini-stat-value success">{{ stats.checked || 0 }}</p>
              </div>
              <div class="mini-stat-icon success">
                <el-icon><CircleCheck /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="mini-stat" shadow="hover">
            <div class="mini-stat-content">
              <div class="mini-stat-info">
                <p class="mini-stat-title">未盘点</p>
                <p class="mini-stat-value warning">{{ stats.unchecked || 0 }}</p>
              </div>
              <div class="mini-stat-icon warning">
                <el-icon><Clock /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="mini-stat" shadow="hover">
            <div class="mini-stat-content">
              <div class="mini-stat-info">
                <p class="mini-stat-title">盘亏</p>
                <p class="mini-stat-value danger">{{ stats.diff || 0 }}</p>
              </div>
              <div class="mini-stat-icon danger">
                <el-icon><Warning /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="assetNo" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="collegeName" label="学院" width="120" />
        <el-table-column prop="location" label="存放位置" width="120" show-overflow-tooltip />
        <el-table-column label="账面数量" width="100" align="center">
          <template #default>
            1
          </template>
        </el-table-column>
        <el-table-column label="实盘数量" width="130">
          <template #default="{ row }">
            <el-input-number
              v-model="row.actualCount"
              :min="0"
              :max="999"
              size="small"
              style="width: 100%"
              @change="handleCountChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.actualCount !== undefined && row.actualCount !== 1" :type="row.actualCount > 1 ? 'success' : 'danger'" size="small">
              {{ row.actualCount > 1 ? '+' : '' }}{{ (row.actualCount || 0) - 1 }}
            </el-tag>
            <el-tag v-else type="info" size="small">0</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="盘点状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isChecked ? 'success' : 'warning'" size="small">
              {{ row.isChecked ? '已盘点' : '未盘点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <el-input
              v-model="row.remark"
              placeholder="盘点备注"
              size="small"
              @change="handleRemarkChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="盘点时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleQuickCheck(row)">
              {{ row.isChecked ? '修改' : '盘点' }}
            </el-button>
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

    <el-dialog v-model="checkDialogVisible" title="资产盘点" width="450px">
      <el-form :model="checkForm" label-width="100px">
        <el-form-item label="资产编号">
          <span>{{ checkForm.assetNo }}</span>
        </el-form-item>
        <el-form-item label="资产名称">
          <span>{{ checkForm.assetName }}</span>
        </el-form-item>
        <el-form-item label="存放位置">
          <span>{{ checkForm.location || '-' }}</span>
        </el-form-item>
        <el-form-item label="实盘数量">
          <el-input-number v-model="checkForm.actualCount" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="盘点结果">
          <el-radio-group v-model="checkForm.isChecked">
            <el-radio :value="true">存在</el-radio>
            <el-radio :value="false">不存在</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCheck" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="diffDialogVisible" title="盘点差异与智能分析" width="960px">
      <el-alert
        v-if="analysisData"
        type="success"
        show-icon
        :closable="false"
        class="analysis-alert"
      >
        <template #title>智能分析摘要</template>
        <div class="analysis-content">
          <p>{{ analysisData.summary }}</p>
          <p><strong>处理建议：</strong>{{ analysisData.suggestion }}</p>
          <p>账实一致 {{ analysisData.matchCount || 0 }} 条，盘盈 {{ analysisData.profitCount || 0 }} 条，盘亏 {{ analysisData.lossCount || 0 }} 条。</p>
        </div>
      </el-alert>
      <el-table :data="diffList" border stripe v-loading="diffLoading" max-height="500">
        <el-table-column prop="assetNo" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="collegeName" label="学院" width="120" />
        <el-table-column prop="bookCount" label="账面数量" width="100" align="center" />
        <el-table-column prop="actualCount" label="实盘数量" width="100" align="center" />
        <el-table-column label="差异数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="(row.actualCount || 0) - (row.bookCount || 0) > 0 ? 'success' : 'danger'" size="small">
              {{ (row.actualCount || 0) - (row.bookCount || 0) > 0 ? '+' : '' }}{{ (row.actualCount || 0) - (row.bookCount || 0) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'" size="small">
              {{ row.riskLevel === 'HIGH' ? '高' : row.riskLevel === 'MEDIUM' ? '中' : '低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="analysis" label="智能说明" min-width="180" show-overflow-tooltip />
        <el-table-column prop="suggestion" label="处理建议" min-width="220" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="diffList.length === 0 && !diffLoading" description="暂无差异数据" />
      <template #footer>
        <el-button @click="diffDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleConfirmDiff" v-if="diffList.length > 0">确认差异</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, CircleCheck, Clock, Warning } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  getInventoryRecordList,
  submitInventoryRecord,
  batchSubmitInventoryRecord,
  getInventoryTaskList,
  getInventoryDiffList,
  analyzeInventoryDiff,
  confirmInventoryDiff
} from '@/api/inventory'

const loading = ref(false)
const submitLoading = ref(false)
const diffLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const taskList = ref([])
const stats = ref(null)
const checkDialogVisible = ref(false)
const diffDialogVisible = ref(false)
const diffList = ref([])
const analysisData = ref(null)
const selectedRows = ref([])
const hasChanges = ref(false)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  taskId: null,
  assetNo: '',
  isChecked: null
})

const checkForm = reactive({
  id: null,
  assetId: null,
  assetNo: '',
  assetName: '',
  location: '',
  actualCount: 1,
  isChecked: true,
  remark: ''
})

async function loadTasks() {
  try {
    const res = await getInventoryTaskList({ pageNum: 1, pageSize: 100, status: 'IN_PROGRESS' })
    taskList.value = res.records || res.list || []
  } catch (error) {
    console.error('Load task list error:', error)
  }
}

let fullRecordList = []

async function loadData() {
  if (!queryForm.taskId) return
  loading.value = true
  try {
    const res = await getInventoryRecordList({ taskId: queryForm.taskId })
    let list = Array.isArray(res) ? res : (res.records || res.list || [])
    fullRecordList = list.map(item => ({
      ...item,
      actualCount: item.actualCount !== undefined ? item.actualCount : (item.isChecked ? 1 : 0)
    }))
    if (queryForm.assetNo) {
      list = list.filter(item => item.assetNo && item.assetNo.includes(queryForm.assetNo))
    }
    if (queryForm.isChecked !== null) {
      list = list.filter(item => Boolean(item.isChecked) === Boolean(queryForm.isChecked))
    }
    total.value = list.length
    const start = (queryForm.pageNum - 1) * queryForm.pageSize
    const end = start + queryForm.pageSize
    tableData.value = list.slice(start, end).map(item => ({
      ...item,
      actualCount: item.actualCount !== undefined ? item.actualCount : (item.isChecked ? 1 : 0)
    }))
    calculateStats()
  } catch (error) {
    console.error('Load inventory record list error:', error)
  } finally {
    loading.value = false
  }
}

function calculateStats() {
  const list = fullRecordList.length > 0 ? fullRecordList : tableData.value
  const totalCount = list.length
  const checkedCount = list.filter(item => item.isChecked).length
  const uncheckedCount = totalCount - checkedCount
  const diffCount = list.filter(item => item.actualCount !== undefined && item.actualCount !== 1).length
  
  stats.value = {
    total: totalCount,
    checked: checkedCount,
    unchecked: uncheckedCount,
    diff: diffCount
  }
}

function handleTaskChange() {
  queryForm.pageNum = 1
  hasChanges.value = false
  loadData()
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.assetNo = ''
  queryForm.isChecked = null
  handleQuery()
}

function handlePageChange() {
  loadData()
}

function handleSelectionChange(selection) {
  selectedRows.value = selection
}

function handleCountChange(row) {
  hasChanges.value = true
  row.isChecked = true
  calculateStats()
}

function handleRemarkChange() {
  hasChanges.value = true
}

function handleQuickCheck(row) {
  Object.assign(checkForm, {
    id: row.id,
    assetId: row.assetId,
    assetNo: row.assetNo,
    assetName: row.assetName,
    location: row.location,
    actualCount: row.actualCount !== undefined ? row.actualCount : 1,
    isChecked: row.isChecked || false,
    remark: row.remark || ''
  })
  checkDialogVisible.value = true
}

async function handleSubmitCheck() {
  submitLoading.value = true
  try {
    await submitInventoryRecord({
      taskId: queryForm.taskId,
      assetId: checkForm.assetId,
      actualQuantity: checkForm.actualCount,
      actualCount: checkForm.actualCount,
      isChecked: checkForm.isChecked,
      remark: checkForm.remark
    })
    ElMessage.success('盘点提交成功')
    checkDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Submit check error:', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleBatchSave() {
  const changedRows = tableData.value.filter(row => row.isChecked && row.actualCount !== undefined)
  if (changedRows.length === 0) {
    ElMessage.warning('没有需要保存的盘点数据')
    return
  }
  
  ElMessageBox.confirm(`确定要保存 ${changedRows.length} 条盘点记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    loading.value = true
    try {
      const records = changedRows.map(row => ({
        assetId: row.assetId,
        actualQuantity: row.actualCount,
        actualCount: row.actualCount,
        isChecked: row.isChecked,
        remark: row.remark
      }))
      await batchSubmitInventoryRecord({
        taskId: queryForm.taskId,
        records
      })
      ElMessage.success('批量保存成功')
      hasChanges.value = false
      loadData()
    } catch (error) {
      console.error('Batch save error:', error)
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}

async function handleViewDiff() {
  if (!queryForm.taskId) {
    ElMessage.warning('请先选择盘点任务')
    return
  }
  diffLoading.value = true
  diffDialogVisible.value = true
  analysisData.value = null
  try {
    const res = await getInventoryDiffList({ taskId: queryForm.taskId })
    diffList.value = res.records || res.list || res || []
  } catch (error) {
    console.error('Load diff list error:', error)
  } finally {
    diffLoading.value = false
  }
}

async function handleAnalyzeDiff() {
  if (!queryForm.taskId) {
    ElMessage.warning('请先选择盘点任务')
    return
  }
  diffLoading.value = true
  diffDialogVisible.value = true
  try {
    const res = await analyzeInventoryDiff({ taskId: queryForm.taskId })
    analysisData.value = res
    diffList.value = res.differences || []
  } catch (error) {
    console.error('Analyze diff error:', error)
  } finally {
    diffLoading.value = false
  }
}

function handleConfirmDiff() {
  ElMessageBox.confirm('确定要确认这些差异吗？确认后将无法修改。', '确认差异', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const assetIds = diffList.value
      .filter(item => item.diffType !== 'MATCH')
      .map(item => item.assetId)
    await confirmInventoryDiff(queryForm.taskId, assetIds)
    ElMessage.success('差异已确认')
    diffDialogVisible.value = false
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadTasks()
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

.stats-row {
  margin-bottom: 20px;

  .mini-stat {
    border: none;

    :deep(.el-card__body) {
      padding: 15px;
    }

    .mini-stat-content {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .mini-stat-info {
        .mini-stat-title {
          color: #909399;
          font-size: 13px;
          margin: 0 0 5px 0;
        }

        .mini-stat-value {
          font-size: 22px;
          font-weight: bold;
          color: #303133;
          margin: 0;

          &.success {
            color: #67c23a;
          }

          &.warning {
            color: #e6a23c;
          }

          &.danger {
            color: #f56c6c;
          }
        }
      }

      .mini-stat-icon {
        width: 45px;
        height: 45px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;

        &.info {
          background: #ecf5ff;
          color: #409eff;
        }

        &.success {
          background: #f0f9eb;
          color: #67c23a;
        }

        &.warning {
          background: #fdf6ec;
          color: #e6a23c;
        }

        &.danger {
          background: #fef0f0;
          color: #f56c6c;
        }
      }
    }
  }
}
</style>
