package com.neighbor.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "OK", null);
    }

    public static ApiResponse<Void> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
