package com.boyu.demo.orchestrator;

import com.boyu.demo.websocket.WebSocketSessionManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DemoStateMachine {

    private volatile DemoPhase phase = DemoPhase.INIT;
    private volatile double progress = 0;
    private volatile String message = "";
    private final WebSocketSessionManager ws;

    public DemoStateMachine(WebSocketSessionManager ws) {
        this.ws = ws;
    }

    public synchronized void transition(DemoPhase target) {
        if (!allowedTransitions(phase).contains(target)) {
            throw new IllegalStateException("非法状态迁移: " + phase + " → " + target);
        }
        this.phase = target;
        if (target != DemoPhase.SIMULATING) {
            this.progress = 0;
        }
        broadcastState();
    }

    public void updateProgress(double progress, String message) {
        this.progress = progress;
        this.message = message;
        broadcastState();
    }

    public void broadcastState() {
        ws.broadcast("demo-state", Map.of(
                "state", phase.name(),
                "progress", progress,
                "message", message));
    }

    public DemoPhase getPhase() {
        return phase;
    }

    /** 新演示重置：回到 INIT（跳过状态迁移校验） */
    public synchronized void resetForNewDemo() {
        this.phase = DemoPhase.INIT;
        this.progress = 0;
        this.message = "";
        broadcastState();
    }

    private List<DemoPhase> allowedTransitions(DemoPhase from) {
        return switch (from) {
            case INIT -> List.of(DemoPhase.EVENT_INJECTED);
            case EVENT_INJECTED -> List.of(DemoPhase.SIMULATING);
            case SIMULATING -> List.of(DemoPhase.SIMULATION_DONE);
            case SIMULATION_DONE -> List.of(DemoPhase.OPTIMIZING);
            case OPTIMIZING -> List.of(DemoPhase.GAMING);
            case GAMING -> List.of(DemoPhase.PLAN_SELECTED);
            case PLAN_SELECTED -> List.of(DemoPhase.DONE, DemoPhase.GAMING);
            case DONE -> List.of();
        };
    }
}
