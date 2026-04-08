-- ==================== 评价表（v1.0 简化版）====================
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

-- ==================== 追评表（v1.1再加，先注释）====================
-- CREATE TABLE review_appends (...) 
--关键决策：
--target_type 保留但默认 'ITEM'，评人功能开关在应用层
--rating_tag 的枚举校验放应用层，方便后期加标签不改表
--只留一个简单 CHECK 保星级范围，兼容 MySQL 8.0.16+
--软删除字段齐全，管理后台可直接用
--应用层需要做的：
--强制 target_type = 'ITEM'，rating_star = NULL
--rating_tag 校验：['AS_DESCRIBED', 'BETTER_THAN_DESCRIBED', 'SLIGHT_WEAR', 'NOTICEABLE_WEAR', 'NOT_AS_DESCRIBED']
--删除时写 deleted_by（用户自己删=用户ID，管理员删=管理员ID）
--