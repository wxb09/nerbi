package com.neighbor.dto;

import com.neighbor.enums.ItemStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ItemDetailDTO(
    Long id,
    String name,
    String description,
    String story,
    Long categoryId,
    String categoryName,
    BigDecimal pricePerDay,
    BigDecimal deposit,
    BigDecimal creditRequired,
    List<String> returnRequirements,
    ItemStatus status,
    Integer borrowCount,
    Integer viewCount,
    Integer favoriteCount,
    List<String> tags,
    OwnerDTO owner,
    String communityName,
    String building,
    LocalDateTime createdAt,
    LocalDateTime publishedAt,
    List<String> images
) {}
