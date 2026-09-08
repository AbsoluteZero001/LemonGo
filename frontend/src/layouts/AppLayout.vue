<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Clock, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const currentTitle = computed(() => String(route.meta.title ?? 'LemonGo'))
const now = ref(new Date())
let clockTimer: ReturnType<typeof setInterval> | undefined
let heartbeatTimer: ReturnType<typeof setInterval> | undefined

const beijingTime = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    timeZone: 'Asia/Shanghai',
    hour12: false,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(now.value),
)

function sendHeartbeat() {
  if (auth.isLoggedIn && document.visibilityState === 'visible') {
    void auth.heartbeat()
  }
}

function handleVisibility() {
  if (document.visibilityState === 'visible') {
    sendHeartbeat()
  }
}

watch(
  () => auth.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      sendHeartbeat()
    }
  },
)

onMounted(() => {
  auth.refreshProfile().catch(() => undefined)
  sendHeartbeat()
  clockTimer = setInterval(() => {
    now.value = new Date()
  }, 1000)
  heartbeatTimer = setInterval(sendHeartbeat, 15_000)
  document.addEventListener('visibilitychange', handleVisibility)
})

onBeforeUnmount(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
  }
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
  }
  document.removeEventListener('visibilitychange', handleVisibility)
})

const userMenu = [
  { path: '/products', title: '商品', icon: 'Goods' },
  { path: '/cart', title: '购物车', icon: 'ShoppingCart' },
  { path: '/orders', title: '订单', icon: 'Tickets' },
  { path: '/profile', title: '个人中心', icon: 'User' },
]

const monitorMenu = [
  { path: '/monitor', title: '监控总览', icon: 'DataLine' },
  { path: '/monitor/live', title: '实时链路', icon: 'Connection' },
  { path: '/monitor/requests', title: '请求日志', icon: 'Document' },
  { path: '/monitor/apis', title: '接口注册表', icon: 'Link' },
  { path: '/monitor/errors', title: '异常日志', icon: 'Warning' },
  { path: '/monitor/users', title: '用户活跃', icon: 'UserFilled' },
  { path: '/monitor/modules', title: '模块监控', icon: 'FolderOpened' },
  { path: '/monitor/developers', title: '开发者', icon: 'Platform' },
]

const adminMenu = [
  { path: '/admin/products', title: '商品管理', icon: 'Goods' },
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单管理', icon: 'Tickets' },
  { path: '/admin/developers', title: '开发者管理', icon: 'Avatar' },
  { path: '/admin/modules', title: '模块管理', icon: 'FolderOpened' },
  { path: '/admin/apis', title: '接口管理', icon: 'Link' },
]

const roleLabel = computed(() => {
  if (auth.role === 'ADMIN') {
    return '管理端'
  }
  if (auth.role === 'MONITOR') {
    return '监控台'
  }
  return '用户端'
})

async function logout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside width="224px" class="app-aside">
      <div class="brand">
        <div class="brand-mark">LG</div>
        <div>
          <div class="brand-name">LemonGo</div>
          <div class="brand-sub">乐檬购</div>
        </div>
      </div>

      <el-menu :default-active="route.path" router class="app-menu">
        <el-menu-item-group v-if="auth.role === 'USER'" title="用户端">
          <el-menu-item v-for="item in userMenu" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group v-if="auth.role === 'ADMIN'" title="管理端">
          <el-menu-item v-for="item in adminMenu" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group v-if="auth.role === 'MONITOR'" title="监控台">
          <el-menu-item v-for="item in monitorMenu" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-inner">
          <span class="page-title">{{ currentTitle }}</span>
          <div class="header-right">
            <div class="beijing-clock" aria-label="北京时间">
              <el-icon><Clock /></el-icon>
              <span>北京时间</span>
              <b>{{ beijingTime }}</b>
            </div>
            <div v-if="auth.profile" class="user-box">
              <el-tag effect="plain" round class="role-tag">{{ roleLabel }}</el-tag>
              <el-tag effect="plain" round class="user-tag">
                {{ auth.profile.nickname }}
              </el-tag>
              <el-button text circle aria-label="退出登录" @click="logout">
                <el-icon><SwitchButton /></el-icon>
              </el-button>
            </div>
            <el-button v-else type="primary" plain @click="router.push('/login')">
              登录
            </el-button>
          </div>
        </div>
      </el-header>
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-aside {
  border-right: 1px solid var(--border-subtle);
  background: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 0 18px;
  border-bottom: 1px solid var(--border-subtle);
}

.brand-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 8px;
  background: var(--lg-green);
  color: #fff;
  font-weight: 700;
}

.brand-name {
  color: #111827;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.2;
}

.brand-sub {
  margin-top: 2px;
  color: #6b7280;
  font-size: 12px;
}

.app-menu {
  border-right: none;
  padding: 8px;
}

.app-menu :deep(.el-menu-item-group__title) {
  padding: 14px 12px 6px;
  color: #9ca3af;
  font-size: 12px;
}

.app-menu :deep(.el-menu-item) {
  height: 42px;
  margin-bottom: 4px;
  border-radius: 6px;
}

.app-menu :deep(.el-menu-item.is-active) {
  background: var(--lg-green-soft);
  color: var(--lg-green);
}

.app-header {
  display: flex;
  align-items: center;
  height: 60px;
  border-bottom: 1px solid var(--border-subtle);
  background: #fff;
}

.page-title {
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.header-inner {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.beijing-clock {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #374151;
  font-size: 12px;
  white-space: nowrap;
}

.beijing-clock b {
  color: #111827;
  font-family: "SFMono-Regular", Consolas, monospace;
  font-weight: 500;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-tag {
  color: var(--lg-green);
  font-weight: 600;
}

.role-tag {
  color: #5b6ee1;
  font-weight: 600;
}

.app-main {
  background: #f3f5f7;
}
</style>
