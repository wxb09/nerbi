package com.neighbor.forum.controller;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.dto.PageResponse;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.forum.dto.AnnouncementDTO;
import com.neighbor.forum.service.AnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementController.class);
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/announcements")
    public ApiResponse<List<AnnouncementDTO>> getAnnouncements(
            @RequestParam(required = false) Long communityId) {
        log.info("getAnnouncements called: communityId={}", communityId);
        List<AnnouncementDTO> announcements = announcementService.getActiveAnnouncements(communityId);
        return ApiResponse.ok(announcements);
    }

    @GetMapping("/announcements/{id}")
    public ApiResponse<AnnouncementDTO> getAnnouncementById(@PathVariable Long id) {
        log.info("getAnnouncementById called: id={}", id);
        return ApiResponse.ok(announcementService.getAnnouncementById(id));
    }

    @GetMapping("/admin/announcements")
    public ApiResponse<Page<AnnouncementDTO>> getAdminAnnouncements(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("getAdminAnnouncements called: page={}, size={}", page, size);
        checkAdmin(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AnnouncementDTO> result = announcementService.getAllAnnouncements(pageable);
        return ApiResponse.ok(result);
    }

    @PostMapping("/admin/announcements")
    public ApiResponse<AnnouncementDTO> createAnnouncement(
            Authentication authentication,
            @RequestBody CreateAnnouncementRequest request) {
        log.info("createAnnouncement called: title={}", request.title());
        checkAdmin(authentication);
        AnnouncementDTO result = announcementService.createAnnouncement(
                request.title(),
                request.content(),
                request.type(),
                request.communityId()
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/admin/announcements/{id}")
    public ApiResponse<AnnouncementDTO> updateAnnouncement(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CreateAnnouncementRequest request) {
        log.info("updateAnnouncement called: id={}", id);
        checkAdmin(authentication);
        AnnouncementDTO result = announcementService.updateAnnouncement(
                id,
                request.title(),
                request.content(),
                request.type()
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/admin/announcements/{id}")
    public ApiResponse<Void> deleteAnnouncement(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("deleteAnnouncement called: id={}", id);
        checkAdmin(authentication);
        announcementService.deleteAnnouncement(id);
        return ApiResponse.ok();
    }

    private void checkAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(1001, "未登录或Token无效");
        }
        if (!"ADMIN".equals(authUser.role())) {
            throw new BusinessException(1002, "无管理员权限");
        }
    }
}

record CreateAnnouncementRequest(
    String title,
    String content,
    String type,
    Long communityId
) {}
