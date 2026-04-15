package com.neighbor.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ResolveDepositDisputeRequest(
    @NotBlank String action,
    BigDecimal actualDeduction,
    String resolution
) {}
