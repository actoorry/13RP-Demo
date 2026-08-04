import { onUnmounted, ref } from 'vue'
import { useDemoStore } from '../stores/demo'

const MAX_RETRY_MS = 5000
const BASE_RETRY_MS = 1000

export function useWebSocket() {
  const store = useDemoStore()
  const connected = ref(false)
  const connecting = ref(false)

  let ws: WebSocket | null = null
  let retryDelay = BASE_RETRY_MS
  let retryTimer: ReturnType<typeof setTimeout> | null = null
  let disposed = false

  function connect() {
    if (disposed) return
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return
    connecting.value = true

    const proto = window.location.protocol === 'https:' ? 'wss' : 'ws'
    ws = new WebSocket(`${proto}://${window.location.host}/ws/demo`)

    ws.onopen = () => {
      connected.value = true
      connecting.value = false
      retryDelay = BASE_RETRY_MS
    }

    ws.onmessage = (ev) => {
      try {
        const msg = JSON.parse(ev.data)
        store.applyWsMessage(msg)
      } catch (err) {
        console.error('[ws] 消息解析失败', err)
      }
    }

    ws.onclose = () => {
      connected.value = false
      connecting.value = false
      ws = null
      if (!disposed) scheduleReconnect()
    }

    ws.onerror = () => {
      // onclose 随后触发，这里仅关闭以进入重连流程
      ws?.close()
    }
  }

  function scheduleReconnect() {
    if (retryTimer) return
    retryTimer = setTimeout(() => {
      retryTimer = null
      connect()
    }, retryDelay)
    retryDelay = Math.min(retryDelay * 2, MAX_RETRY_MS)
  }

  function send(action: string, payload?: unknown) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ action, payload }))
    } else {
      console.warn('[ws] 未连接，丢弃动作：', action)
    }
  }

  connect()

  onUnmounted(() => {
    disposed = true
    if (retryTimer) clearTimeout(retryTimer)
    ws?.close()
    ws = null
  })

  return { connected, connecting, send }
}
