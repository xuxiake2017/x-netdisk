package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.show.FileShowMedia;

public interface FileMediaMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FileMedia record);

    int insertSelective(FileMedia record);

    FileMedia selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FileMedia record);

    int updateByPrimaryKey(FileMedia record);

    /**
     * 获取文件媒体信息
     * @param fileKey
     * @return
     */
    FileShowMedia getFileMediaInfo(String fileKey);
}