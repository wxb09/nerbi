package com.neighbor.repository;

import com.neighbor.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByToUserIdOrderByCreatedAtDesc(Long toUserId, Pageable pageable);
    
    Page<Message> findByToUserIdAndIsReadOrderByCreatedAtDesc(Long toUserId, Boolean isRead, Pageable pageable);
    
    Long countByToUserIdAndIsRead(Long toUserId, Boolean isRead);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.toUser.id = :userId AND m.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
}
