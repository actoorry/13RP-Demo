package com.boyu.demo.controller;

import com.boyu.demo.orchestrator.DemoPhase;
import com.boyu.demo.orchestrator.DemoStateMachine;
import com.boyu.demo.orchestrator.TimelineController;
import com.boyu.demo.service.MockDataService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 决策演示 REST API（WebSocket demo-action 也复用这些方法）
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoStateMachine stateMachine;
    private final TimelineController timeline;
    private final MockDataService mockData;
    private final PrecomputedData precomputed;

    public DemoController(DemoStateMachine stateMachine,
                          TimelineController timeline,
                          MockDataService mockData,
                          PrecomputedData precomputed) {
        this.stateMachine = stateMachine;
        this.timeline = timeline;
        this.mockData = mockData;
        this.precomputed = precomputed;
    }

    @PostMapping("/trigger-event")
    public Map<String, Object> triggerEvent(@RequestBody(required = false) Map<String, Object> body) {
        String eventType = body == null ? "typhoon_port_closure" : String.valueOf(body.getOrDefault("eventType", "typhoon_port_closure"));
        int duration = body == null ? 5 : Integer.parseInt(String.valueOf(body.getOrDefault("duration", 5)));
        return triggerEvent(eventType, duration);
    }

    /** 便捷重载：供 WebSocket handler 直接调用 */
    public Map<String, Object> triggerEvent(String eventType, int duration) {
        if ("typhoon_port_closure".equals(eventType)) {
            mockData.injectTyphoon();
        }
        stateMachine.transition(DemoPhase.EVENT_INJECTED);
        return Map.of("ok", true, "eventType", eventType, "duration", duration);
    }

    @PostMapping("/start-simulation")
    public Map<String, Object> startSimulation() {
        timeline.playSimulation();
        return Map.of("ok", true);
    }

    @PostMapping("/start-optimization")
    public Map<String, Object> startOptimization(@RequestBody(required = false) Map<String, Object> body) {
        return startOptimization(body == null ? "balanced" : String.valueOf(body.getOrDefault("preference", "balanced")));
    }

    /** 便捷重载：供 WebSocket handler 直接调用 */
    public Map<String, Object> startOptimization(String preference) {
        stateMachine.transition(DemoPhase.OPTIMIZING);
        return Map.of("ok", true, "preference", preference);
    }

    @PostMapping("/start-gaming")
    public Map<String, Object> startGaming() {
        stateMachine.transition(DemoPhase.GAMING);
        return Map.of("ok", true);
    }

    @PostMapping("/confirm-plan")
    public Map<String, Object> confirmPlan(@RequestBody Map<String, Object> body) {
        return confirmPlan(String.valueOf(body.getOrDefault("planId", "P1")));
    }

    /** 便捷重载：供 WebSocket handler 直接调用 */
    public Map<String, Object> confirmPlan(String planId) {
        stateMachine.transition(DemoPhase.PLAN_SELECTED);
        return Map.of("ok", true, "planId", planId);
    }

    @PostMapping("/fast-forward")
    public Map<String, Object> fastForward() {
        timeline.fastForward();
        return Map.of("ok", true);
    }

    @PostMapping("/skip-simulation")
    public Map<String, Object> skipSimulation() {
        timeline.skipSimulation();
        return Map.of("ok", true);
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        timeline.reset();
        mockData.reset();
        // 状态机无公开重置方法，这里通过直接设置初始阶段（新增 resetTransition 语义）
        stateMachine.resetForNewDemo();
        return Map.of("ok", true);
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        return Map.of(
                "state", stateMachine.getPhase().name(),
                "phase", stateMachine.getPhase().name());
    }

    @GetMapping("/solutions")
    public JsonNode solutions(@RequestParam(defaultValue = "balanced") String preference) {
        return precomputed.solutions(preference);
    }

    @GetMapping("/game-results")
    public JsonNode gameResults(@RequestParam(defaultValue = "") String factors) {
        return precomputed.gameResults(factors);
    }

    @GetMapping("/instructions")
    public JsonNode instructions(@RequestParam(defaultValue = "P1") String planId) {
        return precomputed.instructions(planId);
    }

    @GetMapping("/paths")
    public JsonNode paths() {
        return precomputed.paths();
    }
}
