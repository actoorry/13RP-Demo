package com.boyu.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 预计算数据加载器：从 classpath demo-data/*.json 懒加载并缓存。
 * <p>使用独立 ObjectMapper（默认命名策略）读取原始树，避免全局 SNAKE_CASE
 * 影响 JSON 文件中的字段名（如 total_paths / winRateBefore 混用）。
 * <p>solutions 按 preferenceRank[preference] 升序返回；instructions 按 planId 读 plan_pX.json。
 */
@Component
public class PrecomputedData {

    private static final Logger log = LoggerFactory.getLogger(PrecomputedData.class);
    private static final String BASE = "demo-data/";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    /** 返回按偏好排序的方案列表（preferenceRank[preference] 升序，越小越优）。 */
    public List<Map<String, Object>> solutions(String preference) {
        Map<String, Object> root = cached("solutions", "solutions.json");
        Object plansObj = root.get("plans");
        if (!(plansObj instanceof List<?> raw)) {
            return List.of();
        }
        String key = (preference == null || preference.isBlank()) ? "balanced" : preference;
        List<Map<String, Object>> plans = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> plan = (Map<String, Object>) m;
                plans.add(plan);
            }
        }
        plans.sort(Comparator.comparingInt(p -> rank(p, key)));
        return plans;
    }

    public Map<String, Object> gameResults() {
        return cached("game-results", "game-results.json");
    }

    public Map<String, Object> instructions(String planId) {
        String id = (planId == null || planId.isBlank()) ? "P1" : planId;
        return cached("instructions-" + id, "instructions/plan_" + id.toLowerCase() + ".json");
    }

    public Map<String, Object> paths() {
        return cached("paths", "paths.json");
    }

    private int rank(Map<String, Object> plan, String key) {
        Object prObj = plan.get("preferenceRank");
        if (prObj instanceof Map<?, ?> pr) {
            Object v = pr.get(key);
            if (v instanceof Number n) {
                return n.intValue();
            }
        }
        return Integer.MAX_VALUE;
    }

    private Map<String, Object> cached(String key, String resourcePath) {
        return cache.computeIfAbsent(key, k -> load(resourcePath));
    }

    private Map<String, Object> load(String resourcePath) {
        String full = BASE + resourcePath;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(full)) {
            if (is == null) {
                throw new IllegalStateException("预计算数据未找到: " + full + "（请确认 demo-data/*.json 已生成）");
            }
            JsonNode node = objectMapper.readTree(is);
            return objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("预计算数据解析失败: " + full + " — " + e.getMessage(), e);
        }
    }
}
