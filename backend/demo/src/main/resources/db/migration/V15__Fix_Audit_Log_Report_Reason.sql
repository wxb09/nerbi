-- ============================================
-- 修复举报原因和处理结果混用问题
-- ============================================

-- 1. 添加举报原因字段（用户举报时填写的原因）
ALTER TABLE audit_logs
    ADD COLUMN reason TEXT COMMENT '举报原因（用户填写）';

-- 2. 将现有的 REPORT 类型记录的 result 迁移到 reason
-- 因为之前的 result 字段存储的是举报原因
UPDATE audit_logs 
    SET reason = result,
        result = NULL
    WHERE review_type = 'REPORT' 
    AND result IS NOT NULL
    AND handler_id IS NULL;
