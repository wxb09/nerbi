package com.neighbor.repository;

import com.neighbor.entity.Review;
import com.neighbor.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findAllByBorrowIdAndFromUserId(Long borrowId, Long fromUserId);
    
    Optional<Review> findByBorrowIdAndFromUserIdAndTargetType(Long borrowId, Long fromUserId, ReviewType targetType);
    
    Page<Review> findByItemIdOrderByCreatedAtDesc(Long itemId, Pageable pageable);
    
    Page<Review> findByItemIdAndTargetTypeOrderByCreatedAtDesc(Long itemId, ReviewType targetType, Pageable pageable);
    
    Page<Review> findByToUserIdOrderByCreatedAtDesc(Long toUserId, Pageable pageable);
    
    Page<Review> findByToUserIdAndTargetTypeOrderByCreatedAtDesc(Long toUserId, ReviewType targetType, Pageable pageable);
    
    Page<Review> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId, Pageable pageable);
    
    Page<Review> findByFromUserIdAndTargetTypeOrderByCreatedAtDesc(Long fromUserId, ReviewType targetType, Pageable pageable);

    Long countByItemId(Long itemId);

    Long countByToUserId(Long toUserId);
    
    Long countByToUserIdAndTargetType(Long toUserId, ReviewType targetType);
    
    List<Review> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    
    List<Review> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId);
}
