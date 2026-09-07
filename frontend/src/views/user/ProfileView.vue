<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, DataLine, Message, Phone } from '@element-plus/icons-vue'
import { fetchUserActivity } from '@/api/system'
import type { UserActivity } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const activity = ref<UserActivity | null>(null)
const loading = ref(false)

const profile = computed(() => auth.profile)

async function load() {
  if (!auth.token) {
    router.replace('/login')
    return
  }
  loading.value = true
  try {
    await auth.refreshProfile()
    const list = await fetchUserActivity()
    activity.value = list.find((item) => item.userId === auth.profile?.id) ?? null
  } catch {
    activity.value = null
  } finally {
    loading.value = false
  }
}

function formatTime(value?: string) {
  return value || '暂无记录'
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="profile-page">
    <template v-if="profile">
      <section class="profile-hero">
        <el-avatar :size="78" class="profile-avatar">
          {{ profile.nickname.slice(0, 1) }}
        </el-avatar>
        <div class="profile-identity">
          <h1>{{ profile.nickname }}</h1>
          <p>@{{ profile.username }} · {{ profile.email || '未填写邮箱' }}</p>
        </div>
        <el-tag v-if="profile.activityScore >= 60" type="success" effect="dark" round>
          活跃用户
        </el-tag>
        <el-tag v-else effect="plain" round>成长中</el-tag>
      </section>

      <div class="metric-grid">
        <div class="metric-cell">
          <span class="metric-label">活跃度</span>
          <strong>{{ profile.activityScore }}</strong>
          <el-icon color="var(--lg-green)"><DataLine /></el-icon>
        </div>
        <div class="metric-cell">
          <span class="metric-label">今日访问</span>
          <strong>{{ activity?.requestCountToday ?? 0 }}</strong>
          <el-icon color="#5b6ee1"><DataLine /></el-icon>
        </div>
        <div class="metric-cell">
          <span class="metric-label">累计访问</span>
          <strong>{{ activity?.requestCountTotal ?? 0 }}</strong>
          <el-icon color="#b7791f"><DataLine /></el-icon>
        </div>
        <div class="metric-cell">
          <span class="metric-label">活跃时长</span>
          <strong>{{ Math.round((activity?.activeSecondsTotal ?? 0) / 60) }}m</strong>
          <el-icon color="#4b5563"><Clock /></el-icon>
        </div>
      </div>

      <section class="profile-detail">
        <div class="detail-row">
          <el-icon><Message /></el-icon>
          <span>邮箱</span>
          <strong>{{ profile.email || '-' }}</strong>
        </div>
        <div class="detail-row">
          <el-icon><Phone /></el-icon>
          <span>手机</span>
          <strong>{{ profile.phone || '-' }}</strong>
        </div>
        <div class="detail-row">
          <el-icon><Clock /></el-icon>
          <span>最近上线</span>
          <strong>{{ formatTime(profile.lastLoginTime) }}</strong>
        </div>
        <div class="detail-row">
          <el-icon><DataLine /></el-icon>
          <span>最近活跃</span>
          <strong>{{ formatTime(profile.lastActiveTime) }}</strong>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.profile-avatar {
  background: var(--lg-green);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
}

.profile-identity {
  flex: 1;
  min-width: 0;
}

.profile-identity h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
}

.profile-identity p {
  margin: 6px 0 0;
  color: #6b7280;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.metric-cell {
  position: relative;
  display: flex;
  min-height: 112px;
  flex-direction: column;
  justify-content: center;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.metric-cell .el-icon {
  position: absolute;
  right: 16px;
  bottom: 14px;
}

.metric-label {
  color: #6b7280;
  font-size: 13px;
}

.metric-cell strong {
  margin-top: 4px;
  color: #111827;
  font-size: 26px;
}

.profile-detail {
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.detail-row {
  display: grid;
  grid-template-columns: 24px 110px 1fr;
  gap: 12px;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  color: #6b7280;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row strong {
  color: #1f2937;
  font-weight: 500;
}

@media (max-width: 760px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
