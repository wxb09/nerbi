-- ============================================
-- 为 items 表添加 version 字段用于乐观锁
-- 创建时间: 2026-04-14
-- ============================================

ALTER TABLE items 
ADD COLUMN version BIGINT DEFAULT 0 COMMENT '版本号(乐观锁)';
