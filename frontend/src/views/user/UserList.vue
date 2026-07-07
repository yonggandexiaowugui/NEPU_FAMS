<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="请选择角色" clearable style="width: 150px">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="学院管理员" value="COLLEGE_ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="学院管理员" value="COLLEGE_ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" style="width: 100%" clearable>
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

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="460px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="90px">
        <el-form-item label="用户">
          <el-input :model-value="passwordForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPassword" :loading="passwordSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getUserList, addUser, updateUser, deleteUser, resetUserPassword } from '@/api/user'
import { getAllColleges } from '@/api/college'

const loading = ref(false)
const submitLoading = ref(false)
const passwordSubmitLoading = ref(false)
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)
const tableData = ref([])
const total = ref(0)
const collegeList = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  role: ''
})

const form = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: 'USER',
  collegeId: null
})

const passwordForm = reactive({
  userId: null,
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度必须在6-32位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function roleText(role) {
  const map = { SUPER_ADMIN: '超级管理员', COLLEGE_ADMIN: '学院管理员', USER: '普通用户' }
  return map[role] || role
}

function roleTagType(role) {
  const map = { SUPER_ADMIN: 'danger', COLLEGE_ADMIN: 'warning', USER: 'success' }
  return map[role] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList(queryForm)
    tableData.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('Load user list error:', error)
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
  queryForm.username = ''
  queryForm.role = ''
  handleQuery()
}

function handlePageChange() {
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    role: 'USER',
    collegeId: null
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
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
          const { password, ...payload } = form
          await updateUser(form.id, payload)
          ElMessage.success('修改成功')
        } else {
          await addUser(form)
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

function handleResetPassword(row) {
  Object.assign(passwordForm, {
    userId: row.id,
    username: row.realName ? `${row.username}（${row.realName}）` : row.username,
    newPassword: '',
    confirmPassword: ''
  })
  passwordDialogVisible.value = true
  passwordFormRef.value?.clearValidate()
}

async function submitResetPassword() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordSubmitLoading.value = true
    try {
      await resetUserPassword(passwordForm.userId, { newPassword: passwordForm.newPassword })
      ElMessage.success('密码重置成功')
      passwordDialogVisible.value = false
    } catch (error) {
      console.error('Reset password error:', error)
    } finally {
      passwordSubmitLoading.value = false
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('Delete error:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
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
