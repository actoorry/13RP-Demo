package com.boyu.demo.controller;

import com.boyu.demo.orchestrator.DemoPhase;
import com.boyu.demo.orchestrator.DemoStateMachine;
import com.boyu.demo.orchestrator.TimelineController;
import com.boyu.demo.service.MockDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 决策演示 REST 入口。
 * <p>每个动作 REST 方法收 {@code @RequestBody Map}，同时提供同名便捷重载
 * （String/List 参数）供 {@code WebSocket} 直调，避免 WS 端拼 Map（⚠️ 已知坑）。
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoStateMachine stateMachine;
    private final TimelineController timeline;
    private final MockDataService mockDataService;
    private final PrecomputedData precomputed;

    public DemoController(DemoStateMachine stateMachine, TimelineController timeline,
                          MockDataService mockDataService, PrecomputedData precomputed) {
        this.stateMachine = stateMachine;
        this.timeline = timeline;
        this.mockDataService = mockDataService;
        this.precomputed = precomputed;
    }

    // ---------------- 动作（POST） ----------------

    @PostMapping("/trigger-event")
    public Map<String, Object> triggerEvent(@RequestBody(required = false) Map<String, Object> body) {
        return triggerEvent();
    }

    public Map<String, Object> triggerEvent() {
        try {
            mockDataService.injectTyphoon();
            stateMachine.transitionTo(DemoPhase.EVENT_INJECTED);
            return ok();
        } catch (IllegalStateException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/start-simulation")
    public Map<String, Object> startSimulation(@RequestBody(required = false) Map<String, Object> body) {
        return startSimulation();
    }

    public Map<String, Object> startSimulation() {
        if (stateMachine.getPhase() != DemoPhase.EVENT_INJECTED) {
            return error("当前状态不可启动推演：" + stateMachine.getPhase());
        }
        timeline.prepare();
        stateMachine.transitionTo(DemoPhase.SIMULATING);
        timeline.start();
        return ok();
    }

    @PostMapping("/start-optimization")
    public Map<String, Object> startOptimization(@RequestBody(required = false) Map<String, Object> body) {
        return startOptimization(str(body, "preference"));
    }

    public Map<String, Object> startOptimization(String preference) {
        try {
            stateMachine.transitionTo(DemoPhase.OPTIMIZING);
            Map<String, Object> m = ok();
            m.put("preference", (preference == null || preference.isBlank()) ? "balanced" : preference);
            return m;
        } catch (IllegalStateException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/start-gaming")
    public Map<String, Object> startGaming(@RequestBody(required = false) Map<String, Object> body) {
        List<String> factors = null;
        if (body != null && body.get("factors") instanceof List<?> raw) {
            factors = raw.stream().map(String::valueOf).toList();
        }
        return startGaming(factors);
    }

    public Map<String, Object> startGaming(List<String> factors) {
        try {
            stateMachine.transitionTo(DemoPhase.GAMING);
            Map<String, Object> m = ok();
            m.put("factors", factors == null ? List.of() : factors);
            return m;
        } catch (IllegalStateException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/confirm-plan")
    public Map<String, Object> confirmPlan(@RequestBody(required = false) Map<String, Object> body) {
        return confirmPlan(str(body, "planId"));
    }

    public Map<String, Object> confirmPlan(String planId) {
        try {
            stateMachine.transitionTo(DemoPhase.PLAN_SELECTED);
            Map<String, Object> m = ok();
            m.put("planId", (planId == null || planId.isBlank()) ? "P1" : planId);
            return m;
        } catch (IllegalStateException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/fast-forward")
    public Map<String, Object> fastForward(@RequestBody(required = false) Map<String, Object> body) {
        return fastForward();
    }

    public Map<String, Object> fastForward() {
        timeline.fastForward();
        return ok();
    }

    @PostMapping("/skip-simulation")
    public Map<String, Object> skipSimulation(@RequestBody(required = false) Map<String, Object> body) {
        return skipSimulation();
    }

    public Map<String, Object> skipSimulation() {
        if (stateMachine.getPhase() != DemoPhase.SIMULATING) {
            return error("当前状态不可跳过推演：" + stateMachine.getPhase());
        }
        timeline.skipSimulation();
        stateMachine.transitionTo(DemoPhase.SIMULATION_DONE, "已跳过推演，直接完成");
        return ok();
    }

    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestBody(required = false) Map<String, Object> body) {
        return reset();
    }

    public Map<String, Object> reset() {
        timeline.prepare();
        mockDataService.reset();
        stateMachine.resetForNewDemo();
        return ok();
    }

    // ---------------- 查询（GET） ----------------

    @GetMapping("/state")
    public Map<String, Object> state() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("phase", stateMachine.getPhase().name());
        m.put("progress", timeline.getProgress());
        m.put("message", stateMachine.getLastMessage());
        return m;
    }

    @GetMapping("/solutions")
    public Map<String, Object> solutions(@RequestParam(value = "preference", defaultValue = "balanced") String preference) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ok", true);
            m.put("preference", preference);
            m.put("plans", precomputed.solutions(preference));
            return m;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/game-results")
    public Map<String, Object> gameResults() {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ok", true);
            m.putAll(precomputed.gameResults());
            return m;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/instructions")
    public Map<String, Object> instructions(@RequestParam(value = "planId", required = false) String planId) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ok", true);
            m.putAll(precomputed.instructions(planId));
            return m;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @GetMapping("/paths")
    public Map<String, Object> paths() {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ok", true);
            m.putAll(precomputed.paths());
            return m;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ---------------- 响应组装 ----------------

    /** null 安全地从 body Map 提取字符串字段（缺字段/显式 null 均返回 null）。 */
    private static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Map<String, Object> ok() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("phase", stateMachine.getPhase().name());
        m.put("progress", timeline.getProgress());
        m.put("message", stateMachine.getLastMessage());
        return m;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = ok();
        m.put("ok", false);
        m.put("error", msg);
        return m;
    }
}
