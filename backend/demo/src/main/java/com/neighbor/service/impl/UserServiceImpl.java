package com.neighbor.service.impl;

import com.neighbor.dto.AddressVerifyRequest;
import com.neighbor.dto.UserDTO;
import com.neighbor.dto.UserStatsDTO;
import com.neighbor.entity.*;
import com.neighbor.enums.AddressVerifyStatus;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.ItemStatus;
import com.neighbor.repository.*;
import com.neighbor.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BorrowRepository borrowRepository;
    private final ReviewRepository reviewRepository;
    private final CommunityRepository communityRepository;
    private final ItemImageRepository itemImageRepository;

    public UserServiceImpl(UserRepository userRepository, ItemRepository itemRepository, 
                         BorrowRepository borrowRepository, ReviewRepository reviewRepository, 
                         CommunityRepository communityRepository, ItemImageRepository itemImageRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.borrowRepository = borrowRepository;
        this.reviewRepository = reviewRepository;
        this.communityRepository = communityRepository;
        this.itemImageRepository = itemImageRepository;
    }

    @Override
    public UserDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToUserDTO(user);
    }

    @Override
    public UserDTO updateUser(Long userId, Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (updateData.containsKey("nickname")) {
            user.setNickname((String) updateData.get("nickname"));
        }
        if (updateData.containsKey("avatar")) {
            user.setAvatar((String) updateData.get("avatar"));
        }
        if (updateData.containsKey("bio")) {
            user.setBio((String) updateData.get("bio"));
        }
        if (updateData.containsKey("building")) {
            user.setBuilding((String) updateData.get("building"));
        }
        if (updateData.containsKey("unit")) {
            user.setUnit((String) updateData.get("unit"));
        }

        user = userRepository.save(user);
        return convertToUserDTO(user);
    }

    @Override
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToUserDTO(user);
    }

    @Override
    public List<Map<String, Object>> getMyItems(Long userId) {
        List<Item> items = itemRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getOwner().getId().equals(userId)) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("name", item.getName());
                itemMap.put("status", item.getStatus());
                itemMap.put("borrowCount", item.getBorrowCount());
                itemMap.put("viewCount", item.getViewCount());
                itemMap.put("createdAt", item.getCreatedAt());
                
                // 获取主图
                List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(item.getId());
                if (!images.isEmpty()) {
                    itemMap.put("image", images.get(0).getUrl());
                }
                
                result.add(itemMap);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyLent(Long userId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Borrow> borrowPage = borrowRepository.findByLenderId(userId, pageable);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Borrow borrow : borrowPage.getContent()) {
            Map<String, Object> borrowMap = new HashMap<>();
            borrowMap.put("id", borrow.getId());
            borrowMap.put("itemName", borrow.getItem().getName());
            borrowMap.put("borrowerName", borrow.getBorrower().getNickname());
            borrowMap.put("borrowerId", borrow.getBorrower().getId());
            borrowMap.put("borrowerAvatar", borrow.getBorrower().getAvatar());
            borrowMap.put("status", borrow.getStatus());
            borrowMap.put("startTime", borrow.getStartDate());
            borrowMap.put("endTime", borrow.getEndDate());
            
            List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(borrow.getItem().getId());
            if (!images.isEmpty()) {
                borrowMap.put("itemImage", images.get(0).getUrl());
            }
            
            result.add(borrowMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyBorrowed(Long userId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Borrow> borrowPage = borrowRepository.findByBorrowerId(userId, pageable);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Borrow borrow : borrowPage.getContent()) {
            Map<String, Object> borrowMap = new HashMap<>();
            borrowMap.put("id", borrow.getId());
            borrowMap.put("itemName", borrow.getItem().getName());
            borrowMap.put("lenderName", borrow.getLender().getNickname());
            borrowMap.put("lenderId", borrow.getLender().getId());
            borrowMap.put("lenderAvatar", borrow.getLender().getAvatar());
            borrowMap.put("status", borrow.getStatus());
            borrowMap.put("startTime", borrow.getStartDate());
            borrowMap.put("endTime", borrow.getEndDate());
            
            List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(borrow.getItem().getId());
            if (!images.isEmpty()) {
                borrowMap.put("itemImage", images.get(0).getUrl());
            }
            
            result.add(borrowMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyPending(Long userId) {
        List<Borrow> borrows = borrowRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Borrow borrow : borrows) {
            if (borrow.getLender().getId().equals(userId) && borrow.getStatus() == BorrowStatus.PENDING) {
                Map<String, Object> borrowMap = new HashMap<>();
                borrowMap.put("id", borrow.getId());
                borrowMap.put("itemName", borrow.getItem().getName());
                borrowMap.put("borrowerName", borrow.getBorrower().getNickname());
                borrowMap.put("requestTime", borrow.getCreatedAt());
                result.add(borrowMap);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByToUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review review : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("borrowId", review.getBorrow().getId());
            reviewMap.put("itemId", review.getItem().getId());
            reviewMap.put("itemName", review.getItem().getName());
            
            List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(review.getItem().getId());
            if (!images.isEmpty()) {
                reviewMap.put("itemImage", images.get(0).getUrl());
            }
            
            reviewMap.put("targetType", review.getTargetType().name());
            reviewMap.put("reviewerName", review.getFromUser().getNickname());
            reviewMap.put("reviewerAvatar", review.getFromUser().getAvatar());
            reviewMap.put("ratingStar", review.getRatingStar());
            reviewMap.put("ratingTag", review.getRatingTag() != null ? review.getRatingTag().name() : null);
            reviewMap.put("ratingTagDesc", review.getRatingTag() != null ? review.getRatingTag().getDescription() : null);
            reviewMap.put("content", review.getContent());
            reviewMap.put("createdAt", review.getCreatedAt());
            result.add(reviewMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyGivenReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByFromUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review review : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("borrowId", review.getBorrow().getId());
            reviewMap.put("itemId", review.getItem().getId());
            reviewMap.put("itemName", review.getItem().getName());
            
            List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(review.getItem().getId());
            if (!images.isEmpty()) {
                reviewMap.put("itemImage", images.get(0).getUrl());
            }
            
            reviewMap.put("targetType", review.getTargetType().name());
            reviewMap.put("counterpartyName", review.getToUser().getNickname());
            reviewMap.put("counterpartyAvatar", review.getToUser().getAvatar());
            reviewMap.put("ratingStar", review.getRatingStar());
            reviewMap.put("ratingTag", review.getRatingTag() != null ? review.getRatingTag().name() : null);
            reviewMap.put("ratingTagDesc", review.getRatingTag() != null ? review.getRatingTag().getDescription() : null);
            reviewMap.put("content", review.getContent());
            reviewMap.put("createdAt", review.getCreatedAt());
            result.add(reviewMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyDrafts(Long userId) {
        List<Item> items = itemRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getOwner().getId().equals(userId) && item.getStatus() == ItemStatus.DRAFT) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("name", item.getName());
                itemMap.put("status", item.getStatus());
                itemMap.put("borrowCount", item.getBorrowCount());
                itemMap.put("viewCount", item.getViewCount());
                itemMap.put("createdAt", item.getCreatedAt());
                itemMap.put("updatedAt", item.getUpdatedAt());
                
                // 获取主图
                List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(item.getId());
                if (!images.isEmpty()) {
                    itemMap.put("image", images.get(0).getUrl());
                }
                
                result.add(itemMap);
            }
        }
        return result;
    }

    @Override
    public UserStatsDTO getUserStats(Long userId) {
        List<Borrow> allBorrows = borrowRepository.findAll();
        int lentCount = 0;
        int borrowedCount = 0;
        int pendingApprovalCount = 0;
        int returnRequestedCount = 0;
        int dueSoonCount = 0;
        
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        
        for (Borrow borrow : allBorrows) {
            if (borrow.getLender().getId().equals(userId)) {
                lentCount++;
                if (borrow.getStatus() == BorrowStatus.PENDING) {
                    pendingApprovalCount++;
                }
                if (borrow.getStatus() == BorrowStatus.RETURN_REQUESTED) {
                    returnRequestedCount++;
                }
            }
            if (borrow.getBorrower().getId().equals(userId)) {
                borrowedCount++;
            }
            if (borrow.getStatus() == BorrowStatus.ACTIVE) {
                if ((borrow.getLender().getId().equals(userId) || borrow.getBorrower().getId().equals(userId))
                    && borrow.getEndDate() != null
                    && !borrow.getEndDate().isBefore(today)
                    && borrow.getEndDate().isBefore(weekLater)) {
                    dueSoonCount++;
                }
            }
        }
        
        int todayCo2Saved = 0;

        return new UserStatsDTO(lentCount, borrowedCount, pendingApprovalCount, returnRequestedCount, dueSoonCount, todayCo2Saved);
    }

    private UserDTO convertToUserDTO(User user) {
        String communityName = user.getCommunity() != null ? user.getCommunity().getName() : "";
        Long communityId = user.getCommunity() != null ? user.getCommunity().getId() : null;
        String addressVerifyStatus = user.getAddressVerifyStatus() != null 
                ? user.getAddressVerifyStatus().name() : "NONE";
        return new UserDTO(
                user.getId(),
                user.getNickname(),
                user.getAvatar(),
                user.getPhone(),
                user.getBio(),
                communityId,
                communityName,
                user.getBuilding(),
                user.getUnit(),
                addressVerifyStatus,
                user.getCreditScore(),
                user.getBorrowCount(),
                user.getLendCount(),
                user.getCo2Saved()
        );
    }

    @Override
    public void submitAddressVerify(Long userId, AddressVerifyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Community community = communityRepository.findById(request.communityId())
                .orElseThrow(() -> new RuntimeException("小区不存在"));
        
        user.setCommunity(community);
        user.setBuilding(request.building());
        user.setUnit(request.unit());
        user.setAddressVerifyStatus(AddressVerifyStatus.PENDING);
        
        userRepository.save(user);
    }

    @Override
    public List<Map<String, Object>> getPendingAddressVerifies() {
        List<User> pendingUsers = userRepository.findByAddressVerifyStatus(AddressVerifyStatus.PENDING);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : pendingUsers) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("nickname", user.getNickname());
            item.put("phone", user.getPhone());
            item.put("communityId", user.getCommunity() != null ? user.getCommunity().getId() : null);
            item.put("communityName", user.getCommunity() != null ? user.getCommunity().getName() : "");
            item.put("building", user.getBuilding());
            item.put("unit", user.getUnit());
            item.put("avatar", user.getAvatar());
            result.add(item);
        }
        return result;
    }

    @Override
    public void approveAddressVerify(Long userId, boolean approved) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getAddressVerifyStatus() != AddressVerifyStatus.PENDING) {
            throw new RuntimeException("该用户不在待审核状态");
        }
        
        user.setAddressVerifyStatus(approved ? AddressVerifyStatus.APPROVED : AddressVerifyStatus.REJECTED);
        userRepository.save(user);
    }
}
