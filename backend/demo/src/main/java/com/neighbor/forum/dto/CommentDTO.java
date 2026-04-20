package com.neighbor.forum.dto;

public record CommentDTO(
        Long id,
        String content,
        Integer likeCount,
        boolean likedByMe,
        UserInfo author,
        Long parentId,
        String createdAt
) {
    public record UserInfo(
            Long id,
            String nickname,
            String avatar,
            String building
    ) {}
}
