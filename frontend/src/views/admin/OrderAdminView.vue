<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { adminOrders, adminUpdateOrderStatus } from '@/api/system'
import type { OrderAdmin } from '@/api/system'

const loading = ref(false)
const rows = ref<OrderAdmin[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const orderStatus = ref('')
const payStatus = ref('')

const dialogVisible = ref(false)
const current = ref<OrderAdmin | null>(null)
const form = reactive({ orderStatus: '', payStatus: '' })

const orderStatusTag = (s: string) => {
  if (s === 'PAID' || s === 'FINISHED') return 'success'
  if (s === 'CANCELLED') return 'danger'
  return 'info'
}

const payStatusTag = (s: string) => (s === 'PAID' ? 'success' : 'warning')

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 20 }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (orderStatus.value) {
      params.orderStatus = orderStatus.value
    }
    if (payStatus.value) {
      params.payStatus = payStatus.value
    }
    const result = await adminOrders(params)
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

function openStatus(row: OrderAdmin) {
  current.value = row
  form.orderStatus = row.orderStatus
  form.payStatus = row.payStatus
  dialogVisible.value = true
}

async function submit() {
  if (!current.value) {
    return
  }
  await adminUpdateOrderStatus(current.value.id, { ...form })
  ElMessage.success('订单状态已更新')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="订单号"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="orderStatus" clearable placeholder="订单状态" class="filter-select">
        <el-option label="已创建" value="CREATED" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="已完成" value="FINISHED" />
      </el-select>
      <el-select v-model="payStatus" clearable placeholder="支付状态" class="filter-select">
        <el-option label="未支付" value="UNPAID" />
        <el-option label="已支付" value="PAID" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无订单">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" min-width="220" />
        <el-table-column prop="username" label="用户" width="110" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderStatusTag(row.orderStatus)" effect="light">
              {{ row.orderStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="payStatusTag(row.payStatus)" effect="light">
              {{ row.payStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="90" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openStatus(row)">状态</el-button>
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

    <el-dialog v-model="dialogVisible" title="修改订单状态" width="420px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span class="mono">{{ current?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="form.orderStatus" style="width: 100%">
            <el-option label="已创建 CREATED" value="CREATED" />
            <el-option label="已支付 PAID" value="PAID" />
            <el-option label="已取消 CANCELLED" value="CANCELLED" />
            <el-option label="已完成 FINISHED" value="FINISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="form.payStatus" style="width: 100%">
            <el-option label="未支付 UNPAID" value="UNPAID" />
            <el-option label="已支付 PAID" value="PAID" />
          </el-select>
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
  width: 240px;
}

.filter-select {
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

.mono {
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 13px;
}
</style>
