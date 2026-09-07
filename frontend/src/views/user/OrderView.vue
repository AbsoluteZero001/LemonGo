<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, View } from '@element-plus/icons-vue'
import { fetchOrder, fetchOrders, payOrder } from '@/api/system'
import type { OrderDetail, OrderSummary } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const orders = ref<OrderSummary[]>([])
const total = ref(0)
const page = ref(1)
const detailVisible = ref(false)
const detail = ref<OrderDetail | null>(null)
const detailLoading = ref(false)

async function load() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const result = await fetchOrders({ page: page.value, size: 20 })
    orders.value = result.records
    total.value = result.total
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function statusType(order: OrderSummary) {
  if (order.payStatus === 'PAID') {
    return 'success'
  }
  if (order.orderStatus === 'CANCELLED') {
    return 'info'
  }
  return 'warning'
}

function statusText(order: OrderSummary) {
  if (order.payStatus === 'PAID') {
    return '已支付'
  }
  return order.orderStatus === 'CANCELLED' ? '已取消' : '待支付'
}

async function openDetail(order: OrderSummary) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await fetchOrder(order.id)
  } catch {
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

async function pay(order: OrderSummary) {
  try {
    await payOrder(order.id)
    ElMessage.success('模拟支付成功')
    load()
  } catch {
    // Shown by the shared error panel.
  }
}

function payCurrent() {
  if (detail.value) {
    pay(detail.value)
  }
}

onMounted(load)
</script>

<template>
  <div class="order-page">
    <div class="order-heading">
      <span>我的订单</span>
      <span class="order-count">共 {{ total }} 笔</span>
    </div>

    <div v-loading="loading" class="order-panel">
      <el-table :data="orders" style="width: 100%" empty-text="暂无订单">
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column label="下单时间" min-width="170">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="金额" width="130">
          <template #default="{ row }">
            <strong class="amount">¥{{ Number(row.totalAmount).toFixed(2) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" effect="light">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="right">
          <template #default="{ row }">
            <el-button text :icon="View" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.payStatus === 'UNPAID'"
              type="primary"
              text
              :icon="Check"
              @click="pay(row)"
            >
              模拟支付
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="total > 20" class="order-pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="20"
          :current-page="page"
          @current-change="(value: number) => { page = value; load() }"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <div class="order-detail-head">
            <div>
              <strong>{{ detail.orderNo }}</strong>
              <p>下单于 {{ detail.createdAt }}</p>
            </div>
            <el-tag :type="detail.payStatus === 'PAID' ? 'success' : 'warning'">
              {{ statusText(detail) }}
            </el-tag>
          </div>
          <div class="order-items">
            <div v-for="item in detail.items" :key="item.id" class="order-item">
              <img
                :src="item.productImageUrl || '/assets/products/lemon-box.svg'"
                :alt="item.productName"
              />
              <div>
                <h4>{{ item.productName }}</h4>
                <span>¥{{ Number(item.unitPrice).toFixed(2) }} × {{ item.quantity }}</span>
              </div>
              <strong>¥{{ Number(item.subtotal).toFixed(2) }}</strong>
            </div>
          </div>
          <div class="order-detail-total">
            合计 <strong>¥{{ Number(detail.totalAmount).toFixed(2) }}</strong>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail?.payStatus === 'UNPAID'"
          type="primary"
          @click="payCurrent"
        >
          模拟支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-heading {
  display: flex;
  align-items: baseline;
  gap: 12px;
  color: #111827;
  font-size: 20px;
  font-weight: 700;
}

.order-count {
  color: #6b7280;
  font-size: 13px;
  font-weight: 400;
}

.order-panel {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.amount {
  color: var(--lg-danger);
}

.order-pagination {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.order-detail-head,
.order-detail-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.order-detail-head p {
  margin: 5px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.order-items {
  margin: 16px 0;
}

.order-item {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 14px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.order-item img {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
}

.order-item h4 {
  margin: 0 0 6px;
  color: #1f2937;
  font-size: 14px;
}

.order-item span {
  color: #6b7280;
  font-size: 13px;
}

.order-detail-total {
  justify-content: flex-end;
}

.order-detail-total strong {
  color: var(--lg-danger);
  font-size: 20px;
}
</style>
