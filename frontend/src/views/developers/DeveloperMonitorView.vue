<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Platform, UserFilled, Warning } from '@element-plus/icons-vue'
import { fetchDevelopers } from '@/api/system'
import type { DeveloperMonitor } from '@/api/system'

const loading = ref(false)
const rows = ref<DeveloperMonitor[]>([])
const totalRequests = computed(() =>
  rows.value.reduce((sum, item) => sum + item.requestCount, 0),
)
const totalErrors = computed(() =>
  rows.value.reduce((sum, item) => sum + item.errorCount, 0),
)

async function load() {
  loading.value = true
  try {
    rows.value = await fetchDevelopers()
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function ownerTone(item: DeveloperMonitor) {
  if (item.errorRate >= 5) {
    return 'danger'
  }
  if (item.errorRate > 0) {
    return 'warning'
  }
  return 'success'
}

function durationText(ms: number) {
  return ms >= 1000 ? `${(ms / 1000).toFixed(1)}s` : `${ms}ms`
}

onMounted(load)
</script>

<template>
  <div class="developer-page">
    <div class="developer-grid">
      <article v-for="item in rows" :key="item.developerId" class="developer-card">
        <header>
          <el-avatar :size="48" class="developer-avatar">
            {{ item.name.slice(0, 1) }}
          </el-avatar>
          <div>
            <h3>{{ item.name }}</h3>
            <p>{{ item.employeeNo }} · {{ item.department }}</p>
          </div>
          <el-tag :type="ownerTone(item)" effect="dark">
            {{ item.errorRate.toFixed(1) }}% 异常
          </el-tag>
        </header>
        <div class="developer-stats">
          <div>
            <span>负责请求</span>
            <strong>{{ item.requestCount }}</strong>
          </div>
          <div>
            <span>异常</span>
            <strong :class="{ danger: item.errorCount > 0 }">{{ item.errorCount }}</strong>
          </div>
          <div>
            <span>累计耗时</span>
            <strong>{{ durationText(item.totalDurationMs) }}</strong>
          </div>
        </div>
      </article>
    </div>

    <div class="metric-grid">
      <div class="metric-cell">
        <span>开发人员</span>
        <strong>{{ rows.length }}</strong>
        <el-icon color="#5b6ee1"><Platform /></el-icon>
      </div>
      <div class="metric-cell">
        <span>负责请求</span>
        <strong>{{ totalRequests }}</strong>
        <el-icon color="var(--lg-green)"><UserFilled /></el-icon>
      </div>
      <div class="metric-cell" :class="{ danger: totalErrors > 0 }">
        <span>异常请求</span>
        <strong>{{ totalErrors }}</strong>
        <el-icon><Warning /></el-icon>
      </div>
    </div>

    <div v-loading="loading" class="developer-panel">
      <el-table :data="rows" style="width: 100%">
        <el-table-column prop="name" label="负责人" min-width="150" />
        <el-table-column prop="employeeNo" label="工号" min-width="120" />
        <el-table-column prop="department" label="部门" min-width="160" />
        <el-table-column label="请求量" width="110" align="right">
          <template #default="{ row }">{{ row.requestCount }}</template>
        </el-table-column>
        <el-table-column label="异常数" width="100" align="right">
          <template #default="{ row }">
            <span :class="{ danger: row.errorCount > 0 }">{{ row.errorCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="错误率" width="170">
          <template #default="{ row }">
            <div class="error-rate">
              <span :style="{ width: `${Math.min(100, row.errorRate)}%` }" />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.developer-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.developer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(330px, 1fr));
  gap: 14px;
}

.developer-card {
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.developer-card header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.developer-avatar {
  background: #2f6f58;
  color: #fff;
  font-weight: 700;
}

.developer-card h3 {
  margin: 0;
  color: #111827;
  font-size: 17px;
}

.developer-card p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 12px;
}

.developer-card header > .el-tag {
  margin-left: auto;
}

.developer-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.developer-stats > div {
  padding: 12px;
  background: #f7f9fa;
  border-radius: 6px;
}

.developer-stats span {
  display: block;
  color: #6b7280;
  font-size: 12px;
}

.developer-stats strong {
  display: block;
  margin-top: 6px;
  color: #111827;
  font-size: 20px;
}

.developer-stats strong.danger {
  color: var(--lg-danger);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.metric-cell.danger strong,
.metric-cell.danger .el-icon {
  color: var(--lg-danger);
}

.developer-panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.error-rate {
  width: 90px;
  height: 8px;
  overflow: hidden;
  border-radius: 99px;
  background: #eef2f1;
}

.error-rate span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--lg-danger);
}

.danger {
  color: var(--lg-danger);
  font-weight: 700;
}

@media (max-width: 720px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
