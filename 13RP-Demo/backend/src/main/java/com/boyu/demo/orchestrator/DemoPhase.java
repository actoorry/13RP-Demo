package com.boyu.demo.orchestrator;

public enum DemoPhase {
    INIT,              // 初始状态：看板正常、物流图正常
    EVENT_INJECTED,    // 事件注入：台风封港，红色报警
    SIMULATING,        // 推演中：进度条动画
    SIMULATION_DONE,   // 推演完成：3 路径展示
    OPTIMIZING,        // 寻优中：雷达图展示
    GAMING,            // 博弈中：胜率分析
    PLAN_SELECTED,     // 方案确认：指令输出
    DONE               // 演示结束
}
