-- 修复帖子类型字段值大小写问题
UPDATE posts SET type = 'NORMAL' WHERE type = 'normal';
UPDATE posts SET type = 'THANKS' WHERE type = 'thanks';
UPDATE posts SET type = 'HELP' WHERE type = 'help';
UPDATE posts SET type = 'EXCHANGE' WHERE type = 'exchange';
