package com.neighbor.forum.dto;

public record LikeStatusDTO(
        boolean liked,
        Integer likeCount
) {}
