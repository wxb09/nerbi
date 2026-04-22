package com.neighbor.entity;

import com.neighbor.enums.PostType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PostType type = PostType.NORMAL;

    @Column(length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 2000)
    private String images;

    @Column(length = 500)
    private String tags;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(length = 20)
    private String status = "PUBLISHED";

    @Column(name = "audit_status")
    private Integer auditStatus = 1;

    @Column(name = "audit_reason", length = 255)
    private String auditReason;

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
