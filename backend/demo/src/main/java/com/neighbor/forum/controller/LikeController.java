package com.neighbor.forum.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.forum.dto.LikeRequest;
import com.neighbor.forum.dto.LikeStatusDTO;
import com.neighbor.forum.service.LikeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/toggle")
    public ApiResponse<LikeStatusDTO> toggleLike(Authentication authentication,
                                                  @RequestBody LikeRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(likeService.toggleLike(request, userId));
    }

    @GetMapping("/check")
    public ApiResponse<LikeStatusDTO> checkLikeStatus(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            Authentication authentication) {
        Long userId = getUserIdSafe(authentication);
        return ApiResponse.ok(likeService.getLikeStatus(targetType, targetId, userId));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }

    private Long getUserIdSafe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            return Long.parseLong(authUser.userId());
        } catch (Exception e) {
            return null;
        }
    }
}
