import { defineStore } from 'pinia';
import { ref } from 'vue';

export type Phase =
  | 'INIT' | 'EVENT_INJECTED' | 'SIMULATING' | 'SIMULATION_DONE'
  | 'OPTIMIZING' | 'GAMING' | 'PLAN_SELECTED' | 'DONE';

export const useDemoStore = defineStore('demo', () => {
  const phase = ref<Phase>('INIT');
  const progress = ref(0);
  const progressMsg = ref('');
  const wsConnected = ref(false);

  // 看板数据
  const deliveryRate = ref(96.0);
  const openOrders = ref(1200);
  const ports = ref<Record<string, string>>({});
  const portNames = ref<Record<string, string>>({});
  const inventory = ref<Record<string, number>>({});

  // 决策数据
  const solutions = ref<any[]>([]);
  const gameResults = ref<any>(null);
  const instructions = ref<any[]>([]);
  const planName = ref('');

  function applyWsMessage(msg: { channel: string; payload: any }) {
    const p = msg.payload || {};
    switch (msg.channel) {
      case 'dashboard':
        if (p.orderDeliveryRate != null) deliveryRate.value = p.orderDeliveryRate;
        if (p.openOrders != null) openOrders.value = p.openOrders;
        if (p.ports) ports.value = p.ports;
        if (p.portNames) portNames.value = p.portNames;
        if (p.inventory) inventory.value = p.inventory;
        break;
      case 'demo-state':
        if (p.state) phase.value = p.state as Phase;
        if (p.progress != null) progress.value = p.progress;
        if (p.message != null) progressMsg.value = p.message;
        break;
    }
  }

  return {
    phase, progress, progressMsg, wsConnected,
    deliveryRate, openOrders, ports, portNames, inventory,
    solutions, gameResults, instructions, planName,
    applyWsMessage
  };
});
