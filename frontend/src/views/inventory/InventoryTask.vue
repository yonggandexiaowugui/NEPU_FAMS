<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>盘点任务</span>
          <el-button type="primary" @click="handleAdd">新建盘点</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input v-model="queryForm.name" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" type="success" link @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="盘点范围" prop="collegeId">
          <el-select v-model="form.collegeId" style="width: 100%" clearable placeholder="全部学院">
            <el-option
              v-for="college in collegeList"
              :key="college.id"
              :label="college.name"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import {
  getInventoryTaskList,
  createInventoryTask,
  updateInventoryTask,
  deleteInventoryTask,
  startInventoryTask,
  completeInventoryTask
} from '@/api/inventory'
import { getAllColleges } from '@/api/college'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const collegeList = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  name: ''
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  collegeId: null
})

const formRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }]
}

function statusText(status) {
  const map = { PENDING: '待开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { PENDING: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getInventoryTaskList(queryForm)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load inventory task list error:', error)
  } finally {
    loading.value = false
  }
}

async function loadColleges() {
  try {
    const res = await getAllColleges()
    collegeList.value = res || []
  } catch (error) {
    console.error('Load colleges error:', error)
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.status = ''
  queryForm.name = ''
  handleQuery()
}

function handlePageChange() {
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新建盘点任务'
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    collegeId: null
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑盘点任务'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateInventoryTask(form.id, form)
          ElMessage.success('修改成功')
        } else {
          await createInventoryTask(form)
          ElMessage.success('创建成功')
        }
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

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除盘点任务 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteInventoryTask(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('Delete error:', error)
    }
  }).catch(() => {})
}

function handleStart(row) {
  ElMessageBox.confirm(`确定要开始盘点任务 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await startInventoryTask(row.id)
      ElMessage.success('任务已开始')
      loadData()
    } catch (error) {
      console.error('Start error:', error)
    }
  }).catch(() => {})
}

function handleComplete(row) {
  ElMessageBox.confirm(`确定要完成盘点任务 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await completeInventoryTask(row.id)
      ElMessage.success('任务已完成')
      loadData()
    } catch (error) {
      console.error('Complete error:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
  loadColleges()
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
