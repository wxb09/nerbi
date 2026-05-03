package com.neighbor.chat.dto;

import java.time.LocalDateTime;

public record ConversationDTO(
    Long id,
    UserInfo otherUser,
    LastMessage lastMessage,
    Long unreadCount
) {
    public record UserInfo(
        Long id,
        String nickname,
        String avatar,
        Boolean isOnline
    ) {}
    
    public record LastMessage(
        String content,
        String type,
        LocalDateTime createdAt,
        Boolean isRead
    ) {}
}
