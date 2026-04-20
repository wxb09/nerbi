package com.neighbor.dto;

import java.math.BigDecimal;

public record UserDTO(
    Long id,
    String nickname,
    String avatar,
    String phone,
    String bio,
    Long communityId,
    String communityName,
    String building,
    String unit,
    String alipayAccount,
    String addressVerifyStatus,
    BigDecimal creditScore,
    Integer borrowCount,
    Integer lendCount,
    Integer co2Saved
) {}
