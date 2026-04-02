package com.neighbor.dto;

import com.neighbor.enums.ItemStatus;
import java.math.BigDecimal;
import java.util.List;

public record ItemListDTO(
    Long id,
    String name,
    String mainImage,
    BigDecimal pricePerDay,
    ItemStatus status,
    OwnerDTO owner,
    String locationText,
    List<String> tags,
    Integer borrowCount,
    Integer viewCount
) {}
