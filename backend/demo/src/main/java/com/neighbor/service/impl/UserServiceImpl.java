package com.neighbor.service.impl;

import com.neighbor.dto.UserDTO;
import com.neighbor.dto.UserStatsDTO;
import com.neighbor.entity.*;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.ItemStatus;
import com.neighbor.repository.*;
import com.neighbor.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<Borrow> borrows = borrowRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Borrow borrow : borrows) {
            if (borrow.getLender().getId().equals(userId)) {
                Map<String, Object> borrowMap = new HashMap<>();
                borrowMap.put("id", borrow.getId());
                borrowMap.put("itemName", borrow.getItem().getName());
                borrowMap.put("borrowerName", borrow.getBorrower().getNickname());
                borrowMap.put("status", borrow.getStatus());
                borrowMap.put("startTime", borrow.getStartDate());
                borrowMap.put("endTime", borrow.getEndDate());
                result.add(borrowMap);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyBorrowed(Long userId) {
        List<Borrow> borrows = borrowRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Borrow borrow : borrows) {
            if (borrow.getBorrower().getId().equals(userId)) {
                Map<String, Object> borrowMap = new HashMap<>();
                borrowMap.put("id", borrow.getId());
                borrowMap.put("itemName", borrow.getItem().getName());
                borrowMap.put("lenderName", borrow.getLender().getNickname());
                borrowMap.put("status", borrow.getStatus());
                borrowMap.put("startTime", borrow.getStartDate());
                borrowMap.put("endTime", borrow.getEndDate());
                result.add(borrowMap);
            }
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
        List<Review> reviews = reviewRepository.findByToUserId(userId, Pageable.unpaged()).getContent();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review review : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("reviewerName", review.getFromUser().getNickname());
            reviewMap.put("rating", review.getRating());
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
        int pendingCount = 0;
        
        for (Borrow borrow : allBorrows) {
            if (borrow.getLender().getId().equals(userId)) {
                lentCount++;
                if (borrow.getStatus() == BorrowStatus.PENDING) {
                    pendingCount++;
                }
            }
            if (borrow.getBorrower().getId().equals(userId)) {
                borrowedCount++;
            }
        }
        
        int dueSoonCount = 0; // 待实现：计算即将到期的借取
        int todayCo2Saved = 0; // 待实现：计算今日节省的 CO2

        return new UserStatsDTO(lentCount, borrowedCount, pendingCount, dueSoonCount, todayCo2Saved);
    }

    private UserDTO convertToUserDTO(User user) {
        String communityName = user.getCommunity() != null ? user.getCommunity().getName() : "";
        return new UserDTO(
                user.getId(),
                user.getNickname(),
                user.getAvatar(),
                user.getPhone(),
                communityName,
                user.getBuilding(),
                user.getCreditScore(),
                user.getBorrowCount(),
                user.getLendCount(),
                user.getCo2Saved()
        );
    }
}
