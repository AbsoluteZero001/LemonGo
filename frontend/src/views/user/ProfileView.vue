<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EditPen, Message, Phone } from '@element-plus/icons-vue'
import { updateMe } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  nickname: '',
  email: '',
  phone: '',
})

const profile = computed(() => auth.profile)

async function load() {
  if (!auth.token) {
    router.replace('/login')
    return
  }
  loading.value = true
  try {
    await auth.refreshProfile()
  } catch {
  } finally {
    loading.value = false
  }
}

function openEditor() {
  if (!profile.value) {
    return
  }
  form.nickname = profile.value.nickname
  form.email = profile.value.email ?? ''
  form.phone = profile.value.phone ?? ''
  dialogVisible.value = true
}

async function saveProfile() {
  const nickname = form.nickname.trim()
  const email = form.email.trim()
  const phone = form.phone.trim()
  if (!nickname) {
    ElMessage.error('昵称不能为空')
    return
  }
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  saving.value = true
  try {
    await updateMe(nickname, email, phone)
    dialogVisible.value = false
    ElMessage.success('个人资料已更新')
    await auth.refreshProfile()
  } catch {
    // The shared HTTP interceptor has already shown the error.
  } finally {
    saving.value = false
  }
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
      <div class="profile-actions">
        <el-button type="primary" plain :icon="EditPen" @click="openEditor">
          编辑资料
        </el-button>
      </div>
      </section>

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
      </section>
    </template>

    <el-dialog
      v-model="dialogVisible"
      title="编辑个人资料"
      width="min(440px, 92vw)"
      :close-on-click-modal="false"
    >
      <el-form label-position="top" @submit.prevent="saveProfile">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="100" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" maxlength="30" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
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

.profile-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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
  .profile-hero {
    flex-wrap: wrap;
  }

  .profile-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
