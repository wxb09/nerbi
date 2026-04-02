package com.neighbor.service;

import com.neighbor.dto.UserDTO;
import com.neighbor.dto.UserStatsDTO;
import com.neighbor.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDTO getCurrentUser(Long userId);
    UserDTO updateUser(Long userId, Map<String, Object> updateData);
    UserDTO getUserProfile(Long userId);
    List<Map<String, Object>> getMyItems(Long userId);
    List<Map<String, Object>> getMyLent(Long userId);
    List<Map<String, Object>> getMyBorrowed(Long userId);
    List<Map<String, Object>> getMyPending(Long userId);
    List<Map<String, Object>> getMyReviews(Long userId);
    UserStatsDTO getUserStats(Long userId);
}
