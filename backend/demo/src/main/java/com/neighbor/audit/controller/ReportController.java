package com.neighbor.audit.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.audit.entity.AuditLog;
import com.neighbor.audit.service.ContentReviewService;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import com.neighbor.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private final ContentReviewService contentReviewService;

    public ReportController(ContentReviewService contentReviewService) {
        this.contentReviewService = contentReviewService;
    }

    @PostMapping("/reports")
    public ApiResponse<Void> reportContent(
            Authentication authentication,
            @RequestBody ReportRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("reportContent: userId={}, target={}#{}", userId, request.targetType(), request.targetId());
        contentReviewService.reportContent(
                request.targetType(),
                request.targetId(),
                userId,
                request.reason()
        );
        return ApiResponse.ok();
    }

    @GetMapping("/admin/reports")
    public ApiResponse<Page<AuditLog>> getReports(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        checkAdmin(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result = contentReviewService.getAllReports(pageable);
        return ApiResponse.ok(result);
    }

    @GetMapping("/admin/reports/pending")
    public ApiResponse<Page<AuditLog>> getPendingReports(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        checkAdmin(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result = contentReviewService.getPendingReports(pageable);
        return ApiResponse.ok(result);
    }

    @PostMapping("/admin/reports/{id}/resolve")
    public ApiResponse<Void> resolveReport(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ResolveReportRequest request) {
        checkAdmin(authentication);
        Long adminId = getUserIdFromAuth(authentication);
        contentReviewService.resolveReport(id, adminId, request.action(), request.result());
        return ApiResponse.ok();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(1001, "未登录");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }

    private void checkAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(1001, "未登录或Token无效");
        }
        if (!"ADMIN".equals(authUser.role())) {
            throw new BusinessException(1002, "无管理员权限");
        }
    }
}

record ReportRequest(
    String targetType,
    Long targetId,
    String reason
) {}

record ResolveReportRequest(
    String action,
    String result
) {}
