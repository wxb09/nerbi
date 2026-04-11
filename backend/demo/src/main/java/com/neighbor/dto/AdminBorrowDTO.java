package com.neighbor.dto;

import com.neighbor.enums.BorrowStatus;

public record AdminBorrowDTO(
    Long id,
    String itemName,
    Long itemId,
    String borrowerNickname,
    Long borrowerId,
    String lenderNickname,
    Long lenderId,
    String startDate,
    String endDate,
    BorrowStatus status,
    String purpose,
    String createdAt
) {}
