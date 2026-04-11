package com.neighbor.dto;

public record AdminStatsDTO(
    Long totalUsers,
    Long totalItems,
    Long totalBorrows,
    Long pendingReviewItems,
    Long activeBorrows,
    Long pendingDisputes,
    Long bannedUsers,
    Long thisMonthUsers,
    Long thisMonthBorrows
) {}
