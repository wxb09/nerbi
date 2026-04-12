package com.neighbor.dto;

import jakarta.validation.constraints.NotNull;

public record AddressVerifyRequest(
        @NotNull(message = "小区不能为空")
        Long communityId,
        String building,
        String unit
) {}
