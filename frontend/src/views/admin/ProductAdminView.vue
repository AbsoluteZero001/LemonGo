<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  adminCreateProduct,
  adminDeleteProduct,
  adminProducts,
  adminRestoreProduct,
  adminUpdateProduct,
} from '@/api/system'
import type { Product } from '@/api/system'

const loading = ref(false)
const rows = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')

const dialogVisible = ref(false)
const editing = ref<number | null>(null)
const form = reactive({
  productName: '',
  category: '',
  price: 0,
  stock: 0,
  imageUrl: '',
  detailText: '',
})

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 20 }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const result = await adminProducts(params)
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

function openCreate() {
  editing.value = null
  Object.assign(form, {
    productName: '',
    category: '',
    price: 0,
    stock: 0,
    imageUrl: '',
    detailText: '',
  })
  dialogVisible.value = true
}

function openEdit(row: Product) {
  editing.value = row.id
  Object.assign(form, {
    productName: row.productName,
    category: row.category,
    price: Number(row.price),
    stock: row.stock,
    imageUrl: row.imageUrl ?? '',
    detailText: row.detailText ?? '',
  })
  dialogVisible.value = true
}

async function submit() {
  const payload: Record<string, unknown> = { ...form }
  if (editing.value == null) {
    await adminCreateProduct(payload)
    ElMessage.success('已新增商品')
  } else {
    await adminUpdateProduct(editing.value, payload)
    ElMessage.success('已更新商品')
  }
  dialogVisible.value = false
  load()
}

async function remove(row: Product) {
  await ElMessageBox.confirm(`确认删除「${row.productName}」？`, '删除商品', {
    type: 'warning',
  })
  await adminDeleteProduct(row.id)
  ElMessage.success('已删除')
  load()
}

async function restore(row: Product) {
  await adminRestoreProduct(row.id)
  ElMessage.success('已恢复上架')
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
        placeholder="搜索商品名称"
        class="filter-input"
        @keyup.enter="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" plain :icon="Plus" @click="openCreate">新增商品</el-button>
    </div>

    <div v-loading="loading" class="panel">
      <el-table :data="rows" style="width: 100%" empty-text="暂无商品">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="sales" label="销量" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否删除" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.deleted === 1" type="danger" effect="light">已删除</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.deleted !== 1"
              text
              type="danger"
              @click="remove(row)"
            >
              删除
            </el-button>
            <el-button
              v-else
              text
              type="success"
              @click="restore(row)"
            >
              恢复
            </el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editing == null ? '新增商品' : '编辑商品'"
      width="480px"
    >
      <el-form label-width="90px">
        <el-form-item label="商品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.detailText" type="textarea" :rows="3" />
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
</style>
