package com.neighbor.chat.repository;

import com.neighbor.chat.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    
    Optional<ChatConversation> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);
    
    @Query("SELECT c FROM ChatConversation c WHERE c.user1Id = :userId OR c.user2Id = :userId ORDER BY c.lastMessageAt DESC")
    List<ChatConversation> findByUserIdOrderByLastMessageAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT c FROM ChatConversation c WHERE (c.user1Id = :userId1 AND c.user2Id = :userId2) OR (c.user1Id = :userId2 AND c.user2Id = :userId1)")
    Optional<ChatConversation> findByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
