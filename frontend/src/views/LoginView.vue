<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: 'zhangsan',
  password: 'zhangsan-123456',
})

async function submit() {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success(`欢迎回来，${auth.profile?.nickname ?? ''}`)
    router.push('/products')
  } catch {
    // The shared error panel already shows the backend trace.
  } finally {
    loading.value = false
  }
}

function chooseDemo(username: string, password: string) {
  form.username = username
  form.password = password
}
</script>

<template>
  <div class="login-layout">
    <section class="login-panel">
      <div class="login-brand">
        <div class="brand-mark">LG</div>
        <div>
          <h1>LemonGo</h1>
          <p>乐檬购</p>
        </div>
      </div>
      <h2>登录</h2>
      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-button"
          :loading="loading"
          native-type="submit"
        >
          登录
        </el-button>
      </el-form>
      <div class="demo-row">
        <el-button size="small" @click="chooseDemo('zhangsan', 'zhangsan-123456')">
          张三
        </el-button>
        <el-button size="small" @click="chooseDemo('lisi', 'lisi-123456')">
          李四
        </el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-layout {
  display: grid;
  min-height: calc(100vh - 148px);
  place-items: center;
  padding: 24px;
}

.login-panel {
  width: min(380px, 100%);
  padding: 28px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 10px;
  background: var(--lg-green);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}

.login-brand h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  line-height: 1.2;
}

.login-brand p {
  margin: 3px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.login-panel h2 {
  margin: 0 0 18px;
  color: #1f2937;
  font-size: 18px;
}

.login-button {
  width: 100%;
  margin-top: 6px;
}

.demo-row {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
