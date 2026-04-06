package com.neighbor.repository;

import com.neighbor.entity.Message;
import com.neighbor.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByToUserIdOrderByCreatedAtDesc(Long toUserId, Pageable pageable);
    
    Page<Message> findByToUserIdAndIsReadOrderByCreatedAtDesc(Long toUserId, Boolean isRead, Pageable pageable);
    
    Long countByToUserIdAndIsRead(Long toUserId, Boolean isRead);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.toUser.id = :userId AND m.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id = :messageId AND m.toUser.id = :userId")
    int markAsRead(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    boolean existsByToUserIdAndTypeAndRelatedIdAndCreatedAtAfter(
        Long toUserId, MessageType type, Long relatedId, LocalDateTime createdAt);
}
