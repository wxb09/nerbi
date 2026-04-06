package com.neighbor.dto;

public record UserStatsDTO(
    Integer lentCount,
    Integer borrowedCount,
    Integer pendingApprovalCount,
    Integer returnRequestedCount,
    Integer dueSoonCount,
    Integer todayCo2Saved
) {}
