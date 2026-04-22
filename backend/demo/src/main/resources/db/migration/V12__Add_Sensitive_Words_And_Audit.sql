-- 敏感词表
CREATE TABLE sensitive_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) DEFAULT 'OTHER',
    severity INT DEFAULT 1,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 审核日志表
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    content_snapshot TEXT,
    audit_result VARCHAR(20) NOT NULL,
    sensitive_words TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 帖子表添加审核状态字段
ALTER TABLE posts ADD COLUMN audit_status TINYINT DEFAULT 1 COMMENT '0-待审核 1-已通过 2-已拦截';
ALTER TABLE posts ADD COLUMN audit_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因';

-- 评论表添加审核状态字段
ALTER TABLE comments ADD COLUMN audit_status TINYINT DEFAULT 1 COMMENT '0-待审核 1-已通过 2-已拦截';
ALTER TABLE comments ADD COLUMN audit_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因';

-- 插入默认敏感词
INSERT INTO sensitive_words (word, category, severity) VALUES
('傻逼', 'INSULT', 3),
('操你', 'INSULT', 3),
('妈的', 'INSULT', 2),
('草泥马', 'INSULT', 3),
('法轮功', 'POLITICAL', 3),
('六四', 'POLITICAL', 3),
('台独', 'POLITICAL', 3),
('藏独', 'POLITICAL', 3),
('疆独', 'POLITICAL', 3),
('卖淫', 'PORN', 3),
('嫖娼', 'PORN', 3),
('约炮', 'PORN', 2),
('一夜情', 'PORN', 2),
('代开发票', 'AD', 2),
('办证', 'AD', 2),
('刷单', 'AD', 2),
('兼职赚钱', 'AD', 1);
