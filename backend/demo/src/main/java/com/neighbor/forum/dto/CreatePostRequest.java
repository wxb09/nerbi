package com.neighbor.forum.dto;

import java.util.List;

public record CreatePostRequest(
        String type,
        String title,
        String content,
        List<String> images,
        List<String> tags,
        Long communityId
) {}
