package com.neighbor.repository;

import com.neighbor.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByToUserId(Long toUserId, Pageable pageable);
    
    Page<Review> findByFromUserId(Long fromUserId, Pageable pageable);
    
    Long countByToUserId(Long toUserId);
}
