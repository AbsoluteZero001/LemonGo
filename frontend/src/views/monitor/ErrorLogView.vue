<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { fetchErrorLogs } from '@/api/system'
import type { ErrorLogRow } from '@/api/system'

const loading = ref(false)
const rows = ref<ErrorLogRow[]>([])
const total = ref(0)
const page = ref(1)
const requestId = ref('')
const errorCode = ref<number | null>(null)

const detailVisible = ref(false)
const current = ref<ErrorLogRow | null>(null)

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 20 }
    if (requestId.value.trim()) {
      params.requestId = requestId.value.trim()
    }
    if (errorCode.value != null) {
      params.errorCode = errorCode.value
    }
    const result = await fetchErrorLogs(params)
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

function openDetail(row: ErrorLogRow) {
  current.value = row
  detailVisible.value = true
}

onMounted(load)
</script>

<template>
  <div class="error-page">
    <div class="filter-bar">
      <el-input
        v-model="requestId"
        clearable
        placeholder="Request ID"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input-number
        v-model="errorCode"
        :min="0"
        :max="999"
        controls-position="right"
        placeholder="错误码"
        class="filter-code"
      />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无异常日志">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="occurredAt" label="发生时间" width="175" />
        <el-table-column prop="requestId" label="Request ID" min-width="150" />
        <el-table-column prop="moduleName" label="模块" width="120" />
        <el-table-column prop="developerName" label="负责人" width="100" />
        <el-table-column label="错误码" width="90">
          <template #default="{ row }">
            <el-tag type="danger" effect="light">{{ row.errorCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorType" label="类型" width="140" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="220" />
        <el-table-column label="操作" width="90" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">堆栈</el-button>
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

    <el-dialog v-model="detailVisible" title="异常详情" width="640px">
      <template v-if="current">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="label">Request ID</span>
            <span class="value mono">{{ current.requestId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">错误码</span>
            <span class="value">{{ current.errorCode }}</span>
          </div>
          <div class="detail-item">
            <span class="label">异常类型</span>
            <span class="value">{{ current.errorType || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">异常类</span>
            <span class="value mono">{{ current.exceptionClass || '-' }}</span>
          </div>
          <div class="detail-item full">
            <span class="label">错误信息</span>
            <span class="value">{{ current.errorMessage || '-' }}</span>
          </div>
        </div>
        <div class="stack">
          <div class="stack-title">堆栈信息</div>
          <pre>{{ current.stackTrace || '无堆栈信息' }}</pre>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.error-page {
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

.filter-code {
  width: 150px;
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

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full {
  grid-column: 1 / -1;
}

.label {
  color: #6b7280;
  font-size: 12px;
}

.value {
  color: #1f2937;
  font-size: 14px;
  word-break: break-all;
}

.mono {
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
}

.stack {
  padding: 12px;
  background: #0f172a;
  border-radius: 8px;
}

.stack-title {
  margin-bottom: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.stack pre {
  max-height: 320px;
  margin: 0;
  overflow: auto;
  color: #e2e8f0;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
