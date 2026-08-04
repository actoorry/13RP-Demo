package com.boyu.demo.service;

import com.boyu.demo.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 四域（订单/供应网络/物流/库存）模拟数据生成器。
 * <p>{@code @Scheduled} 每秒推送 dashboard 数据：订单交付率（初始 96，供应商缺货后渐变到 60）、
 * 未交付订单（1200±）、5 个供应网络节点状态（含中文名映射）、品类库存（稀土·镝 缺货期间递减）。
 */
@Service
public class MockDataService {

    private static final Logger log = LoggerFactory.getLogger(MockDataService.class);

    private static final List<String> NODE_IDS = List.of("BAOTOU", "GANZHOU", "NINGBO", "SUZHOU", "GUANGZHOU");
    private static final Map<String, String> NODE_NAMES = Map.of(
            "BAOTOU", "包头北方稀土矿业", "GANZHOU", "赣州中重稀土", "NINGBO", "宁波东方磁材",
            "SUZHOU", "苏州应用工厂", "GUANGZHOU", "广州深加工基地");
    private static final Map<String, String> NODE_KINDS = Map.of(
            "BAOTOU", "supplier", "GANZHOU", "supplier", "NINGBO", "supplier",
            "SUZHOU", "factory", "GUANGZHOU", "base");
    private static final List<String> ROUTE_KEYS = List.of(
            "bt-sz", "gz-sz", "nb-sz", "sz-gz", "gz-gz");
    private static final Random RANDOM = new Random();

    /** 供应网络节点状态：正常 / 缺货 / 紧张。 */
    public enum SupplierStatus { NORMAL, SHORTAGE, TIGHT }

    private final WebSocketSessionManager ws;

    /** 订单域：交付率（初始 96%，供应商缺货后渐变至 60%）。 */
    private volatile double deliveryRate = 96.0;
    private volatile boolean shortageActive = false;

    /** 供应商缺货持续天数（天）：F4 接收存储，供后续影响交付率下降速率（本任务不改渐变逻辑）。 */
    private volatile int shortageDurationDays = 5;

    /** 供应网络域：5 节点状态。 */
    private final Map<String, SupplierStatus> nodes = new ConcurrentHashMap<>();

    /** 物流域：供应路线时效（天）。 */
    private final Map<String, Integer> routeDays = new ConcurrentHashMap<>();

    /** 库存域：品类库存水位（稀土·镝 缺货期间递减）。 */
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    public MockDataService(WebSocketSessionManager ws) {
        this.ws = ws;
        reset();
    }

    /** 每秒推送四域看板数据。 */
    @Scheduled(fixedRateString = "${demo.data-push-interval-ms:1000}")
    public void generateAndPush() {
        if (shortageActive) {
            deliveryRate = Math.max(60.0, deliveryRate - 0.4);                       // 96 → 60 渐变
            inventory.computeIfPresent("稀土·镝", (k, v) -> Math.max(600, v - 2));    // 缺货期间稀土递减
        }
        Map<String, Object> tick = new LinkedHashMap<>();
        tick.put("orderDeliveryRate", round1(clamp(deliveryRate + noise(2), 0, 100)));
        tick.put("openOrders", 1200 + RANDOM.nextInt(101) - 50);
        tick.put("ports", buildNodes());
        tick.put("routes", buildRoutes());
        tick.put("inventory", buildInventory());
        ws.broadcast("dashboard", tick);
    }

    /**
     * 注入供应商缺货：包头北方稀土矿业置 SHORTAGE，赣州中重稀土（GANZHOU）与
     * 宁波东方磁材（NINGBO）置 TIGHT（供应链连带紧张，让前端图例"紧张"真实生效），并记录持续天数。
     */
    public void injectSupplierShortage(int duration) {
        shortageActive = true;
        shortageDurationDays = duration;
        nodes.put("BAOTOU", SupplierStatus.SHORTAGE);
        nodes.put("GANZHOU", SupplierStatus.TIGHT);
        nodes.put("NINGBO", SupplierStatus.TIGHT);
        log.debug("Supplier shortage injected: BAOTOU -> SHORTAGE, GANZHOU/NINGBO -> TIGHT, duration={}d", shortageDurationDays);
    }

    /** 便捷重载：默认缺货 5 天。 */
    public void injectSupplierShortage() {
        injectSupplierShortage(5);
    }

    /** 供应商缺货持续天数（天）。 */
    public int getShortageDurationDays() {
        return shortageDurationDays;
    }

    /** 恢复初始状态。 */
    public void reset() {
        shortageActive = false;
        shortageDurationDays = 5;
        deliveryRate = 96.0;
        for (String id : NODE_IDS) {
            nodes.put(id, SupplierStatus.NORMAL);
        }
        routeDays.clear();
        routeDays.put("bt-sz", 2);
        routeDays.put("gz-sz", 3);
        routeDays.put("nb-sz", 1);
        routeDays.put("sz-gz", 2);
        routeDays.put("gz-gz", 4);
        inventory.put("稀土·镝", 1200);
        inventory.put("石油化工·聚丙烯", 3400);
        inventory.put("办公设备·打印机", 860);
    }

    public double getDeliveryRate() {
        return deliveryRate;
    }

    public boolean isShortageActive() {
        return shortageActive;
    }

    private List<Map<String, Object>> buildNodes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String id : NODE_IDS) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", id);
            p.put("name", NODE_NAMES.get(id));
            p.put("status", nodes.getOrDefault(id, SupplierStatus.NORMAL).name());
            p.put("kind", NODE_KINDS.get(id));
            list.add(p);
        }
        return list;
    }

    private List<Map<String, Object>> buildRoutes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : ROUTE_KEYS) {
            String[] parts = key.split("-");
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("from", parts[0]);
            r.put("to", parts[1]);
            r.put("days", routeDays.getOrDefault(key, 2));
            list.add(r);
        }
        return list;
    }

    private List<Map<String, Object>> buildInventory() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(inv("稀土·镝", "吨"));
        list.add(inv("石油化工·聚丙烯", "吨"));
        list.add(inv("办公设备·打印机", "台"));
        return list;
    }

    private Map<String, Object> inv(String category, String unit) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", category);
        m.put("qty", inventory.getOrDefault(category, 0));
        m.put("unit", unit);
        return m;
    }

    private static double noise(double amplitude) {
        return RANDOM.nextGaussian() * amplitude;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
