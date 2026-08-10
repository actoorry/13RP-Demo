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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return triggerEvent(str(body, "eventType"), intVal(body, "duration", 5));
    }

    /** WebSocket 便捷重载：携带 eventType 与 duration（模拟宇宙初始事件：断供持续天数）。 */
    public Map<String, Object> triggerEvent(String eventType, int duration) {
        try {
            mockDataService.injectSupplierShortage(duration);
            stateMachine.transitionTo(DemoPhase.EVENT_INJECTED);
            Map<String, Object> m = ok();
            m.put("eventType", (eventType == null || eventType.isBlank()) ? "supplier_shortage" : eventType);
            m.put("duration", duration);
            return m;
        } catch (IllegalStateException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/start-simulation")
    public Map<String, Object> startSimulation(@RequestBody(required = false) Map<String, Object> body) {
        return startSimulation();
    }

    public Map<String, Object> startSimulation() {
        DemoPhase phase = stateMachine.getPhase();
        if (phase == DemoPhase.SIMULATING) {
            return error("推演已在进行中");
        }
        if (phase != DemoPhase.EVENT_INJECTED) {
            return error("当前状态不可启动推演：" + phase);
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
        if (stateMachine.getPhase() != DemoPhase.SIMULATING) {
            return error("当前未在推演");
        }
        timeline.fastForward();
        return ok();
    }

    @PostMapping("/skip-simulation")
    public Map<String, Object> skipSimulation(@RequestBody(required = false) Map<String, Object> body) {
        return skipSimulation();
    }

    public Map<String, Object> skipSimulation() {
        if (stateMachine.getPhase() != DemoPhase.SIMULATING) {
            return error("当前未在推演");
        }
        try {
            timeline.skipSimulation();
            stateMachine.transitionTo(DemoPhase.SIMULATION_DONE, "已跳过推演，直接完成");
        } catch (IllegalStateException e) {
            // tick 恰在 skip 前自动跑完 → phase 已非 SIMULATING，幂等返回
            return error(e.getMessage());
        }
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
    public Map<String, Object> gameResults(@RequestParam(value = "factors", required = false) String factors) {
        try {
            Map<String, Object> root = precomputed.gameResults();
            @SuppressWarnings("unchecked")
            Map<String, Object> before = (Map<String, Object>) root.get("before");
            Map<String, Object> hit = resolveGameHit(root, factors);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ok", true);
            m.put("factors", root.get("factors"));
            m.put("before", before);
            m.put("after", hit);
            m.put("results", buildGameRows(before, hit));
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

    // ---------------- game-results 查询辅助（F2：factors 参数生效） ----------------

    /**
     * 解析 factors 参数（'+' / ',' / 空白 分隔——查询串中的 '+' 会被容器解码为空格），
     * 去空、去重后按字母序排序返回。
     */
    private static List<String> parseFactors(String factorsParam) {
        if (factorsParam == null || factorsParam.isBlank()) {
            return List.of();
        }
        return Arrays.stream(factorsParam.split("[+,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    /** 从预计算数据解析命中的胜率表：未传 factors / 解析为空 / key 不存在 → before 基线。 */
    private Map<String, Object> resolveGameHit(Map<String, Object> root, String factorsParam) {
        @SuppressWarnings("unchecked")
        Map<String, Object> results = (Map<String, Object>) root.get("results");
        @SuppressWarnings("unchecked")
        Map<String, Object> before = (Map<String, Object>) root.get("before");
        List<String> selected = parseFactors(factorsParam);
        if (selected.isEmpty()) {
            return before;
        }
        for (String key : gameKeys(root, selected)) {
            Object hit = results.get(key);
            if (hit instanceof Map<?, ?> hm) {
                @SuppressWarnings("unchecked")
                Map<String, Object> hitMap = (Map<String, Object>) hm;
                return hitMap;
            }
        }
        return before;
    }

    /**
     * 候选 key：主键按 factors 声明序 + ','（与预计算 JSON 的键一致，如 congestion,competitor），
     * 并附 '+' / 字母序变体，兼容前端 sort().join('+') 与未来数据格式调整。
     */
    private List<String> gameKeys(Map<String, Object> root, List<String> selected) {
        List<String> declared = gameFactorIds(root);
        List<String> byDecl = selected.stream()
                .sorted(Comparator.comparingInt(declared::indexOf))
                .toList();
        List<String> byAlpha = new ArrayList<>(selected);
        Set<String> keys = new LinkedHashSet<>();
        keys.add(String.join(",", byDecl));
        keys.add(String.join("+", byDecl));
        keys.add(String.join(",", byAlpha));
        keys.add(String.join("+", byAlpha));
        return new ArrayList<>(keys);
    }

    /** 从 game-results 的 factors 数组按声明序提取 id 列表。 */
    private static List<String> gameFactorIds(Map<String, Object> root) {
        List<String> ids = new ArrayList<>();
        Object factors = root.get("factors");
        if (factors instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> factor) {
                    Object id = factor.get("id");
                    if (id != null) {
                        ids.add(String.valueOf(id));
                    }
                }
            }
        }
        return ids;
    }

    /** before / hit 两张 {P1,P2,P3} 胜率表 → 前端 GameResultRow[]（保持前端兼容）。 */
    private static List<Map<String, Object>> buildGameRows(Map<String, Object> before, Map<String, Object> hit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Object> e : before.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("planId", e.getKey());
            row.put("winRateBefore", e.getValue());
            row.put("winRateAfter", hit.getOrDefault(e.getKey(), e.getValue()));
            rows.add(row);
        }
        return rows;
    }

    // ---------------- 响应组装 ----------------

    /** null 安全地从 body Map 提取字符串字段（缺字段/显式 null 均返回 null）。 */
    private static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    /** null 安全地从 body Map 提取 int 字段（缺字段/非法值返回默认值）。 */
    private static int intVal(Map<String, Object> body, String key, int def) {
        Object v = body == null ? null : body.get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignore) {
                // 非法数字落回默认值
            }
        }
        return def;
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
