package com.neighbor.forum.dto;

public record LikeRequest(
        String targetType,
        Long targetId
) {}
