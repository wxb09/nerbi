package com.neighbor.auth;

import com.neighbor.auth.dto.LoginPhoneRequest;
import com.neighbor.auth.dto.LoginResponse;
import com.neighbor.auth.dto.VerifyCodeRequest;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/phone")
    public ApiResponse<LoginResponse> loginByPhone(@Valid @RequestBody LoginPhoneRequest req) {
        return ApiResponse.ok(authService.loginByPhone(req));
    }

    @PostMapping("/verify-code")
    public ApiResponse<Map<String, Object>> sendVerifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        return ApiResponse.ok(authService.sendVerifyCode(req.phone()));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        return ApiResponse.ok(Map.of("success", true));
    }

    @GetMapping("/guest")
    public ApiResponse<Map<String, Object>> guest() {
        return ApiResponse.ok(authService.guestToken());
    }

    @GetMapping("/me")
    public ApiResponse<AuthUser> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(1001, "未登录或Token无效");
        }
        return ApiResponse.ok(authUser);
    }
}
