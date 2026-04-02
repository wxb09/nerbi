package com.neighbor.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "skill_exchanges")
public class SkillExchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @Column(name = "offer_skill", nullable = false, length = 200)
    private String offerSkill;

    @Column(name = "need_skill", nullable = false, length = 200)
    private String needSkill;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
