package com.neighbor.dto;

public record UserStatsDTO(
    Integer lentCount,
    Integer borrowedCount,
    Integer pendingCount,
    Integer dueSoonCount,
    Integer todayCo2Saved
) {}
