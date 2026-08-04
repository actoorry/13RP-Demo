package com.boyu.demo.controller;

import com.boyu.demo.orchestrator.DemoPhase;
import com.boyu.demo.orchestrator.DemoStateMachine;

/**
 * 预计算数据加载器：从 classpath demo-data/*.json 读取推演/方案/博弈/指令数据
 */
@org.springframework.stereotype.Component
public class PrecomputedData {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final java.util.Map<String, com.fasterxml.jackson.databind.JsonNode> cache = new java.util.concurrent.ConcurrentHashMap<>();

    public com.fasterxml.jackson.databind.JsonNode paths() {
        return load("demo-data/paths.json");
    }

    public com.fasterxml.jackson.databind.JsonNode solutions(String preference) {
        com.fasterxml.jackson.databind.JsonNode root = load("demo-data/solutions.json");
        com.fasterxml.jackson.databind.JsonNode plans = root.path("plans");
        if (plans.isArray() && !"".equals(preference)) {
            // 按偏好排序：preferenceRank 中该偏好的排序值升序
            java.util.List<com.fasterxml.jackson.databind.JsonNode> list = new java.util.ArrayList<>();
            plans.forEach(list::add);
            list.sort((a, b) -> Integer.compare(
                    a.path("preferenceRank").path(preference).asInt(99),
                    b.path("preferenceRank").path(preference).asInt(99)));
            return mapper.createArrayNode().addAll(list);
        }
        return plans;
    }

    public com.fasterxml.jackson.databind.JsonNode gameResults(String factors) {
        return load("demo-data/game-results.json");
    }

    public com.fasterxml.jackson.databind.JsonNode instructions(String planId) {
        return load("demo-data/instructions/plan_" + planId.toLowerCase() + ".json");
    }

    private com.fasterxml.jackson.databind.JsonNode load(String path) {
        return cache.computeIfAbsent(path, p -> {
            try (var in = getClass().getClassLoader().getResourceAsStream(p)) {
                if (in == null) throw new IllegalStateException("缺少预计算数据: " + p);
                return mapper.readTree(in);
            } catch (Exception e) {
                throw new IllegalStateException("预计算数据加载失败: " + p, e);
            }
        });
    }
}
