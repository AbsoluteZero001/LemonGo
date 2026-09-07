<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  Connection,
  Cpu,
  Monitor,
  Reading,
  Timer,
  UserFilled,
} from '@element-plus/icons-vue'
import { fetchRequestDetail } from '@/api/system'
import type { RequestDetail } from '@/api/system'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<RequestDetail | null>(null)

async function load() {
  loading.value = true
  try {
    detail.value = await fetchRequestDetail(String(route.params.requestId))
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

const flowSteps = computed(() => {
  const log = detail.value?.requestLog
  if (!log) {
    return []
  }
  const steps = [
    {
      key: 'frontend',
      title: '前端',
      sub: 'Vue + Axios',
      icon: Monitor,
    },
    {
      key: 'api',
      title: `${log.httpMethod} ${log.uri}`,
      sub: 'API 网关入口',
      icon: Connection,
    },
  ]
  if (log.controllerName) {
    steps.push({
      key: 'controller',
      title: log.controllerName,
      sub: log.controllerMethod || 'Controller',
      icon: Reading,
    })
  }
  if (log.serviceName) {
    steps.push({
      key: 'service',
      title: log.serviceName.split('.')[0],
      sub: log.serviceName.split('.').slice(1).join('.'),
      icon: Cpu,
    })
  }
  if (log.mapperName) {
    steps.push({
      key: 'mapper',
      title: log.mapperName,
      sub: '数据访问层',
      icon: Reading,
    })
  }
  steps.push({
    key: 'response',
    title: `HTTP ${log.httpStatus}`,
    sub: log.success === 1 ? '返回成功' : '返回异常',
    icon: UserFilled,
  })
  return steps
})

function statusType() {
  const status = detail.value?.requestLog.httpStatus
  return status !== undefined && status >= 400 ? 'danger' : 'success'
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <el-button text :icon="ArrowLeft" @click="router.push('/monitor/requests')">
      返回请求日志
    </el-button>

    <template v-if="detail">
      <div class="trace-shell">
        <section class="panel trace-panel">
          <div class="trace-title">
            <span>调用链路</span>
            <el-tag :type="statusType()" effect="dark">
              HTTP {{ detail.requestLog.httpStatus }}
            </el-tag>
          </div>
          <div class="flow-row">
            <template v-for="(step, index) in flowSteps" :key="step.key">
              <div class="flow-step">
                <div class="flow-icon">
                  <el-icon :size="18"><component :is="step.icon" /></el-icon>
                </div>
                <strong>{{ step.title }}</strong>
                <span>{{ step.sub }}</span>
              </div>
              <div v-if="index < flowSteps.length - 1" class="flow-arrow">
                <el-icon><ArrowLeft /></el-icon>
              </div>
            </template>
          </div>
        </section>

        <section class="panel request-panel">
          <div class="panel-title">请求信息</div>
          <dl class="info-grid">
            <div>
              <dt>Request ID</dt>
              <dd>{{ detail.requestLog.requestId }}</dd>
            </div>
            <div>
              <dt>访问时间</dt>
              <dd>{{ detail.requestLog.requestTime }}</dd>
            </div>
            <div>
              <dt>用户</dt>
              <dd>{{ detail.requestLog.username || '未登录' }}</dd>
            </div>
            <div>
              <dt>客户端 IP</dt>
              <dd>{{ detail.requestLog.clientIp || '-' }}</dd>
            </div>
            <div>
              <dt>接口</dt>
              <dd>{{ detail.requestLog.httpMethod }} {{ detail.requestLog.uri }}</dd>
            </div>
            <div>
              <dt>耗时</dt>
              <dd>
                <el-icon><Timer /></el-icon>
                {{ detail.requestLog.durationMs }} ms
              </dd>
            </div>
            <div>
              <dt>业务模块</dt>
              <dd>{{ detail.requestLog.moduleName || '-' }}</dd>
            </div>
            <div>
              <dt>模块负责人</dt>
              <dd>
                <el-icon><UserFilled /></el-icon>
                {{ detail.requestLog.developerName || '-' }}
              </dd>
            </div>
          </dl>
        </section>

        <section v-if="detail.requestLog.success !== 1 && detail.errorLog" class="panel error-panel">
          <div class="panel-title">异常定位</div>
          <div class="error-grid">
            <div>
              <span>异常类型</span>
              <strong>{{ detail.errorLog.errorType || '-' }}</strong>
            </div>
            <div>
              <span>异常类</span>
              <strong>{{ detail.errorLog.exceptionClass || '-' }}</strong>
            </div>
            <div>
              <span>异常信息</span>
              <strong>{{ detail.errorLog.errorMessage || '-' }}</strong>
            </div>
            <div>
              <span>发生时间</span>
              <strong>{{ detail.errorLog.occurredAt }}</strong>
            </div>
          </div>
          <details v-if="detail.errorLog.stackTrace" class="stack-box">
            <summary>异常堆栈</summary>
            <pre>{{ detail.errorLog.stackTrace }}</pre>
          </details>
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.trace-shell {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel {
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.trace-title,
.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #111827;
  font-weight: 700;
}

.trace-title {
  margin-bottom: 18px;
}

.flow-row {
  display: flex;
  align-items: stretch;
  gap: 0;
  overflow-x: auto;
  padding: 6px 0 2px;
}

.flow-step {
  display: flex;
  width: 150px;
  min-width: 130px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14px 8px;
  text-align: center;
  border: 1px solid #d9e5df;
  border-radius: 8px;
  background: #f8fbf9;
}

.flow-icon {
  display: grid;
  width: 40px;
  height: 40px;
  margin-bottom: 8px;
  place-items: center;
  border-radius: 50%;
  background: var(--lg-green-soft);
  color: var(--lg-green);
}

.flow-step strong {
  width: 100%;
  overflow: hidden;
  color: #1f2937;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flow-step span {
  width: 100%;
  margin-top: 4px;
  overflow: hidden;
  color: #6b7280;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flow-arrow {
  display: grid;
  min-width: 34px;
  place-items: center;
  color: #9ca3af;
  transform: rotate(180deg);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 28px;
  margin: 14px 0 0;
}

.info-grid > div {
  padding: 13px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.info-grid dt {
  color: #6b7280;
  font-size: 12px;
}

.info-grid dd {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 5px 0 0;
  overflow-wrap: anywhere;
  color: #111827;
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 13px;
}

.error-panel {
  border-color: #f1b8b8;
  background: #fffafa;
}

.error-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 14px;
}

.error-grid > div {
  padding: 12px;
  background: #fff;
  border: 1px solid #f2d3d3;
  border-radius: 6px;
}

.error-grid span {
  display: block;
  color: #6b7280;
  font-size: 12px;
}

.error-grid strong {
  display: block;
  margin-top: 5px;
  color: #9f1c1c;
  font-size: 13px;
  overflow-wrap: anywhere;
}

.stack-box {
  margin-top: 12px;
  border: 1px solid #f0d4d4;
  border-radius: 6px;
}

.stack-box summary {
  padding: 10px 12px;
  color: #7f1d1d;
  cursor: pointer;
  font-weight: 600;
}

.stack-box pre {
  max-height: 220px;
  margin: 0;
  padding: 12px;
  overflow: auto;
  border-top: 1px solid #f0d4d4;
  color: #6b2737;
  font-size: 11px;
  line-height: 1.6;
}

@media (max-width: 700px) {
  .info-grid,
  .error-grid {
    grid-template-columns: 1fr;
  }
}
</style>
