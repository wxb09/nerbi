package com.neighbor.dto;

import com.neighbor.enums.DisputeStatus;

public record DisputeDTO(
    Long id,
    Long borrowId,
    String itemName,
    String reporterNickname,
    Long reporterId,
    String reason,
    DisputeStatus status,
    String resolution,
    String resolvedByName,
    String createdAt,
    String resolvedAt
) {}
