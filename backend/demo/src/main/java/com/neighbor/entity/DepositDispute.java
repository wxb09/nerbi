package com.neighbor.entity;

import com.neighbor.enums.DepositDisputeStatus;
import com.neighbor.enums.DepositDisputeType;
import com.neighbor.enums.InitiatorType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "deposit_disputes")
public class DepositDispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", nullable = false)
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_type", length = 20, nullable = false)
    private InitiatorType initiatorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_type", length = 20, nullable = false)
    private DepositDisputeType disputeType;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(name = "evidence_images", length = 2000)
    private String evidenceImages;

    @Column(name = "claim_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal claimAmount;

    @Column(name = "claim_reason", length = 500)
    private String claimReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DepositDisputeStatus status = DepositDisputeStatus.PENDING;

    @Column(name = "actual_deduction", precision = 10, scale = 2)
    private BigDecimal actualDeduction;

    @Column(length = 500)
    private String resolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

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
