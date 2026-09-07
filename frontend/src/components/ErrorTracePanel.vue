<script setup lang="ts">
import { errorState } from '@/api/http'
import { CloseBold, CircleCloseFilled, UserFilled } from '@element-plus/icons-vue'
</script>

<template>
  <transition name="el-fade-in">
    <div v-if="errorState.trace || errorState.message" class="error-trace-panel" role="alert">
      <el-icon class="error-icon" :size="22">
        <CircleCloseFilled />
      </el-icon>
      <div class="error-copy">
        <div class="error-headline">
          <strong>{{ errorState.trace?.message || errorState.message }}</strong>
          <el-tag
            v-if="errorState.trace?.code"
            size="small"
            effect="dark"
            :type="errorState.trace!.code >= 500 ? 'danger' : 'warning'"
          >
            HTTP {{ errorState.trace?.code }}
          </el-tag>
        </div>
        <div v-if="errorState.trace" class="error-meta">
          <span>Request {{ errorState.trace.requestId }}</span>
          <span>{{ errorState.trace.method }} {{ errorState.trace.path }}</span>
          <span v-if="errorState.trace.module">{{ errorState.trace.module }}</span>
        </div>
        <div v-if="errorState.trace" class="error-chain">
          <el-tag v-if="errorState.trace.controller" size="small" effect="plain">
            {{ errorState.trace.controller }}
          </el-tag>
          <el-tag v-if="errorState.trace.service" size="small" effect="plain">
            {{ errorState.trace.service }}
          </el-tag>
          <el-tag v-if="errorState.trace.mapper" size="small" effect="plain">
            {{ errorState.trace.mapper }}
          </el-tag>
          <span v-if="errorState.trace.owner" class="owner-cell">
            <el-icon><UserFilled /></el-icon>
            {{ errorState.trace.owner.name }}
          </span>
        </div>
      </div>
      <el-button
        class="error-close"
        text
        circle
        aria-label="关闭错误提示"
        @click="errorState.trace = null; errorState.message = ''"
      >
        <el-icon><CloseBold /></el-icon>
      </el-button>
    </div>
  </transition>
</template>

<style scoped>
.error-trace-panel {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  margin-bottom: 16px;
  border: 1px solid #f1b8b8;
  border-radius: 8px;
  background: #fff7f7;
}

.error-icon {
  margin-top: 2px;
  color: var(--lg-danger);
}

.error-copy {
  flex: 1;
  min-width: 0;
}

.error-headline {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9f1c1c;
  line-height: 1.35;
}

.error-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
  margin-top: 4px;
  color: #7f1d1d;
  font-size: 12px;
}

.error-chain {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}

.owner-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
  color: #5b1b1b;
  font-weight: 600;
}

.error-close {
  margin-left: auto;
}
</style>
