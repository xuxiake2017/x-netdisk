-- 注册流程按邮箱识别用户。空字符串先归一化为 NULL，允许无邮箱的微信用户并存。
UPDATE `x_user`
SET `email` = NULL
WHERE `email` IS NOT NULL AND TRIM(`email`) = '';

-- 如果已有重复的非空邮箱，本语句会失败；请先确认并合并重复账号，避免静默覆盖用户数据。
ALTER TABLE `x_user`
ADD UNIQUE KEY `uk_user_email` (`email`) USING BTREE;
