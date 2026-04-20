package com.neighbor.entity;

import com.neighbor.enums.TransferStatus;
import com.neighbor.enums.TransferType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transfer_records")
public class TransferRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", nullable = false)
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", length = 20, nullable = false)
    private TransferType transferType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "out_biz_no", nullable = false, unique = true, length = 64)
    private String outBizNo;

    @Column(name = "alipay_order_id", length = 64)
    private String alipayOrderId;

    @Column(name = "payee_account", nullable = false, length = 64)
    private String payeeAccount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransferStatus status = TransferStatus.PENDING;

    @Column(name = "fail_reason", length = 500)
    private String failReason;

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
