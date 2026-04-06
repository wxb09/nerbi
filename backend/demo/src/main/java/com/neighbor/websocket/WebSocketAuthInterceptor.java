package com.neighbor.websocket;

import com.neighbor.auth.JwtService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 握手失败：缺少 token");
                return false;
            }
            
            try {
                Claims claims = jwtService.parse(token);
                String userId = claims.get("userId", String.class);
                
                if (userId != null) {
                    attributes.put("userId", Long.parseLong(userId));
                    log.info("WebSocket 握手成功：用户ID={}", userId);
                    return true;
                }
            } catch (Exception e) {
                log.warn("WebSocket 握手失败：token 无效 - {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手异常", exception);
        }
    }
}
