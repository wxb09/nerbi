ALTER TABLE payments ADD COLUMN deduction_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '扣款金额';
ALTER TABLE payments ADD COLUMN deduction_reason VARCHAR(500) COMMENT '扣款原因';
ALTER TABLE payments ADD COLUMN deduction_type VARCHAR(20) COMMENT '扣款类型: DAMAGE/LOSS/OVERDUE/MISSING_PARTS/OTHER';
ALTER TABLE payments ADD COLUMN deduction_evidence VARCHAR(2000) COMMENT '扣款证据图片JSON数组';
ALTER TABLE payments ADD COLUMN actual_refund_amount DECIMAL(10,2) COMMENT '实际退还金额';

CREATE TABLE IF NOT EXISTS deposit_disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '押金纠纷ID',
    payment_id BIGINT NOT NULL COMMENT '支付记录ID',
    borrow_id BIGINT NOT NULL COMMENT '借阅ID',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    initiator_type VARCHAR(20) NOT NULL COMMENT '发起人类型: LENDER/BORROWER',
    dispute_type VARCHAR(20) NOT NULL COMMENT '纠纷类型: DAMAGE/LOSS/OVERDUE/MISSING_PARTS/OTHER',
    description VARCHAR(1000) NOT NULL COMMENT '纠纷描述',
    evidence_images VARCHAR(2000) COMMENT '证据图片JSON数组',
    claim_amount DECIMAL(10,2) NOT NULL COMMENT '申请扣款金额',
    claim_reason VARCHAR(500) COMMENT '申请扣款理由',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/APPROVED/REJECTED/CANCELLED',
    actual_deduction DECIMAL(10,2) COMMENT '实际扣款金额',
    resolution VARCHAR(500) COMMENT '处理说明',
    handler_id BIGINT COMMENT '处理管理员ID',
    handled_at TIMESTAMP NULL COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment (payment_id),
    INDEX idx_borrow (borrow_id),
    INDEX idx_initiator (initiator_id),
    INDEX idx_status (status),
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (borrow_id) REFERENCES borrows(id),
    FOREIGN KEY (initiator_id) REFERENCES users(id),
    FOREIGN KEY (handler_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='押金纠纷表';
