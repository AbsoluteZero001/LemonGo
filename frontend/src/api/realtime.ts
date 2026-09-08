import type { RealtimeEnvelope } from './system'

type RealtimeListener = (event: RealtimeEnvelope) => void

const TOKEN_KEY = 'lemongo_token'
const listeners = new Set<RealtimeListener>()
const statusListeners = new Set<(connected: boolean) => void>()

let socket: WebSocket | null = null
let retryTimer: ReturnType<typeof setTimeout> | undefined
let stopped = false
let retryCount = 0

function monitorSocketUrl(token: string) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${window.location.host}/ws/monitor?token=${encodeURIComponent(token)}`
}

function scheduleReconnect() {
  if (stopped || listeners.size === 0) {
    return
  }
  retryCount += 1
  const delay = Math.min(5_000, 800 * 2 ** Math.min(retryCount, 4))
  retryTimer = setTimeout(connect, delay)
}

function connect() {
  if (stopped) {
    return
  }
  const token = sessionStorage.getItem(TOKEN_KEY) ?? ''
  if (!token) {
    return
  }
  try {
    socket = new WebSocket(monitorSocketUrl(token))
  } catch {
    scheduleReconnect()
    return
  }

  socket.onopen = () => {
    retryCount = 0
    statusListeners.forEach((listener) => listener(true))
  }
  socket.onmessage = (message) => {
    try {
      const envelope = JSON.parse(String(message.data)) as RealtimeEnvelope
      if (!envelope || envelope.type !== 'REQUEST_COMPLETED') {
        return
      }
      listeners.forEach((listener) => listener(envelope))
    } catch {
      // Ignore malformed realtime frames.
    }
  }
  socket.onclose = () => {
    socket = null
    statusListeners.forEach((listener) => listener(false))
    scheduleReconnect()
  }
  socket.onerror = () => {
    socket?.close()
  }
}

export function subscribeRealtime(
  listener: RealtimeListener,
  onStatus?: (connected: boolean) => void,
) {
  listeners.add(listener)
  if (onStatus) {
    statusListeners.add(onStatus)
  }
  if (onStatus && socket?.readyState === WebSocket.OPEN) {
    onStatus(true)
  }
  stopped = false
  if (!socket || socket.readyState === WebSocket.CLOSED) {
    connect()
  }
  return () => {
    listeners.delete(listener)
    if (onStatus) {
      statusListeners.delete(onStatus)
    }
    if (listeners.size === 0) {
      stopped = true
      if (retryTimer) {
        clearTimeout(retryTimer)
        retryTimer = undefined
      }
      socket?.close()
      socket = null
    }
  }
}
