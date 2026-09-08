<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Calendar, Connection, Timer, UserFilled, WarningFilled } from '@element-plus/icons-vue'
import { fetchUserActivity, fetchUserUsage } from '@/api/system'
import type { UserActivity, UserRequestUsage } from '@/api/system'

const loading = ref(false)
const date = ref(beijingDate())
const rows = ref<UserActivity[]>([])
const usageRows = ref<UserRequestUsage[]>([])
const activeTab = ref('activity')

const onlineCount = computed(
  () => rows.value.filter((item) => item.onlineStatus === 1).length,
)
const visitCount = computed(() =>
  rows.value.reduce((sum, item) => sum + item.todayVisits, 0),
)
const activeSeconds = computed(() =>
  rows.value.reduce((sum, item) => sum + item.activeSecondsTotal, 0),
)

async function load() {
  loading.value = true
  try {
    rows.value = await fetchUserActivity(date.value)
    try {
      usageRows.value = await fetchUserUsage(date.value)
    } catch {
      usageRows.value = []
    }
  } catch {
    rows.value = []
    usageRows.value = []
  } finally {
    loading.value = false
  }
}

const usageRequests = computed(() =>
  usageRows.value.reduce((sum, item) => sum + item.requestCount, 0),
)
const usageErrors = computed(() =>
  usageRows.value.reduce((sum, item) => sum + item.errorCount, 0),
)

function formatDuration(seconds: number) {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}h ${minutes}m`
  }
  return `${minutes}m`
}

function formatDateTime(value?: string) {
  return value || '-'
}

function usageDuration(ms: number) {
  return ms >= 1000 ? `${(ms / 1000).toFixed(1)}s` : `${ms}ms`
}

function changeDate(value: string) {
  date.value = value || beijingDate()
  load()
}

function beijingDate() {
  return new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Shanghai' }).format(new Date())
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
        <strong>{{ visitCount }}</strong>
        <el-icon color="#5b6ee1"><Timer /></el-icon>
      </div>
      <div class="metric-cell">
        <span>累计活跃时长</span>
        <strong>{{ formatDuration(activeSeconds) }}</strong>
        <el-icon color="#b7791f"><Timer /></el-icon>
      </div>
    </div>

    <div v-loading="loading" class="activity-panel">
      <el-tabs v-model="activeTab" class="activity-tabs">
        <el-tab-pane label="用户活跃" name="activity">
          <el-table :data="rows" style="width: 100%" empty-text="暂无登录访问记录">
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
            <el-table-column label="最后上线" width="175">
              <template #default="{ row }">{{ row.lastLoginTime || '-' }}</template>
            </el-table-column>
            <el-table-column label="最后退出" width="175">
              <template #default="{ row }">{{ row.lastLogoutTime || '-' }}</template>
            </el-table-column>
            <el-table-column label="最后在线" width="175">
              <template #default="{ row }">{{ row.lastActiveTime || '-' }}</template>
            </el-table-column>
            <el-table-column label="当日访问" width="110" align="right">
              <template #default="{ row }">{{ row.todayVisits }}</template>
            </el-table-column>
            <el-table-column label="累计访问" width="110" align="right">
              <template #default="{ row }">{{ row.totalVisits }}</template>
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
        </el-tab-pane>
        <el-tab-pane label="用户接口请求" name="usage">
          <div class="usage-summary">
            <span>
              <el-icon color="var(--lg-green)"><Connection /></el-icon>
              请求 {{ usageRequests }}
            </span>
            <span :class="{ danger: usageErrors > 0 }">
              <el-icon><WarningFilled /></el-icon>
              异常 {{ usageErrors }}
            </span>
          </div>
          <el-table
            :data="usageRows"
            style="width: 100%"
            empty-text="所选日期暂无用户接口请求"
          >
            <el-table-column label="用户" min-width="130">
              <template #default="{ row }">{{ row.username || '-' }}</template>
            </el-table-column>
            <el-table-column label="接口" min-width="260">
              <template #default="{ row }">
                <div class="usage-uri">
                  <el-tag size="small" effect="plain">{{ row.httpMethod }}</el-tag>
                  <span>{{ row.uri }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="moduleName" label="模块" width="120">
              <template #default="{ row }">{{ row.moduleName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="developerName" label="负责人" width="110">
              <template #default="{ row }">{{ row.developerName || '-' }}</template>
            </el-table-column>
            <el-table-column label="请求次数" width="110" align="right">
              <template #default="{ row }">{{ row.requestCount }}</template>
            </el-table-column>
            <el-table-column label="异常" width="80" align="right">
              <template #default="{ row }">
                <span :class="{ danger: row.errorCount > 0 }">{{ row.errorCount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="总耗时" width="100" align="right">
              <template #default="{ row }">{{ usageDuration(row.totalDurationMs) }}</template>
            </el-table-column>
            <el-table-column label="最后访问" width="175">
              <template #default="{ row }">{{ formatDateTime(row.lastRequestTime) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
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
  overflow-x: auto;
  padding: 4px 16px 12px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.activity-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}

.usage-summary {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 8px 0 12px;
  color: #374151;
  font-size: 13px;
}

.usage-summary span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.usage-summary .danger {
  color: var(--lg-danger);
}

.usage-uri {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.usage-uri span {
  overflow: hidden;
  color: #374151;
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.usage-summary .danger,
.el-table .danger {
  color: var(--lg-danger);
  font-weight: 700;
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

  .usage-summary {
    flex-wrap: wrap;
    gap: 8px 14px;
  }
}
</style>
