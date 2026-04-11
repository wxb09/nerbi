package com.neighbor.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveDisputeRequest(
    @NotBlank String action,
    String resolution
) {}
