package com.neighbor.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private final ObjectMapper objectMapper;

    public WebSocketMessageHandler() {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

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
        
        log.info("[WebSocket] 尝试推送消息: userId={}, session存在={}, 在线人数={}", 
                userId, session != null, userSessions.size());
        
        if (session == null) {
            log.info("[WebSocket] 用户不在线（无session）：userId={}, 当前在线用户={}", 
                    userId, userSessions.keySet());
            return false;
        }
        
        if (!session.isOpen()) {
            log.info("[WebSocket] 用户不在线（session已关闭）：userId={}", userId);
            userSessions.remove(userId);
            return false;
        }
        
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.info("[WebSocket] 消息推送成功：userId={}, message={}", userId, json);
            return true;
        } catch (IOException e) {
            log.error("[WebSocket] 消息推送失败：userId={}", userId, e);
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
