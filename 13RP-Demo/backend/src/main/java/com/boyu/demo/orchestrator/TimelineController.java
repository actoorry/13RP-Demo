package com.boyu.demo.orchestrator;

import com.boyu.demo.websocket.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 推演进度动画：每 0.5 秒推进一次进度，120 秒完成（预计算回放）
 */
@Component
public class TimelineController {

    private final DemoStateMachine stateMachine;
    private final WebSocketSessionManager ws;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;
    private volatile boolean fast = false;

    private static final String[] PATH_POOL = {
            "正在推演：改港青岛 + 铁路转运…",
            "正在推演：空运关键物料…",
            "正在推演：启用备选供应商…",
            "正在推演：宁波→青岛 航线重构…",
            "正在推演：苏州仓库存调拨…",
            "正在推演：天津港分流方案…"
    };

    @Value("${demo.simulation-duration-ms:120000}")
    private long simulationDurationMs;

    public TimelineController(DemoStateMachine stateMachine, WebSocketSessionManager ws) {
        this.stateMachine = stateMachine;
        this.ws = ws;
    }

    /** 启动 120 秒推演动画 */
    public synchronized void playSimulation() {
        cancelTask();
        stateMachine.transition(DemoPhase.SIMULATING);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        final long intervalMs = 500;
        final double stepPerTick = 100.0 / (simulationDurationMs / intervalMs); // 0.5s → ~0.4167%/tick

        task = scheduler.scheduleAtFixedRate(() -> {
            progress += stepPerTick * (fast ? 4 : 1);
            if (progress >= 100) {
                stateMachine.updateProgress(100, "已跑完 342/342 条路径");
                stateMachine.transition(DemoPhase.SIMULATION_DONE);
                cancelTask();
                return;
            }
            int done = (int) (progress * 3.42);
            String msg = PATH_POOL[(int) (Math.random() * PATH_POOL.length)];
            stateMachine.updateProgress(progress, "已跑完 " + done + "/342 条路径 · " + msg);
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private volatile double progress = 0;

    /** 快进：2 倍速率 */
    public synchronized void fastForward() {
        fast = true;
    }

    /** 跳过：直接完成推演 */
    public synchronized void skipSimulation() {
        cancelTask();
        progress = 100;
        stateMachine.updateProgress(100, "已跑完 342/342 条路径");
        stateMachine.transition(DemoPhase.SIMULATION_DONE);
    }

    /** 重置（新演示） */
    public synchronized void reset() {
        cancelTask();
        progress = 0;
        fast = false;
    }

    private void cancelTask() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}
