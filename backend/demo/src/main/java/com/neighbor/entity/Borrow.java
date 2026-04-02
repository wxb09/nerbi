package com.neighbor.entity;

import com.neighbor.enums.BorrowStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "borrows")
public class Borrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BorrowStatus status = BorrowStatus.PENDING;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "remind_count")
    private Integer remindCount = 0;

    @Column(name = "last_remind_at")
    private LocalDateTime lastRemindAt;

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
