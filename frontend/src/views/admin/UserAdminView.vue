<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { adminCreateUser, adminUpdateUser, adminUsers } from '@/api/system'
import type { UserAdmin } from '@/api/system'

const loading = ref(false)
const rows = ref<UserAdmin[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref<number | null>(null)

const dialogVisible = ref(false)
const editing = ref<number | null>(null)
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'USER',
  status: 1,
})

const roleTag = (role: string) => (role === 'ADMIN' ? 'danger' : role === 'MONITOR' ? 'warning' : 'info')

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 20 }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (roleFilter.value) {
      params.role = roleFilter.value
    }
    if (statusFilter.value != null) {
      params.status = statusFilter.value
    }
    const result = await adminUsers(params)
    rows.value = result.records
    total.value = result.total
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function changePage(value: number) {
  page.value = value
  load()
}

function openCreate() {
  editing.value = null
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    role: 'USER',
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: UserAdmin) {
  editing.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email ?? '',
    phone: row.phone ?? '',
    role: row.role,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  const payload: Record<string, unknown> = { ...form }
  if (editing.value == null) {
    await adminCreateUser(payload)
    ElMessage.success('已新增用户')
  } else {
    await adminUpdateUser(editing.value, payload)
    ElMessage.success('已更新用户')
  }
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="用户名 / 昵称"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="roleFilter" clearable placeholder="角色" class="filter-select">
        <el-option label="用户" value="USER" />
        <el-option label="管理" value="ADMIN" />
        <el-option label="监控" value="MONITOR" />
      </el-select>
      <el-select
        v-model="statusFilter"
        clearable
        placeholder="状态"
        class="filter-select"
      >
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" plain :icon="Plus" @click="openCreate">新增用户</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无用户">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.role)" effect="light">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="total > 20" class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="20"
          :current-page="page"
          @current-change="changePage"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editing == null ? '新增用户' : '编辑用户'"
      width="480px"
    >
      <el-form label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="editing != null" />
        </el-form-item>
        <el-form-item :label="editing == null ? '初始密码' : '重置密码'">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="editing == null ? '必填' : '留空则不修改'"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="用户端 USER" value="USER" />
            <el-option label="管理端 ADMIN" value="ADMIN" />
            <el-option label="监控台 MONITOR" value="MONITOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 130px;
}

.panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 16px;
}
</style>
