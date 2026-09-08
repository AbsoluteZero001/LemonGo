<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchApiRegistry } from '@/api/system'
import type { ApiRegistry } from '@/api/system'

const loading = ref(false)
const rows = ref<ApiRegistry[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = await fetchApiRegistry()
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="api-registry-page">
    <div class="filter-bar">
      <span class="title">接口注册表</span>
      <span class="sub">共 {{ rows.length }} 条</span>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
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
        <el-table-column prop="moduleName" label="模块" width="130" />
        <el-table-column prop="developerName" label="负责人" width="100" />
        <el-table-column prop="controllerName" label="控制器" width="180" />
        <el-table-column prop="controllerMethod" label="方法" width="150" />
        <el-table-column prop="serviceName" label="Service" min-width="180" />
        <el-table-column prop="mapperName" label="Mapper" width="160" />
        <el-table-column prop="description" label="描述" min-width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.api-registry-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.sub {
  color: #6b7280;
  font-size: 13px;
}

.panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}
</style>
