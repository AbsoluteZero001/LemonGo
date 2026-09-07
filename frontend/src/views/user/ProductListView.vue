<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { addToCart, fetchProducts } from '@/api/system'
import type { Product } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const category = ref('全部')
const categories = ['全部', '生鲜水果', '厨房电器', '运动户外', '箱包配件', '家居生活']

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: page.value,
      size: 12,
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (category.value && category.value !== '全部') {
      params.category = category.value
    }
    const result = await fetchProducts(params)
    products.value = result.records
    total.value = result.total
  } catch {
    products.value = []
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

function changeCategory(value: string | number | boolean | undefined | null) {
  category.value = String(value ?? '')
  search()
}

async function add(product: Product) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  await addToCart(product.id, 1)
  ElMessage.success('已加入购物车')
}

onMounted(load)
</script>

<template>
  <div class="product-page">
    <div class="toolbar">
      <div class="toolbar-title">
        <span>商品列表</span>
        <span class="toolbar-sub">共 {{ total }} 款</span>
      </div>
      <div class="filters">
        <el-select
          v-model="category"
          class="category-select"
          aria-label="商品分类"
          @change="changeCategory"
        >
          <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索商品"
          class="search-input"
          @keyup.enter="search"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>
    </div>

    <div v-loading="loading" class="product-grid" :class="{ empty: products.length === 0 }">
      <article v-for="product in products" :key="product.id" class="product-card">
        <button
          class="product-image"
          type="button"
          aria-label="查看商品详情"
          @click="router.push(`/products/${product.id}`)"
        >
          <img :src="product.imageUrl || '/assets/products/lemon-box.svg'" :alt="product.productName" />
          <span class="category-pill">{{ product.category }}</span>
        </button>
        <div class="product-info">
          <h3>{{ product.productName }}</h3>
          <div class="product-meta">
            <strong>¥{{ Number(product.price).toFixed(2) }}</strong>
            <span>库存 {{ product.stock }}</span>
          </div>
          <div class="product-footer">
            <span>已售 {{ product.sales }}</span>
            <el-button type="primary" round :icon="Plus" @click="add(product)">
              加购
            </el-button>
          </div>
        </div>
      </article>
      <el-empty v-if="!loading && products.length === 0" description="暂无匹配商品" />
    </div>

    <div class="pagination-row">
      <el-pagination
        v-if="total > 12"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="12"
        :current-page="page"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.product-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  color: #111827;
  font-size: 20px;
  font-weight: 700;
}

.toolbar-sub {
  color: #6b7280;
  font-size: 13px;
  font-weight: 400;
}

.filters {
  display: flex;
  gap: 10px;
}

.category-select {
  width: 138px;
}

.search-input {
  width: 220px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  min-height: 280px;
}

.product-grid.empty {
  display: block;
}

.product-card {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(17, 24, 39, 0.1);
  transform: translateY(-2px);
}

.product-image {
  position: relative;
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  padding: 0;
  overflow: hidden;
  border: none;
  background: #f8fafc;
  cursor: pointer;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-pill {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.72);
  color: #fff;
  font-size: 11px;
}

.product-info {
  padding: 14px;
}

.product-info h3 {
  min-height: 44px;
  margin: 0;
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
}

.product-meta,
.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 10px;
}

.product-meta strong {
  color: var(--lg-danger);
  font-size: 17px;
}

.product-meta span,
.product-footer span {
  color: #6b7280;
  font-size: 12px;
}

.product-footer {
  margin-top: 14px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  padding: 10px 0 4px;
}
</style>
