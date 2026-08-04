package com.boyu.demo.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 推演进度动画控制器 —— progress 状态唯一来源。
 * <p>每 500ms 推进一次，默认 120 秒跑完 342 条路径；{@link #fastForward()} 2x 加速，
 * {@link #skipSimulation()} 直接完成。进度更新经 {@link DemoStateMachine} 广播 demo-state，避免双份进度。
 * <p>⚠️ 竞态防护：tick 定时任务与 prepare/start/fastForward/skipSimulation 共用同一把锁
 * （{@code synchronized this}）修改 progress/running/speed，保证跳过/取消后不再有 tick 推进进度。
 */
@Component
public class TimelineController {

    private static final Logger log = LoggerFactory.getLogger(TimelineController.class);

    /** 每 500ms 步进（百分比），240 步 = 120 秒完成。 */
    private static final double STEP = 100.0 / 240;
    private static final int TOTAL_PATHS = 342;
    private static final List<String> PATH_POOL = List.of(
            "改港青岛+铁路转运", "空运关键物料", "启用备选供应商",
            "宁波→青岛航线重构", "苏州仓库存调拨", "天津港分流方案");
    private static final Random RANDOM = new Random();

    private final DemoStateMachine stateMachine;

    private volatile double progress = 0.0;
    private volatile int speed = 1;          // 1 = 正常，2 = fast-forward
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TimelineController(@Lazy DemoStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /** 500ms 定时心跳：running 时推进 progress 并经状态机广播。 */
    @Scheduled(fixedRate = 500)
    public void tick() {
        synchronized (this) {
            if (!running.get()) {
                return;
            }
            progress += speed * STEP;
            if (progress >= 100.0) {
                progress = 100.0;
                running.set(false);
                stateMachine.transitionTo(DemoPhase.SIMULATION_DONE);
                log.debug("Timeline finished: 342 条路径全部跑完");
            } else {
                stateMachine.broadcastState(buildMessage());
            }
        }
    }

    /** 准备阶段：重置进度/速度/运行标记（不开始计时）。 */
    public void prepare() {
        synchronized (this) {
            progress = 0.0;
            speed = 1;
            running.set(false);
        }
    }

    /** 启动推演动画；已在运行时直接忽略，保证幂等。 */
    public void start() {
        synchronized (this) {
            if (running.get()) {
                return;
            }
            running.set(true);
        }
        log.debug("Timeline start, speed={}", speed);
    }

    /** 2x 快进：每 tick 步长翻倍。 */
    public void fastForward() {
        synchronized (this) {
            speed = 2;
        }
        log.debug("Timeline fast-forward, speed=2");
    }

    /** 跳过推演：进度直接置满、停止计时（状态迁移由调用方负责）。 */
    public void skipSimulation() {
        synchronized (this) {
            progress = 100.0;
            running.set(false);
        }
        log.debug("Timeline skip to end");
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getProgress() {
        return (int) Math.round(progress);
    }

    private String buildMessage() {
        int n = (int) Math.min(TOTAL_PATHS, Math.round(TOTAL_PATHS * progress / 100.0));
        return "已跑完 " + n + "/342 条路径 · " + PATH_POOL.get(RANDOM.nextInt(PATH_POOL.size()));
    }
}
