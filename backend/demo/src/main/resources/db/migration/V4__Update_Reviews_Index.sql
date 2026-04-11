-- ==================== 更新评价表索引（硬删除版本）====================

-- 删除旧的唯一索引
ALTER TABLE reviews DROP INDEX uk_user_borrow;

-- 添加新的唯一索引：同一用户对同一次借阅的每种类型只能评价一次
ALTER TABLE reviews ADD UNIQUE KEY uk_user_borrow_type (from_user_id, borrow_id, target_type) COMMENT '同次借阅同类型只能评一次';

-- 注意：硬删除后不再需要 is_deleted 相关字段和索引
-- 但保留字段以便将来可能的审计需求
