package com.neighbor.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        String phone,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
        String password,
        @Size(max = 50, message = "昵称最长50个字符")
        String nickname
) {
}
