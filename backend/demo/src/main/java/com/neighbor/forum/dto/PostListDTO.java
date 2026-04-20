package com.neighbor.forum.dto;

public record PostListDTO(
        Long id,
        String type,
        String title,
        String content,
        String images,
        String tags,
        Integer likeCount,
        Integer commentCount,
        Integer viewCount,
        String status,
        boolean likedByMe,
        UserInfo author,
        Long communityId,
        String communityName,
        String createdAt
) {
    public record UserInfo(
            Long id,
            String nickname,
            String avatar,
            String building
    ) {}
}
