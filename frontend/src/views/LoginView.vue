<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const mode = ref<'login' | 'register'>('login')
const submitting = ref(false)
const progress = ref(0)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

const progressStatus = computed(() => (progress.value === 100 ? 'success' : ''))

function switchMode() {
  if (submitting.value) return
  mode.value = mode.value === 'login' ? 'register' : 'login'
  progress.value = 0
  form.password = ''
  form.confirmPassword = ''
}

async function submit() {
  if (submitting.value) return
  if (!form.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  if (!form.password) {
    ElMessage.error('请输入密码')
    return
  }
  if (mode.value === 'register') {
    if (form.password.length < 6) {
      ElMessage.error('密码长度不能少于 6 位')
      return
    }
    if (form.password !== form.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
  }

  submitting.value = true
  progress.value = 0
  const timer = window.setInterval(() => {
    if (progress.value < 90) {
      progress.value = Math.min(90, progress.value + 3)
    }
  }, 90)

  try {
    if (mode.value === 'login') {
      await auth.login(form.username.trim(), form.password)
    } else {
      await auth.register(form.username.trim(), form.password, form.nickname.trim() || undefined)
    }
    progress.value = 100
    await new Promise((resolve) => setTimeout(resolve, 260))
    ElMessage.success(
      mode.value === 'login'
        ? `欢迎回来，${auth.profile?.nickname ?? ''}`
        : '注册成功，欢迎加入 LemonGo',
    )
    const home =
      auth.role === 'ADMIN' ? '/admin' : auth.role === 'MONITOR' ? '/monitor' : '/products'
    router.push(home)
  } catch {
    progress.value = 0
  } finally {
    window.clearInterval(timer)
    submitting.value = false
  }
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
      <h2>{{ mode === 'login' ? '登录' : '注册' }}</h2>
      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="昵称（选填）">
          <el-input v-model="form.nickname" size="large" :prefix-icon="User" />
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
        <el-form-item v-if="mode === 'register'" label="确认密码">
          <el-input
            v-model="form.confirmPassword"
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
          :disabled="submitting"
          native-type="submit"
        >
          {{
            submitting
              ? mode === 'login'
                ? '登录中…'
                : '注册中…'
              : mode === 'login'
                ? '登录'
                : '注册'
          }}
        </el-button>
        <el-progress
          v-if="submitting"
          :percentage="progress"
          :stroke-width="6"
          :status="progressStatus"
          class="login-progress"
        />
      </el-form>
      <div class="login-switch">
        <span>{{ mode === 'login' ? '还没有账号？' : '已有账号？' }}</span>
        <el-button link type="primary" @click="switchMode">
          {{ mode === 'login' ? '去注册' : '去登录' }}
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

.login-progress {
  margin-top: 14px;
}

.login-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 18px;
  color: #6b7280;
  font-size: 13px;
}
</style>
