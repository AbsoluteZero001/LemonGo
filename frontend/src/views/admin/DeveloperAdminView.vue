<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  adminCreateDeveloper,
  adminDeleteDeveloper,
  adminDevelopers,
  adminUpdateDeveloper,
} from '@/api/system'
import type { SysDeveloper } from '@/api/system'

const loading = ref(false)
const rows = ref<SysDeveloper[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')

const dialogVisible = ref(false)
const editing = ref<number | null>(null)
const form = reactive({
  name: '',
  employeeNo: '',
  email: '',
  department: '',
  status: 1,
})

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 20 }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const result = await adminDevelopers(params)
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
  Object.assign(form, { name: '', employeeNo: '', email: '', department: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row: SysDeveloper) {
  editing.value = row.id
  Object.assign(form, {
    name: row.name,
    employeeNo: row.employeeNo,
    email: row.email ?? '',
    department: row.department,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  const payload: Record<string, unknown> = { ...form }
  if (editing.value == null) {
    await adminCreateDeveloper(payload)
    ElMessage.success('已新增开发者')
  } else {
    await adminUpdateDeveloper(editing.value, payload)
    ElMessage.success('已更新开发者')
  }
  dialogVisible.value = false
  load()
}

async function remove(row: SysDeveloper) {
  await ElMessageBox.confirm(`确认删除开发者「${row.name}」？`, '删除开发者', {
    type: 'warning',
  })
  await adminDeleteDeveloper(row.id)
  ElMessage.success('已删除')
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
        placeholder="搜索姓名"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" plain :icon="Plus" @click="openCreate">新增开发者</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无开发者">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="employeeNo" label="工号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="department" label="部门" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '在职' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="remove(row)">删除</el-button>
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
      :title="editing == null ? '新增开发者' : '编辑开发者'"
      width="440px"
    >
      <el-form label-width="90px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="form.employeeNo" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在职"
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
