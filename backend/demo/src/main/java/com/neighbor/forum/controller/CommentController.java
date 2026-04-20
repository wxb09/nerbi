package com.neighbor.forum.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.forum.dto.CommentDTO;
import com.neighbor.forum.dto.CreateCommentRequest;
import com.neighbor.forum.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}")
    public ApiResponse<Page<CommentDTO>> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long currentUserId = getUserIdSafe(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ApiResponse.ok(commentService.getCommentsByPostId(postId, currentUserId, pageable));
    }

    @GetMapping("/{parentCommentId}/replies")
    public ApiResponse<Page<CommentDTO>> getReplies(
            @PathVariable Long parentCommentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long currentUserId = getUserIdSafe(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ApiResponse.ok(commentService.getReplies(parentCommentId, currentUserId, pageable));
    }

    @PostMapping
    public ApiResponse<CommentDTO> createComment(Authentication authentication,
                                                  @RequestBody CreateCommentRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        Long commentId = commentService.createComment(request, userId);
        return ApiResponse.ok(commentService.getCommentById(commentId, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        commentService.deleteComment(id, userId);
        return ApiResponse.ok();
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
