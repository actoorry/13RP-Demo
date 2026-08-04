package com.boyu.demo.orchestrator;

/**
 * 决策演示状态机阶段枚举。
 * <p>顺序即演示主线：INIT → EVENT_INJECTED → SIMULATING → SIMULATION_DONE
 * → OPTIMIZING → GAMING → PLAN_SELECTED → DONE；PLAN_SELECTED 可回 GAMING 重新博弈。
 */
public enum DemoPhase {
    INIT,
    EVENT_INJECTED,
    SIMULATING,
    SIMULATION_DONE,
    OPTIMIZING,
    GAMING,
    PLAN_SELECTED,
    DONE
}
