package com.neighbor.auth.dto;

public record LoginResponse(
        String token,
        UserInfo user
) {
}
