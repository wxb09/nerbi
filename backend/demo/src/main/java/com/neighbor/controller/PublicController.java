package com.neighbor.controller;

import com.neighbor.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/ping")
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.ok(Map.of("status", "ok"));
    }
}
