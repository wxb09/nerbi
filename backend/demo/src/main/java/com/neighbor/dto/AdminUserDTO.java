package com.neighbor.dto;

import java.math.BigDecimal;

public record AdminUserDTO(
    Long id,
    String nickname,
    String avatar,
    String phone,
    String communityName,
    String building,
    BigDecimal creditScore,
    Integer borrowCount,
    Integer lendCount,
    Integer co2Saved,
    String status,
    String role,
    String createdAt
) {}
