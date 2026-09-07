<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { FolderOpened, UserFilled } from '@element-plus/icons-vue'
import { fetchModules } from '@/api/system'
import type { ModuleMonitor } from '@/api/system'

const loading = ref(false)
const rows = ref<ModuleMonitor[]>([])
const totalRequests = computed(() =>
  rows.value.reduce((sum, item) => sum + item.requestCount, 0),
)
const totalErrors = computed(() =>
  rows.value.reduce((sum, item) => sum + item.errorCount, 0),
)
const owners = computed(() => new Set(rows.value.map((item) => item.developerName)).size)

async function load() {
  loading.value = true
  try {
    rows.value = await fetchModules()
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function errorRate(item: ModuleMonitor) {
  return item.requestCount === 0 ? 0 : (item.errorCount / item.requestCount) * 100
}

function durationText(ms: number) {
  return ms >= 1000 ? `${(ms / 1000).toFixed(2)}s` : `${ms}ms`
}

onMounted(load)
</script>

<template>
  <div class="module-page">
    <div class="metric-grid">
      <div class="metric-cell">
        <span>业务模块</span>
        <strong>{{ rows.length }}</strong>
        <el-icon color="#5b6ee1"><FolderOpened /></el-icon>
      </div>
      <div class="metric-cell">
        <span>当日请求</span>
        <strong>{{ totalRequests }}</strong>
        <el-icon color="var(--lg-green)"><FolderOpened /></el-icon>
      </div>
      <div class="metric-cell" :class="{ danger: totalErrors > 0 }">
        <span>当日异常</span>
        <strong>{{ totalErrors }}</strong>
        <el-icon><FolderOpened /></el-icon>
      </div>
      <div class="metric-cell">
        <span>负责人数</span>
        <strong>{{ owners }}</strong>
        <el-icon color="#7c3aed"><UserFilled /></el-icon>
      </div>
    </div>

    <div v-loading="loading" class="module-panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无模块数据">
        <el-table-column prop="moduleName" label="模块" min-width="140">
          <template #default="{ row }">
            <div class="module-name">
              <strong>{{ row.moduleName }}</strong>
              <el-tag size="small" effect="plain">{{ row.moduleCode }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="模块说明" min-width="200">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column label="负责人" min-width="170">
          <template #default="{ row }">
            <div class="owner-cell">
              <el-avatar :size="28">{{ row.developerName.slice(0, 1) }}</el-avatar>
              <div>
                <strong>{{ row.developerName }}</strong>
                <span>{{ row.employeeNo }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="当日请求" width="130" align="right">
          <template #default="{ row }">{{ row.requestCount }}</template>
        </el-table-column>
        <el-table-column label="成功" width="100" align="right">
          <template #default="{ row }">{{ row.successCount }}</template>
        </el-table-column>
        <el-table-column label="异常" width="90" align="right">
          <template #default="{ row }">
            <span :class="{ 'error-count': row.errorCount > 0 }">{{ row.errorCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="平均耗时" width="110" align="right">
          <template #default="{ row }">{{ durationText(row.avgDurationMs) }}</template>
        </el-table-column>
        <el-table-column label="错误率" width="160">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round(errorRate(row))"
              :stroke-width="7"
              :color="errorRate(row) > 0 ? '#d64545' : '#138a48'"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.module-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.metric-cell {
  position: relative;
  min-height: 104px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.metric-cell .el-icon {
  position: absolute;
  right: 16px;
  bottom: 14px;
}

.metric-cell span {
  color: #6b7280;
  font-size: 13px;
}

.metric-cell strong {
  display: block;
  margin-top: 8px;
  color: #111827;
  font-size: 26px;
}

.metric-cell.danger {
  background: #fff7f7;
}

.metric-cell.danger strong,
.metric-cell.danger .el-icon {
  color: var(--lg-danger);
}

.module-panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.module-name {
  display: flex;
  flex-direction: column;
  gap: 5px;
  align-items: flex-start;
}

.module-name strong {
  color: #1f2937;
}

.owner-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.owner-cell strong,
.owner-cell span {
  display: block;
}

.owner-cell span {
  color: #6b7280;
  font-size: 11px;
}

.error-count {
  color: var(--lg-danger);
  font-weight: 700;
}

@media (max-width: 720px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
