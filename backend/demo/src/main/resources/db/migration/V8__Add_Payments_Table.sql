-- ============================================
-- 支付模块 - 创建支付表
-- 创建时间: 2026-04-15
-- ============================================

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付ID',
    out_trade_no VARCHAR(64) NOT NULL UNIQUE COMMENT '商户订单号',
    trade_no VARCHAR(64) COMMENT '支付宝交易号',
    borrow_id BIGINT NOT NULL COMMENT '借阅ID',
    payer_id BIGINT NOT NULL COMMENT '支付者ID',
    payee_id BIGINT NOT NULL COMMENT '收款者ID',
    payment_type VARCHAR(20) NOT NULL COMMENT '支付类型: RENT/DEPOSIT/RENT_AND_DEPOSIT',
    rent_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '租金金额',
    deposit_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '押金金额',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/REFUNDING/REFUNDED/REFUND_FAILED/CLOSED',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    refund_trade_no VARCHAR(64) COMMENT '退款交易号',
    refund_amount DECIMAL(10,2) COMMENT '退款金额',
    refunded_at TIMESTAMP NULL COMMENT '退款时间',
    item_snapshot VARCHAR(200) COMMENT '物品快照',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_out_trade_no (out_trade_no),
    INDEX idx_trade_no (trade_no),
    INDEX idx_borrow (borrow_id),
    INDEX idx_payer (payer_id),
    INDEX idx_payee (payee_id),
    INDEX idx_status (status),
    FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE CASCADE,
    FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payee_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
