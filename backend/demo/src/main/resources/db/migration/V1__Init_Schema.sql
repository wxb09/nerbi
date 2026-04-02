-- ============================================
-- 邻里共享平台数据库初始化脚本 V1
-- 创建时间: 2026-03-27
-- 数据库: MySQL 8.0+
-- ============================================

-- 创建社区表
CREATE TABLE IF NOT EXISTS communities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '社区ID',
    name VARCHAR(100) NOT NULL COMMENT '社区名称',
    address VARCHAR(255) COMMENT '详细地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区表';

-- 创建分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(100) COMMENT '图标标识',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) COMMENT '密码（加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    bio VARCHAR(500) COMMENT '个人简介',
    community_id BIGINT COMMENT '所属社区ID',
    building VARCHAR(50) COMMENT '楼栋',
    unit VARCHAR(20) COMMENT '单元',
    credit_score DECIMAL(4,2) DEFAULT 10.00 COMMENT '信用分(0-10)',
    borrow_count INT DEFAULT 0 COMMENT '借入次数',
    lend_count INT DEFAULT 0 COMMENT '借出次数',
    co2_saved INT DEFAULT 0 COMMENT '碳减排量(克)',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/BANNED/DELETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_community (community_id),
    INDEX idx_credit (credit_score),
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建物品表
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '物品ID',
    name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category_id BIGINT COMMENT '分类ID',
    description TEXT COMMENT '物品描述',
    story TEXT COMMENT '背后的故事',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    community_id BIGINT COMMENT '所在社区ID',
    building VARCHAR(50) COMMENT '楼栋',
    price_per_day DECIMAL(10,2) DEFAULT 0.00 COMMENT '每日租金',
    deposit DECIMAL(10,2) DEFAULT 0.00 COMMENT '押金',
    credit_required DECIMAL(3,2) DEFAULT 0.00 COMMENT '信用要求',
    return_requirements TEXT COMMENT '归还要求(JSON数组)',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/AVAILABLE/BORROWED/OFFLINE/DELETED',
    borrow_count INT DEFAULT 0 COMMENT '借阅次数',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    favorite_count INT DEFAULT 0 COMMENT '收藏次数',
    tags VARCHAR(500) COMMENT '标签(JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    INDEX idx_owner (owner_id),
    INDEX idx_category (category_id),
    INDEX idx_community (community_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at),
    FULLTEXT idx_name_desc (name, description),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品表';

-- 创建物品图片表
CREATE TABLE IF NOT EXISTS item_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    url VARCHAR(500) NOT NULL COMMENT '图片URL',
    is_main BOOLEAN DEFAULT FALSE COMMENT '是否主图',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_item (item_id),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品图片表';

-- 创建借阅记录表
CREATE TABLE IF NOT EXISTS borrows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '借阅ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    borrower_id BIGINT NOT NULL COMMENT '借入者ID',
    lender_id BIGINT NOT NULL COMMENT '借出者ID',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    actual_return_date DATE COMMENT '实际归还日期',
    purpose VARCHAR(500) COMMENT '用途说明',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/ACTIVE/RETURNED/OVERDUE/REJECTED/CANCELLED',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    remind_count INT DEFAULT 0 COMMENT '提醒次数',
    last_remind_at TIMESTAMP NULL COMMENT '最后提醒时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_item (item_id),
    INDEX idx_borrower (borrower_id),
    INDEX idx_lender (lender_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lender_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 创建帖子表
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    community_id BIGINT COMMENT '社区ID',
    type VARCHAR(20) DEFAULT 'normal' COMMENT '类型: normal/thanks/help/exchange',
    title VARCHAR(200) COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images VARCHAR(2000) COMMENT '图片URLs(JSON数组)',
    tags VARCHAR(500) COMMENT '标签(JSON数组)',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT '状态: DRAFT/PUBLISHED/DELETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    INDEX idx_community (community_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 创建评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    parent_id BIGINT COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED/DELETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 创建点赞表
CREATE TABLE IF NOT EXISTS likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型: POST/COMMENT',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 创建公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    community_id BIGINT COMMENT '社区ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type VARCHAR(20) DEFAULT 'normal' COMMENT '类型: normal/important',
    status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT '状态: DRAFT/PUBLISHED/DELETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_community (community_id),
    INDEX idx_status (status),
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 创建评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    borrow_id BIGINT NOT NULL COMMENT '借阅ID',
    from_user_id BIGINT NOT NULL COMMENT '评价者ID',
    to_user_id BIGINT NOT NULL COMMENT '被评价者ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    content TEXT COMMENT '评价内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_borrow (borrow_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_item (item_id),
    FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE CASCADE,
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 创建消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    from_user_id BIGINT COMMENT '发送者ID',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    type VARCHAR(20) NOT NULL COMMENT '类型: SYSTEM/BORROW/CHAT',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容',
    related_id BIGINT COMMENT '关联ID(借阅ID/物品ID等)',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_to_user (to_user_id),
    INDEX idx_is_read (is_read),
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 创建技能交换表
CREATE TABLE IF NOT EXISTS skill_exchanges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '技能交换ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    community_id BIGINT COMMENT '社区ID',
    offer_skill VARCHAR(200) NOT NULL COMMENT '提供的技能',
    need_skill VARCHAR(200) NOT NULL COMMENT '需要的技能',
    description TEXT COMMENT '描述',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/COMPLETED/DELETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_community (community_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能交换表';
