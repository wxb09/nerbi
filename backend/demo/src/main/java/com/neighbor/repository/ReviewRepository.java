package com.neighbor.repository;

import com.neighbor.entity.Review;
import com.neighbor.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByToUserId(Long toUserId, Pageable pageable);
    
    Page<Review> findByFromUserId(Long fromUserId, Pageable pageable);
    
    Long countByToUserId(Long toUserId);

    Optional<Review> findByBorrowIdAndFromUserIdAndDeletedFalse(Long borrowId, Long fromUserId);
    
    Optional<Review> findByBorrowIdAndFromUserIdAndTargetTypeAndDeletedFalse(Long borrowId, Long fromUserId, ReviewType targetType);
    
    Optional<Review> findByIdAndDeletedFalse(Long id);
    
    Page<Review> findByItemIdAndDeletedFalseOrderByCreatedAtDesc(Long itemId, Pageable pageable);
    
    Page<Review> findByItemIdAndTargetTypeAndDeletedFalseOrderByCreatedAtDesc(Long itemId, ReviewType targetType, Pageable pageable);
    
    Page<Review> findByToUserIdAndDeletedFalseOrderByCreatedAtDesc(Long toUserId, Pageable pageable);
    
    Page<Review> findByToUserIdAndTargetTypeAndDeletedFalseOrderByCreatedAtDesc(Long toUserId, ReviewType targetType, Pageable pageable);
    
    Page<Review> findByFromUserIdAndDeletedFalseOrderByCreatedAtDesc(Long fromUserId, Pageable pageable);
    
    Page<Review> findByFromUserIdAndTargetTypeAndDeletedFalseOrderByCreatedAtDesc(Long fromUserId, ReviewType targetType, Pageable pageable);

    Long countByItemIdAndDeletedFalse(Long itemId);

    Long countByToUserIdAndDeletedFalse(Long toUserId);
    
    Long countByToUserIdAndTargetTypeAndDeletedFalse(Long toUserId, ReviewType targetType);
}
