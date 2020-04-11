package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.FileShare;
import group.xuxiake.common.entity.show.ShareFileShowInfo;
import group.xuxiake.common.entity.show.ShareFileShowList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileShareMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FileShare record);

    int insertSelective(FileShare record);

    FileShare selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FileShare record);

    int updateByPrimaryKey(FileShare record);

    /**
     * 获取分享文件信息
     * @param shareId
     * @return
     */
    ShareFileShowInfo getShareFileInfo(String shareId);

    /**
     * 根据shareId查找文件
     * @param shareId
     * @return
     */
    FileShare findByShareId(String shareId);

    /**
     * 查找用户分享文件列表
     * @param userId
     * @return
     */
    List<ShareFileShowList> findAllByUserId(@Param("userId") Integer userId, @Param("fileName") String fileName);
}