package com.boyu.demo.websocket;

import com.boyu.demo.controller.DemoController;
import com.boyu.demo.orchestrator.DemoPhase;
import com.boyu.demo.orchestrator.DemoStateMachine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class DemoWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final DemoStateMachine stateMachine;
    private final DemoController demoController;
    private final ObjectMapper mapper = new ObjectMapper();

    public DemoWebSocketHandler(WebSocketSessionManager sessionManager,
                                DemoStateMachine stateMachine,
                                DemoController demoController) {
        this.sessionManager = sessionManager;
        this.stateMachine = stateMachine;
        this.demoController = demoController;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.add(session);
        // 新客户端连上后，立即推送当前状态
        stateMachine.broadcastState();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = mapper.readTree(message.getPayload());
        String action = node.path("action").asText();
        switch (action) {
            case "trigger-event" -> demoController.triggerEvent(
                    node.path("eventType").asText("typhoon_port_closure"),
                    node.path("duration").asInt(5));
            case "start-simulation" -> demoController.startSimulation();
            case "start-optimization" -> demoController.startOptimization(
                    node.path("preference").asText("balanced"));
            case "start-gaming" -> demoController.startGaming();
            case "confirm-plan" -> demoController.confirmPlan(node.path("planId").asText("P1"));
            case "reset" -> demoController.reset();
            default -> session.sendMessage(new TextMessage(mapper.writeValueAsString(
                    Map.of("channel", "error", "payload", Map.of("message", "未知动作: " + action)))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.remove(session);
    }
}
