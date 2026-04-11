package com.neighbor.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDisputeRequest(
    @NotBlank Long borrowId,
    @NotBlank String reason
) {}
