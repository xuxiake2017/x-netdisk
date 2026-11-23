-- 个人资料修改依赖用户名、邮箱和手机号唯一。
UPDATE `x_user` SET `phone` = NULL WHERE `phone` IS NOT NULL AND TRIM(`phone`) = '';

-- 注册时间是历史事实，修改个人资料时不应被 MySQL 自动刷新。
ALTER TABLE `x_user`
    MODIFY COLUMN `reg_time` datetime(0) NULL DEFAULT NULL COMMENT '注册时间';

--  如果历史数据存在重复的非空username 或 phone，下面的索引会创建失败；
-- 请先确认并合并重复账号，避免静默修改用户身份数据。
ALTER TABLE `x_user`
    ADD UNIQUE KEY `uk_user_username` (`username`) USING BTREE,
    ADD UNIQUE KEY `uk_user_phone` (`phone`) USING BTREE;
