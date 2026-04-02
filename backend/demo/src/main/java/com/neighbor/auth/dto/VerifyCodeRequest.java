package com.neighbor.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        String phone
) {
}
