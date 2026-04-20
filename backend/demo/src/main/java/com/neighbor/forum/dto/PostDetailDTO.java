package com.neighbor.forum.dto;

import java.util.List;

public record PostDetailDTO(
        Long id,
        String type,
        String title,
        String content,
        List<String> images,
        List<String> tags,
        Integer likeCount,
        Integer commentCount,
        Integer viewCount,
        String status,
        boolean likedByMe,
        UserInfo author,
        Long communityId,
        String communityName,
        String createdAt,
        String updatedAt
) {
    public record UserInfo(
            Long id,
            String nickname,
            String avatar,
            String building,
            String bio
    ) {}
}
