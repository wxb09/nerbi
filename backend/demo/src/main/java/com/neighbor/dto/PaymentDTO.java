package com.neighbor.dto;

import com.neighbor.enums.PaymentStatus;
import com.neighbor.enums.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(
    Long id,
    String outTradeNo,
    String tradeNo,
    Long borrowId,
    String itemName,
    Long payerId,
    String payerName,
    Long payeeId,
    String payeeName,
    PaymentType paymentType,
    BigDecimal rentAmount,
    BigDecimal depositAmount,
    BigDecimal totalAmount,
    PaymentStatus status,
    LocalDateTime paidAt,
    BigDecimal refundAmount,
    LocalDateTime refundedAt,
    LocalDateTime createdAt
) {}
