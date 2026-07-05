<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <div>
            <el-button @click="handleExport" :loading="exportLoading">导出Excel</el-button>
            <el-button type="danger" @click="handleClear">清空日志</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="操作人">
          <el-input v-model="queryForm.operator" placeholder="请输入操作人" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.operationType" placeholder="全部" clearable style="width: 130px">
            <el-option label="新增" value="ADD" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.operationType)" size="small">{{ typeText(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="description" label="操作描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="110" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center">
          <template #default="{ row }">
            {{ row.costTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="操作类型">
          <el-tag :type="typeTagType(detailData.operationType)">{{ typeText(detailData.operationType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">{{ detailData.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.costTime ? detailData.costTime + ' ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ detailData.method || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div v-if="detailData.params" class="params-box">
            <pre>{{ detailData.params }}</pre>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <div v-if="detailData.result" class="params-box">
            <pre>{{ detailData.result }}</pre>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
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
import { getOperationLogList, getOperationLogDetail, clearOperationLog, exportOperationLog } from '@/api/log'

const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  operator: '',
  operationType: '',
  dateRange: []
})

function typeText(type) {
  const map = {
    ADD: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    QUERY: '查询',
    EXPORT: '导出',
    LOGIN: '登录',
    LOGOUT: '登出'
  }
  return map[type] || type
}

function typeTagType(type) {
  const map = {
    ADD: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info',
    EXPORT: 'primary',
    LOGIN: 'success',
    LOGOUT: 'info'
  }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.dateRange && params.dateRange.length === 2) {
      params.startTime = params.dateRange[0]
      params.endTime = params.dateRange[1]
    }
    delete params.dateRange
    const res = await getOperationLogList(params)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load operation log list error:', error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.operator = ''
  queryForm.operationType = ''
  queryForm.dateRange = []
  handleQuery()
}

function handlePageChange() {
  loadData()
}

async function handleView(row) {
  try {
    const res = await getOperationLogDetail(row.id)
    detailData.value = res
    detailVisible.value = true
  } catch (error) {
    console.error('Get detail error:', error)
  }
}

function handleClear() {
  ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearOperationLog()
      ElMessage.success('清空成功')
      loadData()
    } catch (error) {
      console.error('Clear error:', error)
    }
  }).catch(() => {})
}

async function handleExport() {
  exportLoading.value = true
  try {
    const params = { ...queryForm }
    delete params.pageNum
    delete params.pageSize
    if (params.dateRange && params.dateRange.length === 2) {
      params.startTime = params.dateRange[0]
      params.endTime = params.dateRange[1]
    }
    delete params.dateRange
    const res = await exportOperationLog(params)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `操作日志_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export error:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
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

.params-box {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;

  pre {
    margin: 0;
    font-size: 12px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
