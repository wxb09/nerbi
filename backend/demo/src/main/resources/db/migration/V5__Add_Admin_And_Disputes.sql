ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER' NOT NULL COMMENT '角色: USER/ADMIN' AFTER status;
ALTER TABLE users ADD INDEX idx_role (role);

ALTER TABLE items ADD COLUMN audit_remark VARCHAR(500) COMMENT '审核备注' AFTER tags;
ALTER TABLE items ADD COLUMN audited_at TIMESTAMP NULL COMMENT '审核时间' AFTER audit_remark;

CREATE TABLE IF NOT EXISTS disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '纠纷ID',
    borrow_id BIGINT NOT NULL COMMENT '借阅ID',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    reason VARCHAR(500) NOT NULL COMMENT '纠纷原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/INVESTIGATING/RESOLVED/DISMISSED',
    resolution VARCHAR(500) COMMENT '处理结果',
    resolved_by BIGINT COMMENT '处理人ID',
    resolved_at TIMESTAMP NULL COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_borrow (borrow_id),
    INDEX idx_reporter (reporter_id),
    INDEX idx_status (status),
    INDEX idx_resolved_by (resolved_by),
    FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅纠纷表';

INSERT INTO users (phone, nickname, role, status, credit_score, borrow_count, lend_count, co2_saved)
VALUES ('13800000001', '管理员', 'ADMIN', 'ACTIVE', 10.00, 0, 0, 0);
