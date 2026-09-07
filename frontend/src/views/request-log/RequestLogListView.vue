<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Connection, Refresh, Search } from '@element-plus/icons-vue'
import { fetchRequestLogs } from '@/api/system'
import type { RequestLogRow } from '@/api/system'

const router = useRouter()
const loading = ref(false)
const rows = ref<RequestLogRow[]>([])
const total = ref(0)
const page = ref(1)
const requestId = ref('')
const uri = ref('')
const status = ref('all')

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: page.value,
      size: 20,
    }
    if (requestId.value.trim()) {
      params.requestId = requestId.value.trim()
    }
    if (uri.value.trim()) {
      params.uri = uri.value.trim()
    }
    if (status.value === 'success') {
      params.httpStatus = 200
    } else if (status.value !== 'all') {
      params.httpStatus = Number(status.value)
    }
    const result = await fetchRequestLogs(params)
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

function statusTag(row: RequestLogRow) {
  if (row.httpStatus >= 500) {
    return 'danger'
  }
  if (row.httpStatus >= 400) {
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
  <div class="request-page">
    <div class="filter-bar">
      <el-input v-model="requestId" clearable placeholder="Request ID" class="filter-input" />
      <el-input v-model="uri" clearable placeholder="接口路径" class="filter-input wide">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="status" class="filter-status" aria-label="请求状态">
        <el-option label="全部请求" value="all" />
        <el-option label="成功 2xx" value="success" />
        <el-option label="4xx 异常" value="400" />
        <el-option label="5xx 异常" value="500" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div v-loading="loading" class="request-panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无请求日志">
        <el-table-column label="请求时间" width="175">
          <template #default="{ row }">{{ row.requestTime }}</template>
        </el-table-column>
        <el-table-column label="用户" width="110">
          <template #default="{ row }">{{ row.username || '-' }}</template>
        </el-table-column>
        <el-table-column label="接口" min-width="220">
          <template #default="{ row }">
            <div class="request-uri">
              <el-tag size="small" effect="plain">{{ row.httpMethod }}</el-tag>
              <span>{{ row.uri }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="moduleName" label="模块" width="120" />
        <el-table-column prop="developerName" label="负责人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row)" effect="light">{{ row.httpStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ durationText(row.durationMs) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              :icon="Connection"
              @click="router.push(`/monitor/requests/${row.requestId}`)"
            >
              链路
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="total > 20" class="request-pagination">
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
  </div>
</template>

<style scoped>
.request-page {
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
  width: 190px;
}

.filter-input.wide {
  width: 260px;
}

.filter-status {
  width: 130px;
}

.request-panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.request-uri {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.request-uri span {
  overflow: hidden;
  color: #374151;
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-pagination {
  display: flex;
  justify-content: center;
  padding: 16px;
}
</style>
