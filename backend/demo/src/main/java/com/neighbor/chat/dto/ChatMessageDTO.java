package com.neighbor.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageDTO(
    Long id,
    Long conversationId,
    Long senderId,
    Long receiverId,
    String content,
    String type,
    Boolean isRead,
    LocalDateTime createdAt
) {}
