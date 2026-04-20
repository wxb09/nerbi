package com.neighbor.forum.dto;

public record CreateCommentRequest(
        Long postId,
        Long parentId,
        String content
) {}
