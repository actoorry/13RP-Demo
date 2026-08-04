package com.boyu.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void add(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    /** 向所有客户端广播一条带 channel 字段的消息 */
    public void broadcast(String channel, Object payload) {
        try {
            String json = mapper.writeValueAsString(Map.of(
                    "channel", channel,
                    "payload", payload));
            TextMessage msg = new TextMessage(json);
            sessions.values().forEach(s -> {
                try {
                    if (s.isOpen()) s.sendMessage(msg);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}
