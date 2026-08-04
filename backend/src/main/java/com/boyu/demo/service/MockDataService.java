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
 * 四域（订单/物流/港口/库存）模拟数据生成器。
 * <p>{@code @Scheduled} 每秒推送 dashboard 数据：订单交付率（初始 96，台风后渐变到 60）、
 * 未交付订单（1200±）、5 港口状态（含中文名映射）、品类库存（稀土·镝 封港期间递减）。
 */
@Service
public class MockDataService {

    private static final Logger log = LoggerFactory.getLogger(MockDataService.class);

    private static final List<String> PORT_IDS = List.of("NINGBO", "SHANGHAI", "QINGDAO", "TIANJIN", "GUANGZHOU");
    private static final Map<String, String> PORT_NAMES = Map.of(
            "NINGBO", "宁波舟山港", "SHANGHAI", "上海港", "QINGDAO", "青岛港",
            "TIANJIN", "天津港", "GUANGZHOU", "广州港");
    private static final List<String> ROUTE_KEYS = List.of(
            "NINGBO_QINGDAO", "NINGBO_TIANJIN", "SHANGHAI_SUZHOU", "QINGDAO_TIANJIN", "TIANJIN_GUANGZHOU");
    private static final Random RANDOM = new Random();

    /** 港口状态：正常 / 封港 / 拥堵。 */
    public enum PortStatus { NORMAL, CLOSED, CONGESTED }

    private final WebSocketSessionManager ws;

    /** 订单域：交付率（初始 96%，台风封港后渐变至 60%）。 */
    private volatile double deliveryRate = 96.0;
    private volatile boolean typhoonActive = false;

    /** 港口域：5 港口状态。 */
    private final Map<String, PortStatus> ports = new ConcurrentHashMap<>();

    /** 物流域：港口间航线时效（天）。 */
    private final Map<String, Integer> routeDays = new ConcurrentHashMap<>();

    /** 库存域：品类库存水位（稀土·镝 封港期间递减）。 */
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    public MockDataService(WebSocketSessionManager ws) {
        this.ws = ws;
        reset();
    }

    /** 每秒推送四域看板数据。 */
    @Scheduled(fixedRateString = "${demo.data-push-interval-ms:1000}")
    public void generateAndPush() {
        if (typhoonActive) {
            deliveryRate = Math.max(60.0, deliveryRate - 0.4);                       // 96 → 60 渐变
            inventory.computeIfPresent("稀土·镝", (k, v) -> Math.max(600, v - 2));    // 封港期间稀土递减
        }
        Map<String, Object> tick = new LinkedHashMap<>();
        tick.put("orderDeliveryRate", round1(clamp(deliveryRate + noise(2), 0, 100)));
        tick.put("openOrders", 1200 + RANDOM.nextInt(101) - 50);
        tick.put("ports", buildPorts());
        tick.put("routes", buildRoutes());
        tick.put("inventory", buildInventory());
        ws.broadcast("dashboard", tick);
    }

    /** 注入台风封港：宁波/上海港口置 CLOSED。 */
    public void injectTyphoon() {
        typhoonActive = true;
        ports.put("NINGBO", PortStatus.CLOSED);
        ports.put("SHANGHAI", PortStatus.CLOSED);
        log.debug("Typhoon injected: NINGBO/SHANGHAI -> CLOSED");
    }

    /** 恢复初始状态。 */
    public void reset() {
        typhoonActive = false;
        deliveryRate = 96.0;
        for (String id : PORT_IDS) {
            ports.put(id, PortStatus.NORMAL);
        }
        routeDays.clear();
        routeDays.put("NINGBO_QINGDAO", 2);
        routeDays.put("NINGBO_TIANJIN", 3);
        routeDays.put("SHANGHAI_SUZHOU", 1);
        routeDays.put("QINGDAO_TIANJIN", 2);
        routeDays.put("TIANJIN_GUANGZHOU", 4);
        inventory.put("稀土·镝", 1200);
        inventory.put("石油化工·聚丙烯", 3400);
        inventory.put("办公设备·打印机", 860);
    }

    public double getDeliveryRate() {
        return deliveryRate;
    }

    public boolean isTyphoonActive() {
        return typhoonActive;
    }

    private List<Map<String, Object>> buildPorts() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String id : PORT_IDS) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", id);
            p.put("name", PORT_NAMES.get(id));
            p.put("status", ports.getOrDefault(id, PortStatus.NORMAL).name());
            list.add(p);
        }
        return list;
    }

    private List<Map<String, Object>> buildRoutes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : ROUTE_KEYS) {
            String[] parts = key.split("_");
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
