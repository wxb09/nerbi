package com.neighbor.audit.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSensitiveWordRequest(
    @NotBlank(message = "敏感词不能为空")
    String word,
    String category,
    Integer severity
) {
    public CreateSensitiveWordRequest {
        if (category == null || category.isBlank()) {
            category = "OTHER";
        }
        if (severity == null) {
            severity = 1;
        }
    }
}
