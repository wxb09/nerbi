package com.neighbor.auth.dto;

public record UserInfo(
        String id,
        String nickname,
        String avatar,
        String communityId,
        String role
) {}
