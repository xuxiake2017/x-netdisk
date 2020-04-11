package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.FileOrigin;

public interface FileOriginMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FileOrigin record);

    int insertSelective(FileOrigin record);

    FileOrigin selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FileOrigin record);

    int updateByPrimaryKey(FileOrigin record);

    /**
     * 根据MD5值查找文件
     * @param md5Hex
     * @return
     */
    FileOrigin findFileByMd5Hex(String md5Hex);

    /**
     * 根据userFileId查找
     * @param userFileId
     * @return
     */
    FileOrigin findByUserFileId(Integer userFileId);
}