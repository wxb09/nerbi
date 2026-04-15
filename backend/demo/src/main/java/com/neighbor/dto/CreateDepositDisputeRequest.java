package com.neighbor.dto;

import com.neighbor.enums.DepositDisputeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateDepositDisputeRequest(
    @NotNull Long borrowId,
    @NotNull DepositDisputeType disputeType,
    @NotBlank String description,
    String evidenceImages,
    @NotNull @DecimalMin(value = "0.01") BigDecimal claimAmount,
    String claimReason
) {}
