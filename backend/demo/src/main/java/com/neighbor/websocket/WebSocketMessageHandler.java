package com.neighbor.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketMessageHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHandler.class);
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            WebSocketSession oldSession = userSessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                try {
                    oldSession.close();
                } catch (IOException e) {
                    log.error("关闭旧连接失败", e);
                }
            }
            userSessions.put(userId, session);
            log.info("用户上线：userId={}, sessionId={}, 当前在线人数={}", 
                    userId, session.getId(), userSessions.size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();
        log.debug("收到消息：userId={}, payload={}", userId, payload);
        
        Map<String, Object> response = Map.of(
            "type", "PONG",
            "data", Map.of("timestamp", System.currentTimeMillis())
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户下线：userId={}, reason={}, 当前在线人数={}", 
                    userId, status, userSessions.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("WebSocket 传输错误：userId={}, error={}", userId, exception.getMessage());
    }

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    public int getOnlineCount() {
        return userSessions.size();
    }

    public boolean sendMessageToUser(Long userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session == null || !session.isOpen()) {
            log.debug("用户不在线：userId={}", userId);
            return false;
        }
        
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.debug("消息推送成功：userId={}", userId);
            return true;
        } catch (IOException e) {
            log.error("消息推送失败：userId={}", userId, e);
            userSessions.remove(userId);
            return false;
        }
    }

    public void broadcastToAll(Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化消息失败", e);
            return;
        }
        
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error("广播消息失败：userId={}", userId, e);
                }
            }
        });
    }
}
