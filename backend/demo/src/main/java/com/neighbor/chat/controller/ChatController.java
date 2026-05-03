package com.neighbor.chat.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.chat.dto.ChatMessageDTO;
import com.neighbor.chat.dto.ConversationDTO;
import com.neighbor.chat.dto.SendMessageRequest;
import com.neighbor.chat.dto.UserStatusDTO;
import com.neighbor.chat.service.ChatService;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ApiResponse<List<ConversationDTO>> getConversations(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<ConversationDTO> conversations = chatService.getConversations(userId);
        return ApiResponse.ok(conversations);
    }

    @PostMapping("/conversations")
    public ApiResponse<ConversationDTO> getOrCreateConversation(
            Authentication authentication,
            @RequestBody Map<String, Long> request) {
        Long currentUserId = getUserIdFromAuth(authentication);
        Long otherUserId = request.get("userId");
        ConversationDTO conversation = chatService.getOrCreateConversation(currentUserId, otherUserId);
        return ApiResponse.ok(conversation);
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<PageResponse<ChatMessageDTO>> getMessages(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserIdFromAuth(authentication);
        Page<ChatMessageDTO> messages = chatService.getMessages(id, userId, page, size);
        return ApiResponse.ok(PageResponse.from(messages));
    }

    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<ChatMessageDTO> sendMessage(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        ChatMessageDTO message = chatService.sendMessage(userId, request);
        return ApiResponse.ok(message);
    }

    @PutMapping("/conversations/{id}/read")
    public ApiResponse<Map<String, Boolean>> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        chatService.markAsRead(id, userId);
        return ApiResponse.ok(Map.of("success", true));
    }

    @GetMapping("/users/{userId}/status")
    public ApiResponse<UserStatusDTO> getUserStatus(@PathVariable Long userId) {
        UserStatusDTO status = chatService.getUserStatus(userId);
        return ApiResponse.ok(status);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Long count = chatService.getUnreadCount(userId);
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
