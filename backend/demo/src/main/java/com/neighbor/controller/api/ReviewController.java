package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.dto.ReviewDTO;
import com.neighbor.dto.ReviewRequest;
import com.neighbor.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createReview(
            Authentication authentication, 
            @Valid @RequestBody ReviewRequest request) {
        Long fromUserId = getUserIdFromAuth(authentication);
        Long reviewId = reviewService.createReview(request, fromUserId);
        return ApiResponse.ok(Map.of("reviewId", reviewId, "status", "CREATED"));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReviewDTO> getReviewById(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/item/{itemId}")
    public ApiResponse<Page<ReviewDTO>> getItemReviews(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(reviewService.getReviewsByItemId(itemId, pageable));
    }

    @GetMapping("/user/{userId}/received")
    public ApiResponse<Page<ReviewDTO>> getUserReceivedReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(reviewService.getReviewsByToUserId(userId, pageable));
    }

    @GetMapping("/user/{userId}/given")
    public ApiResponse<Page<ReviewDTO>> getUserGivenReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(reviewService.getReviewsByFromUserId(userId, pageable));
    }

    @GetMapping("/borrow/{borrowId}/check")
    public ApiResponse<Map<String, Boolean>> checkReviewStatus(
            Authentication authentication,
            @PathVariable Long borrowId) {
        Long userId = getUserIdFromAuth(authentication);
        boolean hasReviewed = reviewService.hasReviewed(borrowId, userId);
        return ApiResponse.ok(Map.of("hasReviewed", hasReviewed));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> deleteReview(
            Authentication authentication, 
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        reviewService.deleteReview(id, userId);
        return ApiResponse.ok(Map.of("status", "DELETED"));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }
}
