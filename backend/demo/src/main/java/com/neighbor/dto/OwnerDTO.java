package com.neighbor.dto;

public record OwnerDTO(
    Long id,
    String nickname,
    String avatar,
    Integer borrowCount,
    Integer lendCount,
    String communityName,
    String building
) {}
