-- 回收站并发、补偿清理和物理文件删除重试。
ALTER TABLE `x_file_recycle`
    ADD KEY `idx_recycle_user_status_time` (`delete_user_id`, `recycle_status`, `delete_time`) USING BTREE,
    ADD KEY `idx_recycle_expire_status` (`recycle_status`, `over_time`) USING BTREE,
    ADD KEY `idx_recycle_file` (`file_id`) USING BTREE;

CREATE TABLE IF NOT EXISTS `x_storage_delete_task` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `storage_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '待删除的存储路径',
    `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time` datetime(0) NOT NULL COMMENT '下次重试时间',
    `status` int(1) NOT NULL DEFAULT 0 COMMENT '状态：0待处理，1处理中',
    `lock_until` datetime(0) NULL DEFAULT NULL COMMENT '任务占用超时时间',
    `last_error` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最后一次错误',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_storage_delete_path` (`storage_path`) USING BTREE,
    KEY `idx_storage_delete_retry` (`status`, `next_retry_time`, `lock_until`, `id`) USING BTREE
) ENGINE=InnoDB CHARACTER SET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=Dynamic;
