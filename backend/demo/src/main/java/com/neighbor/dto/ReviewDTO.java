package com.neighbor.dto;

import com.neighbor.enums.RatingTag;
import com.neighbor.enums.ReviewType;
import java.time.LocalDateTime;

public record ReviewDTO(
    Long id,
    Long borrowId,
    Long itemId,
    String itemName,
    UserInfo fromUser,
    UserInfo toUser,
    ReviewType targetType,
    Integer ratingStar,
    RatingTag ratingTag,
    String ratingTagDesc,
    String content,
    LocalDateTime createdAt
) {
    public record UserInfo(
        Long id,
        String nickname,
        String avatar
    ) {}
}
