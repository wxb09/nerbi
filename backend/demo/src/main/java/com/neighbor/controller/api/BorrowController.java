package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import com.neighbor.dto.ApproveRequest;
import com.neighbor.dto.BorrowDTO;
import com.neighbor.dto.BorrowRequest;
import com.neighbor.service.BorrowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/{id}")
    public ApiResponse<BorrowDTO> getBorrowById(@PathVariable Long id) {
        return ApiResponse.ok(borrowService.getBorrowById(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createBorrow(Authentication authentication, @RequestBody BorrowRequest request) {
        Long borrowerId = getUserIdFromAuth(authentication);
        Long borrowId = borrowService.createBorrow(request, borrowerId);
        return ApiResponse.ok(Map.of("borrowId", borrowId, "status", "PENDING"));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Map<String, String>> approveBorrow(Authentication authentication, @PathVariable Long id, @RequestBody ApproveRequest request) {
        Long lenderId = getUserIdFromAuth(authentication);
        borrowService.approveBorrow(id, lenderId, request);
        return ApiResponse.ok(Map.of("status", request.approved() ? "APPROVED" : "REJECTED"));
    }

    @PostMapping("/{id}/pickup")
    public ApiResponse<Map<String, String>> confirmPickup(Authentication authentication, @PathVariable Long id) {
        Long borrowerId = getUserIdFromAuth(authentication);
        borrowService.confirmPickup(id, borrowerId);
        return ApiResponse.ok(Map.of("status", "ACTIVE"));
    }

    @PostMapping("/{id}/return")
    public ApiResponse<Map<String, String>> confirmReturn(Authentication authentication, @PathVariable Long id) {
        Long lenderId = getUserIdFromAuth(authentication);
        borrowService.confirmReturn(id, lenderId);
        return ApiResponse.ok(Map.of("status", "RETURNED"));
    }

    @PostMapping("/{id}/remind")
    public ApiResponse<Map<String, Boolean>> remindReturn(Authentication authentication, @PathVariable Long id) {
        Long lenderId = getUserIdFromAuth(authentication);
        borrowService.remindReturn(id, lenderId);
        return ApiResponse.ok(Map.of("sent", true));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Map<String, String>> cancelBorrow(Authentication authentication, @PathVariable Long id) {
        Long borrowerId = getUserIdFromAuth(authentication);
        borrowService.cancelBorrow(id, borrowerId);
        return ApiResponse.ok(Map.of("status", "CANCELLED"));
    }

    @PostMapping("/{id}/apply-return")
    public ApiResponse<Map<String, String>> applyReturn(Authentication authentication, @PathVariable Long id) {
        Long borrowerId = getUserIdFromAuth(authentication);
        borrowService.applyReturn(id, borrowerId);
        return ApiResponse.ok(Map.of("status", "RETURN_REQUESTED"));
    }

    @GetMapping("/my/borrowed")
    public ApiResponse<PageResponse<BorrowDTO>> getMyBorrowed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long borrowerId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BorrowDTO> pageResult = borrowService.getMyBorrowed(borrowerId, pageable);
        return ApiResponse.ok(PageResponse.from(pageResult));
    }

    @GetMapping("/my/lent")
    public ApiResponse<PageResponse<BorrowDTO>> getMyLent(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long lenderId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BorrowDTO> pageResult = borrowService.getMyLent(lenderId, pageable);
        return ApiResponse.ok(PageResponse.from(pageResult));
    }

    @GetMapping("/my/pending")
    public ApiResponse<List<BorrowDTO>> getMyPending(Authentication authentication) {
        Long lenderId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(borrowService.getPendingBorrows(lenderId));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }
}
