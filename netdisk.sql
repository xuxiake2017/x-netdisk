/*
 Navicat Premium Data Transfer

 Source Server         : vm_01
 Source Server Type    : MySQL
 Source Server Version : 50722
 Source Host           : 192.168.219.129:3306
 Source Schema         : netdisk

 Target Server Type    : MySQL
 Target Server Version : 50722
 File Encoding         : 65001

 Date: 04/05/2019 23:47:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `admin_id` int(11) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT 0,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`admin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for fileupload
-- ----------------------------
DROP TABLE IF EXISTS `fileupload`;
CREATE TABLE `fileupload`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `upload_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `file_path` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `File_Save_Name` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `File_Real_Name` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `file_Ext_Name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `file_Size` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `file_Type` int(11) NULL DEFAULT NULL,
  `is_Dir` int(11) NULL DEFAULT 1,
  `upload_User_Id` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `parent_id` int(11) NULL DEFAULT NULL,
  `media_Cache_Path` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `md5_Hex` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `file_Status` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0',
  `Shoot_Time` datetime(0) NULL DEFAULT NULL,
  `img_width` int(11) NULL DEFAULT NULL,
  `img_height` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1398 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) NULL DEFAULT NULL COMMENT '消息级别 success 0，info 1，warning 2，error 3',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息标题',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息描述',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '消息接收者',
  `status` int(11) NULL DEFAULT 0 COMMENT '消息状态 0 正常 1已读',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for recycle
-- ----------------------------
DROP TABLE IF EXISTS `recycle`;
CREATE TABLE `recycle`  (
  `recycle_id` int(11) NOT NULL AUTO_INCREMENT,
  `file_id` int(11) NULL DEFAULT NULL,
  `delete_user_id` int(11) NULL DEFAULT NULL,
  `delete_time` datetime(0) NULL DEFAULT NULL,
  `over_time` datetime(0) NULL DEFAULT NULL,
  `recycle_status` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`recycle_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 140 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sharefile
-- ----------------------------
DROP TABLE IF EXISTS `sharefile`;
CREATE TABLE `sharefile`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `share_Id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `share_User_Id` int(11) NULL DEFAULT NULL,
  `share_User` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `share_Pwd` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `share_Time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `share_Status` int(11) NULL DEFAULT NULL,
  `file_id` int(11) NULL DEFAULT NULL,
  `access_times` int(11) NULL DEFAULT NULL,
  `download_times` int(11) NULL DEFAULT NULL,
  `save_times` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user_friend_apply_for
-- ----------------------------
DROP TABLE IF EXISTS `user_friend_apply_for`;
CREATE TABLE `user_friend_apply_for`  (
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
-- Table structure for user_friend_list
-- ----------------------------
DROP TABLE IF EXISTS `user_friend_list`;
CREATE TABLE `user_friend_list`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `friend_id` int(11) NULL DEFAULT NULL COMMENT '好友用户id',
  `status` int(1) NULL DEFAULT 0 COMMENT '状态 0：正常；1：删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_friend_message
-- ----------------------------
DROP TABLE IF EXISTS `user_friend_message`;
CREATE TABLE `user_friend_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `from` int(11) NULL DEFAULT NULL COMMENT '消息从何处来（发送消息用户id）',
  `to` int(11) NULL DEFAULT NULL COMMENT '消息到何处去（接收消息用户id）',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `file_id` int(11) NULL DEFAULT NULL COMMENT '关联file id',
  `status` int(1) NULL DEFAULT 0 COMMENT '状态 0：正常；1：删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for usernetdisk
-- ----------------------------
DROP TABLE IF EXISTS `usernetdisk`;
CREATE TABLE `usernetdisk`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sex` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `real_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reg_Time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `total_Memory` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `used_Memory` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `phone` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_Status` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `signature` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Procedure structure for delete_dir
-- ----------------------------
DROP PROCEDURE IF EXISTS `delete_dir`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `delete_dir`(IN rootId INT)
BEGIN	
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_del(rootId,0);
		
		UPDATE fileupload SET file_Status='2' WHERE id in
    (SELECT t.id from (SELECT fileupload.id FROM tmpList,fileupload 
    WHERE tmpList.id=fileupload.id ORDER BY tmpList.sno) t)
		and file_Status='0';
END
;;
delimiter ;

-- ----------------------------
-- Function structure for fn_tree_path
-- ----------------------------
DROP FUNCTION IF EXISTS `fn_tree_path`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` FUNCTION `fn_tree_path`(nid INT,delimit VARCHAR(10)) RETURNS varchar(2000) CHARSET utf8
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
CREATE DEFINER=`xuxiake`@`%` FUNCTION `fn_tree_pathname`(nid INT,delimit VARCHAR(10)) RETURNS varchar(2000) CHARSET utf8
BEGIN
    DECLARE pathid VARCHAR(1000);
    SET pathid='';
    CALL pro_cre_pnlist(nid,delimit,pathid);
    RETURN pathid;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for get_sumsize_del
-- ----------------------------
DROP PROCEDURE IF EXISTS `get_sumsize_del`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `get_sumsize_del`(IN rootId INT,OUT sumsize INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_del(rootId,0);
		
		select sum(0+file_Size) INTO sumsize from fileupload
		WHERE id in
		(SELECT t.id from (SELECT fileupload.id FROM tmpList,fileupload 
    WHERE tmpList.id=fileupload.id ORDER BY tmpList.sno) t)
		and file_Status='0';
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for get_sumsize_rbk
-- ----------------------------
DROP PROCEDURE IF EXISTS `get_sumsize_rbk`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `get_sumsize_rbk`(IN rootId INT,OUT sumsize INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_rbk(rootId,0);
		
		select sum(0+file_Size) INTO sumsize from fileupload
		WHERE id in
		(SELECT t.id from (SELECT fileupload.id FROM tmpList,fileupload 
    WHERE tmpList.id=fileupload.id ORDER BY tmpList.sno) t)
		and file_Status='2';
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_childlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_childlist`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_cre_childlist`(IN rootId int, IN nDepth int)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE b INT;
    DECLARE cur1 CURSOR FOR SELECT id FROM fileupload WHERE parent_id=rootId and file_Status='0';
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
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_cre_childlist_del`(IN rootId INT,IN nDepth INT)
BEGIN   
      DECLARE done INT DEFAULT 0;   
      DECLARE b INT;   
      DECLARE cur1 CURSOR FOR SELECT id FROM fileupload WHERE parent_id=rootId and file_Status='0';   
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
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_cre_childlist_rbk`(IN rootId INT,IN nDepth INT)
BEGIN   
      DECLARE done INT DEFAULT 0;   
      DECLARE b INT;   
      DECLARE cur1 CURSOR FOR SELECT id FROM fileupload WHERE parent_id=rootId and file_Status='2';   
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
-- Procedure structure for pro_cre_parentlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_parentlist`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_cre_parentlist`(IN rootId INT,IN nDepth INT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE b INT;
    DECLARE cur1 CURSOR FOR SELECT parent_id FROM fileupload WHERE id=rootId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth=12;

    INSERT INTO tmpList VALUES(NULL,rootId,nDepth);

    OPEN cur1;

    FETCH cur1 INTO b;
    WHILE done=0 DO
        CALL pro_cre_parentlist(b,nDepth+1);
        FETCH cur1 INTO b;
    END WHILE;

    CLOSE cur1;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for pro_cre_pathlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cre_pathlist`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_cre_pathlist`(IN nid INT,IN delimit VARCHAR(10),
INOUT pathstr VARCHAR(1000))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE parentid INT DEFAULT 0;
    DECLARE cur1 CURSOR FOR 
    SELECT t.parent_id,CONCAT(CAST(t.parent_id AS CHAR),delimit,pathstr) 
        from fileupload AS t WHERE t.id = nid;
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
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_cre_pnlist`(IN nid INT,IN delimit VARCHAR(10),
INOUT pathstr VARCHAR(1000))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE parentid INT DEFAULT 0;
    DECLARE cur1 CURSOR FOR 
    SELECT t.parent_id,CONCAT(t.File_Real_Name,delimit,pathstr) 
        from fileupload AS t WHERE t.id = nid;
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
-- Procedure structure for pro_show_childlist
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_show_childlist`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `pro_show_childlist`(IN rootId int)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
      sno INT PRIMARY KEY AUTO_INCREMENT,
      id INT,
      depth INT);

    CALL pro_cre_childlist_del(rootId,0);

    select tmpList.id from tmpList;
  END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for reback_dir
-- ----------------------------
DROP PROCEDURE IF EXISTS `reback_dir`;
delimiter ;;
CREATE DEFINER=`xuxiake`@`%` PROCEDURE `reback_dir`(IN rootId INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);

    CALL pro_cre_childlist_rbk(rootId,0);
		
		UPDATE fileupload SET file_Status='0' WHERE id in
    (SELECT t.id from (SELECT fileupload.id FROM tmpList,fileupload 
    WHERE tmpList.id=fileupload.id ORDER BY tmpList.sno) t)
		and file_Status='2';

END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
