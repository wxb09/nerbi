-- ============================================
-- 邻里共享平台初始数据 V2
-- ============================================

-- 插入社区数据
INSERT INTO communities (id, name, address) VALUES
(1, '栖霞苑小区', '北京市朝阳区栖霞苑路88号'),
(2, '阳光花园', '北京市海淀区阳光花园路168号'),
(3, '翠湖新城', '北京市丰台区翠湖路56号');

-- 插入分类数据
INSERT INTO categories (id, name, icon, sort_order) VALUES
(1, '五金工具', 'solar:hammer-bold', 1),
(2, '生活家电', 'solar:washing-machine-bold', 2),
(3, '数码设备', 'solar:camera-bold', 3),
(4, '户外装备', 'solar:tent-bold', 4),
(5, '儿童玩具', 'solar:teddy-bear-bold', 5),
(6, '图书音像', 'solar:book-bold', 6),
(7, '运动健身', 'solar:dumbbell-bold', 7),
(8, '其他', 'solar:box-bold', 8);

-- 插入测试用户
INSERT INTO users (id, phone, nickname, avatar, bio, community_id, building, credit_score, borrow_count, lend_count, co2_saved) VALUES
(1, '13800002026', '李大明白', '/uploads/avatars/user1.jpg', '热爱生活，乐于分享', 1, '3号楼1402', 9.6, 12, 8, 3420),
(2, '13900001111', '老周', '/uploads/avatars/user2.jpg', '工具达人，装修专家', 1, '5号楼801', 9.8, 5, 15, 5200),
(3, '13600002222', '小雅', '/uploads/avatars/user3.jpg', '宝妈一枚，喜欢分享', 2, '2号楼503', 9.5, 8, 6, 2800);

-- 插入物品数据
INSERT INTO items (id, name, category_id, description, story, owner_id, community_id, building, price_per_day, deposit, credit_required, return_requirements, status, borrow_count, view_count, tags, published_at) VALUES
(1, '博世专业级电钻', 1, '德国博世GBH 2-20 D专业电钻，冲击钻模式，适合混凝土、砖墙钻孔。配备多种钻头，已消毒。', '去年装修买的，用了几次，现在闲置了。希望能帮到需要的朋友。', 2, 1, '5号楼801', 0.00, 50.00, 9.0, '["已消毒","保持原样","及时归还"]', 'AVAILABLE', 3, 156, '["已消毒","专业工具"]', NOW()),
(2, '黑驴轻量帐篷', 4, '4人露营帐篷，防水防风，适合周末郊游。配套地钉、风绳齐全。', '买来只用过两次，孩子上学后就没时间出去了。', 1, 1, '3号楼1402', 15.00, 100.00, 8.0, '["清理干净","晾干后归还"]', 'AVAILABLE', 2, 89, '["露营","户外"]', NOW()),
(3, '富士 X100VI', 3, '限量版复古相机，等效35mm焦段，适合街拍和人像。快门次数<500。', '冲动消费买的，平时不太用，放在柜子里吃灰可惜。', 2, 1, '5号楼801', 50.00, 500.00, 9.5, '["轻拿轻放","防潮保存","禁止拆卸"]', 'AVAILABLE', 1, 234, '["摄影","限量版"]', NOW()),
(4, '卡坦岛桌游', 5, '经典德式桌游，适合3-4人聚会，中文正版。', '朋友送的，玩过几次，现在孩子太小玩不了。', 3, 2, '2号楼503', 0.00, 30.00, 7.0, '["保持完整","清点配件"]', 'AVAILABLE', 5, 67, '["桌游","聚会神器"]', NOW());

-- 插入物品图片
INSERT INTO item_images (item_id, url, is_main, sort_order) VALUES
(1, '/uploads/items/1/main.jpg', TRUE, 1),
(1, '/uploads/items/1/detail1.jpg', FALSE, 2),
(1, '/uploads/items/1/detail2.jpg', FALSE, 3),
(2, '/uploads/items/2/main.jpg', TRUE, 1),
(2, '/uploads/items/2/detail1.jpg', FALSE, 2),
(3, '/uploads/items/3/main.jpg', TRUE, 1),
(4, '/uploads/items/4/main.jpg', TRUE, 1);

-- 插入帖子数据
INSERT INTO posts (id, user_id, community_id, type, title, content, like_count, comment_count, view_count, status, created_at) VALUES
(1, 1, 1, 'thanks', NULL, '感谢邻居老周借我电钻，帮我完成了书架安装！社区太温暖了，大家都很热心。', 128, 24, 456, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 2, 'help', NULL, '求借水粉画架3天，女儿学校美术活动急需，在线等！', 42, 15, 234, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 1, 'normal', NULL, '周末天气不错，有没有邻居想一起去郊外露营？可以带上我的帐篷，最多住4个人。', 89, 32, 567, 'PUBLISHED', NOW());

-- 插入公告数据
INSERT INTO announcements (community_id, title, content, type, status) VALUES
(1, '邻里共享规则2026版更新', '亲爱的邻居们，为了让大家更好地共享闲置物品，我们更新了借阅规则。主要变化：1. 信用分低于7分的用户需要额外押金；2. 借阅超期将影响信用分；3. 新增技能交换板块。详情请查看帮助中心。', 'important', 'PUBLISHED'),
(1, '周末社区跳蚤市场活动', '本周六上午9点，社区广场将举办跳蚤市场活动，欢迎大家带上闲置物品来交换或赠送！', 'normal', 'PUBLISHED'),
(2, '阳光花园环保月活动', '本月为环保主题月，每借阅一次物品可获得积分，月底可兑换环保礼品！', 'normal', 'PUBLISHED');

-- 插入技能交换数据
INSERT INTO skill_exchanges (user_id, community_id, offer_skill, need_skill, description, status) VALUES
(1, 1, '摄影技巧', '烘焙', '我可以教大家摄影技巧，想学习烘焙做蛋糕', 'ACTIVE'),
(2, 1, '家电维修', '钢琴陪练', '擅长修理各种家电，希望孩子能学钢琴', 'ACTIVE'),
(3, 2, '儿童绘本推荐', '英语口语练习', '宝妈一枚，有很多绘本资源，想练习英语口语', 'ACTIVE');
