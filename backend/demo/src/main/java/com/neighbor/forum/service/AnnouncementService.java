package com.neighbor.forum.service;

import com.neighbor.common.exception.BusinessException;
import com.neighbor.entity.Announcement;
import com.neighbor.enums.ErrorCode;
import com.neighbor.forum.dto.AnnouncementDTO;
import com.neighbor.repository.AnnouncementRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "announcements", key = "#communityId != null ? #communityId : 'all'")
    public List<AnnouncementDTO> getActiveAnnouncements(Long communityId) {
        List<Announcement> announcements;
        if (communityId != null) {
            announcements = announcementRepository.findByCommunityIdAndStatus(communityId, "PUBLISHED", Pageable.unpaged());
        } else {
            announcements = announcementRepository.findByStatus("PUBLISHED", Pageable.unpaged());
        }
        return announcements.stream()
                .limit(5)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "announcementDetail", key = "#id")
    public AnnouncementDTO getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        return toDTO(announcement);
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementDTO> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findByStatusNot("DELETED", pageable)
                .map(this::toDTO);
    }

    @CacheEvict(value = {"announcements", "announcementDetail"}, allEntries = true)
    public AnnouncementDTO createAnnouncement(String title, String content, String type, Long communityId) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(type != null ? type : "normal");
        announcement.setStatus("PUBLISHED");
        
        Announcement saved = announcementRepository.save(announcement);
        return toDTO(saved);
    }

    @CacheEvict(value = {"announcements", "announcementDetail"}, allEntries = true)
    public AnnouncementDTO updateAnnouncement(Long id, String title, String content, String type) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        
        if (title != null) {
            announcement.setTitle(title);
        }
        if (content != null) {
            announcement.setContent(content);
        }
        if (type != null) {
            announcement.setType(type);
        }
        
        Announcement saved = announcementRepository.save(announcement);
        return toDTO(saved);
    }

    @CacheEvict(value = {"announcements", "announcementDetail"}, allEntries = true)
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        announcement.setStatus("DELETED");
        announcementRepository.save(announcement);
    }

    private AnnouncementDTO toDTO(Announcement announcement) {
        String communityName = null;
        Long communityId = null;
        if (announcement.getCommunity() != null) {
            communityName = announcement.getCommunity().getName();
            communityId = announcement.getCommunity().getId();
        }
        
        String formattedDate = announcement.getCreatedAt() != null 
                ? announcement.getCreatedAt().format(DATE_FORMATTER) 
                : null;
        
        return new AnnouncementDTO(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getType(),
                communityId,
                communityName,
                formattedDate
        );
    }
}
