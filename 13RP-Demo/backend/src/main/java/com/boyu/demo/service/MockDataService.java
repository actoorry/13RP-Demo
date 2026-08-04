package com.boyu.demo.service;

import com.boyu.demo.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟数据源：订单 / 物流 / 库存 / 港口 四域，每秒推送一次 dashboard 消息
 */
@Service
public class MockDataService {

    private final WebSocketSessionManager ws;
    private final ObjectMapper mapper = new ObjectMapper();

    // 港口状态
    public enum PortStatus { NORMAL, CLOSED, CONGESTED }

    private final Map<String, PortStatus> ports = new ConcurrentHashMap<>();
    private final Map<String, String> portNames = new LinkedHashMap<>();

    // 订单域
    private volatile double deliveryRate = 96.0;
    private volatile double targetDeliveryRate = 96.0;
    private volatile int openOrders = 1200;

    // 库存域（品类 → 吨）
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    @Value("${demo.data-push-interval-ms:1000}")
    private long pushIntervalMs;

    public MockDataService() {
        ports.put("NINGBO", PortStatus.NORMAL);
        ports.put("SHANGHAI", PortStatus.NORMAL);
        ports.put("QINGDAO", PortStatus.NORMAL);
        ports.put("TIANJIN", PortStatus.NORMAL);
        ports.put("GUANGZHOU", PortStatus.NORMAL);

        portNames.put("NINGBO", "宁波舟山港");
        portNames.put("SHANGHAI", "上海港");
        portNames.put("QINGDAO", "青岛港");
        portNames.put("TIANJIN", "天津港");
        portNames.put("GUANGZHOU", "广州港");

        inventory.put("稀土·镝", 1200);
        inventory.put("石油化工·聚丙烯", 3400);
        inventory.put("办公设备·打印机", 860);
    }

    /** 注入台风封港：宁波/上海 封港，交付率目标降至 60 */
    public void injectTyphoon() {
        ports.put("NINGBO", PortStatus.CLOSED);
        ports.put("SHANGHAI", PortStatus.CLOSED);
        targetDeliveryRate = 60.0;
    }

    /** 重置所有状态（演示重开） */
    public void reset() {
        ports.replaceAll((k, v) -> PortStatus.NORMAL);
        targetDeliveryRate = 96.0;
        deliveryRate = 96.0;
        openOrders = 1200;
        inventory.put("稀土·镝", 1200);
    }

    @Scheduled(fixedRateString = "${demo.data-push-interval-ms:1000}")
    public void generateAndPush() {
        // 交付率逐帧逼近目标（模拟渐变动画）
        deliveryRate += (targetDeliveryRate - deliveryRate) * 0.12;
        deliveryRate = Math.round(deliveryRate * 10) / 10.0;

        // 库存水位（封港期间稀土下降）
        if (ports.get("NINGBO") == PortStatus.CLOSED) {
            inventory.computeIfPresent("稀土·镝", (k, v) -> Math.max(600, v - 8));
        }

        Map<String, Object> tick = new LinkedHashMap<>();
        tick.put("timestamp", java.time.LocalDateTime.now().toString());
        tick.put("orderDeliveryRate", deliveryRate);
        tick.put("openOrders", openOrders + (int) (ThreadLocalRandom.current().nextGaussian() * 20));
        tick.put("ports", ports);
        tick.put("portNames", portNames);
        tick.put("inventory", inventory);
        ws.broadcast("dashboard", tick);
    }
}
