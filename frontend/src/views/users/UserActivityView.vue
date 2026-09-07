<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Calendar, Timer, UserFilled } from '@element-plus/icons-vue'
import { fetchUserActivity } from '@/api/system'
import type { UserActivity } from '@/api/system'

const loading = ref(false)
const date = ref(new Date().toISOString().slice(0, 10))
const rows = ref<UserActivity[]>([])

const onlineCount = computed(
  () => rows.value.filter((item) => item.onlineStatus === 1).length,
)
const requestCount = computed(() =>
  rows.value.reduce((sum, item) => sum + item.requestCountToday, 0),
)
const activeSeconds = computed(() =>
  rows.value.reduce((sum, item) => sum + item.activeSecondsToday, 0),
)

async function load() {
  loading.value = true
  try {
    rows.value = await fetchUserActivity(date.value)
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function formatDuration(seconds: number) {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}h ${minutes}m`
  }
  return `${minutes}m`
}

function changeDate(value: string) {
  date.value = value || new Date().toISOString().slice(0, 10)
  load()
}

onMounted(load)
</script>

<template>
  <div class="activity-page">
    <div class="activity-toolbar">
      <el-date-picker
        v-model="date"
        type="date"
        value-format="YYYY-MM-DD"
        :prefix-icon="Calendar"
        @change="changeDate"
      />
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-cell">
        <span>在线用户</span>
        <strong>{{ onlineCount }}</strong>
        <el-icon color="var(--lg-green)"><UserFilled /></el-icon>
      </div>
      <div class="metric-cell">
        <span>当日访问</span>
        <strong>{{ requestCount }}</strong>
        <el-icon color="#5b6ee1"><Timer /></el-icon>
      </div>
      <div class="metric-cell">
        <span>累计活跃</span>
        <strong>{{ formatDuration(activeSeconds) }}</strong>
        <el-icon color="#b7791f"><Timer /></el-icon>
      </div>
    </div>

    <div v-loading="loading" class="activity-panel">
      <el-table :data="rows" style="width: 100%" empty-text="当日暂无活跃记录">
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32">{{ row.nickname.slice(0, 1) }}</el-avatar>
              <div>
                <strong>{{ row.nickname }}</strong>
                <span>@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最近上线" width="180">
          <template #default="{ row }">{{ row.lastLoginTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="最近活跃" width="180">
          <template #default="{ row }">{{ row.lastActiveTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="当日访问" width="110" align="right">
          <template #default="{ row }">{{ row.requestCountToday }}</template>
        </el-table-column>
        <el-table-column label="累计访问" width="110" align="right">
          <template #default="{ row }">{{ row.requestCountTotal }}</template>
        </el-table-column>
        <el-table-column label="活跃时长" width="120" align="right">
          <template #default="{ row }">
            <strong>{{ formatDuration(row.activeSecondsTotal) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="活跃度" width="190">
          <template #default="{ row }">
            <div class="score-line">
              <el-progress
                :percentage="row.activityScore"
                :stroke-width="8"
                :color="row.activityScore >= 60 ? '#138a48' : '#e9a23b'"
              />
              <b>{{ row.activityScore }}</b>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.onlineStatus === 1 ? 'success' : 'info'" effect="light">
              {{ row.onlineStatus === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.activity-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-toolbar {
  display: flex;
  gap: 10px;
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

.activity-panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-cell strong,
.user-cell span {
  display: block;
}

.user-cell strong {
  color: #1f2937;
}

.user-cell span {
  color: #6b7280;
  font-size: 12px;
}

.score-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-line .el-progress {
  flex: 1;
}

.score-line b {
  width: 24px;
  color: #374151;
  text-align: right;
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
