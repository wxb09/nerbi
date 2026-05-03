package com.neighbor.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
    @NotNull(message = "接收者ID不能为空")
    Long receiverId,
    
    @NotBlank(message = "消息内容不能为空")
    String content,
    
    String type
) {}
