<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import {
  adminApis,
  adminCreateApi,
  adminDeleteApi,
  adminDevelopers,
  adminModules,
  adminRefreshApis,
  adminUpdateApi,
} from '@/api/system'
import type { SysApi, SysDeveloper, SysModule } from '@/api/system'

const loading = ref(false)
const rows = ref<SysApi[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const modules = ref<SysModule[]>([])
const developers = ref<SysDeveloper[]>([])

const dialogVisible = ref(false)
const editing = ref<number | null>(null)
const form = reactive({
  apiPath: '',
  httpMethod: 'GET',
  moduleId: 0 as number,
  controllerName: '',
  controllerMethod: '',
  serviceName: '',
  mapperName: '',
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
    const result = await adminApis(params)
    rows.value = result.records
    total.value = result.total
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [moduleResult, developerResult] = await Promise.all([
      adminModules({ page: 1, size: 1000 }),
      adminDevelopers({ page: 1, size: 1000 }),
    ])
    modules.value = moduleResult.records
    developers.value = developerResult.records
  } catch {
    modules.value = []
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

function moduleName(id: number) {
  return modules.value.find((m) => m.id === id)?.moduleName ?? `#${id}`
}

function developerName(id: number) {
  return developers.value.find((d) => d.id === id)?.name ?? `#${id}`
}

function openCreate() {
  editing.value = null
  Object.assign(form, {
    apiPath: '',
    httpMethod: 'GET',
    moduleId: modules.value[0]?.id ?? 0,
    controllerName: '',
    controllerMethod: '',
    serviceName: '',
    mapperName: '',
    description: '',
    developerId: developers.value[0]?.id ?? 0,
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: SysApi) {
  editing.value = row.id
  Object.assign(form, {
    apiPath: row.apiPath,
    httpMethod: row.httpMethod,
    moduleId: row.moduleId,
    controllerName: row.controllerName ?? '',
    controllerMethod: row.controllerMethod ?? '',
    serviceName: row.serviceName ?? '',
    mapperName: row.mapperName ?? '',
    description: row.description ?? '',
    developerId: row.developerId,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submit() {
  const payload: Record<string, unknown> = { ...form }
  if (editing.value == null) {
    await adminCreateApi(payload)
    ElMessage.success('已新增接口')
  } else {
    await adminUpdateApi(editing.value, payload)
    ElMessage.success('已更新接口')
  }
  dialogVisible.value = false
  load()
}

async function remove(row: SysApi) {
  await ElMessageBox.confirm(`确认删除接口「${row.apiPath}」？`, '删除接口', {
    type: 'warning',
  })
  await adminDeleteApi(row.id)
  ElMessage.success('已删除')
  load()
}

async function refresh() {
  await adminRefreshApis()
  ElMessage.success('接口注册表已刷新')
}

onMounted(() => {
  load()
  loadOptions()
})
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="接口路径 / 控制器"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" plain :icon="Plus" @click="openCreate">新增接口</el-button>
      <el-button :icon="RefreshRight" @click="refresh">刷新注册表</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无接口">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="方法" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.httpMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiPath" label="接口路径" min-width="220" />
        <el-table-column label="模块" width="120">
          <template #default="{ row }">{{ moduleName(row.moduleId) }}</template>
        </el-table-column>
        <el-table-column label="负责人" width="100">
          <template #default="{ row }">{{ developerName(row.developerId) }}</template>
        </el-table-column>
        <el-table-column prop="controllerMethod" label="方法" min-width="130" />
        <el-table-column label="状态" width="80">
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
      :title="editing == null ? '新增接口' : '编辑接口'"
      width="520px"
    >
      <el-form label-width="90px">
        <el-form-item label="接口路径">
          <el-input v-model="form.apiPath" placeholder="/api/xxx/{id}" />
        </el-form-item>
        <el-form-item label="请求方法">
          <el-select v-model="form.httpMethod" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属模块">
          <el-select v-model="form.moduleId" style="width: 100%">
            <el-option
              v-for="m in modules"
              :key="m.id"
              :label="`${m.moduleName} (${m.moduleCode})`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="控制器">
          <el-input v-model="form.controllerName" />
        </el-form-item>
        <el-form-item label="方法">
          <el-input v-model="form.controllerMethod" />
        </el-form-item>
        <el-form-item label="Service">
          <el-input v-model="form.serviceName" />
        </el-form-item>
        <el-form-item label="Mapper">
          <el-input v-model="form.mapperName" />
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
