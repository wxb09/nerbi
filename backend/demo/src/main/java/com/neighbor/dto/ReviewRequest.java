package com.neighbor.dto;

import com.neighbor.enums.RatingTag;
import com.neighbor.enums.ReviewType;
import jakarta.validation.constraints.*;

public record ReviewRequest(
    @NotNull(message = "借阅ID不能为空")
    Long borrowId,
    
    @NotNull(message = "评价类型不能为空")
    ReviewType targetType,
    
    RatingTag ratingTag,
    
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    Integer ratingStar,
    
    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容不能超过500字")
    String content
) {
    public ReviewRequest {
        if (targetType == ReviewType.ITEM && ratingTag == null) {
            throw new IllegalArgumentException("物品评价必须选择评价标签");
        }
        if (targetType == ReviewType.USER && ratingStar == null) {
            throw new IllegalArgumentException("用户评价必须选择评分");
        }
    }
}
