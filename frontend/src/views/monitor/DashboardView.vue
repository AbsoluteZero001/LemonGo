<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import type { ECharts, EChartsOption } from 'echarts'
import { CircleCheck, Connection, UserFilled, WarningFilled } from '@element-plus/icons-vue'
import { fetchDashboard, simulateError } from '@/api/system'
import type { DashboardVo } from '@/api/system'

const loading = ref(false)
const dashboard = ref<DashboardVo | null>(null)
const chartEl = ref<HTMLDivElement | null>(null)
const refreshing = ref(false)
const maxModule = computed(() =>
  Math.max(
    1,
    ...(dashboard.value?.moduleMetrics.map((item) => item.requestCount) ?? [1]),
  ),
)
let chart: ECharts | null = null
let timer: ReturnType<typeof setInterval> | undefined

async function load(quiet = false) {
  if (!quiet) {
    loading.value = true
  } else {
    refreshing.value = true
  }
  try {
    dashboard.value = await fetchDashboard()
    await nextTick()
    renderChart()
  } catch {
    dashboard.value = null
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function renderChart() {
  if (!chartEl.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  const dates = dashboard.value?.trend.map((point) => point.date.slice(5)) ?? []
  const requests = dashboard.value?.trend.map((point) => point.requestCount) ?? []
  const errors = dashboard.value?.trend.map((point) => point.errorCount) ?? []
  const option: EChartsOption = {
    color: ['#138a48', '#d64545'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['访问量', '异常'], top: 0, right: 0 },
    grid: { left: 40, right: 20, top: 42, bottom: 26 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '访问量',
        type: 'bar',
        data: requests,
        barMaxWidth: 28,
        itemStyle: { borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '异常',
        type: 'line',
        smooth: true,
        symbolSize: 7,
        data: errors,
      },
    ],
  }
  chart.setOption(option)
}

function resize() {
  chart?.resize()
}

async function triggerError(path: string) {
  try {
    await simulateError(path)
  } catch {
    // Error is shown by the shared panel.
  } finally {
    load(true)
  }
}

function healthClass(value: number) {
  return value > 0 ? 'metric-danger' : ''
}

onMounted(() => {
  load()
  timer = setInterval(() => load(true), 8000)
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
  window.removeEventListener('resize', resize)
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div class="metric-grid">
      <div class="metric-cell">
        <span class="metric-label">请求总量</span>
        <strong>{{ dashboard?.totalRequests ?? 0 }}</strong>
        <el-icon color="var(--lg-green)"><Connection /></el-icon>
      </div>
      <div class="metric-cell">
        <span class="metric-label">今日请求</span>
        <strong>{{ dashboard?.todayRequests ?? 0 }}</strong>
        <el-icon color="#5b6ee1"><CircleCheck /></el-icon>
      </div>
      <div class="metric-cell" :class="healthClass(dashboard?.todayErrors ?? 0)">
        <span class="metric-label">今日异常</span>
        <strong>{{ dashboard?.todayErrors ?? 0 }}</strong>
        <el-icon><WarningFilled /></el-icon>
      </div>
      <div class="metric-cell">
        <span class="metric-label">5 分钟在线</span>
        <strong>{{ dashboard?.onlineUsers ?? 0 }}</strong>
        <el-icon color="#7c3aed"><UserFilled /></el-icon>
      </div>
    </div>

    <div class="dashboard-grid">
      <section class="panel">
        <div class="panel-title">
          <span>近 7 日请求趋势</span>
          <el-button
            text
            :loading="refreshing"
            size="small"
            @click="load(true)"
          >
            刷新
          </el-button>
        </div>
        <div ref="chartEl" class="trend-chart" />
      </section>

      <section class="panel">
        <div class="panel-title">
          <span>模块访问量</span>
        </div>
        <div class="module-list">
          <div
            v-for="item in dashboard?.moduleMetrics ?? []"
            :key="item.moduleId"
            class="module-line"
          >
            <div class="module-head">
              <strong>{{ item.moduleName }}</strong>
              <span>{{ item.developerName }}</span>
            </div>
            <div class="module-bar">
              <span
                :style="{
                  width: `${Math.min(100, (item.requestCount / maxModule) * 100)}%`,
                }"
                :class="{ 'bar-danger': item.errorCount > 0 }"
              />
            </div>
            <div class="module-num">
              <b>{{ item.requestCount }}</b>
              <em v-if="item.errorCount">异常 {{ item.errorCount }}</em>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div class="bottom-grid">
      <section class="panel">
        <div class="panel-title">
          <span>热点 API</span>
        </div>
        <div class="api-list">
          <div v-for="item in dashboard?.apiMetrics ?? []" :key="item.apiId" class="api-line">
            <el-tag size="small" effect="plain">{{ item.httpMethod }}</el-tag>
            <span>{{ item.apiPath }}</span>
            <strong>{{ item.requestCount }}</strong>
            <el-tag v-if="item.errorCount > 0" type="danger" size="small">
              {{ item.errorCount }}
            </el-tag>
          </div>
          <el-empty
            v-if="!dashboard?.apiMetrics.length"
            description="暂无 API 访问"
            :image-size="56"
          />
        </div>
      </section>

      <section class="panel">
        <div class="panel-title">
          <span>异常演练</span>
        </div>
        <div class="error-actions">
          <el-button type="warning" plain @click="triggerError('400')">400</el-button>
          <el-button type="info" plain @click="triggerError('404')">404</el-button>
          <el-button type="danger" plain @click="triggerError('500')">500</el-button>
          <el-button type="danger" plain @click="triggerError('database')">数据库</el-button>
          <el-button type="danger" plain @click="triggerError('service')">Service</el-button>
        </div>
        <div class="exercise-note">
          <span class="exercise-stats">
            今日异常 {{ dashboard?.todayErrors ?? 0 }} · 最近刷新成功
          </span>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-cell {
  position: relative;
  min-height: 110px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.metric-cell .el-icon {
  position: absolute;
  right: 18px;
  bottom: 16px;
}

.metric-cell span {
  color: #6b7280;
  font-size: 13px;
}

.metric-cell strong {
  display: block;
  margin-top: 6px;
  color: #111827;
  font-size: 30px;
  line-height: 1;
}

.metric-cell.metric-danger {
  background: #fff7f7;
  border-color: #f1b8b8;
}

.metric-cell.metric-danger strong,
.metric-cell.metric-danger .el-icon {
  color: var(--lg-danger);
}

.dashboard-grid,
.bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(300px, 1fr);
  gap: 16px;
}

.bottom-grid {
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 1fr);
}

.panel {
  overflow: hidden;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #111827;
  font-weight: 700;
}

.trend-chart {
  width: 100%;
  height: 300px;
}

.module-list,
.api-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.module-line {
  display: grid;
  grid-template-columns: 1fr 76px;
  gap: 6px 12px;
  align-items: center;
}

.module-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #374151;
}

.module-head span {
  color: #6b7280;
  font-size: 12px;
}

.module-bar {
  height: 8px;
  overflow: hidden;
  grid-column: 1 / -1;
  border-radius: 99px;
  background: #eef2f1;
}

.module-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--lg-green);
}

.module-bar .bar-danger {
  background: var(--lg-danger);
}

.module-num {
  display: flex;
  justify-content: space-between;
  grid-column: 1 / -1;
}

.module-num b {
  color: #111827;
}

.module-num em {
  color: var(--lg-danger);
  font-size: 12px;
  font-style: normal;
}

.api-line {
  display: grid;
  grid-template-columns: 54px 1fr auto auto;
  gap: 10px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.api-line span {
  overflow: hidden;
  color: #374151;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.api-line strong {
  color: #111827;
}

.error-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.exercise-note {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px dashed #e5e7eb;
}

.exercise-note p {
  margin: 0 0 6px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.exercise-stats {
  color: #374151;
  font-size: 12px;
}

@media (max-width: 980px) {
  .metric-grid,
  .dashboard-grid,
  .bottom-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .metric-grid,
  .dashboard-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}
</style>
