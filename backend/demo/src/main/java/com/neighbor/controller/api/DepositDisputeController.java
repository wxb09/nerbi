package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.dto.CreateDepositDisputeRequest;
import com.neighbor.dto.DepositDisputeDTO;
import com.neighbor.service.DepositDisputeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposit-disputes")
public class DepositDisputeController {

    private final DepositDisputeService depositDisputeService;

    public DepositDisputeController(DepositDisputeService depositDisputeService) {
        this.depositDisputeService = depositDisputeService;
    }

    private Long getUserId(Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }

    @PostMapping
    public ApiResponse<DepositDisputeDTO> createDispute(
            Authentication authentication,
            @Valid @RequestBody CreateDepositDisputeRequest request) {
        Long userId = getUserId(authentication);
        DepositDisputeDTO result = depositDisputeService.createDispute(request, userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<DepositDisputeDTO> getDispute(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserId(authentication);
        DepositDisputeDTO result = depositDisputeService.getDispute(id, userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/my")
    public ApiResponse<Page<DepositDisputeDTO>> getMyDisputes(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(authentication);
        Page<DepositDisputeDTO> result = depositDisputeService.getMyDisputes(userId, page, size);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelDispute(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserId(authentication);
        depositDisputeService.cancelDispute(id, userId);
        return ApiResponse.ok();
    }
}
