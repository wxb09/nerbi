package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import com.neighbor.dto.*;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.DepositDisputeStatus;
import com.neighbor.enums.DisputeStatus;
import com.neighbor.enums.ItemStatus;
import com.neighbor.enums.UserRole;
import com.neighbor.enums.UserStatus;
import com.neighbor.service.AdminService;
import com.neighbor.service.DepositDisputeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final DepositDisputeService depositDisputeService;

    public AdminController(AdminService adminService, DepositDisputeService depositDisputeService) {
        this.adminService = adminService;
        this.depositDisputeService = depositDisputeService;
    }

    private void checkAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            log.error("checkAdmin failed: authentication is null or not AuthUser");
            throw new com.neighbor.common.exception.BusinessException(1001, "未登录或Token无效");
        }
        log.info("checkAdmin: userId={}, role={}", authUser.userId(), authUser.role());
        if (!"ADMIN".equals(authUser.role())) {
            log.error("checkAdmin failed: role is not ADMIN, actual role={}", authUser.role());
            throw new com.neighbor.common.exception.BusinessException(1002, "无管理员权限");
        }
    }

    private Long getUserId(Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }

    @GetMapping("/stats")
    public ApiResponse<AdminStatsDTO> getStats(Authentication authentication) {
        log.info("getStats called");
        checkAdmin(authentication);
        try {
            AdminStatsDTO stats = adminService.getStats();
            log.info("getStats result: {}", stats);
            return ApiResponse.ok(stats);
        } catch (Exception e) {
            log.error("getStats error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/users")
    public ApiResponse<Page<AdminUserDTO>> getUsers(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("getUsers called: keyword={}, status={}, role={}, page={}, size={}", keyword, status, role, page, size);
        checkAdmin(authentication);
        try {
            Page<AdminUserDTO> result = adminService.getUsers(keyword, status, role, page, size);
            log.info("getUsers result: totalElements={}, totalPages={}", result.getTotalElements(), result.getTotalPages());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("getUsers error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/users/{id}/ban")
    public ApiResponse<Void> banUser(Authentication authentication, @PathVariable Long id) {
        log.info("banUser called: userId={}", id);
        checkAdmin(authentication);
        adminService.banUser(id, getUserId(authentication));
        return ApiResponse.ok();
    }

    @PostMapping("/users/{id}/unban")
    public ApiResponse<Void> unbanUser(Authentication authentication, @PathVariable Long id) {
        log.info("unbanUser called: userId={}", id);
        checkAdmin(authentication);
        adminService.unbanUser(id);
        return ApiResponse.ok();
    }

    @GetMapping("/items/pending-review")
    public ApiResponse<Page<AdminItemDTO>> getPendingReviewItems(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("getPendingReviewItems called: page={}, size={}", page, size);
        checkAdmin(authentication);
        try {
            Page<AdminItemDTO> result = adminService.getPendingReviewItems(page, size);
            log.info("getPendingReviewItems result: totalElements={}", result.getTotalElements());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("getPendingReviewItems error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/items")
    public ApiResponse<Page<AdminItemDTO>> getAllItems(
            Authentication authentication,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("getAllItems called: status={}, page={}, size={}", status, page, size);
        checkAdmin(authentication);
        try {
            Page<AdminItemDTO> result = adminService.getAllItems(status, page, size);
            log.info("getAllItems result: totalElements={}", result.getTotalElements());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("getAllItems error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/items/{id}/audit")
    public ApiResponse<Void> auditItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AuditItemRequest request) {
        log.info("auditItem called: itemId={}, action={}", id, request.action());
        checkAdmin(authentication);
        adminService.auditItem(id, request, getUserId(authentication));
        return ApiResponse.ok();
    }

    @GetMapping("/borrows")
    public ApiResponse<Page<AdminBorrowDTO>> getBorrows(
            Authentication authentication,
            @RequestParam(required = false) BorrowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("getBorrows called: status={}, page={}, size={}", status, page, size);
        checkAdmin(authentication);
        try {
            Page<AdminBorrowDTO> result = adminService.getBorrows(status, page, size);
            log.info("getBorrows result: totalElements={}", result.getTotalElements());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("getBorrows error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/disputes")
    public ApiResponse<Void> createDispute(
            Authentication authentication,
            @Valid @RequestBody CreateDisputeRequest request) {
        log.info("createDispute called: borrowId={}", request.borrowId());
        checkAdmin(authentication);
        Long userId = getUserId(authentication);
        adminService.createDispute(request.borrowId(), userId, request.reason());
        return ApiResponse.ok();
    }

    @GetMapping("/disputes")
    public ApiResponse<Page<DisputeDTO>> getDisputes(
            Authentication authentication,
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("getDisputes called: status={}, page={}, size={}", status, page, size);
        checkAdmin(authentication);
        try {
            Page<DisputeDTO> result = adminService.getDisputes(status, page, size);
            log.info("getDisputes result: totalElements={}", result.getTotalElements());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("getDisputes error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/disputes/{id}/resolve")
    public ApiResponse<Void> resolveDispute(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ResolveDisputeRequest request) {
        log.info("resolveDispute called: disputeId={}, action={}", id, request.action());
        checkAdmin(authentication);
        adminService.resolveDispute(id, request, getUserId(authentication));
        return ApiResponse.ok();
    }

    @GetMapping("/address-verifies")
    public ApiResponse<List<Map<String, Object>>> getPendingAddressVerifies(Authentication authentication) {
        log.info("getPendingAddressVerifies called");
        checkAdmin(authentication);
        return ApiResponse.ok(adminService.getPendingAddressVerifies());
    }

    @PostMapping("/address-verifies/{userId}")
    public ApiResponse<Void> approveAddressVerify(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestParam boolean approved) {
        log.info("approveAddressVerify called: userId={}, approved={}", userId, approved);
        checkAdmin(authentication);
        adminService.approveAddressVerify(userId, approved);
        return ApiResponse.ok();
    }

    @GetMapping("/deposit-disputes")
    public ApiResponse<Page<DepositDisputeDTO>> getDepositDisputes(
            Authentication authentication,
            @RequestParam(required = false) DepositDisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("getDepositDisputes called: status={}, page={}, size={}", status, page, size);
        checkAdmin(authentication);
        Page<DepositDisputeDTO> result = depositDisputeService.getAdminDisputes(status, page, size);
        return ApiResponse.ok(result);
    }

    @GetMapping("/deposit-disputes/{id}")
    public ApiResponse<DepositDisputeDTO> getDepositDisputeDetail(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("getDepositDisputeDetail called: id={}", id);
        checkAdmin(authentication);
        Long userId = getUserId(authentication);
        DepositDisputeDTO result = depositDisputeService.getDispute(id, userId);
        return ApiResponse.ok(result);
    }

    @PostMapping("/deposit-disputes/{id}/resolve")
    public ApiResponse<DepositDisputeDTO> resolveDepositDispute(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ResolveDepositDisputeRequest request) {
        log.info("resolveDepositDispute called: id={}, action={}", id, request.action());
        checkAdmin(authentication);
        Long adminId = getUserId(authentication);
        DepositDisputeDTO result = depositDisputeService.resolveDispute(id, request, adminId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/deposit-disputes/stats")
    public ApiResponse<Map<String, Object>> getDepositDisputeStats(Authentication authentication) {
        log.info("getDepositDisputeStats called");
        checkAdmin(authentication);
        return ApiResponse.ok(depositDisputeService.getDisputeStats());
    }
}
