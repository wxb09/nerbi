package com.neighbor.service;

import com.neighbor.common.exception.BusinessException;
import com.neighbor.dto.ReviewDTO;
import com.neighbor.dto.ReviewRequest;
import com.neighbor.entity.Borrow;
import com.neighbor.entity.Item;
import com.neighbor.entity.Review;
import com.neighbor.entity.User;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.ErrorCode;
import com.neighbor.enums.RatingTag;
import com.neighbor.enums.ReviewType;
import com.neighbor.repository.BorrowRepository;
import com.neighbor.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    
    private final ReviewRepository reviewRepository;
    private final BorrowRepository borrowRepository;

    public ReviewService(ReviewRepository reviewRepository, BorrowRepository borrowRepository) {
        this.reviewRepository = reviewRepository;
        this.borrowRepository = borrowRepository;
    }

    public Long createReview(ReviewRequest request, Long fromUserId) {
        log.info("[ReviewService] 创建评价: fromUserId={}, borrowId={}, type={}", 
                fromUserId, request.borrowId(), request.targetType());
        
        Borrow borrow = borrowRepository.findById(request.borrowId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (borrow.getStatus() != BorrowStatus.RETURNED) {
            throw new BusinessException(ErrorCode.REVIEW_BORROW_NOT_RETURNED);
        }
        
        User borrower = borrow.getBorrower();
        User lender = borrow.getLender();
        Item item = borrow.getItem();
        
        User fromUser;
        User toUser;
        
        if (request.targetType() == ReviewType.ITEM) {
            if (!borrower.getId().equals(fromUserId)) {
                throw new BusinessException(ErrorCode.REVIEW_NOT_YOUR_BORROW);
            }
            fromUser = borrower;
            toUser = lender;
        } else if (request.targetType() == ReviewType.USER) {
            if (borrower.getId().equals(fromUserId)) {
                fromUser = borrower;
                toUser = lender;
            } else if (lender.getId().equals(fromUserId)) {
                fromUser = lender;
                toUser = borrower;
            } else {
                throw new BusinessException(ErrorCode.REVIEW_NOT_YOUR_BORROW);
            }
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        
        Optional<Review> existingReview = reviewRepository.findByBorrowIdAndFromUserIdAndTargetType(
                request.borrowId(), fromUserId, request.targetType());
        if (existingReview.isPresent()) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        
        Review review = new Review();
        review.setBorrow(borrow);
        review.setFromUser(fromUser);
        review.setToUser(toUser);
        review.setItem(item);
        review.setTargetType(request.targetType());
        review.setContent(request.content());
        
        if (request.targetType() == ReviewType.ITEM) {
            review.setRatingTag(request.ratingTag());
        } else {
            review.setRatingStar(request.ratingStar());
        }
        
        Review saved = reviewRepository.save(review);
        log.info("[ReviewService] 评价已保存: reviewId={}", saved.getId());
        
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return toDTO(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByItemId(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemIdAndTargetTypeOrderByCreatedAtDesc(
                itemId, ReviewType.ITEM, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByToUserId(Long toUserId, Pageable pageable) {
        return reviewRepository.findByToUserIdOrderByCreatedAtDesc(toUserId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByFromUserId(Long fromUserId, Pageable pageable) {
        return reviewRepository.findByFromUserIdOrderByCreatedAtDesc(fromUserId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByToUserIdAndType(Long toUserId, ReviewType type, Pageable pageable) {
        return reviewRepository.findByToUserIdAndTargetTypeOrderByCreatedAtDesc(
                toUserId, type, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByFromUserIdAndType(Long fromUserId, ReviewType type, Pageable pageable) {
        return reviewRepository.findByFromUserIdAndTargetTypeOrderByCreatedAtDesc(
                fromUserId, type, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(Long borrowId, Long fromUserId) {
        List<Review> reviews = reviewRepository.findAllByBorrowIdAndFromUserId(borrowId, fromUserId);
        return !reviews.isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean hasReviewedWithType(Long borrowId, Long fromUserId, ReviewType type) {
        return reviewRepository.findByBorrowIdAndFromUserIdAndTargetType(
                borrowId, fromUserId, type).isPresent();
    }

    public void deleteReview(Long reviewId, Long userId) {
        log.info("[ReviewService] 删除评价: reviewId={}, userId={}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        
        if (!review.getFromUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        reviewRepository.delete(review);
        log.info("[ReviewService] 评价已删除: reviewId={}", reviewId);
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO.UserInfo fromUserInfo = new ReviewDTO.UserInfo(
                review.getFromUser().getId(),
                review.getFromUser().getNickname(),
                review.getFromUser().getAvatar()
        );
        
        ReviewDTO.UserInfo toUserInfo = new ReviewDTO.UserInfo(
                review.getToUser().getId(),
                review.getToUser().getNickname(),
                review.getToUser().getAvatar()
        );
        
        RatingTag ratingTag = review.getRatingTag();
        String ratingTagDesc = ratingTag != null ? ratingTag.getDescription() : null;
        
        return new ReviewDTO(
                review.getId(),
                review.getBorrow().getId(),
                review.getItem().getId(),
                review.getItem().getName(),
                fromUserInfo,
                toUserInfo,
                review.getTargetType(),
                review.getRatingStar(),
                ratingTag,
                ratingTagDesc,
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
