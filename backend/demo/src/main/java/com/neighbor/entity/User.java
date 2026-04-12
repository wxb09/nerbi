package com.neighbor.entity;

import com.neighbor.enums.AddressVerifyStatus;
import com.neighbor.enums.UserRole;
import com.neighbor.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 500)
    private String bio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @Column(length = 50)
    private String building;

    @Column(length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_verify_status", length = 20)
    private AddressVerifyStatus addressVerifyStatus = AddressVerifyStatus.NONE;

    @Column(name = "credit_score", precision = 3, scale = 2)
    private BigDecimal creditScore = BigDecimal.valueOf(10.00);

    @Column(name = "borrow_count")
    private Integer borrowCount = 0;

    @Column(name = "lend_count")
    private Integer lendCount = 0;

    @Column(name = "co2_saved")
    private Integer co2Saved = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role = UserRole.USER;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
