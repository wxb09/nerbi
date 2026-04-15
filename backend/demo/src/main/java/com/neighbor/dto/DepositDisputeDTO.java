package com.neighbor.dto;

import com.neighbor.enums.DepositDisputeStatus;
import com.neighbor.enums.DepositDisputeType;
import com.neighbor.enums.InitiatorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositDisputeDTO(
    Long id,
    Long paymentId,
    Long borrowId,
    String itemName,
    Long initiatorId,
    String initiatorName,
    InitiatorType initiatorType,
    DepositDisputeType disputeType,
    String description,
    String evidenceImages,
    BigDecimal claimAmount,
    String claimReason,
    DepositDisputeStatus status,
    BigDecimal actualDeduction,
    String resolution,
    Long handlerId,
    String handlerName,
    LocalDateTime handledAt,
    BigDecimal depositAmount,
    LocalDateTime createdAt
) {}
