package com.neighbor.chat.repository;

import com.neighbor.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversationId = :conversationId AND m.receiverId = :receiverId AND m.isRead = false")
    Long countUnreadByConversationAndReceiver(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);
    
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.receiverId = :receiverId AND m.isRead = false")
    int markAsReadByConversationAndReceiver(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :receiverId AND m.isRead = false")
    Long countUnreadByReceiver(@Param("receiverId") Long receiverId);
}
