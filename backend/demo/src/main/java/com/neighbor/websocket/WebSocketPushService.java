package com.neighbor.websocket;

import com.neighbor.dto.MessageDTO;
import com.neighbor.dto.PushNotification;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.repository.BorrowRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketPushService {

    private final WebSocketMessageHandler webSocketMessageHandler;
    private final BorrowRepository borrowRepository;

    public WebSocketPushService(WebSocketMessageHandler webSocketMessageHandler, 
                                BorrowRepository borrowRepository) {
        this.webSocketMessageHandler = webSocketMessageHandler;
        this.borrowRepository = borrowRepository;
    }

    public boolean pushMessage(Long userId, MessageDTO message) {
        Map<String, Object> payload = Map.of(
            "type", "NEW_MESSAGE",
            "data", message
        );
        return webSocketMessageHandler.sendMessageToUser(userId, payload);
    }

    public boolean pushUnreadCount(Long userId, Long unreadCount) {
        Map<String, Object> payload = Map.of(
            "type", "UNREAD_COUNT",
            "data", Map.of("count", unreadCount)
        );
        return webSocketMessageHandler.sendMessageToUser(userId, payload);
    }

    public boolean pushNotification(Long userId, String title, String content) {
        Map<String, Object> payload = Map.of(
            "type", "NOTIFICATION",
            "data", Map.of(
                "title", title,
                "content", content,
                "timestamp", System.currentTimeMillis()
            )
        );
        return webSocketMessageHandler.sendMessageToUser(userId, payload);
    }

    public boolean pushToUser(Long userId, PushNotification notification) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", notification.message());
        if (notification.itemId() != null) {
            data.put("itemId", notification.itemId());
        }
        if (notification.itemStatus() != null) {
            data.put("itemStatus", notification.itemStatus());
        }
        if (notification.pendingCount() != null) {
            data.put("pendingCount", notification.pendingCount());
        }
        
        Map<String, Object> payload = Map.of(
            "type", notification.type(),
            "data", data
        );
        return webSocketMessageHandler.sendMessageToUser(userId, payload);
    }

    public void pushToUsers(Long[] userIds, PushNotification notification) {
        for (Long userId : userIds) {
            pushToUser(userId, notification);
        }
    }

    public Long getPendingCount(Long userId) {
        Long pendingApproval = borrowRepository.countByLenderIdAndStatus(userId, BorrowStatus.PENDING);
        Long returnRequested = borrowRepository.countByLenderIdAndStatus(userId, BorrowStatus.RETURN_REQUESTED);
        return (pendingApproval != null ? pendingApproval : 0L) + (returnRequested != null ? returnRequested : 0L);
    }

    public boolean isUserOnline(Long userId) {
        return webSocketMessageHandler.isUserOnline(userId);
    }

    public int getOnlineCount() {
        return webSocketMessageHandler.getOnlineCount();
    }
}
