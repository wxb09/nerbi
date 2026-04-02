package com.neighbor.repository;

import com.neighbor.entity.Announcement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    List<Announcement> findByCommunityIdAndStatus(Long communityId, String status, Pageable pageable);
    
    List<Announcement> findByStatus(String status, Pageable pageable);
}
