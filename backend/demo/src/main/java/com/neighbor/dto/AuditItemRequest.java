package com.neighbor.dto;

import jakarta.validation.constraints.NotBlank;

public record AuditItemRequest(
    @NotBlank String action,
    String remark
) {}
