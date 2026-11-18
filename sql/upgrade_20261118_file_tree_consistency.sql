-- 文件树一致性修复：索引、深层目录递归、BIGINT 容量统计和回收站子树查询。
ALTER TABLE x_user_file
    ADD UNIQUE KEY uk_user_file_key (`key`),
    ADD KEY idx_user_file_user_parent_status (user_id, parent_id, status),
    ADD KEY idx_user_file_parent_status (parent_id, status),
    ADD KEY idx_user_file_origin (origin_id);

DROP PROCEDURE IF EXISTS `pro_cre_pathlist`;
delimiter ;;
CREATE PROCEDURE `pro_cre_pathlist`(IN nid INT, IN delimit VARCHAR(10), INOUT pathstr VARCHAR(1000))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE parentid INT DEFAULT 0;
    DECLARE cur1 CURSOR FOR
        SELECT t.parent_id, CONCAT(CAST(t.parent_id AS CHAR), delimit, pathstr)
        FROM x_user_file AS t WHERE t.id = nid;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth = 255;
    OPEN cur1;
    FETCH cur1 INTO parentid, pathstr;
    WHILE done = 0 DO
        CALL pro_cre_pathlist(parentid, delimit, pathstr);
        FETCH cur1 INTO parentid, pathstr;
    END WHILE;
    CLOSE cur1;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_pnlist`;
delimiter ;;
CREATE PROCEDURE `pro_cre_pnlist`(IN nid INT, IN delimit VARCHAR(10), INOUT pathstr VARCHAR(1000))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE parentid INT DEFAULT 0;
    DECLARE cur1 CURSOR FOR
        SELECT t.parent_id, CONCAT(t.file_name, delimit, pathstr)
        FROM x_user_file AS t WHERE t.id = nid;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth = 255;
    OPEN cur1;
    FETCH cur1 INTO parentid, pathstr;
    WHILE done = 0 DO
        CALL pro_cre_pnlist(parentid, delimit, pathstr);
        FETCH cur1 INTO parentid, pathstr;
    END WHILE;
    CLOSE cur1;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_childlist`;
delimiter ;;
CREATE PROCEDURE `pro_cre_childlist`(IN rootId INT, IN nDepth INT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE childId INT;
    DECLARE cur1 CURSOR FOR SELECT id FROM x_user_file WHERE parent_id = rootId AND status = 0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth = 255;
    INSERT INTO tmpList VALUES (NULL, rootId, nDepth);
    OPEN cur1;
    FETCH cur1 INTO childId;
    WHILE done = 0 DO
        CALL pro_cre_childlist(childId, nDepth + 1);
        FETCH cur1 INTO childId;
    END WHILE;
    CLOSE cur1;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_childlist_del`;
delimiter ;;
CREATE PROCEDURE `pro_cre_childlist_del`(IN rootId INT, IN nDepth INT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE childId INT;
    DECLARE cur1 CURSOR FOR SELECT id FROM x_user_file WHERE parent_id = rootId AND status = 0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth = 255;
    INSERT INTO tmpList VALUES (NULL, rootId, nDepth);
    OPEN cur1;
    FETCH cur1 INTO childId;
    WHILE done = 0 DO
        CALL pro_cre_childlist_del(childId, nDepth + 1);
        FETCH cur1 INTO childId;
    END WHILE;
    CLOSE cur1;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_childlist_rbk`;
delimiter ;;
CREATE PROCEDURE `pro_cre_childlist_rbk`(IN rootId INT, IN nDepth INT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE childId INT;
    DECLARE cur1 CURSOR FOR SELECT id FROM x_user_file WHERE parent_id = rootId AND status = 2;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    SET max_sp_recursion_depth = 255;
    INSERT INTO tmpList VALUES (NULL, rootId, nDepth);
    OPEN cur1;
    FETCH cur1 INTO childId;
    WHILE done = 0 DO
        CALL pro_cre_childlist_rbk(childId, nDepth + 1);
        FETCH cur1 INTO childId;
    END WHILE;
    CLOSE cur1;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_get_sumsize_del`;
delimiter ;;
CREATE PROCEDURE `pro_cre_get_sumsize_del`(IN rootId INT, OUT sumsize BIGINT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);
    CALL pro_cre_childlist_del(rootId, 0);
    SELECT COALESCE(SUM(o.file_size), 0) INTO sumsize
    FROM x_user_file f
    JOIN x_file_origin o ON f.origin_id = o.id
    WHERE f.id IN (SELECT id FROM tmpList) AND f.status = 0;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_get_sumsize_rbk`;
delimiter ;;
CREATE PROCEDURE `pro_cre_get_sumsize_rbk`(IN rootId INT, OUT sumsize BIGINT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);
    CALL pro_cre_childlist_rbk(rootId, 0);
    SELECT COALESCE(SUM(o.file_size), 0) INTO sumsize
    FROM x_user_file f
    JOIN x_file_origin o ON f.origin_id = o.id
    WHERE f.id IN (SELECT id FROM tmpList) AND f.status = 2;
END
;;
delimiter ;

DROP PROCEDURE IF EXISTS `pro_cre_show_recycle_childlist`;
delimiter ;;
CREATE PROCEDURE `pro_cre_show_recycle_childlist`(IN rootId INT)
BEGIN
    DROP TEMPORARY TABLE IF EXISTS tmpList;
    CREATE TEMPORARY TABLE tmpList(
        sno INT PRIMARY KEY AUTO_INCREMENT,
        id INT,
        depth INT);
    CALL pro_cre_childlist_rbk(rootId, 0);
    SELECT id FROM tmpList;
END
;;
delimiter ;
