package com.neighbor.entity;

import com.neighbor.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @Column(length = 50)
    private String building;

    @Column(name = "price_per_day", precision = 10, scale = 2)
    private BigDecimal pricePerDay = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal deposit = BigDecimal.ZERO;

    @Column(name = "credit_required", precision = 3, scale = 2)
    private BigDecimal creditRequired = BigDecimal.ZERO;

    @Column(name = "return_requirements", columnDefinition = "TEXT")
    private String returnRequirements;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ItemStatus status = ItemStatus.DRAFT;

    @Column(name = "borrow_count")
    private Integer borrowCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(length = 500)
    private String tags;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ItemImage> images = new ArrayList<>();

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
