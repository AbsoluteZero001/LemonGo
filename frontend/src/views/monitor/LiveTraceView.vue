<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  ArrowRight,
  Connection,
  Cpu,
  Monitor,
  Reading,
  Timer,
  UserFilled,
  WarningFilled,
} from '@element-plus/icons-vue'
import { subscribeRealtime } from '@/api/realtime'
import type { LiveRequestEvent, RealtimeEnvelope } from '@/api/system'

const envelopes = ref<RealtimeEnvelope[]>([])
const connected = ref(false)
let unsubscribe: (() => void) | undefined

const latest = computed<LiveRequestEvent | null>(
  () => envelopes.value[0]?.data ?? null,
)

const flowSteps = computed(() => {
  const event = latest.value
  if (!event) {
    return []
  }
  const log = event.requestLog
  const steps = [
    {
      key: 'frontend',
      title: '前端',
      sub: 'Vue + Axios',
      icon: Monitor,
      type: 'frontend',
    },
    {
      key: 'api',
      title: `${log.httpMethod} ${log.uri}`,
      sub: 'API 入口',
      icon: Connection,
      type: 'api',
    },
  ]
  for (const layer of event.layers) {
    steps.push({
      key: `${layer.layerType}-${layer.layerName}`,
      title: layer.layerName,
      sub: layer.layerMethod || layer.description,
      icon: layerIcon(layer.layerType),
      type: layer.layerType,
    })
  }
  steps.push({
    key: 'response',
    title: `HTTP ${log.httpStatus}`,
    sub: log.success === 1 ? '返回成功' : '返回异常',
    icon: log.success === 1 ? UserFilled : WarningFilled,
    type: 'response',
  })
  return steps
})

function layerIcon(type: string) {
  if (type === 'CONTROLLER') {
    return Reading
  }
  if (type === 'SERVICE') {
    return Cpu
  }
  if (type === 'MAPPER') {
    return Reading
  }
  return Connection
}

function handleEvent(envelope: RealtimeEnvelope) {
  envelopes.value = [
    envelope,
    ...envelopes.value.filter((item) => item.data.requestLog.requestId !== envelope.data.requestLog.requestId),
  ].slice(0, 60)
}

function focusItem(item: RealtimeEnvelope) {
  envelopes.value = [
    item,
    ...envelopes.value.filter((row) => row.data.requestLog.requestId !== item.data.requestLog.requestId),
  ]
}

function handleStatus(online: boolean) {
  connected.value = online
}

function statusType(row: LiveRequestEvent) {
  return row.requestLog.httpStatus >= 400 ? 'danger' : 'success'
}

function formatTime(value?: string) {
  return value || '-'
}

function durationText(ms: number) {
  return ms >= 1000 ? `${(ms / 1000).toFixed(1)}s` : `${ms}ms`
}

onMounted(() => {
  unsubscribe = subscribeRealtime(handleEvent, handleStatus)
})

onBeforeUnmount(() => {
  unsubscribe?.()
})
</script>

<template>
  <div class="live-page">
    <div class="live-toolbar">
      <div class="live-title">
        <strong>实时链路</strong>
        <el-tag :type="connected ? 'success' : 'info'" effect="dark">
          {{ connected ? '连接中' : '重连中' }}
        </el-tag>
        <span>已推送 {{ envelopes.length }} 条</span>
      </div>
    </div>

    <div v-if="latest" class="live-grid">
      <section class="panel trace-panel">
        <div class="trace-title">
          <span>调用链路</span>
          <el-tag :type="statusType(latest)" effect="dark">
            HTTP {{ latest.requestLog.httpStatus }}
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
              <el-icon><ArrowRight /></el-icon>
            </div>
          </template>
        </div>
        <div class="request-info">
          <div>
            <span>用户</span>
            <strong>{{ latest.requestLog.username || '未登录' }}</strong>
          </div>
          <div>
            <span>模块</span>
            <strong>{{ latest.requestLog.moduleName || '-' }}</strong>
          </div>
          <div>
            <span>负责人</span>
            <strong>{{ latest.requestLog.developerName || '-' }}</strong>
          </div>
          <div>
            <span>耗时</span>
            <strong>{{ durationText(latest.requestLog.durationMs) }}</strong>
          </div>
          <div>
            <span>Request ID</span>
            <strong class="mono">{{ latest.requestLog.requestId }}</strong>
          </div>
          <div>
            <span>客户端 IP</span>
            <strong>{{ latest.requestLog.clientIp || '-' }}</strong>
          </div>
          <div>
            <span>请求时间</span>
            <strong>{{ formatTime(latest.requestLog.requestTime) }}</strong>
          </div>
          <div v-if="latest.requestLog.errorType">
            <span>错误类型</span>
            <strong class="error-text">{{ latest.requestLog.errorType }}</strong>
          </div>
        </div>
      </section>

      <section class="panel feed-panel">
        <div class="panel-title">
          <span>最近实时请求</span>
          <el-icon color="var(--lg-green)"><Timer /></el-icon>
        </div>
        <div class="feed-list">
          <button
            v-for="item in envelopes"
            :key="item.data.requestLog.requestId"
            class="feed-item"
            :class="{
              active: item.data.requestLog.requestId === latest.requestLog.requestId,
              failed: item.data.requestLog.httpStatus >= 400,
            }"
            type="button"
            @click="focusItem(item)"
          >
            <div class="feed-head">
              <strong>{{ item.data.requestLog.username || '-' }}</strong>
              <span>{{ item.data.requestLog.httpMethod }}</span>
            </div>
            <p>{{ item.data.requestLog.uri }}</p>
            <div class="feed-meta">
              <span>{{ item.data.requestLog.moduleName || '-' }}</span>
              <b>{{ item.data.requestLog.httpStatus }}</b>
            </div>
          </button>
          <el-empty
            v-if="!envelopes.length"
            description="等待实时请求"
            :image-size="54"
          />
        </div>
      </section>
    </div>

    <div v-else class="empty-panel">
      <el-empty description="等待实时请求" />
    </div>
  </div>
</template>

<style scoped>
.live-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.live-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.live-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.live-title strong {
  color: #111827;
  font-size: 16px;
}

.live-title span {
  color: #6b7280;
  font-size: 13px;
}

.live-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr);
  gap: 16px;
}

.panel {
  min-width: 0;
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
  margin-bottom: 16px;
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

.flow-step strong,
.flow-step span {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flow-step strong {
  color: #1f2937;
  font-size: 13px;
}

.flow-step span {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.flow-icon {
  display: grid;
  width: 38px;
  height: 38px;
  margin-bottom: 8px;
  place-items: center;
  border-radius: 50%;
  background: var(--lg-green-soft);
  color: var(--lg-green);
}

.flow-arrow {
  display: grid;
  min-width: 32px;
  place-items: center;
  color: #9ca3af;
}

.request-info {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.request-info > div {
  min-width: 0;
  padding: 10px 12px;
  background: #f7f9fa;
  border-radius: 6px;
}

.request-info span {
  display: block;
  color: #6b7280;
  font-size: 12px;
}

.request-info strong {
  display: block;
  margin-top: 5px;
  overflow: hidden;
  color: #1f2937;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mono {
  font-family: "SFMono-Regular", Consolas, monospace;
}

.error-text {
  color: var(--lg-danger);
}

.feed-panel {
  max-height: 640px;
  overflow: hidden;
}

.feed-list {
  display: flex;
  max-height: 590px;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  overflow: auto;
}

.feed-item {
  display: block;
  width: 100%;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  background: #fff;
  color: inherit;
  font: inherit;
}

.feed-item.active {
  border-color: var(--lg-green);
  background: var(--lg-green-soft);
}

.feed-item.failed {
  border-color: #f1b8b8;
  background: #fffafa;
}

.feed-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.feed-head strong {
  color: #111827;
  font-size: 13px;
}

.feed-head span,
.feed-meta span {
  color: #6b7280;
  font-size: 12px;
}

.feed-item p {
  margin: 5px 0;
  overflow: hidden;
  color: #374151;
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feed-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.feed-meta b {
  color: var(--lg-green);
  font-size: 12px;
}

.feed-item.failed .feed-meta b {
  color: var(--lg-danger);
}

.empty-panel {
  display: grid;
  min-height: 420px;
  place-items: center;
  background: #fff;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

@media (max-width: 980px) {
  .live-grid {
    grid-template-columns: 1fr;
  }

  .request-info {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .request-info {
    grid-template-columns: 1fr;
  }
}
</style>
