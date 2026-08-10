package com.boyu.demo.orchestrator;

/**
 * 决策演示状态机阶段枚举。
 * <p>顺序即演示主线：INIT → EVENT_INJECTED → SIMULATING → SIMULATION_DONE
 * → OPTIMIZING → GAMING → PLAN_SELECTED → DONE；PLAN_SELECTED 可回 GAMING 重新博弈。
 * <p>EVENT_INJECTED 对外语义为「模拟宇宙已创建」（初始事件：北方铜业停产、电解铜断供）；
 * 阶段枚举名与迁移表保持不变，仅用户可见文案采用"创建模拟宇宙"口径。
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
