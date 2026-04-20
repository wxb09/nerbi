package com.neighbor.forum.dto;

public record AnnouncementDTO(
        Long id,
        String title,
        String content,
        String type,
        Long communityId,
        String communityName,
        String createdAt
) {}
