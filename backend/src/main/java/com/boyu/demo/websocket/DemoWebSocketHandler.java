package com.boyu.demo.websocket;

import com.boyu.demo.controller.DemoController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * /ws/demo 原生 WebSocket 处理器。
 * <p>收到 JSON {@code {action, ...}} 分发到 {@link DemoController} 的同名便捷重载
 * （String/List 参数），避免 WS 端拼 Map。⚠️ 每个 action 必须有 case，
 * 缺了前端点按钮会报"未知动作"。
 */
@Component
public class DemoWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DemoWebSocketHandler.class);

    private final WebSocketSessionManager ws;
    private final DemoController demoController;
    private final ObjectMapper objectMapper;

    public DemoWebSocketHandler(WebSocketSessionManager ws, DemoController demoController, ObjectMapper objectMapper) {
        this.ws = ws;
        this.demoController = demoController;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        ws.add(session);
        log.debug("WebSocket 连接建立: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ws.remove(session);
        log.debug("WebSocket 连接关闭: {} ({})", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String action;
        Map<String, Object> result;
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            action = root.path("action").asText("");
            result = dispatch(action, root);
        } catch (Exception e) {
            log.warn("WebSocket 消息处理异常: {}", e.getMessage());
            action = "";
            result = new LinkedHashMap<>();
            result.put("ok", false);
            result.put("error", "消息解析失败：" + e.getMessage());
        }

        Map<String, Object> ack = new LinkedHashMap<>();
        ack.put("channel", "demo-action");
        ack.put("action", action);
        ack.putAll(result);
        ws.send(session, ack);
    }

    private Map<String, Object> dispatch(String action, JsonNode root) {
        return switch (action) {
            case "trigger-event" -> demoController.triggerEvent();
            case "start-simulation" -> demoController.startSimulation();
            case "start-optimization" -> demoController.startOptimization(text(root, "preference"));
            case "start-gaming" -> demoController.startGaming(stringList(root, "factors"));
            case "confirm-plan" -> demoController.confirmPlan(text(root, "planId"));
            case "fast-forward" -> demoController.fastForward();
            case "skip-simulation" -> demoController.skipSimulation();
            case "reset" -> demoController.reset();
            default -> {
                log.warn("未知动作: {}", action);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("ok", false);
                m.put("error", "未知动作: " + action);
                yield m;
            }
        };
    }

    private static String text(JsonNode root, String field) {
        JsonNode n = root.get(field);
        return (n == null || n.isNull()) ? null : n.asText();
    }

    private static List<String> stringList(JsonNode root, String field) {
        JsonNode arr = root.get(field);
        if (arr == null || !arr.isArray()) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        arr.forEach(n -> list.add(n.asText()));
        return list;
    }
}
