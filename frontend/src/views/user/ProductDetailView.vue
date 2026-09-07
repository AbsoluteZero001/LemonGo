<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import { addToCart, fetchProduct } from '@/api/system'
import type { Product } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref<Product | null>(null)
const quantity = ref(1)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    product.value = await fetchProduct(String(route.params.id))
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function add() {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  if (product.value) {
    await addToCart(product.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <el-button text :icon="ArrowLeft" class="back-button" @click="router.back()">
      返回
    </el-button>
    <template v-if="product">
      <div class="detail-shell">
        <div class="detail-media">
          <img :src="product.imageUrl || '/assets/products/lemon-box.svg'" :alt="product.productName" />
        </div>
        <div class="detail-copy">
          <el-tag effect="plain">{{ product.category }}</el-tag>
          <h1>{{ product.productName }}</h1>
          <p class="detail-text">{{ product.detailText }}</p>
          <div class="price-row">
            <strong>¥{{ Number(product.price).toFixed(2) }}</strong>
            <span>库存 {{ product.stock }} 件</span>
          </div>
          <div class="quantity-row">
            <span>数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(1, product.stock)" />
          </div>
          <el-button type="primary" size="large" :icon="ShoppingCart" @click="add">
            加入购物车
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 460px;
}

.back-button {
  margin-bottom: 12px;
}

.detail-shell {
  display: grid;
  grid-template-columns: minmax(320px, 520px) 1fr;
  gap: 34px;
  overflow: hidden;
  padding: 22px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.detail-media {
  overflow: hidden;
  border-radius: 8px;
  background: #f4f7f5;
}

.detail-media img {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 360px;
  object-fit: cover;
}

.detail-copy {
  padding: 12px 8px;
}

.detail-copy h1 {
  margin: 12px 0 8px;
  color: #111827;
  font-size: 24px;
  line-height: 1.35;
}

.detail-text {
  margin: 0;
  color: #4b5563;
  line-height: 1.8;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin: 22px 0;
}

.price-row strong {
  color: var(--lg-danger);
  font-size: 30px;
}

.price-row span {
  color: #6b7280;
  font-size: 13px;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 22px;
}

.quantity-row span {
  color: #374151;
  font-weight: 500;
}

@media (max-width: 860px) {
  .detail-shell {
    grid-template-columns: 1fr;
  }

  .detail-media img {
    min-height: auto;
    aspect-ratio: 4 / 3;
  }
}
</style>
