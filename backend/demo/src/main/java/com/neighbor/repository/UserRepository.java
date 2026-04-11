package com.neighbor.repository;

import com.neighbor.entity.User;
import com.neighbor.enums.UserRole;
import com.neighbor.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.status = :status")
    Optional<User> findByIdAndStatus(@Param("id") Long id, @Param("status") UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR u.nickname LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("status") UserStatus status,
                           @Param("role") UserRole role,
                           Pageable pageable);

    Long countByStatus(UserStatus status);

    Long countByRole(UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    Long countByCreatedAtAfter(@Param("since") java.time.LocalDateTime since);
}
