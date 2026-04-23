-- ============================================
-- 审核系统重构 + 举报功能
-- ============================================

-- 1. 删除帖子表和评论表的审核相关字段（这些字段实际未被有效使用）
ALTER TABLE posts DROP COLUMN audit_status;
ALTER TABLE posts DROP COLUMN audit_reason;
ALTER TABLE comments DROP COLUMN audit_status;
ALTER TABLE comments DROP COLUMN audit_reason;

-- 2. 扩展审核日志表，支持举报功能
ALTER TABLE audit_logs
    ADD COLUMN review_type VARCHAR(20) DEFAULT 'AUTO' COMMENT '审核类型: AUTO-自动审核 REPORT-用户举报',
    ADD COLUMN reporter_id BIGINT NULL COMMENT '举报人ID（review_type=REPORT时有效）',
    ADD COLUMN handler_id BIGINT NULL COMMENT '处理管理员ID',
    ADD COLUMN result TEXT COMMENT '处理结果说明',
    MODIFY COLUMN audit_result VARCHAR(20) NOT NULL COMMENT '结果: PASS/BLOCK/PENDING/RESOLVED/REJECTED';

-- 3. 添加索引优化查询
CREATE INDEX idx_review_type ON audit_logs(review_type);
CREATE INDEX idx_reporter ON audit_logs(reporter_id);
CREATE INDEX idx_handler ON audit_logs(handler_id);
CREATE INDEX idx_audit_result ON audit_logs(audit_result);

-- 4. 更新现有数据：将历史记录标记为自动审核
UPDATE audit_logs SET review_type = 'AUTO' WHERE review_type IS NULL;

-- 5. 帖子表添加UNDER_REVIEW状态支持（用于被举报后待审核）
-- 注意：status字段已支持 PUBLISHED/DELETED，应用层需处理 UNDER_REVIEW
