ALTER TABLE users ADD COLUMN alipay_account VARCHAR(64) COMMENT '支付宝账号';

CREATE TABLE IF NOT EXISTS transfer_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '转账记录ID',
    payment_id BIGINT COMMENT '关联支付记录ID',
    borrow_id BIGINT NOT NULL COMMENT '借阅ID',
    from_user_id BIGINT COMMENT '转出用户ID(系统为NULL)',
    to_user_id BIGINT NOT NULL COMMENT '转入用户ID',
    transfer_type VARCHAR(20) NOT NULL COMMENT '转账类型: RENT/DEDUCTION',
    amount DECIMAL(10,2) NOT NULL COMMENT '转账金额',
    out_biz_no VARCHAR(64) NOT NULL UNIQUE COMMENT '商户转账唯一订单号',
    alipay_order_id VARCHAR(64) COMMENT '支付宝转账订单号',
    payee_account VARCHAR(64) NOT NULL COMMENT '收款方支付宝账号',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/SUCCESS/FAILED',
    fail_reason VARCHAR(500) COMMENT '失败原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment (payment_id),
    INDEX idx_borrow (borrow_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_status (status),
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (borrow_id) REFERENCES borrows(id),
    FOREIGN KEY (to_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转账记录表';
