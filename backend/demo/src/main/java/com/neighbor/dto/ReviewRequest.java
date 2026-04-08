package com.neighbor.dto;

import com.neighbor.enums.RatingTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
    @NotNull(message = "借阅ID不能为空")
    Long borrowId,
    
    @NotNull(message = "评价标签不能为空")
    RatingTag ratingTag,
    
    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容不能超过500字")
    String content
) {}
