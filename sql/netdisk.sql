/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.140.201
 Source Server Type    : MySQL
 Source Server Version : 50732
 Source Host           : 192.168.140.201:3306
 Source Schema         : netdisk

 Target Server Type    : MySQL
 Target Server Version : 50732
 File Encoding         : 65001

 Date: 02/05/2021 18:46:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `BLOB_DATA` blob NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `CALENDAR_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `CRON_EXPRESSION` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TIME_ZONE_ID` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ENTRY_ID` varchar(95) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `INSTANCE_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `IS_DURABLE` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `IS_UPDATE_DATA` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `JOB_DATA` blob NULL,
  PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `LOCK_NAME` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `INSTANCE_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `STR_PROP_1` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `STR_PROP_2` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `STR_PROP_3` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `INT_PROP_1` int(11) NULL DEFAULT NULL,
  `INT_PROP_2` int(11) NULL DEFAULT NULL,
  `LONG_PROP_1` bigint(20) NULL DEFAULT NULL,
  `LONG_PROP_2` bigint(20) NULL DEFAULT NULL,
  `DEC_PROP_1` decimal(13, 4) NULL DEFAULT NULL,
  `DEC_PROP_2` decimal(13, 4) NULL DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) NULL DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) NULL DEFAULT NULL,
  `PRIORITY` int(11) NULL DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `TRIGGER_TYPE` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) NULL DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) NULL DEFAULT NULL,
  `JOB_DATA` blob NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  INDEX `SCHED_NAME`(`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `qrtz_job_details` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_file_media
-- ----------------------------
DROP TABLE IF EXISTS `x_file_media`;
CREATE TABLE `x_file_media`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `origin_id` int(11) NULL DEFAULT NULL COMMENT '源文件id',
  `shoot_time` datetime(0) NULL DEFAULT NULL COMMENT '图片拍摄时间',
  `img_width` int(11) NULL DEFAULT NULL COMMENT '图片宽',
  `img_height` int(11) NULL DEFAULT NULL COMMENT '图片高',
  `video_width` int(11) NULL DEFAULT NULL COMMENT '视频宽',
  `video_height` int(11) NULL DEFAULT NULL COMMENT '视频高',
  `video_duration` int(11) NULL DEFAULT NULL COMMENT '视频时长',
  `thumbnail_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '缩率图地址（视频、图片）',
  `music_poster` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '音乐封面图',
  `music_artist` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '音乐演唱者',
  `reserve_column_1` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备用字段1',
  `reserve_column_2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备用字段2',
  `reserve_column_3` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备用字段3',
  `reserve_column_4` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备用字段4',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_file_origin
-- ----------------------------
DROP TABLE IF EXISTS `x_file_origin`;
CREATE TABLE `x_file_origin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_path` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件储存的path（FastDFS）',
  `preview_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件预览url',
  `file_real_name` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件真实名',
  `file_ext_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小',
  `file_type` int(2) NULL DEFAULT NULL COMMENT '文件类型',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '最初上传的用户id',
  `md5_hex` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件md5值',
  `file_status` int(2) NULL DEFAULT 0 COMMENT '文件状态',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '源文件' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for x_file_recycle
-- ----------------------------
DROP TABLE IF EXISTS `x_file_recycle`;
CREATE TABLE `x_file_recycle`  (
  `recycle_id` int(11) NOT NULL AUTO_INCREMENT,
  `file_id` int(11) NULL DEFAULT NULL COMMENT '关联x_user_file的id',
  `delete_user_id` int(11) NULL DEFAULT NULL COMMENT '删除文件用户id',
  `delete_time` datetime(0) NULL DEFAULT NULL COMMENT '删除时间',
  `over_time` datetime(0) NULL DEFAULT NULL COMMENT '自动清理时间',
  `recycle_status` int(2) NULL DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`recycle_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for x_file_share
-- ----------------------------
DROP TABLE IF EXISTS `x_file_share`;
CREATE TABLE `x_file_share`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `share_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '分享id（标识符）',
  `share_user_id` int(11) NULL DEFAULT NULL COMMENT '分享文件用户id',
  `share_pwd` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '分享密码',
  `is_public` int(1) NULL DEFAULT 0 COMMENT '是否公开（公开不需要密码）1公开 0不公开',
  `share_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '分享时间',
  `over_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间（null表示永不过期）',
  `share_status` int(11) NULL DEFAULT NULL COMMENT '分享状态',
  `file_id` int(11) NULL DEFAULT NULL COMMENT '关联文件id（x_user_file）',
  `access_times` int(11) NULL DEFAULT NULL COMMENT '访问词次数',
  `download_times` int(11) NULL DEFAULT NULL COMMENT '下载次数',
  `save_times` int(11) NULL DEFAULT NULL COMMENT '保存次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for x_sys_message
-- ----------------------------
DROP TABLE IF EXISTS `x_sys_message`;
CREATE TABLE `x_sys_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) NULL DEFAULT NULL COMMENT '消息级别 success 0，info 1，warning 2，error 3',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息标题',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息描述',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '消息接收者',
  `status` int(11) NULL DEFAULT 0 COMMENT '消息状态 0 正常 1已读',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_user
-- ----------------------------
DROP TABLE IF EXISTS `x_user`;
CREATE TABLE `x_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `sex` int(1) NULL DEFAULT NULL COMMENT '性别',
  `real_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `reg_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '注册时间',
  `total_memory` bigint(20) NULL DEFAULT NULL COMMENT '网盘总空间',
  `used_memory` bigint(20) NULL DEFAULT NULL COMMENT '网盘已用空间',
  `phone` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `user_status` int(2) NULL DEFAULT 0 COMMENT '用户状态',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `signature` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '个性签名',
  `nick_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for x_user_file
-- ----------------------------
DROP TABLE IF EXISTS `x_user_file`;
CREATE TABLE `x_user_file`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `origin_id` int(11) NULL DEFAULT NULL COMMENT '源文件id',
  `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件名称',
  `file_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件路径（数据库逻辑路径）',
  `is_dir` int(1) NULL DEFAULT NULL COMMENT '是否是文件夹',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `parent_id` int(11) NULL DEFAULT NULL COMMENT '文件父id',
  `key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标识符',
  `status` int(2) NULL DEFAULT NULL COMMENT '文件状态',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_user_friend_list
-- ----------------------------
DROP TABLE IF EXISTS `x_user_friend_list`;
CREATE TABLE `x_user_friend_list`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `friend_id` int(11) NULL DEFAULT NULL COMMENT '好友用户id',
  `alias` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int(1) NULL DEFAULT 0 COMMENT '状态 0：正常；1：删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_user_friend_message
-- ----------------------------
DROP TABLE IF EXISTS `x_user_friend_message`;
CREATE TABLE `x_user_friend_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `from` int(11) NULL DEFAULT NULL COMMENT '消息从何处来（发送消息用户id）',
  `to` int(11) NULL DEFAULT NULL COMMENT '消息到何处去（接收消息用户id）',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `file_id` int(11) NULL DEFAULT NULL COMMENT '关联file id',
  `status` int(1) NULL DEFAULT 0 COMMENT '状态 0：正常；1：删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_user_friend_request
-- ----------------------------
DROP TABLE IF EXISTS `x_user_friend_request`;
CREATE TABLE `x_user_friend_request`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `applicant` int(11) NULL DEFAULT NULL COMMENT '申请者id',
  `respondent` int(11) NULL DEFAULT NULL COMMENT '被申请者id',
  `verify` int(1) NULL DEFAULT 0 COMMENT '验证状态 0：待验证；1：同意好友申请；2：不同意好友申请',
  `status` int(1) NULL DEFAULT 0 COMMENT '状态 0：正常；1：删除',
  `postscript` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '附言',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户好友申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for x_wechat_user
-- ----------------------------
DROP TABLE IF EXISTS `x_wechat_user`;
CREATE TABLE `x_wechat_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `avatar_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户头像',
  `city` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '城市',
  `country` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '国家',
  `gender` int(2) NULL DEFAULT NULL COMMENT '性别 1男 2女 0未知',
  `language` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所用的语言',
  `nick_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `province` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '省份',
  `openid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户唯一标识',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '系统用户id',
  `status` int(2) NULL DEFAULT 0 COMMENT '状态 0正常 1删除',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Procedure structure for pro_cre_pathlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_pathlist`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_pathlist`(IN nid INT,IN delimit VARCHAR(10),
INOUT pathstr VARCHAR(1000))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE parentid INT DEFAULT 0;
    DECLARE cur1 CURSOR FOR 
    SELECT t.parent_id,CONCAT(CAST(t.parent_id AS CHAR),delimit,pathstr) 
        from x_user_file AS t WHERE t.id = nid;
    -- 下面这行表示若没有数据返回，程序继续，并将变量done设为1
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    -- mysql中可以利用系统参数 max_sp_recursion_depth 来控制递归调用的层数上限。
    SET max_sp_recursion_depth=12;

    OPEN cur1;
    -- 游标向下走一步
    FETCH cur1 INTO parentid,pathstr;
    WHILE done=0 DO
        CALL pro_cre_pathlist(parentid,delimit,pathstr);
        -- 游标向下走一步
        FETCH cur1 INTO parentid,pathstr;
    END WHILE;

    CLOSE cur1;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_pnlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_pnlist`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_pnlist`(IN nid INT,IN delimit VARCHAR(10),
INOUT pathstr VARCHAR(1000))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE parentid INT DEFAULT 0;
    DECLARE cur1 CURSOR FOR 
    SELECT t.parent_id,CONCAT(t.file_name,delimit,pathstr) 
        from x_user_file AS t WHERE t.id = nid;
    -- 下面这行表示若没有数据返回，程序继续，并将变量done设为1
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    -- mysql中可以利用系统参数 max_sp_recursion_depth 来控制递归调用的层数上限。
    SET max_sp_recursion_depth=12;

    OPEN cur1;
    -- 游标向下走一步
    FETCH cur1 INTO parentid,pathstr;
    WHILE done=0 DO
        CALL pro_cre_pnlist(parentid,delimit,pathstr);
        -- 游标向下走一步
        FETCH cur1 INTO parentid,pathstr;
    END WHILE;

    CLOSE cur1;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for fn_tree_path
-- ----------------------------
DROP FUNCTION IF EXISTS `fn_tree_path`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_tree_path`(nid INT,delimit VARCHAR(10)) RETURNS varchar(2000) CHARSET utf8
BEGIN 
    DECLARE pathid VARCHAR(1000);
    
    SET pathid = CAST(nid AS CHAR);
    CALL pro_cre_pathlist(nid,delimit,pathid);
    
    RETURN pathid;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for fn_tree_pathname
-- ----------------------------
DROP FUNCTION IF EXISTS `fn_tree_pathname`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_tree_pathname`(nid INT,delimit VARCHAR(10)) RETURNS varchar(2000) CHARSET utf8
BEGIN
    DECLARE pathid VARCHAR(1000);
    SET pathid='';
    CALL pro_cre_pnlist(nid,delimit,pathid);
    RETURN pathid;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_childlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_childlist`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_childlist`(IN rootId int, IN nDepth int)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE b INT;
    DECLARE cur1 CURSOR FOR SELECT id FROM x_user_file WHERE parent_id = rootId and `status`=0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth=12;

    INSERT INTO tmpList VALUES (NULL,rootId,nDepth);

    OPEN cur1;

    FETCH cur1 INTO b;
    WHILE done=0 DO
      CALL pro_cre_childlist(b,nDepth+1);
      FETCH cur1 INTO b;
    END WHILE;

    CLOSE cur1;
  END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_childlist_del
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_childlist_del`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_childlist_del`(IN rootId INT,IN nDepth INT)
BEGIN   
      DECLARE done INT DEFAULT 0;   
      DECLARE b INT;   
      DECLARE cur1 CURSOR FOR SELECT id FROM x_user_file WHERE parent_id=rootId and `status`=0;   
      DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;   
      SET max_sp_recursion_depth=12;   
       
      INSERT INTO tmpList VALUES (NULL,rootId,nDepth);   
       
      OPEN cur1;   
       
      FETCH cur1 INTO b;   
      WHILE done=0 DO   
              CALL pro_cre_childlist_del(b,nDepth+1);   
              FETCH cur1 INTO b;   
      END WHILE;   
       
      CLOSE cur1;   
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_childlist_rbk
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_childlist_rbk`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_childlist_rbk`(IN rootId INT,IN nDepth INT)
BEGIN   
      DECLARE done INT DEFAULT 0;   
      DECLARE b INT;   
      DECLARE cur1 CURSOR FOR SELECT id FROM x_user_file WHERE parent_id=rootId and `status`=2;   
      DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;   
      SET max_sp_recursion_depth=12;   
       
      INSERT INTO tmpList VALUES (NULL,rootId,nDepth);   
       
      OPEN cur1;   
       
      FETCH cur1 INTO b;   
      WHILE done=0 DO   
              CALL pro_cre_childlist_rbk(b,nDepth+1);   
              FETCH cur1 INTO b;   
      END WHILE;   
       
      CLOSE cur1;   
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_delete_dir
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_delete_dir`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_delete_dir`(IN rootId INT)
BEGIN	
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_del(rootId,0);
		
		UPDATE x_user_file SET status=2 WHERE id in
    (SELECT tmpList.id FROM tmpList ORDER BY tmpList.sno)
		and status=0;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_get_sumsize_del
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_get_sumsize_del`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_get_sumsize_del`(IN rootId INT,OUT sumsize INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_del(rootId,0);
		
		SELECT 
			SUM(x_file_origin.file_size) INTO sumsize
		FROM
			x_user_file,
			x_file_origin
		WHERE
			x_user_file.id IN 
			(SELECT tmpList.id FROM tmpList ORDER BY tmpList.sno)
			AND x_user_file.origin_id = x_file_origin.id
			AND x_user_file.`status` = 0;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_get_sumsize_rbk
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_get_sumsize_rbk`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_get_sumsize_rbk`(IN rootId INT,OUT sumsize INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_rbk(rootId,0);
		
		SELECT 
			SUM(x_file_origin.file_size) INTO sumsize
		FROM
			x_user_file,
			x_file_origin
		WHERE
			x_user_file.id IN 
			(SELECT tmpList.id FROM tmpList ORDER BY tmpList.sno)
			AND x_user_file.origin_id = x_file_origin.id
			AND x_user_file.`status` = 2;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_reback_dir
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_reback_dir`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_reback_dir`(IN rootId INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_rbk(rootId,0);
		
		UPDATE x_user_file SET status=0 WHERE id in
    (SELECT tmpList.id FROM tmpList ORDER BY tmpList.sno)
		and `status`=2;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_show_childlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_show_childlist`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cre_show_childlist`(IN rootId int)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
      sno INT PRIMARY KEY AUTO_INCREMENT,
      id INT,
      depth INT);

    CALL pro_cre_childlist(rootId,0);

    select tmpList.id from tmpList;
  END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
