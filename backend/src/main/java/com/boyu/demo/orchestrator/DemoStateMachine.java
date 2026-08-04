package com.boyu.demo.orchestrator;

import com.boyu.demo.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 决策演示状态机。
 * <p>合法迁移表（非法迁移抛 {@link IllegalStateException}）：
 * INIT → EVENT_INJECTED → SIMULATING → SIMULATION_DONE → OPTIMIZING → GAMING → PLAN_SELECTED → DONE，
 * 且 PLAN_SELECTED → GAMING 允许（方案重选）。
 * <p>每次状态变化经 {@link WebSocketSessionManager} 广播 demo-state（含 phase / progress / message）；
 * progress 唯一来源是 {@link TimelineController}。
 */
@Component
public class DemoStateMachine {

    private static final Logger log = LoggerFactory.getLogger(DemoStateMachine.class);

    /** 合法迁移表。 */
    private static final Map<DemoPhase, Set<DemoPhase>> TRANSITIONS = new EnumMap<>(DemoPhase.class);

    static {
        TRANSITIONS.put(DemoPhase.INIT, EnumSet.of(DemoPhase.EVENT_INJECTED));
        TRANSITIONS.put(DemoPhase.EVENT_INJECTED, EnumSet.of(DemoPhase.SIMULATING));
        TRANSITIONS.put(DemoPhase.SIMULATING, EnumSet.of(DemoPhase.SIMULATION_DONE));
        TRANSITIONS.put(DemoPhase.SIMULATION_DONE, EnumSet.of(DemoPhase.OPTIMIZING));
        TRANSITIONS.put(DemoPhase.OPTIMIZING, EnumSet.of(DemoPhase.GAMING));
        TRANSITIONS.put(DemoPhase.GAMING, EnumSet.of(DemoPhase.PLAN_SELECTED));
        TRANSITIONS.put(DemoPhase.PLAN_SELECTED, EnumSet.of(DemoPhase.DONE, DemoPhase.GAMING));
        TRANSITIONS.put(DemoPhase.DONE, EnumSet.noneOf(DemoPhase.class));
    }

    private final WebSocketSessionManager ws;
    private final TimelineController timeline;

    private volatile DemoPhase phase = DemoPhase.INIT;
    private volatile String lastMessage = "演示就绪，等待事件注入";

    public DemoStateMachine(WebSocketSessionManager ws, TimelineController timeline) {
        this.ws = ws;
        this.timeline = timeline;
    }

    /** 尝试迁移到目标阶段；非法迁移抛出 {@link IllegalStateException}，并广播 demo-state。 */
    public synchronized DemoPhase transitionTo(DemoPhase target) {
        return transitionTo(target, null);
    }

    /** 尝试迁移到目标阶段，message 缺省用阶段默认文案。 */
    public synchronized DemoPhase transitionTo(DemoPhase target, String message) {
        Set<DemoPhase> allowed = TRANSITIONS.get(phase);
        if (allowed == null || !allowed.contains(target)) {
            throw new IllegalStateException("非法状态迁移：" + phase + " -> " + target);
        }
        DemoPhase from = this.phase;
        this.phase = target;
        log.info("状态迁移: {} -> {}", from, target);
        broadcastState(message);
        return phase;
    }

    /** 广播 demo-state；progress 从 {@link TimelineController} 读取（单一来源）。 */
    public void broadcastState(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("phase", phase.name());
        payload.put("progress", timeline.getProgress());
        payload.put("message", message != null ? message : defaultMessage(phase));
        this.lastMessage = (String) payload.get("message");
        ws.broadcast("demo-state", payload);
    }

    /** 重开一场新演示：直接回到 INIT 并广播（不走迁移表）。 */
    public synchronized void resetForNewDemo() {
        DemoPhase from = this.phase;
        this.phase = DemoPhase.INIT;
        log.info("状态迁移: {} -> {}", from, DemoPhase.INIT);
        broadcastState("演示已重置，等待事件注入");
    }

    public DemoPhase getPhase() {
        return phase;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    private static String defaultMessage(DemoPhase p) {
        return switch (p) {
            case INIT -> "演示就绪，等待事件注入";
            case EVENT_INJECTED -> "台风「海燕」封港事件已注入";
            case SIMULATING -> "7RP 推演启动，342 条路径并行回放...";
            case SIMULATION_DONE -> "推演完成：342 条路径全部跑完";
            case OPTIMIZING -> "8RP 多目标寻优启动...";
            case GAMING -> "9RP 博弈对抗启动...";
            case PLAN_SELECTED -> "方案已确认，指令生成中...";
            case DONE -> "演示完成";
        };
    }
}
