package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.dto.MessageDTO;
import com.neighbor.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<Page<MessageDTO>> getMessages(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(messageService.getMessages(userId, pageable));
    }

    @GetMapping("/unread")
    public ApiResponse<Page<MessageDTO>> getUnreadMessages(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(messageService.getUnreadMessages(userId, pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Long count = messageService.getUnreadCount(userId);
        return ApiResponse.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Map<String, Boolean>> markAsRead(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        messageService.markAsRead(id, userId);
        return ApiResponse.ok(Map.of("success", true));
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        int count = messageService.markAllAsRead(userId);
        return ApiResponse.ok(Map.of("count", count));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }
}
