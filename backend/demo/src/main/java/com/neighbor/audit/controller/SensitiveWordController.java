package com.neighbor.audit.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.audit.dto.CreateSensitiveWordRequest;
import com.neighbor.audit.dto.SensitiveWordDTO;
import com.neighbor.audit.service.SensitiveWordService;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.enums.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    private void checkAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        if (!"ADMIN".equals(authUser.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    @GetMapping("/status")
    public ApiResponse<Boolean> getAuditStatus(Authentication authentication) {
        checkAdmin(authentication);
        return ApiResponse.ok(sensitiveWordService.isAuditEnabled());
    }

    @PostMapping("/status")
    public ApiResponse<Void> setAuditStatus(@RequestParam boolean enabled, Authentication authentication) {
        checkAdmin(authentication);
        sensitiveWordService.setAuditEnabled(enabled);
        return ApiResponse.ok();
    }

    @GetMapping("/words")
    public ApiResponse<PageResponse<SensitiveWordDTO>> getWords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        checkAdmin(authentication);
        Page<SensitiveWordDTO> pageResult = sensitiveWordService.getWords(keyword, category, page, size);
        return ApiResponse.ok(PageResponse.from(pageResult));
    }

    @PostMapping("/words")
    public ApiResponse<SensitiveWordDTO> addWord(@Valid @RequestBody CreateSensitiveWordRequest request,
                                                   Authentication authentication) {
        checkAdmin(authentication);
        return ApiResponse.ok(sensitiveWordService.addWord(request));
    }

    @DeleteMapping("/words/{id}")
    public ApiResponse<Void> deleteWord(@PathVariable Long id, Authentication authentication) {
        checkAdmin(authentication);
        sensitiveWordService.deleteWord(id);
        return ApiResponse.ok();
    }

    @PutMapping("/words/{id}/toggle")
    public ApiResponse<SensitiveWordDTO> toggleWordStatus(@PathVariable Long id, Authentication authentication) {
        checkAdmin(authentication);
        return ApiResponse.ok(sensitiveWordService.toggleWordStatus(id));
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> getCategories(Authentication authentication) {
        checkAdmin(authentication);
        return ApiResponse.ok(sensitiveWordService.getCategories());
    }

    @PostMapping("/refresh")
    public ApiResponse<Void> refreshWordBuffer(Authentication authentication) {
        checkAdmin(authentication);
        sensitiveWordService.refreshWordBuffer();
        return ApiResponse.ok();
    }
}
