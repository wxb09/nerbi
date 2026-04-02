package com.neighbor.dto;

import java.math.BigDecimal;

public record UserDTO(
    Long id,
    String nickname,
    String avatar,
    String phone,
    String communityName,
    String building,
    BigDecimal creditScore,
    Integer borrowCount,
    Integer lendCount,
    Integer co2Saved
) {}
