-- ==================== 更新评价表结构（v1.0 简化版）====================

-- 1. 先删表（因为没数据，直接删最干净）
DROP TABLE IF EXISTS reviews;

-- 2. 重新创建表（用你上传的最新结构）
CREATE TABLE reviews (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- 关联
                         borrow_id BIGINT NOT NULL COMMENT '借阅记录ID',
                         from_user_id BIGINT NOT NULL COMMENT '评价人ID',
                         to_user_id BIGINT NOT NULL COMMENT '被评价人ID（物品主人）',
                         item_id BIGINT NOT NULL COMMENT '物品ID',

    -- 类型（预留评人扩展）
                         target_type VARCHAR(20) NOT NULL DEFAULT 'ITEM' COMMENT 'USER/ITEM，前期固定ITEM',

    -- 评分（前期只评物品，用标签）
                         rating_star TINYINT NULL COMMENT '1-5星，评人时用，前期NULL',
                         rating_tag VARCHAR(20) NULL COMMENT '物品标签：AS_DESCRIBED等',

    -- 内容
                         content TEXT NOT NULL,

    -- 软删除（预留管理后台审计）
                         is_deleted TINYINT(1) DEFAULT 0 COMMENT '0=正常 1=已删除',
                         deleted_at TIMESTAMP NULL,
                         deleted_by BIGINT NULL COMMENT '删除操作人ID',

                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 简单CHECK：只约束星级范围，标签放应用层校验
                         CONSTRAINT chk_rating_star CHECK (rating_star IS NULL OR rating_star BETWEEN 1 AND 5),

    -- 索引
                         UNIQUE KEY uk_user_borrow (from_user_id, borrow_id, is_deleted) COMMENT '同次借阅只能评一次',
                         KEY idx_item (item_id, is_deleted, created_at) COMMENT '物品评价列表',
                         KEY idx_user_from (from_user_id, is_deleted, created_at) COMMENT '我发出的评价',
                         KEY idx_user_to (to_user_id, is_deleted, created_at) COMMENT '我收到的评价',
                         KEY idx_borrow (borrow_id)

) ENGINE=InnoDB COMMENT='评价表v1.0';