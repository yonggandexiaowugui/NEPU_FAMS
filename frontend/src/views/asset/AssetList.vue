<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>资产台账</span>
          <div>
            <el-button @click="handleExport" :loading="exportLoading">导出Excel</el-button>
            <el-button type="primary" @click="handleAdd">新增资产</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="名称/编号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-tree-select
            v-model="queryForm.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择分类"
            clearable
            style="width: 180px"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="在用" value="IN_USE" />
            <el-option label="闲置" value="IDLE" />
            <el-option label="维修中" value="REPAIRING" />
            <el-option label="已报废" value="SCRAPPED" />
            <el-option label="盘亏" value="LOSS" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="queryForm.collegeId" placeholder="全部" clearable style="width: 150px">
            <el-option
              v-for="college in collegeList"
              :key="college.id"
              :label="college.name"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价值区间">
          <el-input-number v-model="queryForm.minPrice" :min="0" placeholder="最小值" style="width: 100px" />
          <span style="margin: 0 5px">-</span>
          <el-input-number v-model="queryForm.maxPrice" :min="0" placeholder="最大值" style="width: 100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="assetNo" label="资产编号" width="140" />
        <el-table-column prop="name" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="collegeName" label="所属学院" width="120" />
        <el-table-column prop="location" label="存放位置" width="120" show-overflow-tooltip />
        <el-table-column prop="purchasePrice" label="价值(元)" width="110">
          <template #default="{ row }">
            {{ formatMoney(row.purchasePrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购置日期" width="110" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产编号">
              <el-input v-model="form.assetNo" placeholder="新增时系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择分类"
                style="width: 100%"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学院" prop="collegeId">
              <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
                <el-option
                  v-for="college in collegeList"
                  :key="college.id"
                  :label="college.name"
                  :value="college.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购置价格" prop="purchasePrice">
              <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="在用" value="IN_USE" />
                <el-option label="闲置" value="IDLE" />
                <el-option label="维修中" value="REPAIRING" />
                <el-option label="已报废" value="SCRAPPED" />
                <el-option label="盘亏" value="LOSS" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="form.location" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购置日期" prop="purchaseDate">
              <el-date-picker v-model="form.purchaseDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="资产详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="资产编号">{{ detailData.assetNo }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ detailData.collegeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购置价格">{{ formatMoney(detailData.purchasePrice) }} 元</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ detailData.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ detailData.purchaseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
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
import { getAssetList, addAsset, updateAsset, deleteAsset, exportAsset, getAsset } from '@/api/asset'
import { getCategoryTree } from '@/api/category'
import { getAllColleges } from '@/api/college'
import { formatMoney } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const exportLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const categoryTree = ref([])
const collegeList = ref([])
const detailData = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: null,
  status: '',
  collegeId: null,
  minPrice: null,
  maxPrice: null
})

const form = reactive({
  id: null,
  assetNo: '',
  name: '',
  categoryId: null,
  collegeId: null,
  purchasePrice: 0,
  status: 'IDLE',
  location: '',
  purchaseDate: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
  collegeId: [{ required: true, message: '请选择所属学院', trigger: 'change' }]
}

function statusText(status) {
  const map = { IN_USE: '在用', IDLE: '闲置', REPAIRING: '维修中', SCRAPPED: '已报废', LOSS: '盘亏' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { IN_USE: 'success', IDLE: 'info', REPAIRING: 'danger', SCRAPPED: 'info', LOSS: 'warning' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.minPrice === null) delete params.minPrice
    if (params.maxPrice === null) delete params.maxPrice
    const res = await getAssetList(params)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load asset list error:', error)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res || []
  } catch (error) {
    console.error('Load categories error:', error)
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
  queryForm.keyword = ''
  queryForm.categoryId = null
  queryForm.status = ''
  queryForm.collegeId = null
  queryForm.minPrice = null
  queryForm.maxPrice = null
  handleQuery()
}

function handlePageChange() {
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增资产'
  Object.assign(form, {
    id: null,
    assetNo: '',
    name: '',
    categoryId: null,
    collegeId: null,
    purchasePrice: 0,
    status: 'IDLE',
    location: '',
    purchaseDate: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑资产'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleView(row) {
  try {
    const res = await getAsset(row.id)
    detailData.value = res
    detailVisible.value = true
  } catch (error) {
    console.error('Get asset detail error:', error)
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateAsset(form.id, form)
          ElMessage.success('修改成功')
        } else {
          await addAsset(form)
          ElMessage.success('新增成功')
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
  ElMessageBox.confirm(`确定要删除资产 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAsset(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('Delete error:', error)
    }
  }).catch(() => {})
}

async function handleExport() {
  exportLoading.value = true
  try {
    const params = { ...queryForm }
    delete params.pageNum
    delete params.pageSize
    if (params.minPrice === null) delete params.minPrice
    if (params.maxPrice === null) delete params.maxPrice
    const res = await exportAsset(params)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `资产列表_${new Date().getTime()}.xlsx`)
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
  loadCategories()
  loadColleges()
})
</script>

<style lang="scss" scoped>
.page-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .query-form {
    margin-bottom: 20px;
  }
}
</style>
