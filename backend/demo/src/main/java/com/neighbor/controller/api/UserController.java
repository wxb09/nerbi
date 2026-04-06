package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.dto.UserDTO;
import com.neighbor.dto.UserStatsDTO;
import com.neighbor.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserDTO> updateUser(Authentication authentication, @RequestBody Map<String, Object> updateData) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.updateUser(userId, updateData));
    }

    @GetMapping("/{id}/profile")
    public ApiResponse<UserDTO> getUserProfile(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserProfile(id));
    }

    @GetMapping("/me/items")
    public ApiResponse<List<Map<String, Object>>> getMyItems(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getMyItems(userId));
    }

    @GetMapping("/me/lent")
    public ApiResponse<List<Map<String, Object>>> getMyLent(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getMyLent(userId));
    }

    @GetMapping("/me/borrowed")
    public ApiResponse<List<Map<String, Object>>> getMyBorrowed(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getMyBorrowed(userId));
    }

    @GetMapping("/me/pending")
    public ApiResponse<List<Map<String, Object>>> getMyPending(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getMyPending(userId));
    }

    @GetMapping("/me/reviews")
    public ApiResponse<List<Map<String, Object>>> getMyReviews(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getMyReviews(userId));
    }

    @GetMapping("/me/drafts")
    public ApiResponse<List<Map<String, Object>>> getMyDrafts(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getMyDrafts(userId));
    }

    @GetMapping("/me/stats")
    public ApiResponse<UserStatsDTO> getUserStats(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(userService.getUserStats(userId));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        // 从AuthUser中获取userId并转换为Long
        return Long.parseLong(authUser.userId());
    }
}
