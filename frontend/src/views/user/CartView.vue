<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, ShoppingBag } from '@element-plus/icons-vue'
import {
  createOrder,
  fetchCart,
  removeCartItem,
  updateCartQuantity,
} from '@/api/system'
import type { CartItem } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const items = ref<CartItem[]>([])
const checked = reactive<Record<number, boolean>>({})
const checkoutVisible = ref(false)
const paying = ref(false)

const selectedItems = computed(() =>
  items.value.filter((item) => checked[item.id]),
)
const totalAmount = computed(() =>
  selectedItems.value.reduce(
    (sum, item) => sum + Number(item.price) * item.quantity,
    0,
  ),
)

async function load() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loading.value = true
  try {
    items.value = await fetchCart()
    items.value.forEach((item) => {
      checked[item.id] = item.checked === 1
    })
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function changeQuantity(item: CartItem, quantity?: number | null) {
  const next = Number(quantity ?? 1)
  try {
    await updateCartQuantity(item.id, next)
    item.quantity = next
  } catch {
    load()
  }
}

function onQuantityChange(item: CartItem, event: number | undefined | null) {
  changeQuantity(item, event)
}

async function remove(item: CartItem) {
  try {
    await removeCartItem(item.id)
    delete checked[item.id]
    items.value = items.value.filter((entry) => entry.id !== item.id)
    ElMessage.success('已移除')
  } catch {
    // The shared error trace panel shows the failure.
  }
}

function openCheckout() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择商品')
    return
  }
  checkoutVisible.value = true
}

async function submitOrder() {
  paying.value = true
  try {
    const ids = selectedItems.value.map((item) => item.id)
    await createOrder(ids)
    checkoutVisible.value = false
    ElMessage.success('订单已创建')
    router.push('/orders')
  } catch {
    // The shared error trace panel shows the failure owner.
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="cart-page">
    <div class="cart-heading">
      <span>购物车</span>
      <span class="cart-count">{{ items.length }} 件商品</span>
    </div>
    <div v-loading="loading" class="cart-list">
      <div v-for="item in items" :key="item.id" class="cart-row">
        <el-checkbox v-model="checked[item.id]" />
        <img
          class="cart-image"
          :src="item.imageUrl || '/assets/products/lemon-box.svg'"
          :alt="item.productName"
        />
        <div class="cart-product">
          <h3>{{ item.productName }}</h3>
          <span class="cart-category">{{ item.category }} · 库存 {{ item.stock }}</span>
        </div>
        <div class="cart-quantity">
          <el-input-number
            :model-value="item.quantity"
            :min="1"
            :max="Math.max(1, item.stock)"
            @change="onQuantityChange(item, $event)"
          />
        </div>
        <div class="cart-price">¥{{ (Number(item.price) * item.quantity).toFixed(2) }}</div>
        <el-button
          text
          circle
          aria-label="移除商品"
          @click="remove(item)"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      <el-empty v-if="!loading && items.length === 0" description="购物车还是空的">
        <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-if="items.length" class="cart-summary">
      <span>
        已选 <strong>{{ selectedItems.length }}</strong> 项
      </span>
      <span class="summary-amount">
        合计 <strong>¥{{ totalAmount.toFixed(2) }}</strong>
      </span>
      <el-button type="primary" :icon="ShoppingBag" @click="openCheckout">
        去结算
      </el-button>
    </div>

    <el-dialog v-model="checkoutVisible" title="确认结算" width="420px">
      <div class="checkout-lines">
        <div v-for="item in selectedItems" :key="item.id" class="checkout-line">
          <span>{{ item.productName }} × {{ item.quantity }}</span>
          <span>¥{{ (Number(item.price) * item.quantity).toFixed(2) }}</span>
        </div>
      </div>
      <div class="checkout-total">
        应付总额 <strong>¥{{ totalAmount.toFixed(2) }}</strong>
      </div>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="submitOrder">
          创建订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.cart-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-heading {
  display: flex;
  align-items: baseline;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.cart-count {
  color: #6b7280;
  font-size: 13px;
  font-weight: 400;
}

.cart-list {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  min-height: 180px;
}

.cart-row {
  display: grid;
  grid-template-columns: auto 92px minmax(160px, 1fr) 150px 110px auto;
  gap: 14px;
  align-items: center;
  min-height: 112px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-subtle);
}

.cart-row:last-child {
  border-bottom: none;
}

.cart-image {
  width: 86px;
  height: 86px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f7fa;
}

.cart-product h3 {
  margin: 0 0 6px;
  color: #1f2937;
  font-size: 15px;
  line-height: 1.4;
}

.cart-category {
  color: #6b7280;
  font-size: 12px;
}

.cart-price {
  color: var(--lg-danger);
  font-weight: 700;
  text-align: right;
}

.cart-summary {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 22px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: #4b5563;
}

.cart-summary strong {
  color: #111827;
}

.summary-amount strong {
  color: var(--lg-danger);
  font-size: 20px;
}

.checkout-lines {
  max-height: 280px;
  overflow: auto;
}

.checkout-line,
.checkout-total {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.checkout-total {
  margin-top: 8px;
  border-bottom: none;
  font-size: 15px;
}

.checkout-total strong {
  color: var(--lg-danger);
}

@media (max-width: 900px) {
  .cart-row {
    grid-template-columns: auto 76px 1fr auto;
  }

  .cart-quantity,
  .cart-price {
    grid-column: 3;
  }
}
</style>
