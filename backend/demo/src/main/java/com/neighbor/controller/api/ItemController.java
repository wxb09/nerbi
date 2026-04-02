package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.dto.ItemDetailDTO;
import com.neighbor.dto.ItemListDTO;
import com.neighbor.entity.Item;
import com.neighbor.enums.ItemStatus;
import com.neighbor.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ApiResponse<Page<ItemListDTO>> getItems(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ItemStatus> statuses = Arrays.asList(ItemStatus.AVAILABLE);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ItemListDTO> items = itemService.getItems(statuses, communityId, categoryId, pageable);
        return ApiResponse.ok(items);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ItemListDTO>> searchItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ItemStatus> statuses = Arrays.asList(ItemStatus.AVAILABLE);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ItemListDTO> items = itemService.searchItems(keyword, statuses, pageable);
        return ApiResponse.ok(items);
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemDetailDTO> getItemById(@PathVariable Long id) {
        ItemDetailDTO item = itemService.getItemById(id);
        return ApiResponse.ok(item);
    }

    @PostMapping
    public ApiResponse<Item> createItem(Authentication authentication, @RequestBody Map<String, Object> itemData) {
        Long userId = getUserIdFromAuth(authentication);
        Item item = itemService.createItem(itemData, userId);
        return ApiResponse.ok(item);
    }

    @PutMapping("/{id}")
    public ApiResponse<Item> updateItem(Authentication authentication, @PathVariable Long id, @RequestBody Map<String, Object> itemData) {
        Long userId = getUserIdFromAuth(authentication);
        Item item = itemService.updateItem(id, itemData, userId);
        return ApiResponse.ok(item);
    }

    @PutMapping("/{id}/withdraw")
    public ApiResponse<Void> withdrawItem(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        itemService.withdrawItem(id, userId);
        return ApiResponse.ok();
    }

    @PostMapping("/draft")
    public ApiResponse<Item> saveDraft(Authentication authentication, @RequestBody Map<String, Object> itemData) {
        Long userId = getUserIdFromAuth(authentication);
        Item item = itemService.saveDraft(itemData, userId);
        return ApiResponse.ok(item);
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<List<ItemListDTO>> getSimilarItems(@PathVariable Long id, @RequestParam(defaultValue = "4") int limit) {
        List<ItemListDTO> similarItems = itemService.getSimilarItems(id, limit);
        return ApiResponse.ok(similarItems);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.valueOf(authUser.userId());
    }
}
