package com.neighbor.dto;

import com.neighbor.enums.ItemStatus;
import java.math.BigDecimal;

public record AdminItemDTO(
    Long id,
    String name,
    String mainImage,
    ItemStatus status,
    String categoryName,
    BigDecimal pricePerDay,
    String ownerNickname,
    Long ownerId,
    String auditRemark,
    String createdAt
) {}
