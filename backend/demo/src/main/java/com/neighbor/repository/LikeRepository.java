package com.neighbor.repository;

import com.neighbor.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.targetType = :type AND l.targetId = :id")
    Long countByTargetTypeAndTargetId(@Param("type") String type, @Param("id") Long id);
    
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
