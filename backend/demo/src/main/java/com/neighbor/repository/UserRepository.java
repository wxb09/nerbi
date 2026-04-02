package com.neighbor.repository;

import com.neighbor.entity.User;
import com.neighbor.enums.UserStatus;
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
}
