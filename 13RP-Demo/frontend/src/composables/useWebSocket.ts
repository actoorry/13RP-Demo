import { onUnmounted } from 'vue';

export type WsMessage = {
  channel: string;
  payload: any;
};

/**
 * WebSocket 连接：自动重连（指数退避 max 5s）
 */
export function useWebSocket(onMessage: (msg: WsMessage) => void) {
  let ws: WebSocket | null = null;
  let retry = 0;
  let closed = false;
  let timer: number | null = null;

  const url = () => {
    const proto = location.protocol === 'https:' ? 'wss' : 'ws';
    return `${proto}://${location.host}/ws/demo`;
  };

  const connect = () => {
    if (closed) return;
    ws = new WebSocket(url());
    ws.onopen = () => { retry = 0; };
    ws.onmessage = (ev) => {
      try {
        onMessage(JSON.parse(ev.data));
      } catch { /* ignore */ }
    };
    ws.onclose = () => {
      if (closed) return;
      retry = Math.min(retry + 1, 5);
      timer = window.setTimeout(connect, retry * 1000);
    };
  };

  const send = (action: string, payload: Record<string, unknown> = {}) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ action, ...payload }));
    }
  };

  connect();
  onUnmounted(() => {
    closed = true;
    if (timer) clearTimeout(timer);
    ws?.close();
  });

  return { send, isOpen: () => ws?.readyState === WebSocket.OPEN };
}
