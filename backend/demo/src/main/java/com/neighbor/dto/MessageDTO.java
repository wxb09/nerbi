package com.neighbor.dto;

import com.neighbor.enums.MessageType;
import java.time.LocalDateTime;

public record MessageDTO(
    Long id,
    String type,
    String typeDesc,
    String title,
    String content,
    Long relatedId,
    Boolean isRead,
    LocalDateTime createdAt,
    BorrowInfo borrow
) {
    public record BorrowInfo(
        Long id,
        String itemName,
        String borrowerName,
        String lenderName
    ) {}
}
