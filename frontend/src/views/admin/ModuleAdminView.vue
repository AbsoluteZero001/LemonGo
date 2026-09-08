<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  adminCreateModule,
  adminDeleteModule,
  adminDevelopers,
  adminModules,
  adminUpdateModule,
} from '@/api/system'
import type { SysDeveloper, SysModule } from '@/api/system'

const loading = ref(false)
const rows = ref<SysModule[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const developers = ref<SysDeveloper[]>([])

const dialogVisible = ref(false)
const editing = ref<number | null>(null)
const form = reactive({
  moduleName: '',
  moduleCode: '',
  description: '',
  developerId: 0 as number,
  status: 1,
})

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 20 }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const result = await adminModules(params)
    rows.value = result.records
    total.value = result.total
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function loadDevelopers() {
  try {
    const result = await adminDevelopers({ page: 1, size: 1000 })
    developers.value = result.records
  } catch {
    developers.value = []
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

function developerName(id: number) {
  return developers.value.find((d) => d.id === id)?.name ?? `#${id}`
}

function openCreate() {
  editing.value = null
  Object.assign(form, {
    moduleName: '',
    moduleCode: '',
    description: '',
    developerId: developers.value[0]?.id ?? 0,
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: SysModule) {
  editing.value = row.id
  Object.assign(form, {
    moduleName: row.moduleName,
    moduleCode: row.moduleCode,
    description: row.description ?? '',
    developerId: row.developerId,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  const payload: Record<string, unknown> = { ...form }
  if (editing.value == null) {
    await adminCreateModule(payload)
    ElMessage.success('已新增模块')
  } else {
    await adminUpdateModule(editing.value, payload)
    ElMessage.success('已更新模块')
  }
  dialogVisible.value = false
  load()
}

async function remove(row: SysModule) {
  await ElMessageBox.confirm(`确认删除模块「${row.moduleName}」？`, '删除模块', {
    type: 'warning',
  })
  await adminDeleteModule(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => {
  load()
  loadDevelopers()
})
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="模块名称 / 编码"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" plain :icon="Plus" @click="openCreate">新增模块</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无模块">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="moduleName" label="模块名称" width="150" />
        <el-table-column prop="moduleCode" label="编码" width="110" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="负责人" width="110">
          <template #default="{ row }">{{ developerName(row.developerId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
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
      :title="editing == null ? '新增模块' : '编辑模块'"
      width="440px"
    >
      <el-form label-width="90px">
        <el-form-item label="模块名称">
          <el-input v-model="form.moduleName" />
        </el-form-item>
        <el-form-item label="模块编码">
          <el-input v-model="form.moduleCode" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.developerId" style="width: 100%">
            <el-option
              v-for="d in developers"
              :key="d.id"
              :label="`${d.name} (${d.employeeNo})`"
              :value="d.id"
            />
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
  width: 220px;
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
