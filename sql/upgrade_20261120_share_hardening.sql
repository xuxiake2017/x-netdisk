-- 分享链接唯一性、查询索引及创建时间语义修复。
-- 如果历史数据存在重复 share_id，添加唯一索引会失败，请先人工确认并清理重复分享。
ALTER TABLE `x_file_share`
    MODIFY COLUMN `share_time` datetime(0) NULL DEFAULT NULL COMMENT '分享时间',
    ADD UNIQUE KEY `uk_file_share_share_id` (`share_id`) USING BTREE,
    ADD KEY `idx_file_share_user_status` (`share_user_id`, `share_status`) USING BTREE,
    ADD KEY `idx_file_share_file` (`file_id`) USING BTREE;
