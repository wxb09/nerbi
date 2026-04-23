package com.neighbor.audit.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "review_type", length = 20)
    private String reviewType = "AUTO";

    @Column(name = "content_snapshot", columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "audit_result", nullable = false, length = 20)
    private String auditResult;

    @Column(name = "sensitive_words", columnDefinition = "TEXT")
    private String sensitiveWords;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
