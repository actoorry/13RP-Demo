package com.boyu.demo.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理：ConcurrentHashMap 保存在线会话，提供广播与定向发送。
 * 消息体统一带 {@code channel} 字段，供前端按 channel 分发到对应 Pinia store。
 */
@Component
public class WebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public WebSocketSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void add(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public int count() {
        return sessions.size();
    }

    /** 广播到所有在线会话。 */
    public void broadcast(String channel, Map<String, Object> payload) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", channel);
        if (payload != null) {
            message.putAll(payload);
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("demo broadcast 序列化失败, channel={}", channel, e);
            return;
        }
        for (WebSocketSession session : sessions.values()) {
            send(session, json);
        }
    }

    /** 定向发送（用于动作回执）。 */
    public void send(WebSocketSession session, Map<String, Object> payload) {
        if (payload == null) {
            return;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("demo send 序列化失败", e);
            return;
        }
        send(session, json);
    }

    private void send(WebSocketSession session, String json) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.warn("向会话 {} 发送失败: {}", session.getId(), e.getMessage());
            sessions.remove(session.getId());
        }
    }
}
