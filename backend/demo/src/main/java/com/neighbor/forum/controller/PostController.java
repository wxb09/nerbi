package com.neighbor.forum.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import com.neighbor.forum.dto.CreatePostRequest;
import com.neighbor.forum.dto.PostDetailDTO;
import com.neighbor.forum.dto.PostListDTO;
import com.neighbor.forum.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PostListDTO>> getPosts(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long currentUserId = getUserIdSafe(authentication);
        Pageable pageable;
        if ("hot".equals(sort)) {
            pageable = PageRequest.of(page, size);
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        Page<PostListDTO> pageResult = postService.getPosts(communityId, type, sort, currentUserId, pageable);
        return ApiResponse.ok(PageResponse.from(pageResult));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailDTO> getPostById(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = getUserIdSafe(authentication);
        return ApiResponse.ok(postService.getPostById(id, currentUserId));
    }

    @PostMapping
    public ApiResponse<PostDetailDTO> createPost(Authentication authentication,
                                                  @RequestBody CreatePostRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        Long postId = postService.createPost(request, userId);
        return ApiResponse.ok(postService.getPostById(postId, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostDetailDTO> updatePost(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @RequestBody CreatePostRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        postService.updatePost(id, request, userId);
        return ApiResponse.ok(postService.getPostById(id, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        postService.deletePost(id, userId);
        return ApiResponse.ok();
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<PostListDTO>> getMyPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostListDTO> pageResult = postService.getMyPosts(userId, pageable);
        return ApiResponse.ok(PageResponse.from(pageResult));
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
