-- x_user_file 的目录关系以 parent_id 为准，删除不再使用的冗余逻辑路径字段。
ALTER TABLE x_user_file
    DROP COLUMN file_path;
